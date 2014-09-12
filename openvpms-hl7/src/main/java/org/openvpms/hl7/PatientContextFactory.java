package org.openvpms.hl7;

import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;

/**
 * Factory for {@link PatientContext} instances.
 *
 * @author Tim Anderson
 */
public class PatientContextFactory {

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Constructs a {@link PatientContextFactory}.
     *
     * @param patientRules  the patient rules
     * @param customerRules the customer rules
     * @param service       the archetype service
     * @param lookups       the lookup service
     */
    public PatientContextFactory(PatientRules patientRules, CustomerRules customerRules, IArchetypeService service,
                                 ILookupService lookups) {
        this.patientRules = patientRules;
        this.customerRules = customerRules;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Creates a new context.
     *
     * @param patient   the patient
     * @param customer  the customer
     * @param visit     the patient visit (an <em>act.patientClinicalEvent</em>
     * @param location  the practice location
     * @param clinician the clinician
     * @return a new {@link PatientContext}
     */
    public PatientContext createContext(Party patient, Party customer, Act visit, Party location, User clinician) {
        return new PatientContext(patient, customer, visit, location, clinician, patientRules, customerRules, service,
                                  lookups);
    }
}
