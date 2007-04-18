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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.jxpath.ExpressionContext;
import org.openvpms.archetype.function.party.PartyFunctions;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Wrapper around {@link PartyFunctions} that excludes default contacts
 * whilst loading customers.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderPartyFunctions {

    /**
     * Returns a set of default Contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return an empty set
     */
    public static Set<Contact> getDefaultContacts(ExpressionContext context) {
        return new HashSet<Contact>();
    }

    /**
     * Returns a list of default contacts.
     *
     * @param party the party
     * @return <tt>null</tt>
     */
    public static Set<Contact> getDefaultContacts(Party party) {
        return new HashSet<Contact>();
    }

    /**
     * Returns the full name for the passed party.
     *
     * @param context the expression context. Expected to reference a patient
     *                party.
     * @return the parties full name.
     */
    public static String getPartyFullName(ExpressionContext context) {
        return PartyFunctions.getPartyFullName(context);
    }

    /**
     * Returns a formatted name for a party.
     *
     * @param party the party
     * @return the party's formatted name
     */
    public static String getPartyFullName(Party party) {
        return PartyFunctions.getPartyFullName(party);
    }

    /**
     * Returns the current owner party for the passed party.
     *
     * @param context the expression context. Expected to reference a patient
     *                party or act containing an <em>participation.patient</em>
     * @return the patients current owner Party. May be <code>null</code>
     */
    public static Party getPatientOwner(ExpressionContext context) {
        return PartyFunctions.getPatientOwner(context);
    }

    /**
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or <code>null</code> if none can be found
     */
    public static Party getPatientOwner(Party patient) {
        return PartyFunctions.getPatientOwner(patient);
    }

    /**
     * Returns the owner of a patient associated with an act.
     *
     * @param act the act
     * @return the associated patients owner, or <code>null</code>
     */
    public static Party getPatientOwner(Act act) {
        return PartyFunctions.getPatientOwner(act);
    }

    /**
     * Marks a patient as being deceased.
     *
     * @param patient the patient
     */
    public static void setPatientDeceased(Party patient) {
        PartyFunctions.setPatientDeceased(patient);
    }

    /**
     * Marks a patient as being desexed.
     *
     * @param patient the patient
     */
    public static void setPatientDesexed(Party patient) {
        PartyFunctions.setPatientDesexed(patient);
    }

    /**
     * Returns a formatted list of preferred contacts for a party.
     *
     * @param context the expression context. Expected to reference a party.
     * @return a formatted list of contacts. May be <code>null</code>
     */
    public static String getPreferredContacts(ExpressionContext context) {
        return PartyFunctions.getPreferredContacts(context);
    }

    /**
     * Returns a formatted list of preferred contacts for a party.
     *
     * @param party the party
     * @return a formatted list of contacts
     */
    public static String getPreferredContacts(Party party) {
        return PartyFunctions.getPreferredContacts(party);
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party or
     *                act
     * @return a formatted billing address, or <code>null</code>
     */

    public static String getBillingAddress(ExpressionContext context) {
        return PartyFunctions.getBillingAddress(context);
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
        return PartyFunctions.getBillingAddress(party);
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
        return PartyFunctions.getBillingAddress(act);
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param context the expression context. Expected to reference a party or
     *                act
     * @return a formatted billing address, or <code>null</code>
     */

    public static String getCorrespondenceAddress(ExpressionContext context) {
        return PartyFunctions.getCorrespondenceAddress(context);
    }

    /**
     * Returns a formatted correspondence address for a party.
     *
     * @return a formatted correspondence address for a party. May be empty if
     *         there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static String getCorrespondenceAddress(Party party) {
        return PartyFunctions.getCorrespondenceAddress(party);
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
        return PartyFunctions.getCorrespondenceAddress(act);
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @param context the expression context. Expected to reference a party
     * @return a formatted fax number, or <code>null</code>
     */

    public static String getFaxNumber(ExpressionContext context) {
        return PartyFunctions.getFaxNumber(context);
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @return a formatted fax number for a party. May be empty if
     *         there is no corresponding <em>contact.faxNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static String getFaxNumber(Party party) {
        return PartyFunctions.getFaxNumber(party);
    }

    /**
     * Returns a formatted contact purpose string for the Contact.
     *
     * @param context the expression context. Expected to reference a contact.
     * @return a formatted string with the contacts contact purposes,
     *         or <code>null</code>
     */

    public static String getContactPurposes(ExpressionContext context) {
        return PartyFunctions.getContactPurposes(context);
    }

    /**
     * Returns a stringfield form of a party's identities.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities or
     *         <code>null</code>
     */
    public static String identities(ExpressionContext context) {
        return PartyFunctions.identities(context);
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
        return PartyFunctions.getPatientReferralVet(context);
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
        return PartyFunctions.getPatientReferralVet(act);
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
        return PartyFunctions.getPatientReferralVetPractice(context);
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
        return PartyFunctions.getPatientReferralVetPractice(act);
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
        return PartyFunctions.getReferralVetPractice(vet, time);
    }

    /**
     * Returns a string form of a patients age.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the stringified form of the party's identities or
     *         <code>null</code>
     */
    public static String getPatientAge(ExpressionContext context) {
        return PartyFunctions.getPatientAge(context);
    }

}
