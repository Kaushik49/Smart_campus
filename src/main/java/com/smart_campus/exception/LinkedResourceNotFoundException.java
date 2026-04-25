package com.smart_campus.exception;

/**
 * Thrown when a resource references a dependency that does not exist.
 * E.g., POSTing a Sensor with a roomId that is not in the system.
 *
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 *
 * REPORT — Why 422 over 404?
 *   HTTP 404 means "the requested URI was not found on the server".
 *   But in this case the URI (/api/v1/sensors) IS valid and was found.
 *   The problem is inside the valid JSON payload — it contains a reference
 *   (roomId) pointing to a resource that doesn't exist.
 *   HTTP 422 means "the request was well-formed but contains semantic errors",
 *   which is a much more precise description of the actual problem.
 *   404 would mislead the client into thinking the endpoint itself is wrong.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}