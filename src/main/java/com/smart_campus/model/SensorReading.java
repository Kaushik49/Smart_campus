package com.smart_campus.model;

import java.util.UUID;

/**
 * Represents a single historical reading recorded by a Sensor.
 * Each reading is immutable once created and logged against a sensor's history.
 */
public class SensorReading {

    private String id;        // Unique reading event ID (UUID)
    private long timestamp;   // Epoch time (ms) when the reading was captured
    private double value;     // The actual metric value recorded by the hardware

    // ── Constructors ──────────────────────────────────────────────────────────

    public SensorReading() {}

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    @Override
    public String toString() {
        return "SensorReading{id='" + id + "', timestamp=" + timestamp + ", value=" + value + "}";
    }
}