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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.XAD;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.segment.PID;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.patient.PatientContext;

import java.util.Date;

import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Populates a {@code PID} segment.
 *
 * @author Tim Anderson
 */
class PIDPopulator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Constructs a {@link PIDPopulator}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public PIDPopulator(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Populate a PID segment.
     *
     * @param pid     the segment
     * @param context the patient context
     * @param config  the message population configuration
     * @throws HL7Exception for any error
     */
    public void populate(PID pid, PatientContext context, MessageConfig config) throws HL7Exception {
        pid.getSetIDPID().setValue("1");

        pid.getPatientID().getIDNumber().setValue(Long.toString(context.getPatientId()));

        XPN patientName = pid.getPatientName(0);
        patientName.getFamilyName().getSurname().setValue(context.getPatientLastName());
        patientName.getGivenName().setValue(context.getPatientFirstName());

        Date dateOfBirth = context.getDateOfBirth();
        if (dateOfBirth != null) {
            populateDTM(pid.getDateTimeOfBirth().getTime(), dateOfBirth, config);
        }

        pid.getAdministrativeSex().setValue(context.getPatientSex());

        Contact home = context.getAddress();
        if (home != null) {
            XAD address = pid.getPatientAddress(0);
            populateAddress(address, home);
        }
        pid.getPhoneNumberHome(0).getTelephoneNumber().setValue(context.getHomePhone());
        pid.getPhoneNumberBusiness(0).getTelephoneNumber().setValue(context.getWorkPhone());

        populateSpecies(pid, context);
        populateBreed(pid, context);
    }

    /**
     * Populates an address.
     *
     * @param address the address
     * @param home    the home contact
     * @throws HL7Exception for any error
     */
    private void populateAddress(XAD address, Contact home) throws HL7Exception {
        IMObjectBean bean = new IMObjectBean(home, service);
        address.getStreetAddress().getStreetOrMailingAddress().setValue(bean.getString("address"));
        address.getCity().setValue(lookups.getName(home, "suburb"));
        address.getZipOrPostalCode().setValue(bean.getString("postcode"));
        address.getStateOrProvince().setValue(lookups.getName(home, "state"));
    }

    /**
     * Populates the species.
     *
     * @param pid     the segment
     * @param context the patient context
     * @throws HL7Exception for any error
     */
    private void populateSpecies(PID pid, PatientContext context) throws HL7Exception {
        String species = context.getSpeciesCode();
        if (species != null) {
            CE code = pid.getSpeciesCode();
            code.getIdentifier().setValue(species);
            code.getText().setValue(context.getSpeciesName());
            code.getNameOfCodingSystem().setValue("OpenVPMS");
        }
    }

    /**
     * Populates the breed.
     *
     * @param pid     the segment
     * @param context the patient context
     * @throws HL7Exception for any error
     */
    private void populateBreed(PID pid, PatientContext context) throws HL7Exception {
        String breed = context.getBreedCode();
        if (breed != null) {
            CE code = pid.getBreedCode();
            code.getIdentifier().setValue(breed);
            code.getText().setValue(context.getBreedName());
            code.getNameOfCodingSystem().setValue("OpenVPMS");
        }
    }
}
