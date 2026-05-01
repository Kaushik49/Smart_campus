package com.smart_campus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;


/*

API Observability Filter, logs each and every request and response.
This implementation runs as a pair of filters:
ContainerRequestFilter and ContainerResponseFilter enabling the same single class to carry out the logging of both inbound and outbound messages.
Details logged with each request: the HTTP method along with the entire request URI
Details logged with each response: HTTP status code
REPORT, Why rely on filters rather than just placing Logger.info() inside each resource method?
1. Cross-cutting concern: Logging is something that is commonplace to all endpoints in the same way.
So attaching Logger.info() to 20 methods means that you have to update 20 separate places in case the
log format changes.
2. DRY (Don't Repeat Yourself): A single filter is a single source of truth for
the logging logic.
3. Separation of concerns: Resource classes are meant for the business logic only; the filter
can take care of the observability part which leads to a much cleaner and more
testable code on both sides.
4. Consistency: A filter ensures that every request gets logged even if a developer
fails to remember to put the log statement in a newly created resource method.
5. Aspect-Oriented: JAX-RS filters are the official way for cross-cutting
concerns such as logging, authentication, CORS, and rate-limiting.




*/
 
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Called BEFORE the request reaches a resource method.
     * Logs: HTTP method + request URI
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(String.format("[REQUEST]  %s %s", method, uri));
    }

    /**
     * Called AFTER the resource method returns its response.
     * Logs: HTTP status code
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        int statusCode = responseContext.getStatus();
        String method  = requestContext.getMethod();
        String uri     = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(String.format("[RESPONSE] %s %s → %d", method, uri, statusCode));
    }
}