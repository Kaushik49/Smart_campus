package com.smart_campus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Discovery Endpoint — GET /api/v1
 *
 * Returns API metadata including version, admin contact, and navigation
 * links to all primary resource collections (HATEOAS principle).
 *
 * HATEOAS NOTE (for report):
 *   Hypermedia As The Engine Of Application State (HATEOAS) means API responses
 *   include links to related resources, so clients can navigate the API
 *   dynamically without relying solely on hardcoded documentation.
 *
 *   Benefits:
 *   - Clients discover available actions at runtime — reducing coupling to docs.
 *   - API can evolve (e.g., change paths) without breaking clients that follow links.
 *   - Easier onboarding: a developer can "walk" the API from this root endpoint.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();

        // ── API Metadata ──────────────────────────────────────────────────────
        response.put("apiName", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("adminContact", "admin@smartcampus.ac.uk");

        // ── Resource Links (HATEOAS) ──────────────────────────────────────────
        Map<String, String> links = new HashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("links", links);

        return Response.ok(response).build();
    }
}