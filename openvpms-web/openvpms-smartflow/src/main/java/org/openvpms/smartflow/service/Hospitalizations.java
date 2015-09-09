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
import javax.ws.rs.core.Response;

/**
 * Smart Flow Sheet Hospitalizations API.
 *
 * @author Tim Anderson
 */
@Path("/hospitalization")
public interface Hospitalizations {

    /**
     * Adds a new hospitalizations.
     *
     * @param hospitalization the hospitalization to add
     * @return the created hospitalization
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    Hospitalization add(Hospitalization hospitalization);

    /**
     * Returns a hospitalization, given its identifier.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the corresponding hospitalization, or {@code null} if none is found
     */
    @GET
    @Path("/{hospitalizationId}")
    @Produces({MediaType.APPLICATION_JSON})
    Hospitalization get(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Removes a hospitalization, given its identifier.
     *
     * @param hospitalizationId the hospitalization identifier
     */
    @DELETE
    @Path("/{hospitalizationId}")
    void remove(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the medical records report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     */
    @GET
    @Path("/{hospitalizationId}/medicalrecordsreport")
    @Produces({"application/pdf"})
    Response getMedicalRecordsReport(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the inventory report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     */
    @GET
    @Path("/{hospitalizationId}/inventoryreport")
    @Produces({"application/pdf"})
    Response getInventoryReport(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the tech notes report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     */
    @GET
    @Path("/{hospitalizationId}/technotesreport")
    @Produces({"application/pdf"})
    Response getTechNotesReport(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the flow sheet report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     */
    @GET
    @Path("/{hospitalizationId}/flowsheetreport")
    @Produces({"application/pdf"})
    Response getFlowSheetReport(@PathParam("hospitalizationId") String hospitalizationId);
}
