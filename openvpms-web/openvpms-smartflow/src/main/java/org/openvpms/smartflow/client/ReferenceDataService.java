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

package org.openvpms.smartflow.client;

import org.apache.commons.logging.LogFactory;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Department;
import org.openvpms.smartflow.model.TreatmentTemplate;
import org.openvpms.smartflow.service.Departments;
import org.openvpms.smartflow.service.TreatmentTemplates;

import javax.ws.rs.NotAuthorizedException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Smart Flow Sheet reference data service.
 *
 * @author benjamincharlton on 21/10/2015.
 * @author Tim Anderson
 */
public class ReferenceDataService extends FlowSheetService {

    /**
     * Constructs a {@link ReferenceDataService}.
     *
     * @param url          the Smart Flow Sheet URL
     * @param emrApiKey    the EMR API key
     * @param clinicApiKey the clinic API key
     * @param timeZone     the timezone. This determines how dates are serialized
     */
    public ReferenceDataService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone) {
        super(url, emrApiKey, clinicApiKey, timeZone, LogFactory.getLog(ReferenceDataService.class));
    }

    /**
     * Returns the departments.
     *
     * @return the departments
     */
    public List<Department> getDepartments() {
        List<Department> departments = new ArrayList<>();
        javax.ws.rs.client.Client client = getClient();
        try {
            Departments service = getResource(Departments.class, client);
            departments.addAll(service.getDepartments());
        } catch (NotAuthorizedException exception) {
            notAuthorised(exception);
        } catch (Throwable exception) {
            checkSSL(exception);
            throw new FlowSheetException(FlowSheetMessages.failedToGetDepartments(), exception);
        } finally {
            client.close();
        }
        return departments;
    }

    /**
     * Returns the treatment template names.
     *
     * @return the treatment template names
     */
    public List<String> getTreatmentTemplates() {
        List<String> templates = new ArrayList<>();
        javax.ws.rs.client.Client client = getClient();
        try {
            TreatmentTemplates service = getResource(TreatmentTemplates.class, client);
            for (TreatmentTemplate template : service.getTemplates()) {
                templates.add(template.getName());
            }
        } catch (NotAuthorizedException exception) {
            notAuthorised(exception);
        } catch (Throwable exception) {
            checkSSL(exception);
            throw new FlowSheetException(FlowSheetMessages.failedToGetTemplates(), exception);
        } finally {
            client.close();
        }
        return templates;
    }


}
