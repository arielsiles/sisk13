package com.encens.khipus.restful;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.Serializable;

@Name("purchaseOrderResource")
@Path("/purchaseOrder")
@Transactional
public class PurchaseOrderResource implements Serializable {

    private static final long serialVersionUID = 8655977096519941447L;



    @GET
    @Path("/{reference}")
    @Produces("text/plain")
    public String closePurchaseOrder(@PathParam("reference") String reference) {
        System.out.println("request received [reference:'" + reference + "']");
        return "ok";
    }

}
