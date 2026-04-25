package com.smart_campus.resource;

import com.smart_campus.exception.LinkedResourceNotFoundException;
import com.smart_campus.model.Room;
import com.smart_campus.model.Sensor;
import com.smart_campus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sensor Resource — manages /api/v1/sensors
 *
 * Endpoints:
 *   GET  /api/v1/sensors             — list all sensors (optional ?type= filter)
 *   POST /api/v1/sensors             — register a new sensor (validates roomId exists)
 *   GET  /api/v1/sensors/{sensorId}  — get a specific sensor
 *   DELETE /api/v1/sensors/{sensorId}— delete a sensor
 *   *    /api/v1/sensors/{sensorId}/readings — delegated to SensorReadingResource
 *
 * REPORT Q1 — @Consumes mismatch:
 *   If a client sends Content-Type: text/plain or application/xml to a method
 *   annotated @Consumes(MediaType.APPLICATION_JSON), JAX-RS returns HTTP 415
 *   Unsupported Media Type automatically — before our code even runs.
 *   The framework checks the incoming Content-Type header against the declared
 *   @Consumes value and rejects mismatches without invoking the resource method.
 *
 * REPORT Q2 — @QueryParam vs path segment for filtering:
 *   /api/v1/sensors?type=CO2 is preferred because:
 *   - Query params are optional by design; the same endpoint handles all-sensors
 *     and filtered-sensors without needing multiple @Path patterns.
 *   - Path segments imply a distinct resource identity (/sensors/type/CO2 suggests
 *     "type" is a resource, not a filter criterion).
 *   - Query params are semantically the correct HTTP mechanism for search/filter.
 *   - It is easier to add more filters (e.g., ?type=CO2&status=ACTIVE) without
 *     creating an explosion of path combinations.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /sensors  (with optional ?type= filter) ───────────────────────────

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(result).build();
    }

    // ── POST /sensors ─────────────────────────────────────────────────────────

    /**
     * Registers a new sensor.
     * Validates that the referenced roomId actually exists.
     * Throws LinkedResourceNotFoundException → mapped to HTTP 422.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Sensor 'id' is required."))
                    .build();
        }

        if (store.getSensors().containsKey(sensor.getId())) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(Map.of("message", "Sensor '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // ── Integrity check: roomId must reference an existing room ────────────
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Sensor 'roomId' is required."))
                    .build();
        }

        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            // Room not found — throw 422 via mapper
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor: room '" + sensor.getRoomId() + "' does not exist in the system."
            );
        }

        // Set default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        // Persist sensor
        store.getSensors().put(sensor.getId(), sensor);

        // ── Update the room's sensorIds list ──────────────────────────────────
        room.addSensorId(sensor.getId());

        return Response
                .status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    // ── GET /sensors/{sensorId} ───────────────────────────────────────────────

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Sensor not found: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // ── DELETE /sensors/{sensorId} ────────────────────────────────────────────

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Sensor not found: " + sensorId))
                    .build();
        }

        // Remove from parent room's sensorIds list
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.removeSensorId(sensorId);
        }

        store.getSensors().remove(sensorId);
        store.getReadings().remove(sensorId); // clean up readings too

        return Response.noContent().build(); // 204
    }

    // ── Sub-Resource Locator: /sensors/{sensorId}/readings ───────────────────

    /**
     * Sub-Resource Locator — delegates all /readings sub-paths to SensorReadingResource.
     *
     * REPORT (Sub-resource locator pattern):
     *   Instead of defining every reading endpoint in this file, we return an
     *   instance of SensorReadingResource. JAX-RS then dispatches the request to
     *   that class. Benefits:
     *   - Separation of concerns: readings logic lives in its own class.
     *   - Easier unit testing: SensorReadingResource can be tested in isolation.
     *   - Scalability: adding more sub-resources (e.g., /alerts, /config) does not
     *     bloat SensorResource — each gets its own dedicated class.
     *   - Cleaner code: avoids one massive resource class with hundreds of methods.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}