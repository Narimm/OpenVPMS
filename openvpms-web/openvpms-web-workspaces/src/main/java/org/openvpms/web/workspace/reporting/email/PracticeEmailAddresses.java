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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.email;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.archetype.rules.party.PurposeMatcher;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.HashMap;
import java.util.Map;

/**
 * Practice and practice location email addresses, used for reporting.
 *
 * @author Tim Anderson
 */
public class PracticeEmailAddresses {

    /**
     * EmailAddress addresses keyed on practice/practice location reference.
     */
    private final Map<IMObjectReference, EmailAddress> addresses = new HashMap<IMObjectReference, EmailAddress>();

    /**
     * The default email address.
     */
    private final EmailAddress defaultAddress;

    /**
     * The archetype service
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link PracticeEmailAddresses}.
     *
     * @param practice the practice
     * @param purpose  the contact purpose to locate email contacts
     */
    public PracticeEmailAddresses(Party practice, String purpose) {
        this(practice, purpose, ServiceHelper.getBean(PracticeRules.class), ServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a {@link PracticeEmailAddresses}.
     *
     * @param practice the practice
     * @param purpose  the contact purpose to locate email contacts
     * @param rules    the practice rules
     * @param service  the archetype service
     */
    public PracticeEmailAddresses(Party practice, String purpose, PracticeRules rules, IArchetypeService service) {
        this.service = service;
        this.defaultAddress = getPracticeEmail(practice, purpose);
        for (Party location : rules.getLocations(practice)) {
            EmailAddress address = getLocationEmail(location, purpose);
            if (address != null) {
                addresses.put(location.getObjectReference(), address);
            }
        }
    }

    /**
     * Returns the address to use when sending emails to a customer.
     * <p/>
     * If the customer has a practice location, this will determine the address used.
     *
     * @param customer the customer
     * @return the from address
     */
    public EmailAddress getAddress(Party customer) {
        IMObjectBean bean = new IMObjectBean(customer);
        IMObjectReference locationRef = bean.getNodeTargetObjectRef("practice");
        EmailAddress result = addresses.get(locationRef);
        if (result == null) {
            result = defaultAddress;
        }
        return result;
    }

    /**
     * Returns the practice email address, preferably with the specified purpose.
     *
     * @param practice the practice
     * @param purpose  the email contact purpose
     * @return the practice email address
     */
    private EmailAddress getPracticeEmail(Party practice, String purpose) {
        PurposeMatcher matcher = new PurposeMatcher(ContactArchetypes.EMAIL, purpose, false, service);
        Contact contact = Contacts.find(practice.getContacts(), matcher);
        if (contact == null) {
            throw new ReportingException(ReportingException.ErrorCode.NoEmailContact, practice.getName());
        }
        EmailAddress result = getEmailAddress(contact, practice);
        if (result == null) {
            throw new ReportingException(ReportingException.ErrorCode.InvalidEmailAddress, practice.getName());
        }
        return result;
    }

    /**
     * Returns the email address for a practice location.
     *
     * @param practice the practice/practice location
     * @param purpose  the email contact purpose
     * @return the address, or {@code null} if a valid address is not found and {@code fail} is {@code false}
     */
    private EmailAddress getLocationEmail(Party practice, String purpose) {
        EmailAddress result = null;
        PurposeMatcher matcher = new PurposeMatcher(ContactArchetypes.EMAIL, purpose, true, service);
        Contact contact = Contacts.find(practice.getContacts(), matcher);
        if (contact != null) {
            result = getEmailAddress(contact, practice);
        }
        return result;
    }

    /**
     * Returns an email address for an email contact.
     *
     * @param contact  the contact
     * @param practice the practice/practice location
     * @return the email address, or {@code null} if the contact is invalid
     */
    private EmailAddress getEmailAddress(Contact contact, Party practice) {
        IMObjectBean bean = new IMObjectBean(contact);
        String address = bean.getString("emailAddress");
        if (!StringUtils.isEmpty(address)) {
            return new EmailAddress(address, practice.getName());
        }
        return null;
    }

}
