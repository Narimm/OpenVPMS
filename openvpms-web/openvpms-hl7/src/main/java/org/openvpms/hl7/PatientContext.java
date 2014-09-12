package org.openvpms.hl7;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Patient context information.
 *
 * @author Tim Anderson
 */
public class PatientContext {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The visit.
     */
    private final Act visit;

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * The clinician.
     */
    private final User clinician;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The clinician first name.
     */
    private String clinicianFirstName;

    /**
     * The clinician last name.
     */
    private String clinicianLastName;

    /**
     * The most recent patient weight, if any.
     */
    private Act weight;

    /**
     * The patient allergies.
     */
    private List<Act> allergies = null;

    /**
     * The patient bean.
     */
    private final IMObjectBean patientBean;

    /**
     * The customer bean.
     */
    private final IMObjectBean customerBean;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Constructs an {@link PatientContext}.
     *
     * @param patient       the patient
     * @param customer      the customer. May be {@code null}
     * @param visit         the visit
     * @param location      the practice location
     * @param clinician     the clinician. May be {@code null}
     * @param patientRules  the patient rules
     * @param customerRules the customer rules
     * @param service       the archetype service
     * @param lookups       the lookup service
     */
    public PatientContext(Party patient, Party customer, Act visit, Party location, User clinician,
                          PatientRules patientRules, CustomerRules customerRules, IArchetypeService service,
                          ILookupService lookups) {
        this.patient = patient;
        this.customer = customer;
        this.visit = visit;
        this.location = location;
        this.clinician = clinician;
        this.patientRules = patientRules;
        this.customerRules = customerRules;
        this.service = service;
        this.lookups = lookups;
        patientBean = new IMObjectBean(patient, service);
        customerBean = (customer != null) ? new IMObjectBean(customer, service) : null;

        if (clinician != null) {
            String[] firstLast = clinician.getName().split(" ", 2);
            if (firstLast.length == 2) {
                clinicianFirstName = firstLast[0];
                clinicianLastName = firstLast[1];
            } else if (firstLast.length == 1) {
                clinicianLastName = firstLast[0];
            }
        }
    }

    /**
     * Returns the location.
     *
     * @return the location
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Returns the visit act.
     *
     * @return the visit act
     */
    public Act getVisit() {
        return visit;
    }

    /**
     * Returns the patient visit identifier.
     *
     * @return the patient visit identifier
     */
    public long getVisitId() {
        return visit.getId();
    }

    /**
     * Returns the visit start time.
     *
     * @return the visit start time
     */
    public Date getVisitStartTime() {
        return visit.getActivityStartTime();
    }

    /**
     * Returns the visit end time.
     *
     * @return the visit end time, or {@code null} if it hasn't ended
     */
    public Date getVisitEndTime() {
        return visit.getActivityEndTime();
    }

    /**
     * Returns the patient identifier.
     *
     * @return the patient identifier
     */
    public long getPatientId() {
        return patient.getId();
    }

    /**
     * Returns the patient first name.
     *
     * @return the patient first name
     */
    public String getPatientFirstName() {
        return patient.getName();
    }

    /**
     * Returns the patient last name.
     *
     * @return the patient owner's surname, if the customer was provided at construction, otherwise {@code null}
     */
    public String getPatientLastName() {
        return (customerBean != null) ? customerBean.getString("lastName") : null;
    }

    /**
     * Returns the patient sex.
     *
     * @return the sex. On of {@code M} (male), {@code F} (female) or {@code U} (unknown)
     */
    public String getPatientSex() {
        String result;
        String sex = patientBean.getString("sex");
        if ("MALE".equals(sex)) {
            result = "M";
        } else if ("FEMALE".equals(sex)) {
            result = "F";
        } else {
            result = "U";
        }
        return result;
    }

    /**
     * Returns the patient date of birth.
     *
     * @return the patient date of birth. May be {@code null}
     */
    public Date getDateOfBirth() {
        return patientRules.getDateOfBirth(patient);
    }

    /**
     * Returns the patient weight.
     *
     * @return the patient weight, or {@code null} if unknown
     */
    public BigDecimal getPatientWeight() {
        getWeightAct();
        return (weight != null) ? patientRules.getWeight(weight) : null;
    }

    /**
     * Returns the date when the patient was weighed.
     *
     * @return the date, or {@code null} if the patient has no weight record
     */
    public Date getWeighDate() {
        getWeightAct();
        return weight != null ? weight.getActivityStartTime() : null;
    }

    /**
     * Returns the customer home phone.
     *
     * @return the customer home phone. May be {@code null}
     */
    public String getHomePhone() {
        return (customer != null) ? customerRules.getHomeTelephone(customer) : null;
    }

    /**
     * Returns the customer work phone.
     *
     * @return the customer work phone. May be {@code null}
     */
    public String getWorkPhone() {
        return (customer != null) ? customerRules.getWorkTelephone(customer) : null;
    }

    /**
     * Returns the patient species code.
     *
     * @return the patient species code
     */
    public String getSpeciesCode() {
        return patientBean.getString("species");
    }

    /**
     * Returns the patient species name.
     *
     * @return the patient species name
     */
    public String getSpeciesName() {
        return lookups.getName(patient, "species");
    }

    /**
     * Returns the patient breed code.
     *
     * @return the patient breed code. May be {@code null}
     */
    public String getBreedCode() {
        return patientBean.getString("breed");
    }

    /**
     * Returns the patient breed name.
     *
     * @return the patient breed name. May be {@code null}
     */
    public String getBreedName() {
        return lookups.getName(patient, "breed");
    }

    /**
     * Returns the location name.
     *
     * @return the location name
     */
    public String getLocationName() {
        return location.getName();
    }

    /**
     * Returns the clinician identifier.
     *
     * @return the clinician identifier, or {@code -1} if none was provided at construction
     */
    public long getClinicianId() {
        return (clinician != null) ? clinician.getId() : -1;
    }

    /**
     * Returns the clinician first name.
     *
     * @return the clinician first name. May be {@code null}
     */
    public String getClinicianFirstName() {
        return clinicianFirstName;
    }

    /**
     * Returns the clinician last name.
     *
     * @return the clinician last name. May be {@code null}
     */
    public String getClinicianLastName() {
        return clinicianLastName;
    }

    /**
     * Returns the customer address.
     *
     * @return the customer address. May be {@code null}
     */
    public Contact getAddress() {
        return (customer != null) ? customerRules.getContact(customer, ContactArchetypes.LOCATION, "HOME") : null;
    }

    /**
     * Returns any allergies the patient may have.
     *
     * @return the patient allergies
     */
    public List<Act> getAllergies() {
        if (allergies == null) {
            allergies = new ArrayList<Act>();
            ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.ALERT);
            query.add(Constraints.eq("status", ActStatus.IN_PROGRESS));
            query.add(Constraints.join("patient").add(Constraints.eq("entity", patient)));
            query.add(Constraints.sort("id"));
            IMObjectQueryIterator<Act> alerts = new IMObjectQueryIterator<Act>(query);
            while (alerts.hasNext()) {
                Act alert = alerts.next();
                IMObjectBean bean = new IMObjectBean(alert, service);
                if ("ALLERGY".equals(bean.getString("alertType"))) {
                    allergies.add(alert);
                }
            }
        }
        return allergies;
    }

    /**
     * Gets the most recent weight for a patient, if it hasn't already been retrieved.
     */
    private void getWeightAct() {
        if (weight == null) {
            weight = patientRules.getWeightAct(patient);
        }
    }

}
