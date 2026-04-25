package com.smart_campus.resource;

import com.smart_campus.exception.RoomNotEmptyException;
import com.smart_campus.model.Room;
import com.smart_campus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Room Resource — manages /api/v1/rooms
 *
 * Endpoints:
 *   GET    /api/v1/rooms            — list all rooms
 *   POST   /api/v1/rooms            — create a new room
 *   GET    /api/v1/rooms/{roomId}   — get a specific room
 *   DELETE /api/v1/rooms/{roomId}   — delete a room (blocked if sensors exist)
 *
 * REPORT Q1 — Full objects vs IDs:
 *   Returning full room objects is bandwidth-heavy but reduces round trips.
 *   Returning only IDs is lightweight but forces clients to make N extra requests.
 *   Best practice: return full objects for small collections; use pagination/sparse
 *   fields for large ones. For this campus API, full objects are returned.
 *
 * REPORT Q2 — DELETE idempotency:
 *   DELETE is idempotent: the end state after multiple identical DELETE requests
 *   is the same — the room does not exist. The first call returns 204 No Content
 *   (success). Subsequent calls return 404 Not Found because the room is already
 *   gone. Although the HTTP status differs, the server's state is unchanged —
 *   making it idempotent by definition.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /rooms ────────────────────────────────────────────────────────────

    @GET
    public Response getAllRooms() {
        Collection<Room> allRooms = store.getRooms().values();
        return Response.ok(new ArrayList<>(allRooms)).build();
    }

    // ── POST /rooms ───────────────────────────────────────────────────────────

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Room 'id' is required."))
                    .build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(Map.of("message", "Room with id '" + room.getId() + "' already exists."))
                    .build();
        }

        // Ensure sensorIds list is initialised
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        store.getRooms().put(room.getId(), room);

        return Response
                .status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    // ── GET /rooms/{roomId} ───────────────────────────────────────────────────

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Room not found: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    // ── DELETE /rooms/{roomId} ────────────────────────────────────────────────

    /**
     * Deletes a room — BLOCKED if the room still has sensors assigned to it.
     * Throws RoomNotEmptyException → mapped to HTTP 409 Conflict.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);

        if (room == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Room not found: " + roomId))
                    .build();
        }

        // ── Business Logic: block deletion if sensors are still assigned ──────
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted because it still has " +
                room.getSensorIds().size() + " sensor(s) assigned. " +
                "Remove all sensors first."
            );
        }

        store.getRooms().remove(roomId);
        return Response.noContent().build(); // 204 No Content
    }
}