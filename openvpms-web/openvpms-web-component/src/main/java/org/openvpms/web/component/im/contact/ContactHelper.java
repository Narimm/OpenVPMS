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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.contact;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectSorter;

import java.util.ArrayList;
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
        return getContacts(party, SMSPredicate.INSTANCE, SMSPredicate.TELEPHONE_NUMBER);
    }

    /**
     * Returns email contacts for a party.
     * <p/>
     * The preferred email contact is the first element in the returned list, if it exists.
     *
     * @param party the party. May be {@code null}
     * @return the email contacts
     */
    public static List<Contact> getEmailContacts(Party party) {
        if (party == null) {
            return Collections.emptyList();
        }
        return getContacts(party, EmailPredicate.INSTANCE, EmailPredicate.EMAIL_ADDRESS);
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
     * Returns a formatted email address.
     *
     * @param address  the email address
     * @param personal the personal name. May be {@code null}
     * @param strict   if {@code true}, quote the name to ensure the address conforms to RFC822
     * @return the formatted email address
     */
    public static String getEmail(String address, String personal, boolean strict) {
        String result;
        if (!StringUtils.isEmpty(personal)) {
            StringBuilder builder = new StringBuilder();
            if (strict) {
                builder.append('"');
                builder.append(personal.replaceAll("\"", "")); // remove all quotes, before quoting the name
                builder.append("\" ");
            } else {
                builder.append(personal);
                builder.append(' ');
            }
            builder.append('<');
            builder.append(address);
            builder.append('>');
            result = builder.toString();
        } else {
            result = address;
        }
        return result;
    }

    /**
     * Returns the default value for the <em>contact.email</em> name node.
     *
     * @return the default email name
     */
    public static String getDefaultEmailName() {
        ArchetypeDescriptor archetypeDescriptor = DescriptorHelper.getArchetypeDescriptor(ContactArchetypes.EMAIL);
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
     * Returns contacts for the specified party that match the predicate.
     *
     * @param party     the party
     * @param predicate the predicate
     * @param sortNode  the node to sort on
     * @return the matching contacts
     */
    private static List<Contact> getContacts(Party party, Predicate predicate, String sortNode) {
        List<Contact> result = new ArrayList<Contact>();
        CollectionUtils.select(party.getContacts(), predicate, result);
        if (result.size() > 1) {
            SortConstraint[] sort = {new NodeSortConstraint("preferred", false),
                                     new NodeSortConstraint(sortNode, true)};
            IMObjectSorter.sort(result, sort);
        }
        return result;
    }


    private static class SMSPredicate implements Predicate {

        /**
         * The singleton instance.
         */
        public static Predicate INSTANCE = new SMSPredicate();

        /**
         * The telephone number node.
         */
        private static final String TELEPHONE_NUMBER = "telephoneNumber";

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         */
        public boolean evaluate(Object object) {
            boolean result = false;
            Contact contact = (Contact) object;
            if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (bean.getBoolean("sms")) {
                    String phone = bean.getString(TELEPHONE_NUMBER);
                    if (!StringUtils.isEmpty(phone)) {
                        result = true;
                    }
                }
            }
            return result;
        }
    }

    private static class EmailPredicate implements Predicate {

        /**
         * The singleton instance.
         */
        public static Predicate INSTANCE = new EmailPredicate();

        /**
         * The email address node.
         */
        private static final String EMAIL_ADDRESS = "emailAddress";

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         */
        public boolean evaluate(Object object) {
            boolean result = false;
            Contact contact = (Contact) object;
            if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (!StringUtils.isEmpty(bean.getString(EMAIL_ADDRESS))) {
                    result = true;
                }
            }
            return result;
        }

    }

}
