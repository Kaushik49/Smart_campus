package com.smart_campus.mapper;

import com.smart_campus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "catch-all" Exception Mapper — the API's safety net.
 *
 * Intercepts ANY uncaught exception (NullPointerException, ArrayIndexOutOfBoundsException,
 * etc.) and converts it into a safe HTTP 500 response WITHOUT exposing the raw stack trace.
 *
 * REPORT — Security risk of exposing stack traces:
 *   Exposing a Java stack trace to external API consumers reveals:
 *   1. Internal package/class structure — attackers learn class names, method signatures,
 *      and the framework in use (e.g., "org.glassfish.jersey…"), which helps target known CVEs.
 *   2. File paths — absolute server paths may appear (e.g., /home/ubuntu/app/…).
 *   3. Library versions — dependency names in the trace reveal versions with known exploits.
 *   4. Business logic — the call stack exposes how the code works internally, making
 *      targeted injection or exploitation much easier.
 *   By returning a generic message we deny attackers this reconnaissance data.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full exception server-side so developers can investigate
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by GlobalExceptionMapper", exception);

        // Return a generic, safe response to the client — no stack trace!
        ErrorResponse error = new ErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            "An unexpected error occurred. Please contact the API administrator."
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}