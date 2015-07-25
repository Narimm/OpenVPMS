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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;
import org.openvpms.hl7.util.HL7Archetypes;

/**
 * The services required by the {@link OrderPlacer}.
 *
 * @author Tim Anderson
 */
public class OrderServices {

    /**
     * The pharmacy order service.
     */
    private final PharmacyOrderService pharmacyService;

    /**
     * The pharmacies.
     */
    private final Pharmacies pharmacies;

    /**
     * The laboratory order service.
     */
    private final LaboratoryOrderService laboratoryService;

    /**
     * The laboratory.
     */
    private final Laboratories laboratories;

    /**
     * The patient context factory.
     */
    private final PatientContextFactory factory;

    /**
     * The patient information service, used to send updates when ordering when a patient isn't checked in.
     */
    private final PatientInformationService informationService;

    /**
     * Medical record rules, used to retrieve events.
     */
    private final MedicalRecordRules rules;

    public OrderServices(PharmacyOrderService pharmacyService, Pharmacies pharmacies,
                         LaboratoryOrderService laboratoryService, Laboratories laboratories,
                         PatientContextFactory factory, PatientInformationService informationService,
                         MedicalRecordRules rules) {
        this.pharmacyService = pharmacyService;
        this.pharmacies = pharmacies;
        this.laboratoryService = laboratoryService;
        this.laboratories = laboratories;
        this.factory = factory;
        this.informationService = informationService;
        this.rules = rules;
    }

    public PharmacyOrderService getPharmacyService() {
        return pharmacyService;
    }


    public LaboratoryOrderService getLaboratoryService() {
        return laboratoryService;
    }

    public PatientContextFactory getFactory() {
        return factory;
    }

    public PatientInformationService getInformationService() {
        return informationService;
    }

    public MedicalRecordRules getRules() {
        return rules;
    }

    public Pharmacies getPharmacies() {
        return pharmacies;
    }

    public Entity getService(Entity group, Party location) {
        if (TypeHelper.isA(group, HL7Archetypes.PHARMACY_GROUP)) {
            return pharmacies.getService(group, location.getObjectReference());
        } else if (TypeHelper.isA(group, HL7Archetypes.LABORATORY_GROUP)) {
            return laboratories.getService(group, location.getObjectReference());
        }
        return null;
    }
}
