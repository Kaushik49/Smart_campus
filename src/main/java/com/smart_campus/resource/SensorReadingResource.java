package com.smart_campus.resource;

import com.smart_campus.exception.SensorUnavailableException;
import com.smart_campus.model.Sensor;
import com.smart_campus.model.SensorReading;
import com.smart_campus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * SensorReadingResource — Sub-resource for /api/v1/sensors/{sensorId}/readings
 *
 * This class is instantiated by SensorResource's sub-resource locator method,
 * receiving the parent sensorId as a constructor argument.
 *
 * Endpoints:
 *   GET  /api/v1/sensors/{sensorId}/readings        — list all historical readings
 *   POST /api/v1/sensors/{sensorId}/readings        — append a new reading
 *
 * Side effect of POST:
 *   Updates the parent Sensor's currentValue field to reflect the latest reading,
 *   keeping the sensor's summary data consistent with the full reading history.
 */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    /**
     * Constructor called by the sub-resource locator in SensorResource.
     * @param sensorId the parent sensor's ID, extracted from the URI path
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ── GET /sensors/{sensorId}/readings ──────────────────────────────────────

    @GET
    public Response getAllReadings() {
        // Verify sensor exists
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Sensor not found: " + sensorId))
                    .build();
        }

        List<SensorReading> history = store.getOrCreateReadingsList(sensorId);
        return Response.ok(history).build();
    }

    // ── POST /sensors/{sensorId}/readings ─────────────────────────────────────

    /**
     * Appends a new reading for this sensor.
     *
     * BLOCKED if sensor status is "MAINTENANCE" — throws SensorUnavailableException
     * which is mapped to HTTP 403 Forbidden.
     *
     * SIDE EFFECT: Updates sensor.currentValue to match the new reading's value.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // ── Verify sensor exists ──────────────────────────────────────────────
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Sensor not found: " + sensorId))
                    .build();
        }

        // ── State Constraint: block if sensor is under MAINTENANCE ────────────
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently in MAINTENANCE mode " +
                "and cannot accept new readings."
            );
        }

        // ── Also block if OFFLINE ─────────────────────────────────────────────
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is OFFLINE and cannot accept new readings."
            );
        }

        // ── Auto-generate id/timestamp if missing ──────────────────────────────
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // ── Persist the reading ───────────────────────────────────────────────
        List<SensorReading> history = store.getOrCreateReadingsList(sensorId);
        synchronized (history) {
            history.add(reading);
        }

        // ── Side Effect: update the sensor's currentValue field ───────────────
        sensor.setCurrentValue(reading.getValue());

        return Response
                .status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}