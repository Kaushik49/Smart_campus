package com.smart_campus.store;

import com.smart_campus.model.Room;
import com.smart_campus.model.Sensor;
import com.smart_campus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store for the Smart Campus API.
 *
 * WHY A SINGLETON?
 *   JAX-RS creates a new resource instance per request (per-request lifecycle).
 *   If we stored data inside the resource class fields, each request would see
 *   empty data. By placing all data in this singleton, every request shares
 *   the same data regardless of which resource instance handles it.
 *
 * THREAD SAFETY:
 *   ConcurrentHashMap is used for rooms, sensors, and readings so that
 *   concurrent requests (e.g., simultaneous POST and GET) do not corrupt the
 *   data or cause ConcurrentModificationExceptions.
 *
 *   For the readings list per sensor, the list itself is wrapped in
 *   synchronization when adding entries to prevent race conditions.
 */
public class DataStore {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static final DataStore INSTANCE = new DataStore();

    private DataStore() {}

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── In-Memory Collections ─────────────────────────────────────────────────

    /** All rooms on campus, keyed by room ID */
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /** All sensors on campus, keyed by sensor ID */
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /**
     * Historical readings per sensor.
     * Key = sensorId, Value = ordered list of SensorReading objects.
     */
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getReadings() {
        return readings;
    }

    // ── Convenience Helpers ───────────────────────────────────────────────────

    /** Get readings list for a sensor (creates empty list if not present). */
    public List<SensorReading> getOrCreateReadingsList(String sensorId) {
        return readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }
}