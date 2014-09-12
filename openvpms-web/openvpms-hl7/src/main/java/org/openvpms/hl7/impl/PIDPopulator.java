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
import org.openvpms.hl7.PatientContext;

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

    public void populate(PID pid, PatientContext context) throws HL7Exception {
        pid.getSetIDPID().setValue("1");

        pid.getPatientIdentifierList(0).getIDNumber().setValue(Long.toString(context.getPatientId()));

        XPN patientName = pid.getPatientName(0);
        patientName.getFamilyName().getSurname().setValue(context.getPatientLastName());
        patientName.getGivenName().setValue(context.getPatientFirstName());

        pid.getDateTimeOfBirth().getTime().setValue(context.getDateOfBirth());

        pid.getAdministrativeSex().setValue(context.getPatientSex());

        Contact home = context.getAddress();
        if (home != null) {
            XAD address = pid.getPatientAddress(0);
            populateAddress(address, home);
        }
        pid.getPhoneNumberHome(0).getTelephoneNumber().setValue(context.getHomePhone());
        pid.getPhoneNumberBusiness(0).getTelephoneNumber().setValue(context.getWorkPhone());

        setSpecies(pid, context);
        setBreed(pid, context);
    }

    private void populateAddress(XAD address, Contact home) throws HL7Exception {
        IMObjectBean bean = new IMObjectBean(home, service);
        address.getStreetAddress().getStreetOrMailingAddress().setValue(bean.getString("address"));
        address.getCity().setValue(lookups.getName(home, "suburb"));
        address.getZipOrPostalCode().setValue(bean.getString("postcode"));
        address.getStateOrProvince().setValue(lookups.getName(home, "state"));
    }

    private void setSpecies(PID pid, PatientContext context) throws HL7Exception {
        String species = context.getSpeciesCode();
        if (species != null) {
            CE code = pid.getSpeciesCode();
            code.getIdentifier().setValue(species);
            code.getText().setValue(context.getSpeciesName());
            code.getNameOfCodingSystem().setValue("OpenVPMS");
        }
    }

    private void setBreed(PID pid, PatientContext context) throws HL7Exception {
        String breed = context.getBreedCode();
        if (breed != null) {
            CE code = pid.getBreedCode();
            code.getIdentifier().setValue(breed);
            code.getText().setValue(context.getBreedName());
            code.getNameOfCodingSystem().setValue("OpenVPMS");
        }
    }
}
