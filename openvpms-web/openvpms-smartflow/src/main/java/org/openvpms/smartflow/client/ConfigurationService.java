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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.client;

import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.TreatmentTemplate;
import org.openvpms.smartflow.service.TreatmentTemplates;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * The configuration service for Smart Flow Sheet.
 *
 * @author benjamincharlton on 21/10/2015.
 */
public class ConfigurationService extends FlowSheetService {

    /**
     * The treatment template names.
     */
    private List<String> templates;

    /**
     * Constructs a {@link ConfigurationService}.
     *
     * @param url          the Smart Flow Sheet URL
     * @param emrApiKey    the EMR API key
     * @param clinicApiKey the clinic API key
     * @param timeZone     the timezone. This determines how dates are serialized
     */
    public ConfigurationService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone) {
        super(url, emrApiKey, clinicApiKey, timeZone, LogFactory.getLog(ConfigurationService.class));
    }

    /**
     * Returns the treatment template names.
     *
     * @return the treatment template names
     */
    public List<String> getTreatmentTemplates() {
        if (templates == null) {
            templates = new ArrayList<>();
            javax.ws.rs.client.Client client = getClient();
            try {
                WebTarget target = getWebTarget(client);
                TreatmentTemplates service = getTreatmentTemplates(target);
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
        }
        return templates;
    }

    /**
     * Creates a new {@link TreatmentTemplates} proxy for the specified target.
     *
     * @param target the target
     * @return a new proxy
     */
    private TreatmentTemplates getTreatmentTemplates(WebTarget target) {
        return WebResourceFactory.newResource(TreatmentTemplates.class, target, false, getHeaders(),
                                              Collections.<Cookie>emptyList(), EMPTY_FORM);
    }
}
