package com.smart_campus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API Observability Filter — logs every request and response.
 *
 * Implements BOTH ContainerRequestFilter and ContainerResponseFilter
 * so a single class handles inbound and outbound logging.
 *
 * Logged per request:  HTTP method + full request URI
 * Logged per response: HTTP status code
 *
 * REPORT — Why use filters instead of Logger.info() in every resource method?
 *   1. Cross-cutting concern: Logging applies to every endpoint uniformly.
 *      Adding Logger.info() to 20 methods means 20 places to update if the
 *      log format changes.
 *   2. DRY (Don't Repeat Yourself): One filter = one source of truth for logging logic.
 *   3. Separation of concerns: Resource classes focus on business logic; the filter
 *      handles observability. This makes both easier to read and test.
 *   4. Consistency: A filter guarantees every request is logged even if a developer
 *      forgets to add the log statement to a new resource method.
 *   5. Aspect-Oriented: JAX-RS filters are the standard mechanism for cross-cutting
 *      concerns like logging, authentication, CORS, and rate-limiting.
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