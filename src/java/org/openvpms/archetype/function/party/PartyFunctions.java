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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.party;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Date;


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
     * The customer account rules.
     */
    private final CustomerAccountRules customerAccountRules;


    /**
     * Constructs a {@code PartyFunctions}.
     *
     * @param service      the archetype service
     * @param patientRules the patient rules
     */
    public PartyFunctions(IArchetypeService service, PatientRules patientRules) {
        this.service = service;
        this.patientRules = patientRules;
        partyRules = new PartyRules(service);
        customerAccountRules = new CustomerAccountRules(service);
        supplierRules = new SupplierRules(service);
    }

    /**
     * Returns a specified node for a customer.
     *
     * @param customer the customer
     * @param nodeName to node to return
     * @return the node Object, or {@code null} if none can be found
     */
    public Object getCustomerNode(Party customer, String nodeName) {
        return partyRules.getCustomerNode(customer, nodeName);
    }

    /**
     * Returns the specified node of a customer associated with an act.
     *
     * @param act      the act
     * @param nodeName to node to return
     * @return the node Object, or {@code null}
     */
    public Object getCustomerNode(Act act, String nodeName) {
        return partyRules.getCustomerNode(act, nodeName);
    }

    /**
     * Returns the full name for the passed party.
     *
     * @param context the expression context. Expected to reference a patient
     *                party or an act.
     * @return the parties full name.
     */
    public String getPartyFullName(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getPartyFullName((Party) value);
        } else if (value instanceof Act) {
            return getPartyFullName((Act) value);
        }
        return null;
    }

    /**
     * Returns a formatted name for a party.
     *
     * @param party the party
     * @return the party's formatted name
     */
    public String getPartyFullName(Party party) {
        return partyRules.getFullName(party);
    }

    /**
     * Returns a formatted name for an act.
     *
     * @param act the act
     * @return the party's formatted name
     */
    public String getPartyFullName(Act act) {
        return partyRules.getFullName(act);
    }

    /**
     * Returns the current owner party for the passed party.
     *
     * @param context the expression context. Expected to reference a patient party or act containing an
     *                <em>participation.patient</em>
     * @return the patients current owner Party. May be {@code null}
     */
    public Party getPatientOwner(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getPatientOwner((Party) value);
        } else if (value instanceof Act) {
            return getPatientOwner((Act) value);
        }
        return null;
    }

    /**
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or {@code null} if none can be found
     */
    public Party getPatientOwner(Party patient) {
        return patientRules.getOwner(patient);
    }

    /**
     * Returns the owner of a patient associated with an act.
     *
     * @param act the act
     * @return the associated patients owner, or {@code null}
     */
    public Party getPatientOwner(Act act) {
        return patientRules.getOwner(act);
    }

    /**
     * Returns the most current owner of a patient associated with an act.
     *
     * @param act the act
     * @return the associated patients owner, or {@code null}
     */
    public Party getPatientCurrentOwner(Act act) {
        return patientRules.getCurrentOwner(act);
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
     * @param context the expression context. Expected to reference a party.
     * @return a formatted list of contacts. May be {@code null}
     */
    public String getPreferredContacts(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        return getPreferredContacts((Party) pointer.getValue());
    }

    /**
     * Returns a formatted list of preferred contacts for a party.
     *
     * @param party the party
     * @return a formatted list of contacts
     */
    public String getPreferredContacts(Party party) {
        if (party != null) {
            return partyRules.getPreferredContacts(party);
        }
        return "";
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party or
     *                act
     * @return a formatted billing address, or {@code null}
     */

    public String getBillingAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getBillingAddress((Party) value);

        } else if (value instanceof Act) {
            return getBillingAddress((Act) value);
        }
        return null;
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param party the party. May be {@code null}.
     * @return a formatted billing address for the party, or an empty string
     *         if the party is null or if the party has no corresponding
     *         <em>contact.location</em> contact
     */
    public String getBillingAddress(Party party) {
        if (party != null) {
            return partyRules.getBillingAddress(party);
        }
        return "";
    }

    /**
     * Returns a formatted billing address for a customer associated with an
     * act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted billing address for a party. May be empty if the act has no customer party or the party has
     *         no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getBillingAddress(Act act) {
        if (act != null) {
            return partyRules.getBillingAddress(act);
        }
        return "";
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return a formatted billing address, or {@code null}
     */
    public String getCorrespondenceAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getCorrespondenceAddress((Party) value);
        } else if (value instanceof Act) {
            return getCorrespondenceAddress((Act) value);
        }
        return null;
    }

    /**
     * Returns a formatted correspondence address for a party.
     *
     * @return a formatted correspondence address for a party. May be empty if
     *         there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceAddress(Party party) {
        if (party != null) {
            return partyRules.getCorrespondenceAddress(party);
        }
        return "";
    }

    /**
     * Returns a formatted correspondence address for a customer associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @return a formatted billing address for a party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceAddress(Act act) {
        if (act != null) {
            return partyRules.getCorrespondenceAddress(act);
        }
        return "";
    }

    /**
     * Returns a formatted correspondence name and address for a customer associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @return a formatted name and billing address for a party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceNameAddress(Act act) {
        if (act != null) {
            return partyRules.getCorrespondenceNameAddress(act);
        }
        return "";
    }

    /**
     * Retuurns a formatted telephone number for a customer.
     *
     * @param party the customer
     * @return a formatted telephone number. party. May be empty if there is no corresponding
     *         <em>contact.phoneNumber</em> contact.
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getTelephone(Party party) {
        return (party != null) ? partyRules.getTelephone(party) : "";
    }

    /**
     * Returns a formatted telephone number for a customer associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted telephone number. party. May be empty if there is no corresponding
     *         <em>contact.phoneNumber</em> contact.
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getTelephone(Act act) {
        return (act != null) ? partyRules.getTelephone(act) : "";
    }

    /**
     * Retuurns a formatted home telephone number for a customer.
     *
     * @param party the customer
     * @return a formatted telephone number. party. May be empty if
     *         there is no corresponding <em>contact.phoneNumber</em> contact
     *         with <em>HOME</em> purpose
     */
    public String getHomeTelephone(Party party) {
        if (party != null) {
            return partyRules.getHomeTelephone(party);
        }
        return "";
    }

    /**
     * Returns a formatted home telephone number for a customer associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted telephone number for the party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.phoneNumber</em> contact with <em>HOME</em> purpose
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getHomeTelephone(Act act) {
        if (act != null) {
            return partyRules.getHomeTelephone(act);
        }
        return "";
    }

    /**
     * Retuurns a formatted work telephone number for a customer.
     *
     * @param party the customer
     * @return a formatted telephone number for the party. May be empty if
     *         there is no corresponding <em>contact.phoneNumber</em> contact
     *         with <em>WORK</em> purpose
     */
    public String getWorkTelephone(Party party) {
        if (party != null) {
            return partyRules.getWorkTelephone(party);
        }
        return "";
    }

    /**
     * Returns a formatted work telephone number for a customer associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted telephone number for the party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.phoneNumber</em> contact with <em>WORK</em> purpose
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getWorkTelephone(Act act) {
        if (act != null) {
            return partyRules.getWorkTelephone(act);
        }
        return "";
    }

    /**
     * Returns a formatted mobile telephone number for a customer.
     *
     * @param party the customer
     * @return a formatted telephone number for the party. May be empty if
     *         there is no corresponding <em>contact.phoneNumber</em> contact
     *         with <em>MOBILE</em> purpose
     */
    public String getMobileTelephone(Party party) {
        if (party != null) {
            return partyRules.getMobileTelephone(party);
        }
        return "";
    }

    /**
     * Returns a formatted mobile telephone number for a customer associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted telephone number for the party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.phoneNumber</em> contact with <em>MOBILE</em> purpose
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getMobileTelephone(Act act) {
        if (act != null) {
            return partyRules.getMobileTelephone(act);
        }
        return "";
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @param context the expression context. Expected to reference a party
     * @return a formatted fax number. party. May be empty if
     *         there is no corresponding <em>contact.faxNumber</em> contact
     */
    public String getFaxNumber(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getFaxNumber((Party) value);
        } else if (value instanceof Act) {
            return getFaxNumber((Act) value);
        }
        return "";
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @return a formatted fax number for a party. May be empty if
     *         there is no corresponding <em>contact.faxNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFaxNumber(Party party) {
        if (party != null) {
            return partyRules.getFaxNumber(party);
        }
        return "";
    }

    /**
     * Returns a formatted fax number for an act.
     *
     * @return a formatted fax number for a party. May be empty if
     *         there is no corresponding <em>contact.faxNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFaxNumber(Act act) {
        if (act != null) {
            Party party = partyRules.getCustomer(act);
            if (party == null) {
                party = patientRules.getOwner(act);
            }
            if (party != null) {
                return partyRules.getFaxNumber(party);
            }
        }
        return "";
    }

    /**
     * Returns a formatted email address for a party.
     *
     * @param context the expression context. Expected to reference a party
     * @return a formatted email address. party. May be empty if
     *         there is no corresponding <em>contact.email</em> contact
     */
    public String getEmailAddress(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getEmailAddress((Party) value);
        } else if (value instanceof Act) {
            return getEmailAddress((Act) value);
        }
        return "";
    }

    /**
     * Returns a formatted email address for a party.
     *
     * @return a formatted email address for a party. May be empty if
     *         there is no corresponding <em>contact.email</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getEmailAddress(Party party) {
        if (party != null) {
            return partyRules.getEmailAddress(party);
        }
        return "";
    }

    /**
     * Returns a formatted email Address for an act.
     *
     * @return a formatted email Address for a party. May be empty if
     *         there is no corresponding <em>contact.email</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getEmailAddress(Act act) {
        if (act != null) {
            Party party = partyRules.getCustomer(act);
            if (party == null) {
                party = patientRules.getOwner(act);
            }
            if (party != null) {
                return partyRules.getEmailAddress(party);
            }
        }
        return "";
    }

    /**
     * Returns a formatted contact purpose string for the Contact.
     *
     * @param context the expression context. Expected to reference a contact.
     * @return a formatted string with the contacts contact purposes,
     *         or {@code null}
     */
    public String getContactPurposes(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Contact)) {
            return null;
        }
        Contact contact = (Contact) pointer.getValue();
        return partyRules.getContactPurposes(contact);
    }

    /**
     * Returns a stringfield form of a party's identities.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities or
     *         {@code null}
     */
    public String identities(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        Party party = (Party) pointer.getValue();
        return partyRules.getIdentities(party);
    }

    /**
     * Returns the current account Balance for a party or act.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return the account balance
     */
    public BigDecimal getAccountBalance(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getAccountBalance((Party) value);

        } else if (value instanceof Act) {
            return getAccountBalance((Act) value);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns the account balance for a party.
     *
     * @param party the party. May be {@code null}.
     * @return the current account Balance
     */
    public BigDecimal getAccountBalance(Party party) {
        if (party != null) {
            return customerAccountRules.getBalance(party);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns the current account balance for a customer associated with an
     * act via an <em>participation.customer</em> or <em>participation.patient</em>
     * participation.
     *
     * @param act the act
     * @return the current account balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getAccountBalance(Act act) {
        if (act != null) {
            Party party = partyRules.getCustomer(act);
            if (party == null) {
                party = patientRules.getOwner(act);
            }
            return (party != null) ? getAccountBalance(party) : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns the referral vet for a patient.
     * <p/>
     * This is the patient's associated party from the first matching <em>entityRelationship.referredFrom</em> or
     * <em>entityrRelationship.referredTo</em> that matches the:
     * <ul>
     * <li>act's start time, if the context refers to an act; or</li>
     * <li>the current time, if it refers to a patient</li>
     * </ul>
     *
     * @param context the expression context. Expected to reference an act or patient.
     * @return the referral vet, or {@code null} if there is no patient associated with the act, the act has no start
     *         time, or the patient isn't being referred
     */
    public Party getPatientReferralVet(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Act) {
            return getPatientReferralVet((Act) value);
        } else if (value instanceof Party) {
            return getPatientReferralVet((Party) value);
        }
        return null;
    }

    /**
     * Returns the referral vet for a patient linked to an act.
     * <p/>
     * This is the patient's associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or
     * <em>entityRelationship.referredTo</em> overlapping the act's start time.
     *
     * @param act the act. May be {@code null}
     * @return the referral vet, or {@code null} if there is no patient associated with the act, the act has no start
     *         time, or the patient isn't being referred
     */
    public Party getPatientReferralVet(Act act) {
        Party vet = null;
        if (act != null) {
            Date startTime = act.getActivityStartTime();
            Party patient = getPatient(act);
            if (patient != null && startTime != null) {
                PatientRules rules = patientRules;
                vet = rules.getReferralVet(patient, startTime);
            }
        }
        return vet;
    }

    /**
     * Returns the referral vet for a patient.
     * <p/>
     * This is the patient's associated party from the first matching <em>entityRelationship.referredFrom</em> or
     * <em>entityRelationship.referredTo</em> overlapping the current time.
     *
     * @param patient the patient. May be {@code null}
     * @return the referral vet, or {@code null} if there is no patient associated with the act, the act has no start
     *         time, or the patient isn't being referred
     */
    public Party getPatientReferralVet(Party patient) {
        return (patient != null) ? patientRules.getReferralVet(patient, new Date()) : null;
    }

    /**
     * Returns the referral vet practice for a vet.
     *
     * @param context the expression context. Expected to reference an patient, or an act containing a patient
     * @return the practice the vet is associated with or {@code null} if the vet is not associated with any practice
     */
    public Party getPatientReferralVetPractice(ExpressionContext context) {
        Party result = null;
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Act) {
            result = getPatientReferralVetPractice((Act) value);
        } else if (value instanceof Party) {
            result = getPatientReferralVetPractice((Party) value);
        }
        return result;
    }

    /**
     * Returns the referral vet practice for a vet associated with the supplied act's patient.
     *
     * @param act the act. May be {@code null}
     * @return the practice the vet is associated with or {@code null} if the vet is not associated with any practice
     */
    public Party getPatientReferralVetPractice(Act act) {
        Party result = null;
        if (act != null) {
            Date startTime = act.getActivityStartTime();
            if (startTime != null) {
                Party vet = getPatientReferralVet(act);
                if (vet != null) {
                    result = getReferralVetPractice(vet, startTime);
                }
            }
        }
        return result;
    }

    /**
     * Returns the referral vet practice for a vet associated with the supplied patient.
     *
     * @param patient the patient. May be {@code null}
     * @return the practice the vet is associated with or {@code null} if the vet is not associated with any practice
     */
    public Party getPatientReferralVetPractice(Party patient) {
        Party result = null;
        if (patient != null) {
            Party vet = getPatientReferralVet(patient);
            if (vet != null) {
                result = getReferralVetPractice(vet, new Date());
            }
        }
        return result;
    }

    /**
     * Returns the referral vet practice for a vet overlapping the specified
     * time.
     *
     * @param vet  the vet
     * @param time the time
     * @return the practice the vet is associated with or {@code null} if
     *         the vet is not associated with any practice
     */
    public Party getReferralVetPractice(Party vet, Date time) {
        return supplierRules.getReferralVetPractice(vet, time);
    }

    /**
     * Returns the age of the patient.
     * <p/>
     * If the patient is deceased, the age of the patient when they died will be returned
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the patient's age
     */
    public String getPatientAge(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        return getPatientAge((Party) pointer.getValue());
    }

    /**
     * Returns the age of the patient.
     * <p/>
     * If the patient is deceased, the age of the patient when they died will be returned
     *
     * @param patient the patient
     * @return the stringified form of the patient's age
     */
    public String getPatientAge(Party patient) {
        return patientRules.getPatientAge(patient);
    }

    /**
     * Returns the patient microchip.
     *
     * @param patient the patient
     * @return the microchip, or an empty string if none is found
     */
    public String getPatientMicrochip(Party patient) {
        String result = patientRules.getMicrochip(patient);
        return (result != null) ? result : "";
    }

    /**
     * Returns the microchip of a patient associated with an act.
     *
     * @param act the act
     * @return the microchip, or an empty string if none is found
     */
    public String getPatientMicrochip(Act act) {
        Party patient = getPatient(act);
        return (patient != null) ? getPatientMicrochip(patient) : "";
    }

    /**
     * Returns the patient pet tag.
     *
     * @param patient the patient
     * @return the pet tag, or an empty string if none is found
     */
    public String getPatientPetTag(Party patient) {
        String result = patientRules.getPetTag(patient);
        return (result != null) ? result : "";
    }

    /**
     * Returns the pet tag of a patient associated with an act.
     *
     * @param act the act
     * @return the pet tag, or an empty string if none is found
     */
    public String getPatientPetTag(Act act) {
        Party patient = getPatient(act);
        return (patient != null) ? getPatientPetTag(patient) : "";
    }

    /**
     * Returns the most recent weight in string format for a patient.
     *
     * @param context the expression context. Expected to reference a party or
     *                act
     * @return a formatted weight , or {@code null}
     */

    public String getPatientWeight(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getPatientWeight((Party) value);

        } else if (value instanceof Act) {
            return getPatientWeight((Act) value);
        }
        return null;
    }

    /**
     * Returns a formatted weight for a patient.
     *
     * @param party the patient. May be {@code null}.
     * @return a formatted weight for the party, or an empty string
     */
    public String getPatientWeight(Party party) {
        if (party != null) {
            return patientRules.getPatientWeight(party);
        }
        return "";
    }

    /**
     * Returns a formatted weight for a patient given a act.
     *
     * @param act the act. May be {@code null}.
     * @return a formatted weight for the party, or an empty string
     */
    public String getPatientWeight(Act act) {
        if (act != null) {
            return patientRules.getPatientWeight(act);
        }
        return "";
    }

    /**
     * Returns the patient weight, in kilos.
     * <p/>
     * This uses the most recent recorded weight for the patient.
     *
     * @param patient the patient. May be {@code null}
     * @return the patient weight, in kilos
     */
    public BigDecimal getWeight(Party patient) {
        return (patient != null) ? patientRules.getWeight(patient) : BigDecimal.ZERO;
    }

    /**
     * Returns the patient weight, in the specified units.
     * <p/>
     * This uses the most recent recorded weight for the patient.
     *
     * @param patient the patient. May be {@code null}
     * @param units   the units. One of {@code KILOGRAMS}, {@code GRAMS}, or {@code POUNDS}
     * @return the patient weight in the specified units
     */
    public BigDecimal getWeight(Party patient, String units) {
        return (patient != null) ? patientRules.getWeight(patient, WeightUnits.valueOf(units)) : BigDecimal.ZERO;
    }

    /**
     * Returns the patient weight, in kilos, for the patient associated with an act.
     * <p/>
     * This uses the most recent recorded weight for the patient.
     *
     * @param act the act. May be {@code null}.
     * @return the patient weight, in kilos
     */
    public BigDecimal getWeight(Act act) {
        Party patient = getPatient(act);
        return getWeight(patient);
    }

    /**
     * Returns the patient weight, in kilos, for the patient associated with an act.
     * <p/>
     * This uses the most recent recorded weight for the patient.
     *
     * @param act   the act. May be {@code null}.
     * @param units the units. One of {@code KILOGRAMS}, {@code GRAMS}, or {@code POUNDS}
     * @return the patient weight in the specified units
     */
    public BigDecimal getWeight(Act act, String units) {
        Party patient = getPatient(act);
        return getWeight(patient, units);
    }

    /**
     * Returns the Desex status of a Patient.
     *
     * @param context the expression context. Expected to reference a party or
     *                act
     * @return desex status, or an empty string
     */

    public String getPatientDesexStatus(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getPatientDesexStatus((Party) value);

        } else if (value instanceof Act) {
            return getPatientDesexStatus((Act) value);
        }
        return null;
    }

    /**
     * Returns the Desex status of a Patient.
     *
     * @param party the patient. May be {@code null}.
     * @return desex status, or an empty string
     */
    public String getPatientDesexStatus(Party party) {
        return patientRules.getPatientDesexStatus(party);
    }

    /**
     * Returns the Desex Status for a Patient.
     *
     * @param act the act. May be {@code null}.
     * @return desex status, or an empty string
     */
    public String getPatientDesexStatus(Act act) {
        if (act != null) {
            return patientRules.getPatientDesexStatus(act);
        }
        return "";
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
     * Returns the Practice Address as a single line String.
     *
     * @return the practice address as a string
     */
    public String getPracticeAddress() {
        return partyRules.getPracticeAddress();
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
     * Returns the Bpay ID for a customer.
     *
     * @param context the expression context. Expected to reference a party or act
     * @return a Bpay ID for the customer, or {@code null}
     */

    public String getBpayID(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getBpayId((Party) value);

        } else if (value instanceof Act) {
            return getBpayId((Act) value);
        }
        return null;
    }

    /**
     * Returns a Bpay Id for the Party.
     * Utilises the party uid and adds a check digit using a Luntz 10 algorithm.
     *
     * @param party the party
     * @return string bpay id
     */
    public String getBpayId(Party party) {
        return partyRules.getBpayId(party);
    }

    /**
     * Returns the Bpay ID for customer associated with an
     * act via an <em>participation.customer</em> or <em>participation.patient</em>
     * participation.
     *
     * @param act the act
     * @return the Bpay ID
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getBpayId(Act act) {
        if (act != null) {
            Party party = partyRules.getCustomer(act);
            if (party == null) {
                party = patientRules.getOwner(act);
            }
            return (party != null) ? getBpayId(party) : null;
        }
        return null;
    }

    /**
     * Helper to return the patient associated with an act.
     *
     * @param act the act. May be {@code null}
     * @return the patient, or {@code null} if none is found
     */
    private Party getPatient(Act act) {
        if (act != null) {
            ActBean bean = new ActBean(act, service);
            return (Party) bean.getParticipant("participation.patient");
        }
        return null;
    }

}
