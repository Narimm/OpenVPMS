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

import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.test.AbstractAppTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link MultiContactSelector}.
 *
 * @author Tim Anderson
 */
public class MultiContactSelectorTestCase extends AbstractAppTest {

    /**
     * The selector.
     */
    private MultiContactSelector selector;


    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        selector = new MultiContactSelector(new ToAddressFormatter(), context);
    }

    /**
     * Verifies that multiple email addresses can be entered.
     */
    @Test
    public void testAddresses() {
        assertEquals(DescriptorHelper.getDisplayName(ContactArchetypes.EMAIL), selector.getType());
        assertTrue(selector.isValid());
        assertTrue(selector.getObjects().isEmpty());
        assertEquals("", selector.getText());

        TextField field = selector.getTextField();
        field.setText("foo@bar.com");
        assertTrue(selector.isValid());
        List<Contact> objects = selector.getObjects();
        assertEquals(1, objects.size());
        checkEmail(objects.get(0), "Email Address", "foo@bar.com");

        field.setText("foo@bar.com; jsmith@gmail.com");
        assertTrue(selector.isValid());
        objects = selector.getObjects();
        assertEquals(2, objects.size());
        checkEmail(objects.get(0), "Email Address", "foo@bar.com");
        checkEmail(objects.get(1), "Email Address", "jsmith@gmail.com");
    }

    /**
     * Verifies that addresses can be entered that have a name.
     */
    @Test
    public void testNamedAddresses() {
        TextField field = selector.getTextField();
        field.setText("Foo Bar <foo@bar.com>");
        assertTrue(selector.isValid());
        List<Contact> objects = selector.getObjects();
        assertEquals(1, objects.size());
        checkEmail(objects.get(0), "Foo Bar", "foo@bar.com");

        field.setText("Foo Bar <foo@bar.com>; J Smith <jsmith@gmail.com>");
        assertTrue(selector.isValid());
        objects = selector.getObjects();
        assertEquals(2, objects.size());
        checkEmail(objects.get(0), "Foo Bar", "foo@bar.com");
        checkEmail(objects.get(1), "J Smith", "jsmith@gmail.com");
    }

    /**
     * Verifies that if just a name is entered, a query is used to resolve the email contact.
     */
    @Test
    public void testQuery() {
        Party customer1 = TestHelper.createCustomer("Foo", "Bar", false);
        final Contact contact1 = TestHelper.createEmailContact("foo@bar.com");
        customer1.addContact(contact1);

        Party customer2 = TestHelper.createCustomer("J", "Smith", false);
        final Contact contact2 = TestHelper.createEmailContact("jsmith@gmail.com");
        contact2.setName("J Smith");
        customer2.addContact(contact2);

        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        selector = new MultiContactSelector(new ToAddressFormatter(), context) {
            @Override
            protected Query<Contact> createQuery(String value) {
                List<Contact> contacts = new ArrayList<Contact>();
                if ("Foo Bar".equals(value)) {
                    contacts.add(contact1);
                } else if ("J Smith".equals(value)) {
                    contacts.add(contact2);
                }
                return new ListQuery<Contact>(contacts, ContactArchetypes.EMAIL, false, Contact.class);
            }
        };

        TextField field = selector.getTextField();
        field.setText("Foo Bar");
        field.processInput(TextComponent.INPUT_ACTION, null);  // simulate an enter - this will run the query
        assertTrue(selector.isValid());
        assertEquals("foo@bar.com; ", field.getText());

        List<Contact> objects = selector.getObjects();
        assertEquals(1, objects.size());
        checkEmail(objects.get(0), "Email Address", "foo@bar.com");

        // verify that multiple contacts can be queried
        field.setText("Foo Bar; J Smith");
        field.processInput(TextComponent.INPUT_ACTION, null);
        assertTrue(selector.isValid());
        assertEquals("foo@bar.com; J Smith <jsmith@gmail.com>; ", field.getText());

        objects = selector.getObjects();
        assertEquals(2, objects.size());
        checkEmail(objects.get(0), "Email Address", "foo@bar.com");
        checkEmail(objects.get(1), "J Smith", "jsmith@gmail.com");
    }

    /**
     * Verifies an email contact matches that expected.
     *
     * @param contact the contact
     * @param name    the email name
     * @param email   the email address
     */
    private void checkEmail(Contact contact, String name, String email) {
        assertNotNull(contact);
        assertEquals(name, contact.getName());
        IMObjectBean bean = new IMObjectBean(contact);
        assertEquals(email, bean.getString("emailAddress"));
    }
}
