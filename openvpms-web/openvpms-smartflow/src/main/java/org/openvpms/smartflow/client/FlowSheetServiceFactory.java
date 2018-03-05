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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.smartflow.i18n.FlowSheetMessages;

import java.util.TimeZone;

/**
 * Factory for Smart Flow Sheet services.
 *
 * @author Tim Anderson
 */
public class FlowSheetServiceFactory {

    /**
     * The Smart Flow Sheet URL.
     */
    private final String url;

    /**
     * The emrApiKey submitted with each request.
     */
    private final String emrApiKey;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The medical record rules.
     */
    private final MedicalRecordRules rules;

    /**
     * Constructs an {@link FlowSheetServiceFactory}.
     *
     * @param url       the Smart Flow Sheet URL
     * @param emrApiKey the emrApiKey submitted with each request
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param handlers  the document handlers
     * @param rules     the medical record rules
     */
    public FlowSheetServiceFactory(String url, String emrApiKey, IArchetypeService service, ILookupService lookups,
                                   DocumentHandlers handlers, MedicalRecordRules rules) {
        this.url = url;
        this.emrApiKey = emrApiKey;
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
        this.rules = rules;
    }

    /**
     * Determines if a practice location has a clinic API key.
     *
     * @param location the practice location
     * @return {@code true} if the location has a key
     */
    public boolean isSmartFlowSheetEnabled(Party location) {
        return getClinicAPIKey(location) != null;
    }

    /**
     * Returns the clinic API key for the practice.
     *
     * @param location the practice location
     * @return the clinic API key, or {@code null} if none exists
     */
    public String getClinicAPIKey(Party location) {
        String result = null;
        if (location != null) {
            IMObjectBean bean = new IMObjectBean(location, service);
            result = StringUtils.trimToNull(bean.getString("smartFlowSheetKey"));
        }
        return result;
    }

    /**
     * Creates a {@link HospitalizationService} for the specified practice location.
     *
     * @param location the practice location
     * @return a new {@link HospitalizationService}
     */
    public HospitalizationService getHospitalizationService(Party location) {
        String apiKey = getRequiredClinicAPIKey(location);
        return getHospitalizationService(apiKey);
    }

    /**
     * Creates a {@link HospitalizationService} for the specified practice location.
     *
     * @param apiKey the clinic API key
     * @return a new {@link HospitalizationService}
     */
    public HospitalizationService getHospitalizationService(String apiKey) {
        return new HospitalizationService(url, emrApiKey, apiKey, TimeZone.getDefault(), service, lookups, handlers,
                                          rules);
    }

    /**
     * Creates a {@link ReferenceDataService} for the specified practice location.
     *
     * @param location the practice location
     * @return a new {@link ReferenceDataService}
     */
    public ReferenceDataService getReferenceDataService(Party location) {
        String clinicKey = getRequiredClinicAPIKey(location);
        return new ReferenceDataService(url, emrApiKey, clinicKey, TimeZone.getDefault(), location, service);
    }

    /**
     * Creates a {@link InventoryService} for the specified practice location.
     *
     * @param location the practice location
     * @return a new {@link InventoryService}
     */
    public InventoryService getInventoryService(Party location) {
        String clinicKey = getRequiredClinicAPIKey(location);
        return new InventoryService(url, emrApiKey, clinicKey, TimeZone.getDefault(), service, lookups);
    }

    /**
     * Returns the clinic API key for the practice, throwing an exception if it doesn't exist
     *
     * @param location the practice location
     * @return the clinic API key
     * @throws FlowSheetException if the API key does not exist
     */
    protected String getRequiredClinicAPIKey(Party location) {
        String clinicKey = getClinicAPIKey(location);
        if (clinicKey == null) {
            throw new FlowSheetException(FlowSheetMessages.notConfigured(location));
        }
        return clinicKey;
    }

}
