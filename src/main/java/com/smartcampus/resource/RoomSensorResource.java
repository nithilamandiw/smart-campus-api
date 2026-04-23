package com.smartcampus.resource;

import com.smartcampus.exception.InvalidInputException;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.RoomStore;
import com.smartcampus.store.SensorStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/rooms/{roomId}/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomSensorResource {

    @GET
    public Response getSensorsByRoom(@PathParam("roomId") String roomId) {

        Room room = RoomStore.rooms.get(roomId);

        if (room == null) {
            throw new LinkedResourceNotFoundException("Room not found");
        }

        List<Sensor> sensors = room.getSensorIds()
                .stream()
                .map(id -> SensorStore.sensors.get(id))
                .collect(Collectors.toList());

        return Response.ok(sensors).build();
    }

    @POST
    public Response addSensorToRoom(@PathParam("roomId") String roomId, Sensor sensor) {

        Room room = RoomStore.rooms.get(roomId);

        if (room == null) {
            throw new LinkedResourceNotFoundException("Room not found");
        }

        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            throw new InvalidInputException("Sensor ID is required");
        }

        if (SensorStore.sensors.containsKey(sensor.getId())) {
            throw new InvalidInputException("Sensor already exists");
        }

        sensor.setRoomId(roomId);

        SensorStore.sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }
}