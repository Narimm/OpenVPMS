/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.function.party;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.party.SupplierRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Date;
import java.util.Set;


/**
 * JXPath extension functions that operate on {@link Party} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PartyFunctions {

    /**
     * Returns a set of default Contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a set of contacts. May be <code>null</code>
     */
    public static Set<Contact> getDefaultContacts(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }

        return getDefaultContacts((Party) pointer.getValue());
    }

    /**
     * Returns a list of default contacts.
     *
     * @param party the party
     * @return a list of default contacts
     */
    public static Set<Contact> getDefaultContacts(Party party) {
        return new PartyRules().getDefaultContacts();
    }

    /**
     * Returns the full name for the passed party.
     *
     * @param context the expression context. Expected to reference a patient
     *                party.
     * @return the parties full name.
     */
    public static String getPartyFullName(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getPartyFullName((Party) value);
        }
        return null;
    }

    /**
     * Returns a formatted name for a party.
     *
     * @param party the party
     * @return the party's formatted name
     */
    public static String getPartyFullName(Party party) {
        return new PartyRules().getFullName(party);
    }

    /**
     * Returns the current owner party for the passed party.
     *
     * @param context the expression context. Expected to reference a patient
     *                party or act containing an <em>participation.patient</em>
     * @return the patients current owner Party. May be <code>null</code>
     */
    public static Party getPatientOwner(ExpressionContext context) {
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
     * @return the patient's owner, or <code>null</code> if none can be found
     */
    public static Party getPatientOwner(Party patient) {
        return new PatientRules().getOwner(patient);
    }

    /**
     * Returns the owner of a patient associated with an act.
     *
     * @param act the act
     * @return the associated patients owner, or <code>null</code>
     */
    public static Party getPatientOwner(Act act) {
        return new PatientRules().getOwner(act);
    }

    /**
     * Marks a patient as being deceased.
     *
     * @param patient the patient
     */
    public static void setPatientDeceased(Party patient) {
        new PatientRules().setDeceased(patient);
    }

    /**
     * Marks a patient as being desexed.
     *
     * @param patient the patient
     */
    public static void setPatientDesexed(Party patient) {
        new PatientRules().setDesexed(patient);
    }

    /**
     * Returns a formatted list of preferred contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a formatted list of contacts. May be <code>null</code>
     */
    public static String getPreferredContacts(ExpressionContext context) {
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
    public static String getPreferredContacts(Party party) {
        if (party != null) {
            return new PartyRules().getPreferredContacts(party);
        }
        return "";
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party or
     *                act
     * @return a formatted billing address, or <code>null</code>
     */

    public static String getBillingAddress(ExpressionContext context) {
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
     * @param party the party. May be <code>null</code>.
     * @return a formatted billing address for the party, or an empty string
     *         if the party is null or if the party has no corresponding
     *         <em>contact.location</em> contact
     */
    public static String getBillingAddress(Party party) {
        if (party != null) {
            return new PartyRules().getBillingAddress(party);
        }
        return "";
    }

    /**
     * Returns a formatted billing address for a customer associated with an
     * act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted billing address for a party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static String getBillingAddress(Act act) {
        if (act != null) {
            return new PartyRules().getBillingAddress(act);
        }
        return "";
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party or
     *                act
     * @return a formatted billing address, or <code>null</code>
     */

    public static String getCorrespondenceAddress(ExpressionContext context) {
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
    public static String getCorrespondenceAddress(Party party) {
        if (party != null) {
            return new PartyRules().getCorrespondenceAddress(party);
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
    public static String getCorrespondenceAddress(Act act) {
        if (act != null) {
            return new PartyRules().getCorrespondenceAddress(act);
        }
        return "";
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @param context the expression context. Expected to reference a party
     * @return a formatted fax number, or <code>null</code>
     */

    public static String getFaxNumber(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof Party) {
            return getFaxNumber((Party) value);
        }
        return null;
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @return a formatted fax number for a party. May be empty if
     *         there is no corresponding <em>contact.faxNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static String getFaxNumber(Party party) {
        if (party != null) {
            return new PartyRules().getFaxNumber(party);
        }
        return "";
    }

    /**
     * Returns a formatted contact purpose string for the Contact.
     *
     * @param context the expression context. Expected to reference a contact.
     * @return a formatted string with the contacts contact purposes,
     *         or <code>null</code>
     */

    public static String getContactPurposes(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Contact)) {
            return null;
        }
        Contact contact = (Contact) pointer.getValue();
        return new PartyRules().getContactPurposes(contact);
    }

    /**
     * Returns a stringfield form of a party's identities.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities or
     *         <code>null</code>
     */
    public static String identities(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        Party party = (Party) pointer.getValue();
        return new PartyRules().getIdentities(party);
    }

    /**
     * Returns the referral vet for a patient linked to an act.
     * This is the patient's associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or
     * <em>entityrRelationship.referredTo</em> overlapping the act's start time.
     *
     * @param context the expression context. Expected to reference an act.
     * @return the referral vet, or <code>null</code> if there is no patient
     *         associated with the act, the act has no start time, or the
     *         patient isn't being referred
     */
    public static Party getPatientReferralVet(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Act)) {
            return null;
        }
        return getPatientReferralVet((Act) pointer.getValue());
    }

    /**
     * Returns the referral vet for a patient linked to an act.
     * This is the patient's associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or
     * <em>entityrRelationship.referredTo</em> overlapping the act's start time.
     *
     * @param act the act
     * @return the referral vet, or <code>null</code> if there is no patient
     *         associated with the act, the act has no start time, or the
     *         patient isn't being referred
     */
    public static Party getPatientReferralVet(Act act) {
        Party vet = null;
        Date startTime = act.getActivityStartTime();
        ActBean bean = new ActBean(act);
        Party patient = (Party) bean.getParticipant("participation.patient");
        if (patient != null && startTime != null) {
            PatientRules rules = new PatientRules();
            vet = rules.getReferralVet(patient, startTime);
        }
        return vet;
    }

    /**
     * Returns the referral vet practice for a vet associated with the
     * supplied act's patient.
     *
     * @param context the expression context. Expected to reference an act.
     * @return the practice the vet is associated with or <code>null</code> if
     *         the vet is not associated with any practice
     */
    public static Party getPatientReferralVetPractice(
            ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Act)) {
            return null;
        }
        return getPatientReferralVetPractice((Act) pointer.getValue());
    }

    /**
     * Returns the referral vet practice for a vet associated with the
     * supplied act's patient.
     *
     * @param act the act
     * @return the practice the vet is associated with or <code>null</code> if
     *         the vet is not associated with any practice
     */
    public static Party getPatientReferralVetPractice(Act act) {
        Date startTime = act.getActivityStartTime();
        if (startTime != null) {
            Party vet = getPatientReferralVet(act);
            if (vet != null) {
                return getReferralVetPractice(vet, startTime);
            }
        }

        return null;
    }

    /**
     * Returns the referral vet practice for a vet overlapping the specified
     * time.
     *
     * @param vet  the vet
     * @param time the time
     * @return the practice the vet is associated with or <code>null</code> if
     *         the vet is not associated with any practice
     */
    public static Party getReferralVetPractice(Party vet, Date time) {
        return new SupplierRules().getReferralVetPractice(vet, time);
    }

    /**
     * Returns a string form of a patients age.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities or
     *         <code>null</code>
     */
    public static String getPatientAge(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        if (pointer == null || !(pointer.getValue() instanceof Party)) {
            return null;
        }
        Party party = (Party) pointer.getValue();
        return new PatientRules().getPatientAge(party);
    }

}
