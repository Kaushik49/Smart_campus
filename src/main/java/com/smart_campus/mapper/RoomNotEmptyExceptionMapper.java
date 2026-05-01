package com.smart_campus.mapper;

import com.smart_campus.exception.RoomNotEmptyException;
import com.smart_campus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


  /*
  
  
  
Maps RoomNotEmptyException → HTTP 409 Conflict.
 
Triggered when a DELETE /rooms/{roomId} is attempted on a room that still
has sensors assigned. The JSON body explains which sensors are blocking deletion.
  
  */

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorResponse error = new ErrorResponse(
            Response.Status.CONFLICT.getStatusCode(),
            "Conflict",
            exception.getMessage()
        );
        return Response
                .status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}