package com.smart_campus.model;

/**
 * Standardised JSON error body returned by all Exception Mappers.
 *
 * Example response body:
 * {
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "Room still has active sensors assigned to it.",
 *   "timestamp": 1714000000000
 * }
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private long timestamp;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}