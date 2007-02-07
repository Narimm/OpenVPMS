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

package org.openvpms.archetype.rules.party;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Business rules for <em>party.*</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PartyRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <code>PartyRules</code>.
     *
     * @throws ArchetypeServiceException if the archetype service is not
     *                                   configured
     */
    public PartyRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>PartyRules/code>.
     *
     * @param service the archetype service
     */
    public PartyRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the formatted full name of a party.
     *
     * @param party the party
     * @return the formatted full name of the party
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFullName(Party party) {
        String name;
        if (TypeHelper.isA(party, "party.customerperson")) {
            name = getPersonName(party);
        } else {
            name = party.getName();
        }
        return (name != null) ? name : "";
    }

    /**
     * Returns a set of default contacts containing an
     * <em>contact.phoneNumber</em> and <em>contact.location</em>.
     *
     * @return a new set of default contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Set<Contact> getDefaultContacts() {
        Set<Contact> contacts = new HashSet<Contact>();
        Contact phone = (Contact) service.create("contact.phoneNumber");
        Contact location = (Contact) service.create("contact.location");
        service.deriveValues(phone);
        service.deriveValues(location);
        contacts.add(phone);
        contacts.add(location);
        return contacts;
    }

    /**
     * Returns a formatted string of preferred contacts for a party.
     *
     * @param party the party
     * @return a formatted string of preferred contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPreferredContacts(Party party) {
        StringBuffer result = new StringBuffer();
        for (Contact contact : party.getContacts()) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            if (bean.hasNode("preferred")) {
                boolean preferred = bean.getBoolean("preferred");
                if (preferred && bean.hasNode("description")) {
                    String description = bean.getString("description", "");
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(description);
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted string of contact purposes.
     *
     * @param contact the contact
     * @return the formatted contact purposes string. May be empty.
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getContactPurposes(Contact contact) {
        StringBuffer result = new StringBuffer();
        IMObjectBean bean = new IMObjectBean(contact, service);
        if (bean.hasNode("purposes")) {
            List<IMObject> list = bean.getValues("purposes");
            if (!list.isEmpty()) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append("(");
                result.append(getValues(list, "name"));
                result.append(")");
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @return a formatted billing address for a party. May be empty if
     *         there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getBillingAddress(Party party) {
        return getAddress(party, "BILLING");
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
    public String getBillingAddress(Act act) {
        ActBean bean = new ActBean(act, service);
        Party party = (Party) bean.getParticipant("participation.customer");
        return (party != null) ? getBillingAddress(party) : "";
    }

    /**
     * Returns a formatted correspondence address for a party.
     *
     * @return a formatted correspondence address for a party. May be empty if
     *         there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceAddress(Party party) {
        return getAddress(party, "CORRESPONDENCE");
    }

    /**
     * Returns a formatted correspondence address for a customer associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted billing address for a party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceAddress(Act act) {
        ActBean bean = new ActBean(act, service);
        Party party = (Party) bean.getParticipant("participation.customer");
        return (party != null) ? getCorrespondenceAddress(party) : "";
    }

    /**
     * Returns a formatted <em>contact.location</em> address with the
     * specified purpose, for a party if one exists.
     *
     * @param party   the party
     * @param purpose the contact purpose of the address
     * @return a formatted address. May be empty if there is no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getAddress(Party party, String purpose) {
        Contact contact = getContact(party, "contact.location", purpose);
        return (contact != null) ? formatAddress(contact) : "";
    }

    /**
     * Returns a formatted string of a party's identities.
     *
     * @param party the party
     * @return a formatted string of the party's identities
     */
    public String getIdentities(Party party) {
        StringBuffer result = new StringBuffer();
        for (EntityIdentity identity : party.getIdentities()) {
            IMObjectBean bean = new IMObjectBean(identity, service);
            if (bean.hasNode("name")) {
                String name = bean.getString("name");
                String displayName = bean.getDisplayName();
                if (name != null) {
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(displayName);
                    result.append(": ");
                    result.append(name);
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted name for a <em>party.customerPerson</em>.
     *
     * @param party the party
     * @return a formatted name
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the title lookup is incorrect
     */
    private String getPersonName(Party party) {
        IMObjectBean bean = new IMObjectBean(party, service);
        NodeDescriptor descriptor = bean.getDescriptor("title");
        String title = LookupHelper.getName(service, descriptor, party);
        String firstName = bean.getString("firstName", "");
        String lastName = bean.getString("lastName", "");
        StringBuffer result = new StringBuffer();
        if (title != null) {
            result.append(title).append(" ");
        }
        result.append(firstName).append(" ").append(lastName);
        return result.toString();
    }

    /**
     * Returns a contact for the specified party, contact type and purpose.
     * If cannot find one with matching purpose returns last preferred contact.
     * If cannot find with matching purpose and preferred returns last found.
     *
     * @param party   the party
     * @param type    the contact archetype shortname
     * @param purpose the contact purpose
     * @return the corresponding contact, or <code>null</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Contact getContact(Party party, String type, String purpose) {
        Contact result = null;
        if (party != null) {
            for (Contact contact : party.getContacts()) {
                if (TypeHelper.isA(contact, type)) {
                    IMObjectBean bean = new IMObjectBean(contact, service);
                    if (hasContactPurpose(contact, purpose)) {
                        // direct match
                        result = contact;
                        break;
                    }
                    if (bean.hasNode("preferred") && bean.getBoolean(
                            "preferred"))
                        // if preferred contact, save but keep searching just in
                        // case there is another contact that is a direct match
                        result = contact;
                    else {
                        if (result == null) {
                            // closest match thus far
                            result = contact;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines if a contact has a particular purpose.
     *
     * @param contact the contact
     * @param purpose the contact purpose
     * @return <code>true</code> if the contact has the specified purpose,
     *         otherwise <code>false</code>
     */
    private boolean hasContactPurpose(Contact contact, String purpose) {
        for (Lookup classification : contact.getClassifications()) {
            if (classification.getCode().equals(purpose))
                return true;
        }
        return false;
    }

    /**
     * Formats an address from an <em>contact.location</em> contact.
     *
     * @param contact contact
     * @return a formatted address
     * @throws ArchetypeServiceException for any archetype service error
     */
    private String formatAddress(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        String address = bean.getString("address");
        String suburb = bean.getString("suburb");
        String state = bean.getString("state");
        String postcode = bean.getString("postcode");
        return address + "\n" + suburb + " " + state + " " + postcode;
    }

    /**
     * Returns a concatenated list of values for a set of objects.
     *
     * @param objects the objects
     * @param node    the node name
     * @return the stringified value of <code>node</code> for each object,
     *         separated by ", "
     * @throws ArchetypeServiceException for any archetype service error
     */
    private String getValues(List<IMObject> objects, String node) {
        StringBuffer result = new StringBuffer();

        for (IMObject object : objects) {
            IMObjectBean bean = new IMObjectBean(object, service);
            if (bean.hasNode(node)) {
                if (result.length() != 0) {
                    result.append(", ");
                }
                result.append(bean.getString(node));
            }
        }
        return result.toString();
    }

}
