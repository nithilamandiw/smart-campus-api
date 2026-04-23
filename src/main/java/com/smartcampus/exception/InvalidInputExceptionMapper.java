package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException> {

    @Override
    public Response toResponse(InvalidInputException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}