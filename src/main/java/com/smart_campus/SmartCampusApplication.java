package com.smart_campus;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

// @ApplicationPath is now irrelevant since App.java sets the path directly
// Keep this file only if required for the report/submission
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // All registration is handled in App.java
    }
}