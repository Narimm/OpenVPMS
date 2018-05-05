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

package org.openvpms.smartflow.client;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.user.ClinicianQueryFactory;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.i18n.Message;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Department;
import org.openvpms.smartflow.model.Medic;
import org.openvpms.smartflow.model.Medics;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.openvpms.smartflow.model.TreatmentTemplate;
import org.openvpms.smartflow.service.ReferenceData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Smart Flow Sheet reference data service.
 *
 * @author benjamincharlton on 21/10/2015.
 * @author Tim Anderson
 */
public class ReferenceDataService extends FlowSheetService {

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link ReferenceDataService}.
     *
     * @param url          the Smart Flow Sheet URL
     * @param emrApiKey    the EMR API key
     * @param clinicApiKey the clinic API key
     * @param timeZone     the timezone. This determines how dates are serialized
     * @param location     the practice location
     * @param service      the archetype service
     */
    public ReferenceDataService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone,
                                Party location, IArchetypeService service) {
        super(url, emrApiKey, clinicApiKey, timeZone, LogFactory.getLog(ReferenceDataService.class));
        this.location = location;
        this.service = service;
    }

    /**
     * Returns the departments.
     *
     * @return the departments
     */
    public List<Department> getDepartments() {
        Call<List<Department>, ReferenceData> call = new Call<List<Department>, ReferenceData>() {
            @Override
            public List<Department> call(ReferenceData resource) throws Exception {
                List<Department> departments = resource.getDepartments();
                if (departments == null) {
                    departments = new ArrayList<>();
                }
                return departments;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetDepartments();
            }
        };
        return call(ReferenceData.class, call);
    }

    /**
     * Returns the medics.
     *
     * @return the medics
     */
    public List<Medic> getMedics() {
        Call<List<Medic>, ReferenceData> call = new Call<List<Medic>, ReferenceData>() {
            @Override
            public List<Medic> call(ReferenceData resource) throws Exception {
                List<Medic> medics = resource.getMedics();
                if (medics == null) {
                    medics = new ArrayList<>();
                }
                return medics;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetMedics();
            }
        };
        return call(ReferenceData.class, call);
    }

    public void updateMedics(final List<Medic> medics, final UUID uuid) {
        Call<Void, ReferenceData> call = new Call<Void, ReferenceData>() {
            @Override
            public Void call(ReferenceData resource) throws Exception {
                Medics update = new Medics();
                update.setMedics(medics);
                update.setId(uuid.toString());
                resource.updateMedics(update);
                return null;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToUpdateMedics();
            }
        };
        call(ReferenceData.class, call);
    }

    /**
     * Removes a medic.
     *
     * @param medic the medic to remove
     */
    public void removeMedic(final Medic medic) {
        Call<Void, ReferenceData> call = new Call<Void, ReferenceData>() {
            @Override
            public Void call(ReferenceData resource) throws Exception {
                resource.removeMedic(medic.getMedicId());
                return null;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToRemoveMedic(medic.getMedicId(), medic.getName());
            }
        };
        call(ReferenceData.class, call);
    }

    /**
     * Synchronises medics.
     *
     * @return the synchronisation state
     */
    public SyncState synchroniseMedics() {
        Iterator<User> clinicians = getClinicians();
        Map<String, Medic> medicMap = getMedicMap();
        int added = 0;
        int updated = 0;
        int removed = 0;
        List<Medic> changed = new ArrayList<>();
        while (clinicians.hasNext()) {
            User clinician = clinicians.next();
            String id = Long.toString(clinician.getId());
            Medic currentMedic = medicMap.remove(id);
            Medic updatedMedic = synchronise(clinician, currentMedic, id);
            if (updatedMedic != null) {
                changed.add(updatedMedic);
                if (currentMedic == null) {
                    added++;
                } else {
                    updated++;
                }
            }
        }
        if (!changed.isEmpty()) {
            updateMedics(changed, UUID.randomUUID());
        }
        for (Medic item : medicMap.values()) {
            removeMedic(item);
            removed++;
        }
        return new SyncState(added, updated, removed);
    }

    /**
     * Returns the treatment template names.
     *
     * @return the treatment template names
     */
    public List<String> getTreatmentTemplates() {
        Call<List<String>, ReferenceData> call = new Call<List<String>, ReferenceData>() {
            @Override
            public List<String> call(ReferenceData resource) throws Exception {
                List<String> templates = new ArrayList<>();
                for (TreatmentTemplate template : resource.getTemplates()) {
                    templates.add(template.getName());
                }
                return templates;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetTemplates();
            }
        };
        return call(ReferenceData.class, call);
    }

    /**
     * Returns the Azure Service Bus configuration.
     *
     * @return the configuration
     */
    public ServiceBusConfig getServiceBusConfig() {
        Call<ServiceBusConfig, ReferenceData> call = new Call<ServiceBusConfig, ReferenceData>() {
            @Override
            public ServiceBusConfig call(ReferenceData resource) throws Exception {
                return resource.getServiceBusConfig();
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetServiceBusConfig();
            }
        };
        return call(ReferenceData.class, call);
    }

    /**
     * Synchronise a medic with its clinician.
     *
     * @param clinician the clinican
     * @param medic     the medic, or {@code null} if it doesn't exist
     * @param id        the item identifier
     * @return the synchronised item, or {@code null} if no synchronisation is required
     */
    private Medic synchronise(User clinician, Medic medic, String id) {
        Medic result = null;

        String name = clinician.getName();
        if (medic == null || !ObjectUtils.equals(name, medic.getName())) {
            result = new Medic();
            result.setMedicId(id);
            result.setName(name);
        }
        return result;
    }

    /**
     * Returns an iterator over the clinicians.
     *
     * @return the clinicians
     */
    private Iterator<User> getClinicians() {
        IArchetypeQuery query = ClinicianQueryFactory.create(location);
        return new IMObjectQueryIterator<>(service, query);
    }

    /**
     * Returns the medics.
     *
     * @return the medics
     */
    private Map<String, Medic> getMedicMap() {
        Map<String, Medic> result = new HashMap<>();
        for (Medic medic : getMedics()) {
            result.put(medic.getMedicId(), medic);
        }
        return result;
    }

}
