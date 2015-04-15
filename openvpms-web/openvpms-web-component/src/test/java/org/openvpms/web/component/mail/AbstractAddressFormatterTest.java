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

package org.openvpms.web.component.mail;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Base class for {@link AddressFormatter} test cases.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAddressFormatterTest extends ArchetypeServiceTest {

    /**
     * The address formatter.
     */
    private AddressFormatter formatter;

    /**
     * The test contact.
     */
    private Contact email;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        formatter = createAddressFormatter();
        Party customer = TestHelper.createCustomer("Foo", "Bar", false);
        getArchetypeService().deriveValues(customer);
        IMObjectBean bean = new IMObjectBean(customer);
        Lookup mr = TestHelper.getLookup("lookup.personTitle", "MR");
        bean.setValue("title", mr.getCode());
        email = TestHelper.createEmailContact("foo@bar.com");
        customer.addContact(email);
    }

    /**
     * Tests the {@link AddressFormatter#getAddress(Contact)} method.
     */
    @Test
    public void testGetAddress() {
        assertNull(formatter.getAddress(null));
        assertEquals("foo@bar.com", formatter.getAddress(email));
    }

    /**
     * Tests the {@link AddressFormatter#getName(Contact)} method.
     */
    @Test
    public void testGetName() {
        assertEquals("Bar,Foo", formatter.getName(email));

        email.setName("Foo Bar");
        assertEquals("Foo Bar", formatter.getName(email));
    }

    /**
     * Tests the {@link AddressFormatter#getQualifiedName(Contact)} method.
     */
    @Test
    public void testGetQualifiedName() {
        assertEquals("Bar,Foo", formatter.getQualifiedName(email));

        email.setName("Foo Bar");
        assertEquals("Foo Bar (Bar,Foo)", formatter.getQualifiedName(email));
    }

    /**
     * Tests the {@link AddressFormatter#getNameAddress(Contact, boolean)} method.
     */
    @Test
    public void testGetNameAddress() {
        assertEquals("Bar,Foo <foo@bar.com>", formatter.getNameAddress(email, false));
        assertEquals("\"Bar,Foo\" <foo@bar.com>", formatter.getNameAddress(email, true));

        email.setName("Foo Bar");
        assertEquals("Foo Bar <foo@bar.com>", formatter.getNameAddress(email, false));
        assertEquals("\"Foo Bar\" <foo@bar.com>", formatter.getNameAddress(email, true));
    }

    /**
     * Tests the {@link AddressFormatter#format(Contact)} method.
     */
    @Test
    public void testFormat() {
        assertEquals("Bar,Foo <foo@bar.com> - Customer", formatter.format(email));

        email.setName("Foo Bar");
        assertEquals("Foo Bar (Bar,Foo) <foo@bar.com> - Customer", formatter.format(email));
    }

    /**
     * Tests the {@link AddressFormatter#getType(Contact)} method.
     */
    @Test
    public void testGetType() {
        assertEquals("Customer", formatter.getType(email));
    }

    /**
     * Creates a new address formatter.
     *
     * @return a new address formatter
     */
    protected abstract AddressFormatter createAddressFormatter();

}
