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

package org.openvpms.web.workspace.reporting.email;

import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.mail.EmailAddress;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link PracticeEmailAddresses} class.
 *
 * @author Tim Anderson
 */
public class PracticeEmailAddressesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link PracticeEmailAddresses#getAddress(Party)} method.
     */
    @Test
    public void testGetAddress() {
        Party practice = TestHelper.getPractice();
        for (org.openvpms.component.model.party.Contact contact : new ArrayList<>(practice.getContacts())) {
            if (contact.isA(ContactArchetypes.EMAIL)) {
                practice.removeContact(contact);
            }
        }
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party location3 = TestHelper.createLocation();
        location1.setName("X Location 1");
        location2.setName("X Location 2");
        location2.setName("X Location 3");
        save(location1, location2, location3);
        EntityBean bean = new EntityBean(practice);
        practice.setName("OpenVPMS Practice");
        bean.addNodeTarget("locations", location1);
        bean.addNodeTarget("locations", location2);
        bean.addNodeTarget("locations", location3);

        practice.addContact(createEmailContact("mainreminder@practice.com", "REMINDER"));
        practice.addContact(createEmailContact("mainbilling@practice.com", "BILLING"));
        location1.addContact(createEmailContact("branch1reminder@practice.com", "REMINDER"));
        location1.addContact(createEmailContact("branch1billing@practice.com", "BILLING"));
        location2.addContact(createEmailContact("branch2billing@practice.com", "BILLING"));
        location3.addContact(createEmailContact("branch3billing@practice.com", "Name Override", "REMINDER"));
        save(practice, location1, location2, location3);

        // customer1 has a link to location1, so should get the location1 reminder address
        Party customer1 = createCustomer(location1);
        EntityBean customerBean = new EntityBean(customer1);
        customerBean.addNodeTarget("practice", location1);

        // customer 2 has a link to location 2
        Party customer2 = createCustomer(location2);

        // customer 3 has a link to location 3
        Party customer3 = createCustomer(location3);

        // customer 4 has no location, so should get the practice reminder address
        Party customer4 = createCustomer(null);

        PracticeRules rules = new PracticeRules(getArchetypeService(), null);
        PracticeEmailAddresses addresses = new PracticeEmailAddresses(practice, "REMINDER", rules,
                                                                      getArchetypeService());

        checkAddress(addresses.getAddress(customer1), "branch1reminder@practice.com", "X Location 1");
        checkAddress(addresses.getAddress(customer2), "mainreminder@practice.com", "OpenVPMS Practice");
        checkAddress(addresses.getAddress(customer3), "branch3billing@practice.com", "Name Override");
        checkAddress(addresses.getAddress(customer4), "mainreminder@practice.com", "OpenVPMS Practice");
    }

    /**
     * Creates a new email contact.
     *
     * @param address the email address
     * @param purpose the contact purpose
     * @return a new contact
     */
    protected Contact createEmailContact(String address, String purpose) {
        return TestHelper.createEmailContact(address, false, purpose);
    }

    /**
     * Creates a new email contact.
     *
     * @param address the email address
     * @param name    the contact name
     * @param purpose the contact purpose
     * @return a new contact
     */
    protected Contact createEmailContact(String address, String name, String purpose) {
        Contact emailContact = TestHelper.createEmailContact(address, false, purpose);
        emailContact.setName(name);
        return emailContact;
    }

    /**
     * Verifies an address matches that expected.
     *
     * @param emailAddress the address
     * @param address      the expected address
     * @param name         the expected name
     */
    private void checkAddress(EmailAddress emailAddress, String address, String name) {
        assertEquals(address, emailAddress.getAddress());
        assertEquals(name, emailAddress.getName());
    }

    /**
     * Creates a customer.
     *
     * @param location the customer practice location. May be {@code null}
     * @return a new customer
     */
    private Party createCustomer(Party location) {
        Party customer = TestHelper.createCustomer(false);
        if (location != null) {
            EntityBean customerBean = new EntityBean(customer);
            customerBean.addNodeTarget("practice", location);
        }
        return customer;
    }

}
