package com.smartcampus.resource;

import com.smartcampus.exception.InvalidInputException;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.SensorStore;
import com.smartcampus.store.RoomStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @POST
    public Response createSensor(Sensor sensor) {

        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            throw new InvalidInputException("Sensor ID is required");
        }

        if (sensor.getType() == null || sensor.getType().isEmpty()) {
            throw new InvalidInputException("Sensor type is required");
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            throw new InvalidInputException("Room ID is required");
        }

        if (!RoomStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room with id '" + sensor.getRoomId() + "' does not exist");
        }

        if (SensorStore.sensors.containsKey(sensor.getId())) {
            throw new InvalidInputException("Sensor with id '" + sensor.getId() + "' already exists");
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        SensorStore.sensors.put(sensor.getId(), sensor);
        RoomStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {

        if (type == null) {
            return new ArrayList<>(SensorStore.sensors.values());
        }

        return SensorStore.sensors.values()
                .stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // Sub-resource locator: delegates /sensors/{id}/readings to SensorReadingResource
    @Path("/{id}/readings")
    public SensorReadingResource getReadingResource(@PathParam("id") String id) {
        // Validate sensor exists before delegating to the sub-resource
        if (!SensorStore.sensors.containsKey(id)) {
            throw new ResourceNotFoundException("Sensor with id '" + id + "' not found");
        }
        return new SensorReadingResource(id);
    }
}