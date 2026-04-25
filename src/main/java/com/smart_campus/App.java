package com.smart_campus;

import com.smart_campus.filter.LoggingFilter;
import com.smart_campus.mapper.GlobalExceptionMapper;
import com.smart_campus.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smart_campus.mapper.RoomNotEmptyExceptionMapper;
import com.smart_campus.mapper.SensorUnavailableExceptionMapper;
import com.smart_campus.resource.DiscoveryResource;
import com.smart_campus.resource.RoomResource;
import com.smart_campus.resource.SensorResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    // Base URI includes /api/v1 — no need for @ApplicationPath at all
    private static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";

    public static void main(String[] args) throws IOException, InterruptedException {

        // Build config manually — most reliable approach with Grizzly fat JARs
        ResourceConfig config = new ResourceConfig()
            // Resources
            .register(DiscoveryResource.class)
            .register(RoomResource.class)
            .register(SensorResource.class)
            // Exception Mappers
            .register(RoomNotEmptyExceptionMapper.class)
            .register(LinkedResourceNotFoundExceptionMapper.class)
            .register(SensorUnavailableExceptionMapper.class)
            .register(GlobalExceptionMapper.class)
            // Filter
            .register(LoggingFilter.class)
            // JSON
            .register(JacksonFeature.class);

        final HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), config);

        LOGGER.info("Smart Campus API started at: http://localhost:8080/api/v1");
        LOGGER.info("Press CTRL+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            server.shutdown();
        }));

        Thread.currentThread().join();
    }
}