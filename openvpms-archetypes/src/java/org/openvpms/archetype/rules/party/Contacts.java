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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.party;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Contact helpers.
 *
 * @author Tim Anderson
 */
public class Contacts {

    /**
     * The email address node.
     */
    public static final String EMAIL_ADDRESS = "emailAddress";

    /**
     * The telephone number node.
     */
    public static final String TELEPHONE_NUMBER = "telephoneNumber";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link Contacts}.
     *
     * @param service the archetype service
     */
    public Contacts(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns fully populated email contacts for a party.
     *
     * @param party the party
     * @return the email contacts
     */
    public List<Contact> getEmailContacts(Party party) {
        return getContacts(party, new EmailPredicate());
    }

    /**
     * Returns fully populated SMS contacts for a party.
     *
     * @param party the party
     * @return the SMS contacts
     */
    public List<Contact> getSMSContacts(Party party) {
        return getContacts(party, new SMSPredicate());
    }

    /**
     * Determines if a party can receive SMS messages.
     *
     * @param party the party
     * @return {@code true} if the party can receive SMS messages
     */
    @SuppressWarnings("unchecked")
    public boolean canSMS(Party party) {
        return CollectionUtils.find((Set<Contact>) (Set) party.getContacts(), new SMSPredicate()) != null;
    }

    /**
     * Returns the phone number from a contact, extracting any formatting.
     *
     * @param contact the phone contact
     * @return the phone number. May be {@code null}
     */
    public String getPhone(org.openvpms.component.model.party.Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        String areaCode = bean.getString("areaCode");
        String phone = bean.getString("telephoneNumber");
        String result = null;
        if (!StringUtils.isEmpty(areaCode)) {
            result = areaCode;
            if (!StringUtils.isEmpty(phone)) {
                result += phone;
            }
        } else if (!StringUtils.isEmpty(phone)) {
            result = phone;
        }
        result = getPhone(result);
        return result;
    }

    /**
     * Sorts contacts on increasing identifier.
     * <p>
     * Note that this operation modifies the supplied list.
     *
     * @param contacts the contacts to sort
     * @return the sorted contact
     */
    public static <T extends org.openvpms.component.model.party.Contact> List<T> sort(List<T> contacts) {
        if (contacts.size() > 1) {
            Collections.sort(contacts, (o1, o2) -> Long.compare(o1.getId(), o2.getId()));
        }
        return contacts;
    }

    /**
     * Sorts contacts on increasing identifier.
     *
     * @param contacts the contacts to sort
     * @return the sorted contacts
     */
    public static <T extends org.openvpms.component.model.party.Contact> List<T> sort(Collection<T> contacts) {
        return sort(new ArrayList<>(contacts));
    }

    /**
     * Looks for a contact that matches the criteria.
     * <p>
     * For consistent results over multiple calls, sort the contacts first.
     *
     * @param contacts the contacts
     * @param matcher  the contact matcher
     * @return the matching contact or {@code null} if none is found
     */
    public static Contact find(Collection<org.openvpms.component.model.party.Contact> contacts,
                               ContactMatcher matcher) {
        for (org.openvpms.component.model.party.Contact contact : contacts) {
            if (matcher.matches(contact)) {
                break;
            }
        }
        return (Contact) matcher.getMatch();
    }

    /**
     * Finds all contacts that match the criteria.
     *
     * @param contacts the contacts
     * @param matcher  the contact matcher
     * @return the matching contacts
     */
    public static List<Contact> findAll(Collection<org.openvpms.component.model.party.Contact> contacts,
                                        ContactMatcher matcher) {
        List<Contact> result = new ArrayList<>();
        for (org.openvpms.component.model.party.Contact contact : contacts) {
            if (matcher.matches(contact)) {
                result.add((Contact) contact);
            }
        }
        return result;
    }

    /**
     * Returns the phone number from a string, extracting any formatting.
     *
     * @param phone the formatted phone number
     * @return the phone number. May be {@code null}
     */
    public static String getPhone(String phone) {
        String result = phone;
        if (!StringUtils.isEmpty(result)) {
            // strip any spaces, hyphens, and brackets, and any characters after the last digit.
            result = result.replaceAll("[\\s\\-()]", "").replaceAll("[^\\d\\+].*", "");
        }
        return result;
    }

    /**
     * Returns contacts matching a predicate.
     *
     * @param party     the party
     * @param predicate the predicate
     * @return contacts matching the predicate
     */
    @SuppressWarnings("unchecked")
    public static List<Contact> getContacts(Party party, Predicate<Contact> predicate) {
        List<Contact> result = new ArrayList<>();
        CollectionUtils.select((Set<Contact>) (Set) party.getContacts(), predicate, result);
        return result;
    }

    private class SMSPredicate implements Predicate<Contact> {

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param contact the object to evaluate, should not be changed
         * @return true or false
         */
        public boolean evaluate(Contact contact) {
            boolean result = false;
            if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
                IMObjectBean bean = new IMObjectBean(contact, service);
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

    private class EmailPredicate implements Predicate<Contact> {

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param contact the object to evaluate, should not be changed
         * @return true or false
         */
        public boolean evaluate(Contact contact) {
            boolean result = false;
            if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                IMObjectBean bean = new IMObjectBean(contact, service);
                if (!StringUtils.isEmpty(bean.getString(EMAIL_ADDRESS))) {
                    result = true;
                }
            }
            return result;
        }
    }

}
