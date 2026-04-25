package com.smart_campus.mapper;

import com.smart_campus.exception.SensorUnavailableException;
import com.smart_campus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps SensorUnavailableException → HTTP 403 Forbidden.
 *
 * Triggered when a POST /sensors/{sensorId}/readings is attempted on a sensor
 * that is in MAINTENANCE or OFFLINE status. The client does not have permission
 * to write readings to an unavailable sensor.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorResponse error = new ErrorResponse(
            Response.Status.FORBIDDEN.getStatusCode(),
            "Forbidden",
            exception.getMessage()
        );
        return Response
                .status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}