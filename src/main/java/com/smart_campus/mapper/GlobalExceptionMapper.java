package com.smart_campus.mapper;

import com.smart_campus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;


 /*
 
 
 
 Global "catch-all" Exception Mapper, the API's safety net.
That is the method to intercept ANY exception not caught already (NullPointerException
ArrayIndexOutOfBoundsException, etc.) and turning the exception into a harmless HTTP 500
response WITHOUT revealing raw stack trace.
REPORT, The security risk of exposing stack traces:
Exposing a Java stack trace to external API consumers provides:
1. Internal package/class structure, the attacker can find out the class names, method signatures, and the framework used
(e.g., "org.glassfish.jersey…"), which ultimately is useful in targeting known CVEs.
2. File paths, fully qualified server paths may be visible (e.g., /home/ubuntu/app/…).
3. Library versions, the trace contains dependency names that reveal versions with known exploits.
4. Business logic, the call stack literally shows how the internal code works which can be
used to make injection or exploitation more easily.
Returning a generic message denies attackers their reconnaissance data.

 
 
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