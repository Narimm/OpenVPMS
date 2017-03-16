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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.contact;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collections;
import java.util.List;


/**
 * Helper routines for {@link Contact}s.
 *
 * @author Tim Anderson
 */
public class ContactHelper {

    /**
     * Returns phone numbers that are flagged for SMS messaging.
     * <p/>
     * The preferred no.s are at the head of the list
     *
     * @param party the party
     * @return a list of phone contacts flagged for SMS messaging
     */
    public static List<Contact> getSMSContacts(Party party) {
        if (party == null) {
            return Collections.emptyList();
        }
        return sort(new Contacts(ServiceHelper.getArchetypeService()).getSMSContacts(party), Contacts.TELEPHONE_NUMBER);
    }

    /**
     * Returns email contacts for a party.
     * <p/>
     * The preferred email contact is the first element in the returned list, if it exists.
     * <p/>
     * Any email contact that doesn't have an email address will be excluded.
     *
     * @param party the party. May be {@code null}
     * @return the email contacts
     */
    public static List<Contact> getEmailContacts(Party party) {
        if (party == null) {
            return Collections.emptyList();
        }
        return sort(new Contacts(ServiceHelper.getArchetypeService()).getEmailContacts(party), Contacts.EMAIL_ADDRESS);
    }

    /**
     * Returns the preferred email address for a party.
     *
     * @param party the party. May be {@code null}
     * @return the party's preferred email address or {@code null} if the party has no email address
     */
    public static Contact getPreferredEmail(Party party) {
        List<Contact> list = getEmailContacts(party);
        return (!list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * Returns the email address from an email contact.
     *
     * @param contact the contact. May be {@code null}
     * @return the email address. May be {@code null}
     */
    public static String getEmail(Contact contact) {
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact);
            return bean.getString("emailAddress");
        }
        return null;
    }

    /**
     * Returns the default value for the <em>contact.phoneNumber</em> name node.
     *
     * @return the default value for the name node
     */
    public static String getDefaultPhoneName() {
        return getDefaultContactName(ContactArchetypes.PHONE);
    }

    /**
     * Returns the default value for the <em>contact.email</em> name node.
     *
     * @return the default value for the name node
     */
    public static String getDefaultEmailName() {
        return getDefaultContactName(ContactArchetypes.EMAIL);
    }

    /**
     * Returns the default value for a contact's name node.
     *
     * @return the default value for the name node
     */
    protected static String getDefaultContactName(String shortName) {
        ArchetypeDescriptor archetypeDescriptor = DescriptorHelper.getArchetypeDescriptor(shortName);
        String value = null;
        if (archetypeDescriptor != null) {
            NodeDescriptor descriptor = archetypeDescriptor.getNodeDescriptor("name");
            if (descriptor != null) {
                value = descriptor.getDefaultValue();
                if (value != null) {
                    // defaultValue is an xpath expression. Rather than evaluating it, just support the simple case of
                    // a quoted string.
                    value = StringUtils.strip(value, "'");
                }
                if (StringUtils.isEmpty(value)) {
                    value = null;
                }
            }
        }
        return value;
    }

    /**
     * Sorts contacts. Any preferred contact will appear first.
     *
     * @param contacts the contacts
     * @param sortNode the node to sort on
     * @return the sorted contacts
     */
    private static List<Contact> sort(List<Contact> contacts, String sortNode) {
        if (contacts.size() > 1) {
            SortConstraint[] sort = {new NodeSortConstraint("preferred", false),
                                     new NodeSortConstraint(sortNode, true)};
            IMObjectSorter.sort(contacts, sort);
        }
        return contacts;
    }

}
