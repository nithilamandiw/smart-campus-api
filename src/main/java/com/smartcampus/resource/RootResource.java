package com.smartcampus.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class RootResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Smart Campus API");
        response.put("version", "1.0");
        response.put("description", "RESTful API for managing smart campus rooms, sensors, and readings");
        response.put("contact", "admin@westminster.ac.uk");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("rooms", "/api/v1/rooms");
        endpoints.put("sensors", "/api/v1/sensors");
        response.put("endpoints", endpoints);

        return Response.ok(response).build();
    }
}
