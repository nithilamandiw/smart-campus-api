package com.smartcampus.resource;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.SensorStore;
import com.smartcampus.store.SensorReadingStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        return SensorReadingStore.readings
                .getOrDefault(sensorId, new ArrayList<>());
    }

    @POST
    public Response addReading(SensorReading reading) {

        Sensor sensor = SensorStore.sensors.get(sensorId);

        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with id '" + sensorId + "' not found");
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently under maintenance and cannot accept readings");
        }

        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        sensor.setCurrentValue(reading.getValue());

        SensorReadingStore.readings
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}
