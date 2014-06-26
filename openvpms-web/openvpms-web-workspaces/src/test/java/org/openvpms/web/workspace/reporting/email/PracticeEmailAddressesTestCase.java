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

import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.List;

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
        List<Contact> contacts = new ArrayList<Contact>(practice.getContacts());
        for (Contact contact : contacts) {
            if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                practice.removeContact(contact);
            }
        }
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        EntityBean bean = new EntityBean(practice);
        bean.addNodeTarget("locations", location1);
        bean.addNodeTarget("locations", location2);

        practice.addContact(TestHelper.createEmailContact("mainreminder@practice.com", false, "REMINDER"));
        practice.addContact(TestHelper.createEmailContact("mainbilling@practice.com", false, "BILLING"));
        location1.addContact(TestHelper.createEmailContact("branch1reminder@practice.com", false, "REMINDER"));
        location1.addContact(TestHelper.createEmailContact("branch1billing@practice.com", false, "BILLING"));
        location2.addContact(TestHelper.createEmailContact("branch2billing@practice.com", false, "BILLING"));
        save(practice, location1, location2);

        // customer1 has a link to location1, so should get the location1 reminder address
        Party customer1 = createCustomer(location1);
        EntityBean customerBean = new EntityBean(customer1);
        customerBean.addNodeTarget("practice", location1);

        // customer 2 has a link to location 2
        Party customer2 = createCustomer(location2);

        // customer 3 has no link, so should get the practice reminder address
        Party customer3 = createCustomer(null);

        PracticeRules rules = new PracticeRules(getArchetypeService());
        PracticeEmailAddresses addresses = new PracticeEmailAddresses(practice, "REMINDER", rules,
                                                                      getArchetypeService());

        assertEquals("branch1reminder@practice.com", addresses.getAddress(customer1).getAddress());
        assertEquals("mainreminder@practice.com", addresses.getAddress(customer2).getAddress());
        assertEquals("mainreminder@practice.com", addresses.getAddress(customer3).getAddress());
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
