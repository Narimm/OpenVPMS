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
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Department;
import org.openvpms.smartflow.model.TreatmentTemplate;
import org.openvpms.smartflow.service.Departments;
import org.openvpms.smartflow.service.TreatmentTemplates;

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
        Call<List<Department>, Departments> call = new Call<List<Department>, Departments>() {
            @Override
            public List<Department> call(Departments resource) throws Exception {
                List<Department> departments = resource.getDepartments();
                if (departments == null) {
                    departments = new ArrayList<>();
                }
                return departments;
            }

            @Override
            public Message failed(Exception exception) {
                return FlowSheetMessages.failedToGetDepartments();
            }
        };
        return call(Departments.class, call);
    }

    /**
     * Returns the treatment template names.
     *
     * @return the treatment template names
     */
    public List<String> getTreatmentTemplates() {
        Call<List<String>, TreatmentTemplates> call = new Call<List<String>, TreatmentTemplates>() {
            @Override
            public List<String> call(TreatmentTemplates resource) throws Exception {
                List<String> templates = new ArrayList<>();
                for (TreatmentTemplate template : resource.getTemplates()) {
                    templates.add(template.getName());
                }
                return templates;
            }

            @Override
            public Message failed(Exception exception) {
                return FlowSheetMessages.failedToGetTemplates();
            }
        };
        return call(TreatmentTemplates.class, call);
    }

}
