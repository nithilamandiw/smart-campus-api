package com.smartcampus.resource;

import com.smartcampus.exception.InvalidInputException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.RoomStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @Context
    private UriInfo uriInfo;

    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(RoomStore.rooms.values())).build();
    }

    @POST
    public Response createRoom(Room room) {

        if (room.getId() == null || room.getId().isEmpty()) {
            throw new InvalidInputException("Room ID is required");
        }

        if (room.getName() == null || room.getName().isEmpty()) {
            throw new InvalidInputException("Room name is required");
        }

        if (room.getCapacity() <= 0) {
            throw new InvalidInputException("Capacity must be greater than 0");
        }

        if (RoomStore.rooms.containsKey(room.getId())) {
            throw new InvalidInputException("Room with id '" + room.getId() + "' already exists");
        }

        RoomStore.rooms.put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        return Response.created(location)
                .entity(room)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") String id) {

        Room room = RoomStore.rooms.get(id);

        if (room == null) {
            throw new ResourceNotFoundException("Room with id '" + id + "' not found");
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {

        Room room = RoomStore.rooms.get(id);

        if (room == null) {
            throw new ResourceNotFoundException("Room with id '" + id + "' not found");
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + id + "' still has " + room.getSensorIds().size()
                    + " sensor(s) assigned and cannot be deleted");
        }

        RoomStore.rooms.remove(id);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Room '" + id + "' deleted successfully");

        return Response.ok(response).build();
    }
}
