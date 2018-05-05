/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.service;

import org.openvpms.smartflow.model.Anesthetics;
import org.openvpms.smartflow.model.Form;
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
import java.util.List;

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
     * Returns anaesthetics information for a given hospitalization identifier.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the corresponding hospitalization, or {@code null} if none is found
     */
    @GET
    @Path("/{hospitalizationId}/anesthetics")
    @Produces({MediaType.APPLICATION_JSON})
    Anesthetics getAnesthetics(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Discharges a patient.
     *
     * @param hospitalizationId the hospitalization identifier
     * @param empty             must be an empty string. This is required to force a Content-Length: 0 header to be
     *                          passed
     * @return the hospitalization
     */
    @POST
    @Path("/discharge/{hospitalizationId}")
    @Produces({MediaType.APPLICATION_JSON})
    Hospitalization discharge(@PathParam("hospitalizationId") String hospitalizationId, String empty);

    /**
     * Removes a hospitalization, given its identifier.
     *
     * @param hospitalizationId the hospitalization identifier
     */
    @DELETE
    @Path("/{hospitalizationId}")
    void remove(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the forms for a patient.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the hospitalization
     */
    @GET
    @Path("/{hospitalizationId}/forms")
    @Produces({MediaType.APPLICATION_JSON})
    List<Form> getForms(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the medical records report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the medical records report PDF
     */
    @GET
    @Path("/{hospitalizationId}/medicalrecordsreport")
    @Produces({"application/pdf"})
    Response getMedicalRecordsReport(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the billing report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the billing report PDF
     */
    @GET
    @Path("/{hospitalizationId}/billingreport")
    @Produces({"application/pdf"})
    Response getBillingReport(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the notes report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the notes report PDF
     */
    @GET
    @Path("/{hospitalizationId}/notesreport")
    @Produces({"application/pdf"})
    Response getNotesReport(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the flow sheet report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the flow sheet report PDF
     */
    @GET
    @Path("/{hospitalizationId}/flowsheetreport")
    @Produces({"application/pdf"})
    Response getFlowSheetReport(@PathParam("hospitalizationId") String hospitalizationId);

    /**
     * Returns the form report PDF for a hospitalization.
     *
     * @param hospitalizationId the hospitalization identifier
     * @param formGuid          the form identifier
     * @return the flow sheet report PDF
     */
    @GET
    @Path("/{hospitalizationId}/formreport/{formGuid}")
    @Produces({"application/pdf"})
    Response getFormReport(@PathParam("hospitalizationId") String hospitalizationId,
                           @PathParam("formGuid") String formGuid);

}
