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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.party;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.archetype.rules.contact.AddressFormatter;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.BalanceCalculator;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.act.Participation;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.component.model.bean.Policy;
import org.openvpms.component.model.bean.Predicates;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Contact;
import org.openvpms.component.model.party.Party;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * JXPath extension functions that operate on {@link Party} instances.
 * NOTE: This class should only be used via jxpath expressions.
 *
 * @author Tim Anderson
 * @author Tony De Keizer
 */
public class PartyFunctions {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The party rules.
     */
    private final PartyRules partyRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The supplier rules.
     */
    private final SupplierRules supplierRules;

    /**
     * The appointment rules.
     */
    private final AppointmentRules appointmentRules;

    /**
     * Policy to return the first customer participation from an act.
     */
    private static final Policy<Participation> CUSTOMER_PARTICIPATION
            = Policies.any(Participation.class, Predicates.isA(CustomerArchetypes.CUSTOMER_PARTICIPATION));

    /**
     * Policy to return the first patient participation from an act.
     */
    private static final Policy<Participation> PATIENT_PARTICIPATION
            = Policies.any(Participation.class, Predicates.isA(PatientArchetypes.PATIENT_PARTICIPATION));

    /**
     * Constructs a {@link PartyFunctions}.
     *
     * @param service          the archetype service
     * @param lookups          the lookup service
     * @param patientRules     the patient rules
     * @param addressFormatter the address formatter
     */
    public PartyFunctions(IArchetypeService service, ILookupService lookups, PatientRules patientRules,
                          AddressFormatter addressFormatter) {
        this.service = service;
        this.patientRules = patientRules;
        partyRules = new PartyRules(service, lookups, addressFormatter);
        supplierRules = new SupplierRules(service);
        appointmentRules = new AppointmentRules(service);
    }

    /**
     * Returns a specified node for a customer.
     *
     * @param object   the object. May be a party or act
     * @param nodeName to node to return
     * @return the node Object, or {@code null} if none can be found
     */
    @Deprecated
    public Object getCustomerNode(Object object, String nodeName) {
        Party customer = unwrapParty(object);
        return (customer != null) ? partyRules.getCustomerNode(customer, nodeName) : null;
    }

    /**
     * Returns the full name for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return the party's full name.
     * @see #getPartyFullName(Object object)
     */
    public String getPartyFullName(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPartyFullName(pointer.getValue());
    }

    /**
     * Returns a formatted name for a party.
     * <p/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party or act, or {@code null}
     * @return the party's formatted name, or an empty string if the party cannot be found
     */
    public String getPartyFullName(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getFullName(party) : "";
    }

    /**
     * Returns the owner of a patient.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return the patient's owner. May be {@code null}
     * @see #getPatientOwner(Object)
     */
    public Party getPatientOwner(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientOwner(pointer.getValue());
    }

    /**
     * Returns the owner of a patient.
     * <p/>
     * If the supplied object is an act, then the returned owner will be that whose ownership period encompasses the
     * act start time. If there is no such owner, the returned owner will be that whose ownership began
     * closest to the act start time.
     *
     * @param object the patient, or an act referring to the patient, or {@code null}
     * @return the patient's owner, or {@code null} if none can be found
     */
    public Party getPatientOwner(Object object) {
        object = unwrap(object);
        if (object instanceof Party) {
            return patientRules.getOwner((Party) object);
        } else if (object instanceof Act) {
            return patientRules.getOwner((Act) object);
        }
        return null;
    }

    /**
     * Returns the current owner of a patient associated with an act.
     *
     * @param act the act
     * @return the associated patients owner, or {@code null}
     */
    public Party getPatientCurrentOwner(Act act) {
        return patientRules.getCurrentOwner(act);
    }

    /**
     * Returns the location for the patient.
     *
     * @param object the patient, or an act referring to the patient
     * @return the associated party at the patient location, or {@code null}
     */
    public Party getPatientLocation(Object object) {
        object = unwrap(object);
        if (object instanceof Party) {
            return patientRules.getLocation((Party) object);
        } else if (object instanceof Act) {
            return patientRules.getLocation((Act) object);
        }
        return null;
    }

    /**
     * Returns the current location party for the patient associated with an act.
     *
     * @param act the act. May be {@code null}
     * @return the associated party at the patient location, or {@code null}
     */
    public Party getPatientCurrentLocation(Act act) {
        return patientRules.getCurrentLocation(act);
    }

    /**
     * Marks a patient as being inactive.
     *
     * @param patient the patient
     */
    public void setPatientInactive(Party patient) {
        patientRules.setInactive(patient);
    }

    /**
     * Marks a patient as being deceased.
     *
     * @param patient the patient
     */
    public void setPatientDeceased(Party patient) {
        patientRules.setDeceased(patient);
    }

    /**
     * Marks a patient as being desexed.
     *
     * @param patient the patient
     */
    public void setPatientDesexed(Party patient) {
        patientRules.setDesexed(patient);
    }

    /**
     * Returns a formatted list of preferred contacts for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return a formatted list of contacts, or an empty string if there are none
     * @see #getPreferredContacts(Object)
     */
    public String getPreferredContacts(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPreferredContacts(pointer.getValue());
    }

    /**
     * Returns a formatted list of preferred contacts for a party.
     * <p/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party or act, or {@code null}
     * @return a formatted list of contacts, or an empty string if there are none
     */
    public String getPreferredContacts(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getPreferredContacts(party) : "";
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return a formatted billing address, or an empty string if there is no corresponding
     * <em>contact.location</em> contact
     * @see #getBillingAddress(Object)
     */
    public String getBillingAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getBillingAddress(pointer.getValue());
    }

    /**
     * Returns a formatted billing address for a party.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party or act, or {@code null}
     * @return a formatted billing address, or an empty string if there is no corresponding
     * <em>contact.location</em> contact
     */
    public String getBillingAddress(Object object) {
        return getBillingAddress(object, false);
    }

    /**
     * Returns a formatted billing address for a party.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object     the object. May be a party or act, or {@code null}
     * @param singleLine if {@code true}, return the address as a single line
     * @return a formatted billing address, or an empty string if there is no corresponding
     * <em>contact.location</em> contact
     */
    public String getBillingAddress(Object object, boolean singleLine) {
        Party customer = unwrapParty(object);
        return (customer != null) ? partyRules.getBillingAddress(customer, singleLine) : "";
    }

    /**
     * Returns a formatted correspondence address for a party.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return a formatted correspondence address for a party. May be empty if there is no corresponding
     * <em>contact.location</em> contact
     * @see #getCorrespondenceAddress(Object)
     */
    public String getCorrespondenceAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getCorrespondenceAddress(pointer.getValue());
    }

    /**
     * Returns a formatted correspondence address for a party.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return a formatted correspondence address for a party. May be empty if there is no corresponding
     * <em>contact.location</em> contact
     */
    public String getCorrespondenceAddress(Object object) {
        return getCorrespondenceAddress(object, false);
    }

    /**
     * Returns a formatted correspondence address for a party.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object     the object. May be a party, or an act, or {@code null}
     * @param singleLine if {@code true}, return the address as a single line
     * @return a formatted correspondence address for a party. May be empty if
     * there is no corresponding <em>contact.location</em> contact
     */
    public String getCorrespondenceAddress(Object object, boolean singleLine) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getCorrespondenceAddress(party, singleLine) : "";
    }

    /**
     * Returns a formatted <em>contact.location</em> address with the specified purpose for a party.
     * <br/>
     * If it cannot find the specified purpose, it uses the preferred location contact or
     * any location contact if there is no preferred.
     *
     * @param party   the party
     * @param purpose the contact purpose of the address
     * @return a formatted address. May be empty if there is no corresponding <em>contact.location</em> contact
     */
    public String getAddress(Party party, String purpose) {
        return getAddress(party, purpose, false);
    }

    /**
     * Returns a formatted <em>contact.location</em> address with the specified purpose for a party.
     * <br/>
     * If it cannot find the specified purpose, it uses the preferred location contact or
     * any location contact if there is no preferred.
     *
     * @param party      the party
     * @param purpose    the contact purpose of the address
     * @param singleLine if {@code true}, return the address as a single line
     * @return a formatted address. May be empty if there is no corresponding <em>contact.location</em> contact
     */
    public String getAddress(Party party, String purpose, boolean singleLine) {
        return partyRules.getAddress(party, purpose, singleLine);
    }

    /**
     * Returns a formatted telephone number for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return a formatted telephone number. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact.
     * @see #getTelephone(Object)
     */
    public String getTelephone(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getTelephone(pointer.getValue());
    }

    /**
     * Returns a formatted telephone number for a party.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return a formatted telephone number. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact.
     */
    public String getTelephone(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getTelephone(party) : "";
    }

    /**
     * Returns a formatted home telephone number for a party.
     *
     * @param context the expression context. Expected to reference a party or an act
     * @return a formatted home telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     * @see #getHomeTelephone(Object)
     */
    public String getHomeTelephone(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getHomeTelephone(pointer.getValue());
    }

    /**
     * Returns a formatted home telephone number for a party.
     * <p>
     * This will return a phone contact with HOME purpose, or any phone contact if there is none.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return a formatted home telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     */
    public String getHomeTelephone(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getHomeTelephone(party) : "";
    }

    /**
     * Returns a formatted work telephone number for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return a formatted home telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     * @see #getWorkTelephone(Object)
     */
    public String getWorkTelephone(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getWorkTelephone(pointer.getValue());
    }

    /**
     * Returns a formatted work telephone number for a party.
     * <p>
     * This will only return a phone contact with WORK purpose.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return a formatted telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact with <em>WORK</em> purpose
     */
    public String getWorkTelephone(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getWorkTelephone(party) : "";
    }

    /**
     * Returns a formatted mobile telephone number for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return a formatted home telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     * @see #getMobileTelephone(Object)
     */
    public String getMobileTelephone(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getMobileTelephone(pointer.getValue());
    }

    /**
     * Returns a formatted mobile telephone number for a party.
     * <p>
     * This will only return a phone contact with MOBILE purpose.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return a formatted telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact with <em>MOBILE</em> purpose
     */
    public String getMobileTelephone(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getMobileTelephone(party) : "";
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return a formatted fax number. party. May be empty if there is no corresponding <em>contact.phoneNumber</em>
     * contact with a FAX purpose
     * @see #getFaxNumber(Object)
     */
    public String getFaxNumber(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getFaxNumber(pointer.getValue());
    }

    /**
     * Returns a formatted fax number for a party.
     * <p>
     * This will only return a phone contact with FAX purpose.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return a formatted fax number. party. May be empty if there is no corresponding <em>contact.phoneNumber</em>
     * contact with a FAX purpose
     */
    public String getFaxNumber(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getFaxNumber(party) : "";
    }

    /**
     * Returns a formatted email address for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return a formatted email address. May be empty if there is no corresponding <em>contact.email</em> contact
     * @see #getEmailAddress(Object)
     */
    public String getEmailAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getEmailAddress(pointer.getValue());
    }

    /**
     * Returns a formatted email address for a party.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return a formatted email address for a party. May be empty if there is no corresponding <em>contact.email</em>
     * contact
     */
    public String getEmailAddress(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getEmailAddress(party) : "";
    }

    /**
     * Returns a website URL for a party.
     *
     * @param context the expression context. Expected to reference a party or an act.
     * @return the website URL of the party. May be empty if there is no corresponding <em>contact.website</em> contact
     * @see #getWebsite(Object)
     */
    public String getWebsite(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getWebsite(pointer.getValue());
    }

    /**
     * Returns the website URL for a party.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return the website URL of the party. May be empty if there is no corresponding <em>contact.website</em> contact
     */
    public String getWebsite(Object object) {
        Party party = unwrapParty(object);
        return (party != null) ? partyRules.getWebsite(party) : "";
    }

    /**
     * Returns a formatted contact purpose string for the Contact.
     *
     * @param context the expression context. Expected to reference a contact.
     * @return a formatted string with the contacts contact purposes, or an empty string if none are present
     */
    public String getContactPurposes(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Contact)) {
            return "";
        }
        Contact contact = (Contact) pointer.getValue();
        return partyRules.getContactPurposes(contact);
    }

    /**
     * Returns a stringified form of a party's identities.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities
     */
    public String identities(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return identities(pointer.getValue());
    }

    /**
     * Returns a stringified form of a party's identities.
     *
     * @param object the object. May be {@code null}
     * @return the stringified form of the party's identities
     */
    public String identities(Object object) {
        Party party = unwrapPatient(object);
        return (party != null) ? partyRules.getIdentities(party) : "";
    }

    /**
     * Returns the current account Balance for a party or act.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return the account balance
     */
    public BigDecimal getAccountBalance(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getAccountBalance(pointer.getValue());
    }

    /**
     * Returns the account balance for a party.
     * <br/>
     * If the supplied object is an act, the party is retrieved via an <em>participation.customer</em> participation.
     * If there is none, then the owner of the patient associated with any <em>participation.patient</em> is used.
     *
     * @param object the object. May be a party, or an act, or {@code null}
     * @return the current account Balance
     */
    public BigDecimal getAccountBalance(Object object) {
        BigDecimal result = BigDecimal.ZERO;
        Party party = unwrapParty(object);
        if (party != null) {
            BalanceCalculator calculator = new BalanceCalculator(service);
            result = calculator.getBalance(party);
        }
        return result;
    }

    /**
     * Returns the referral vet for a patient.
     * <p>
     * This is the patient's associated party from the first matching <em>entityRelationship.referredFrom</em> or
     * <em>entityRelationship.referredTo</em> that matches the:
     * <ul>
     * <li>act's start time, if the context refers to an act; or</li>
     * <li>the current time, if it refers to a patient</li>
     * </ul>
     *
     * @param context the expression context. Expected to reference an act or patient.
     * @return the referral vet, or {@code null} if there is no patient associated with the act, the act has no start
     * time, or the patient isn't being referred
     */
    public Party getPatientReferralVet(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientReferralVet(pointer.getValue());
    }

    /**
     * Returns the referral vet for a patient.
     * <p/>
     * If a patient is supplied, this is the patient's associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or <em>entityRelationship.referredTo</em> overlapping the current time.
     * <br/>
     * If an act is supplied, this is the patient's associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or <em>entityRelationship.referredTo</em> overlapping the act's start
     * time.
     *
     * @param object the object. May be {@code null}
     * @return the referral vet, or {@code null} if there is no patient associated with the act, the act has no start
     * time, or the patient isn't being referred
     */
    public Party getPatientReferralVet(Object object) {
        Party vet = null;
        object = unwrap(object);
        if (object instanceof Party) {
            vet = patientRules.getReferralVet((Party) object, new Date());
        } else if (object instanceof Act) {
            Act act = (Act) object;
            Date startTime = act.getActivityStartTime();
            Party patient = getPatient(act);
            if (patient != null && startTime != null) {
                vet = patientRules.getReferralVet(patient, startTime);
            }
        }
        return vet;
    }

    /**
     * Returns the referral vet practice for a vet.
     *
     * @param context the expression context. Expected to reference an patient, or an act containing a patient
     * @return the practice the vet is associated with or {@code null} if the vet is not associated with any practice
     */
    public Party getPatientReferralVetPractice(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientReferralVetPractice(pointer.getValue());
    }

    /**
     * Returns the referral vet practice for a vet associated with the supplied act's patient.
     *
     * @param object the act. May be {@code null}
     * @return the practice the vet is associated with or {@code null} if the vet is not associated with any practice
     */
    public Party getPatientReferralVetPractice(Object object) {
        Party result = null;
        object = unwrap(object);
        if (object instanceof Act) {
            Act act = (Act) object;
            Date startTime = act.getActivityStartTime();
            if (startTime != null) {
                Party vet = getPatientReferralVet(act);
                if (vet != null) {
                    result = getReferralVetPractice(vet, startTime);
                }
            }
        } else if (object instanceof Party) {
            Party vet = getPatientReferralVet(object);
            if (vet != null) {
                result = getReferralVetPractice(vet, new Date());
            }
        }
        return result;
    }

    /**
     * Returns the referral vet practice for a vet overlapping the specified time.
     *
     * @param vet  the vet
     * @param time the time
     * @return the practice the vet is associated with or {@code null} if the vet is not associated with any practice
     */
    public Party getReferralVetPractice(Party vet, Date time) {
        return supplierRules.getReferralVetPractice(vet, time);
    }

    /**
     * Returns the age of the patient.
     * <p>
     * If the patient is deceased, the age of the patient when they died will be returned
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the patient's age
     */
    public String getPatientAge(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientAge(pointer.getValue());
    }

    /**
     * Returns the age of the patient.
     * <p>
     * If the patient is deceased, the age of the patient when they died will be returned
     *
     * @param object the patient
     * @return the stringified form of the patient's age
     */
    public String getPatientAge(Object object) {
        Party patient = unwrapPatient(object);
        return (patient != null) ? patientRules.getPatientAge(patient) : "";
    }

    /**
     * Returns the patient microchip.
     *
     * @param context the expression context. Expected to reference an patient, or an act containing a patient
     * @return the microchip, or an empty string if none is found
     */
    public String getPatientMicrochip(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientMicrochip(pointer.getValue());
    }


    /**
     * Returns the patient microchip.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return the microchip, or an empty string if none is found
     */
    public String getPatientMicrochip(Object object) {
        String result = null;
        Party patient = unwrapPatient(object);
        if (patient != null) {
            result = patientRules.getMicrochipNumber(patient);
        }
        return (result != null) ? result : "";
    }

    /**
     * Returns the patient microchips, separated by commas.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return the microchips, or an empty string if none is found
     */
    public String getPatientMicrochips(Object object) {
        String result = null;
        Party patient = unwrapPatient(object);
        if (patient != null) {
            result = patientRules.getMicrochipNumbers(patient);
        }
        return (result != null) ? result : "";
    }

    /**
     * Returns the most recent active microchip identity for a patient.
     *
     * @param context the expression context
     * @return the active microchip object, or {@code null} if none is found
     */
    public EntityIdentity getMicrochip(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getMicrochip(pointer.getValue());
    }

    /**
     * Returns the most recent active microchip identity for a party.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return the active microchip object, or {@code null} if none is found
     */
    public EntityIdentity getMicrochip(Object object) {
        Party patient = unwrapPatient(object);
        return (patient != null) ? patientRules.getMicrochip(patient) : null;
    }

    /**
     * Returns the patient pet tag.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return the pet tag, or an empty string if none is found
     */
    public String getPatientPetTag(Object object) {
        String result = null;
        Party patient = unwrapPatient(object);
        if (patient != null) {
            result = patientRules.getPetTag(patient);
        }
        return (result != null) ? result : "";
    }

    /**
     * Returns the patient rabies tag.
     *
     * @param context the expression context
     * @return the rabies tag, or an empty string if none is found
     */
    public String getPatientRabiesTag(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientRabiesTag(pointer.getValue());
    }

    /**
     * Returns the patient rabies tag.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return the rabies tag, or an empty string if none is found
     */
    public String getPatientRabiesTag(Object object) {
        String result = null;
        Party patient = unwrapPatient(object);
        if (patient != null) {
            result = patientRules.getRabiesTag(patient);
        }
        return (result != null) ? result : "";
    }

    /**
     * Returns the most recent weight in string format for a patient.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return a formatted weight, or an empty string is none is recorded
     */
    public String getPatientWeight(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientWeight(pointer.getValue());
    }

    /**
     * Returns a formatted weight for a patient.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return a formatted weight, or an empty string is none is recorded
     */
    public String getPatientWeight(Object object) {
        String result = null;
        Party patient = unwrapPatient(object);
        if (patient != null) {
            result = patientRules.getPatientWeight(patient);
        }
        return (result != null) ? result : "";
    }

    /**
     * Returns the patient weight, in kilos.
     * <p>
     * This uses the most recent recorded weight for the patient.
     *
     * @param context the expression context. May be a patient, act, or {@code null}
     * @return the patient weight, in kilos, or {@link BigDecimal#ZERO} if none exists
     */
    public BigDecimal getWeight(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getWeight(pointer.getValue());
    }

    /**
     * Returns the patient weight, in kilos.
     * <p>
     * This uses the most recent recorded weight for the patient.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return the patient weight, in kilos, or {@link BigDecimal#ZERO} if none exists
     */
    public BigDecimal getWeight(Object object) {
        Party patient = unwrapPatient(object);
        return (patient != null) ? patientRules.getWeight(patient).toKilograms() : BigDecimal.ZERO;
    }

    /**
     * Returns the patient weight, in the specified units.
     * <p>
     * This uses the most recent recorded weight for the patient.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @param units  the units. One of {@code KILOGRAMS}, {@code GRAMS}, or {@code POUNDS}
     * @return the patient weight in the specified units
     */
    public BigDecimal getWeight(Object object, String units) {
        BigDecimal result = BigDecimal.ZERO;
        Party patient = unwrapPatient(object);
        if (patient != null) {
            Weight weight = patientRules.getWeight(patient);
            result = weight.convert(WeightUnits.valueOf(units));
        }
        return result;
    }

    /**
     * Returns the desex status of a patient.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return the desex status, or an empty string
     */
    public String getPatientDesexStatus(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getPatientDesexStatus(pointer.getValue());
    }

    /**
     * Returns the desex status of a patient.
     *
     * @param object the object. May be a patient, act, or {@code null}
     * @return the desex status, or an empty string
     */
    public String getPatientDesexStatus(Object object) {
        Party patient = unwrapPatient(object);
        return (patient != null) ? patientRules.getPatientDesexStatus(patient) : "";
    }

    /**
     * Returns the most recent <em>act.patientClinicalEvent</em> (i.e. Visit) for a patient.
     *
     * @param patient the patient. May be {@code null}
     * @return the most recent visit for {@code patient}, or {@code null} if none is found
     */
    public Act getPatientVisit(Party patient) {
        if (patient != null) {
            return new MedicalRecordRules(service).getEvent(patient);
        }
        return null;
    }

    /**
     * Returns the Practice party object.
     *
     * @return the practice party object
     */
    public Party getPractice() {
        return partyRules.getPractice();
    }

    /**
     * Returns the practice address as a single line string.
     *
     * @return the practice address as a string
     */
    public String getPracticeAddress() {
        return getPracticeAddress(true);
    }

    /**
     * Returns the practice address.
     *
     * @param singleLine if {@code true}, return the address as a single line string, otherwise as a multi-line string
     * @return the practice address as a string
     */
    public String getPracticeAddress(boolean singleLine) {
        return partyRules.getPracticeAddress(singleLine);
    }

    /**
     * Returns the Practice Telephone Number.
     *
     * @return the practice telephone as a string
     */
    public String getPracticeTelephone() {
        return partyRules.getPracticeTelephone();
    }

    /**
     * Returns the Practice Fax Number as a String.
     *
     * @return the practice fax number as a string
     */
    public String getPracticeFaxNumber() {
        return partyRules.getPracticeFaxNumber();
    }

    /**
     * Returns the BPAY Id for a customer.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return a Bpay ID for the customer, or {@code null}
     * @deprecated due to inconsistent naming
     */
    @Deprecated
    public String getBpayID(ExpressionContext context) {
        return getBpayId(context);
    }

    /**
     * Returns the BPAY Id for a customer.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return a BPAY Id for the customer, or {@code null}
     * @see #getBpayId(Object)
     */
    public String getBpayId(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        return getBpayId(pointer.getValue());
    }

    /**
     * Returns a BPAY Id for a party.
     * <p/>
     * This uses the party id and adds a check digit using a Luntz 10 algorithm.
     *
     * @param object the party, or {@code null}
     * @return string bpay id
     */
    public String getBpayId(Object object) {
        Party party = unwrapParty(object);
        return party != null ? partyRules.getBpayId(party) : null;
    }

    /**
     * Returns the location to use for contacts in letterhead.
     * <p>
     * If the supplied practice location has a letterhead, and specifies that a different location should be used
     * for contacts, then this is returned, otherwise the supplied location is returned.
     *
     * @param object the practice location
     * @return the location to use for contacts, or {@code null} if {@code location} is {@code null}
     */
    public Party getLetterheadContacts(Object object) {
        Party result = null;
        object = unwrap(object);
        if (object instanceof Party) {
            Party party = (Party) object;
            result = party;
            IMObjectBean bean = service.getBean(party);
            Entity letterhead = bean.getTarget("letterhead", Entity.class);
            if (letterhead != null) {
                bean = service.getBean(letterhead);
                Reference contacts = bean.getTargetRef("contacts");
                if (contacts != null && !contacts.equals(party.getObjectReference())) {
                    result = (Party) service.get(contacts);
                }
            }
        }
        return result;
    }

    /**
     * Returns pending appointments for a customer or patient.
     *
     * @param party    the party
     * @param interval the interval, relative to the current date/time
     * @param units    the interval units
     * @return the pending appointments for the customer
     */
    public Iterable<Act> getAppointments(Party party, int interval, String units) {
        if (interval > 0 && units != null) {
            if (TypeHelper.isA(party, CustomerArchetypes.PERSON)) {
                return appointmentRules.getCustomerAppointments(party, interval, DateUnits.valueOf(units));
            } else {
                return appointmentRules.getPatientAppointments(party, interval, DateUnits.valueOf(units));
            }
        }
        return Collections.emptyList();
    }

    /**
     * Unwraps a customer from the supplied object.
     * <p/>
     * If the supplied object object is an act, returns the customer associated with an act via an
     * <em>participation.customer</em> participation, or the the patient owner if there is no customer participation.
     *
     * @param object the object. May be {@code null}
     * @return the corresponding customer, or {@code null} if none is present
     */
    private Party unwrapParty(Object object) {
        Party party = null;
        object = unwrap(object);
        if (object instanceof Act) {
            party = getCustomer((Act) object);
        } else if (object instanceof Party) {
            party = (Party) object;
        }
        return party;
    }

    /**
     * Unwraps a patient from the supplied object.
     * <p/>
     * If the supplied object object is an act, returns the patient associated with an act via an
     * <em>participation.customer</em> participation.
     *
     * @param object the object. May be {@code null}
     * @return the corresponding customer, or {@code null} if none is present
     */
    private Party unwrapPatient(Object object) {
        Party patient = null;
        object = unwrap(object);
        if (object instanceof Act) {
            patient = getPatient((Act) object);
        } else if (object instanceof Party) {
            patient = (Party) object;
        }
        return patient;
    }

    /**
     * Returns the customer associated with an act via an <em>participation.customer</em> participation,
     * or the the patient owner if there is no customer participation.
     *
     * @param act the act. May be {@code null}
     * @return the customer, or {@code null} if none is present
     */
    private Party getCustomer(Act act) {
        Party customer = null;
        if (act != null) {
            IMObjectBean bean = service.getBean(act);
            customer = bean.getTarget(act.getParticipations(), Party.class, CUSTOMER_PARTICIPATION);
            if (customer == null) {
                customer = patientRules.getOwner(act);
            }
        }
        return customer;
    }

    /**
     * Helper to return the patient associated with an act.
     *
     * @param act the act. May be {@code null}
     * @return the patient, or {@code null} if none is found
     */
    private Party getPatient(Act act) {
        Party patient = null;
        if (act != null) {
            IMObjectBean bean = service.getBean(act);
            patient = bean.getTarget(act.getParticipations(), Party.class, PATIENT_PARTICIPATION);
        }
        return patient;
    }

    /**
     * Helper to get access to the actual object supplied by JXPath.
     * <p/>
     * This is a workaround to allow functions to be supplied null arguments, which JXPath handles by wrapping in a
     * list.
     *
     * @param object the object to unwrap
     * @return the unwrapped object. May be {@code null}
     */
    private Object unwrap(Object object) {
        if (object instanceof List) {
            List values = (List) object;
            object = !values.isEmpty() ? values.get(0) : null;
        }
        return object;
    }

}
