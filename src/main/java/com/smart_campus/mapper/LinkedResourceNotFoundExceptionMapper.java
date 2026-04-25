package com.smart_campus.mapper;

import com.smart_campus.exception.LinkedResourceNotFoundException;
import com.smart_campus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity.
 *
 * Triggered when a POST /sensors body contains a roomId that does not exist.
 * The request URI is valid and the JSON is well-formed, but the referenced
 * resource (room) is missing — hence 422 is semantically more accurate than 404.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    // HTTP 422 is not in JAX-RS 2.x Status enum, so we use the raw code.
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            UNPROCESSABLE_ENTITY,
            "Unprocessable Entity",
            exception.getMessage()
        );
        return Response
                .status(UNPROCESSABLE_ENTITY)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}