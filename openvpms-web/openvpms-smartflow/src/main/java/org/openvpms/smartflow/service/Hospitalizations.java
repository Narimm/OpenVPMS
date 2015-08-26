package org.openvpms.smartflow.service;

import org.openvpms.smartflow.model.Hospitalization;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * .
 *
 * @author Tim Anderson
 */
@Path("/hospitalization")
public interface Hospitalizations {

    @GET
    @Path("/{hospitalizationId}")
    @Produces({MediaType.APPLICATION_JSON})
    Hospitalization get(@PathParam("hospitalizationId") String hospitalizationId);

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    void add(Hospitalization hospitalization);

    @DELETE
    @Path("/{hospitalizationId}")
    void remove(@PathParam("hospitalizationId") String hospitalizationId);
}
