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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.service;

import org.openvpms.smartflow.model.Department;
import org.openvpms.smartflow.model.Medic;
import org.openvpms.smartflow.model.Medics;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.openvpms.smartflow.model.TreatmentTemplate;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Smart Flow Sheet reference data API.
 *
 * @author Tim Anderson.
 */
public interface ReferenceData {

    /**
     * Returns the departments.
     *
     * @return the departments
     */
    @GET
    @Path("/departments")
    @Produces({MediaType.APPLICATION_JSON})
    List<Department> getDepartments();

    /**
     * Returns the medics.
     *
     * @return the medics
     */
    @GET
    @Path("/medics")
    @Produces({MediaType.APPLICATION_JSON})
    List<Medic> getMedics();

    /**
     * Updates the medics.
     *
     * @param medics the medics
     */
    @POST
    @Path("/medics")
    @Consumes({MediaType.APPLICATION_JSON})
    void updateMedics(Medics medics);

    /**
     * Removes a medic, given its identifier.
     *
     * @param id the medic identifier
     */
    @DELETE
    @Path("/medic/{id}")
    void removeMedic(@PathParam("id") String id);

    /**
     * Returns a list of Treatment Templates from SFS.
     *
     * @return the treatment templates
     */
    @GET
    @Path("/treatmenttemplates")
    @Produces({MediaType.APPLICATION_JSON})
    List<TreatmentTemplate> getTemplates();

    /**
     * Returns the service bus configuration.
     *
     * @return the service bus configuration
     */
    @GET
    @Path("/account/sb-credentials")
    @Produces({MediaType.APPLICATION_JSON})
    ServiceBusConfig getServiceBusConfig();
}
