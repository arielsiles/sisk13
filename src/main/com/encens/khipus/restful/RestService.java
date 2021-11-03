package com.encens.khipus.restful;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/prueba") /*URL a la que atenderá las peticiones*/
public class RestService {

    @GET /*Tipo de llamada*/
    @Path("/{param}")
    public Response getMsg(@PathParam("param") String msg) {
        /*Mostrará por pantalla el parámetro que le hemos pasado a la URL*/
        String output = "My Jersey application says : " + msg;
        return Response.status(200).entity(output).build();
    }

}
