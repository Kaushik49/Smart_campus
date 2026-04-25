package com.smart_campus.exception;

/**
 * Thrown when a client attempts to POST a reading to a sensor that is in
 * MAINTENANCE or OFFLINE status and therefore cannot accept new readings.
 *
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}