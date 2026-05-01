# Smart Campus Sensor & Room Management API




## Video of the overall running project

<video src="https://github.com/Kaushik49/Smart_campus/blob/main/assets/Screen%20Recording%202026-05-01%20at%2016.10.36.mov" controls width="600"></video>


A RESTful API built with **JAX-RS (Jersey)** and an embedded **Grizzly** HTTP server
for managing campus rooms and IoT sensors.

---

## Project Structure

```
src/main/java/com/smartcampus/
├── App.java                          ← Main entry point (starts Grizzly server)
├── SmartCampusApplication.java       ← @ApplicationPath config + component registration
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   ├── SensorReading.java
│   └── ErrorResponse.java            ← Standard error JSON body
├── store/
│   └── DataStore.java                ← Singleton in-memory store (ConcurrentHashMap)
├── resource/
│   ├── DiscoveryResource.java        ← GET /api/v1
│   ├── RoomResource.java             ← /api/v1/rooms
│   ├── SensorResource.java           ← /api/v1/sensors
│   └── SensorReadingResource.java    ← Sub-resource /api/v1/sensors/{id}/readings
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   └── SensorUnavailableException.java
├── mapper/
│   ├── RoomNotEmptyExceptionMapper.java         ← 409 Conflict
│   ├── LinkedResourceNotFoundExceptionMapper.java ← 422 Unprocessable Entity
│   ├── SensorUnavailableExceptionMapper.java    ← 403 Forbidden
│   └── GlobalExceptionMapper.java              ← 500 Internal Server Error (catch-all)
└── filter/
    └── LoggingFilter.java                      ← Logs all requests & responses
```

---

## How to Build & Run

### Prerequisites
- Java 11+
- Maven 3.6+

### Build

```bash
mvn clean package
```

This produces `target/smart-campus-api-1.0-SNAPSHOT-jar-with-dependencies.jar`.

### Run

```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The server starts at **http://localhost:8080/api/v1**

---

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Create a Sensor (linked to the room above)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### 4. Get all CO2 sensors (filtered)
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 5. Post a sensor reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":412.5}'
```

### 6. Get all readings for a sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/CO2-001/readings
```

### 7. Try to delete a room that still has sensors (expect 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 8. Try to register a sensor with a non-existent roomId (expect 422)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"GHOST-999"}'
```

### 9. Set sensor to MAINTENANCE then try posting a reading (expect 403)
```bash
# First update status (create another sensor for demo):
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"MAINTENANCE","currentValue":0.0,"roomId":"LIB-301"}'

# Now try posting a reading — expect 403:
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.5}'
```

---

## Report — Answers to Coursework Questions

### Part 1 — Q1: JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new resource class instance for every incoming HTTP request**
(per-request scope). This means resource classes are **not singletons** — a fresh object is
instantiated each time a request arrives and discarded afterwards.

**Impact on in-memory data management:**
If we stored data in instance fields of the resource class, each request would see an empty
map because it gets a brand-new object. To share state across requests, all data is stored in
`DataStore` — a dedicated **singleton** class that lives outside the resource classes.
`DataStore` uses `ConcurrentHashMap` instead of `HashMap` to prevent race conditions when
multiple requests read and write simultaneously (e.g., a POST and a GET arriving at the same
time). For the readings list per sensor, we additionally use `synchronized` blocks when
appending entries to prevent `ConcurrentModificationException`.

---

### Part 1 — Q2: HATEOAS

**Hypermedia As The Engine Of Application State (HATEOAS)** means that API responses include
links to related resources and available actions, so clients can navigate the API dynamically.

**Benefits over static documentation:**
- Clients discover available actions at runtime — they do not need to memorise every URL.
- If the API changes its paths, clients that follow links adapt automatically rather than breaking.
- A new developer can start at `GET /api/v1` and explore all resources by following the links
  returned in each response, without needing to read external documentation first.
- It reduces tight coupling between clients and the server's URL structure.

---

### Part 2 — Q1: Full Objects vs IDs

| Approach | Pros | Cons |
|---|---|---|
| Return full objects | Single request fetches everything; less client-side code | Higher payload size; wasteful if the client only needs IDs |
| Return only IDs | Tiny payload; client fetches only what it needs | Requires N extra GET requests — one per room (N+1 problem) |

For this API, full objects are returned for small campus room collections. For very large
datasets, pagination (e.g., `?page=1&size=20`) combined with sparse field selection would
be the industry-standard approach.

---

### Part 2 — Q2: DELETE Idempotency

**Yes, DELETE is idempotent in this implementation.**

Idempotency means: applying the same operation multiple times produces the same end state.

- **First DELETE call:** room exists → room is removed → server responds `204 No Content`.
- **Second DELETE call:** room is already gone → server responds `404 Not Found`.

Although the HTTP status code changes between the first and second call, the **server's state
is identical after both calls** — the room does not exist. This satisfies the definition of
idempotency. The HTTP specification (RFC 7231) confirms that DELETE is idempotent.

---

### Part 3 — Q1: @Consumes Mismatch

If a client sends `Content-Type: text/plain` or `application/xml` to a method annotated
`@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS automatically returns **HTTP 415 Unsupported
Media Type** — before our resource method code even executes. The framework inspects the
incoming `Content-Type` header and compares it to the declared `@Consumes` value. If they do
not match, the request is rejected at the framework level. This protects resource methods from
receiving data in an unexpected format.

---

### Part 3 — Q2: @QueryParam vs Path Segment for Filtering

| `@QueryParam` (`/sensors?type=CO2`) | Path segment (`/sensors/type/CO2`) |
|---|---|
| Optional by nature — same endpoint handles all and filtered | Requires separate `@Path` patterns for each filter |
| Easy to combine: `?type=CO2&status=ACTIVE` | Combining filters creates complex nested paths |
| Semantically correct: query params = search criteria | Path segments imply a distinct resource identity |
| REST standard for filtering collections | Misleadingly suggests "type" is a sub-resource |

Query parameters are the accepted REST convention for filtering, searching, and sorting
collections because they are optional modifiers on a resource, not part of the resource's
identity.

---

### Part 4 — Q1: Sub-Resource Locator Pattern

The sub-resource locator pattern allows a resource method to **return another resource object**
rather than a response. JAX-RS then dispatches the remainder of the URI to that returned object.

**Benefits:**
1. **Separation of concerns**, The main logic for reading is contained within a dedicated `SensorReadingResource` rather than being hidden inside
`SensorResource`, so both classes become smaller and have more targeted focus.
2. **Testability**, It is possible to perform unit tests on `SensorReadingResource` without requiring `SensorResource`.
3. **Scalability**, If  decided to implement `/alerts`, `/config`, or `/calibration` sub-resources, it means a whole new class will be created rather than adding yet more method to the already large class.
4. **Readability**, This approach is a big improvement over having one "god class" with
different dozens of `@Path` methods for pretty much every possible nested route.


---

### Part 5 — Q2: HTTP 422 vs 404

- **404 Not Found** means the *requested URI* was not found on the server.
- **422 Unprocessable Entity** means the URI and JSON format are valid, but the *content*
  contains a semantic error — in this case, a `roomId` referencing a room that does not exist.

Using 422 is more accurate because: the endpoint `/api/v1/sensors` was found (no 404), the
JSON was well-formed (no 400), but the payload contains an invalid reference. The 422 tells
the client "your request was understood but the data inside it is logically inconsistent."
A 404 would incorrectly imply the API endpoint itself is missing.

---

### Part 5 — Q4: Security Risk of Exposing Stack Traces

Exposing raw Java stack traces to external clients is a serious security risk:

1. **Framework disclosure** — class names like `org.glassfish.jersey…` or `com.fasterxml.jackson…`
   reveal exact library versions, enabling attackers to look up known CVEs for those versions.
2. **Package & class structure** — internal class names and method signatures expose the
   application's architecture, helping attackers plan targeted injection attacks.
3. **File system paths** — absolute paths (e.g., `/home/ubuntu/app/target/…`) reveal server
   directory structure, which assists path traversal or file inclusion attacks.
4. **Business logic leakage** — the call stack shows execution flow, making it easier to craft
   inputs that trigger specific code paths (e.g., bypassing validation).

The `GlobalExceptionMapper` prevents all of this by logging the full exception server-side
(for developers) while returning only a generic safe message to the client.

---

### Part 5 — Q5: Filters vs Manual Logging

Using JAX-RS filters for cross-cutting concerns like logging is superior because:

1. **DRY principle** — one filter covers all endpoints; no need to add `Logger.info()` to
   every resource method.
2. **Consistency** — a filter guarantees every request is logged, even if a developer forgets
   to add the log line to a new endpoint.
3. **Separation of concerns** — resource methods focus purely on business logic; the filter
   handles observability.
4. **Maintainability** — changing log format requires editing one class, not 20.
5. **Standard pattern** — filters are the JAX-RS-standard mechanism for cross-cutting concerns
   (logging, authentication, CORS, rate limiting), making the codebase easier to understand
   for any JAX-RS developer.


