package com.smart_campus.mapper;

import com.smart_campus.exception.LinkedResourceNotFoundException;
import com.smart_campus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


 /*
 
 Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
Raised if the POST /sensors payload includes a non-existing roomId.
The request URI is correct and the JSON is properly formed, however the
referenced resource (room) is absent, so 422 is really the more
semantically correct response compared to 404.
 
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