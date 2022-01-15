package com.encens.khipus.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/hello")
public class HelloRS {

    @GET
    @Path("/{name}")
    public String sayHello(@PathParam("name") String name) {

        return "Hello, " + name;
    }
}
