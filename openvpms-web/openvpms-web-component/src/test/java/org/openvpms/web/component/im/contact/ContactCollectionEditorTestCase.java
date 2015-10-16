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

import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ContactCollectionEditor}.
 *
 * @author Tim Anderson
 */
public class ContactCollectionEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that if {@link ContactCollectionEditor#setExcludeUnmodifiedContacts(boolean)} is {@code true},
     * new, unmodified contacts are excluded.
     */
    @Test
    public void testExcludeUnmodifiedContacts() {
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");
        Contact phone1 = TestHelper.createPhoneContact("03", "987654321");
        customer.addContact(phone1);
        save(customer);

        PropertySet set = new PropertySet(customer);
        CollectionProperty property = (CollectionProperty) set.get("contacts");
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        ContactCollectionEditor editor = new ContactCollectionEditor(property, customer, context);
        editor.setExcludeUnmodifiedContacts(true);
        editor.getComponent();

        Contact phone2 = (Contact) create(ContactArchetypes.PHONE);
        Contact location = (Contact) create(ContactArchetypes.LOCATION);
        Contact email = (Contact) create(ContactArchetypes.EMAIL);
        editor.add(phone2);
        editor.add(location);
        editor.add(email);

        assertTrue(customer.getContacts().contains(phone1));
        assertFalse(customer.getContacts().contains(phone2));
        assertFalse(customer.getContacts().contains(location));
        assertFalse(customer.getContacts().contains(email));

        assertTrue(editor.getCurrentObjects().contains(phone1));
        assertTrue(editor.getCurrentObjects().contains(phone2));
        assertTrue(editor.getCurrentObjects().contains(location));
        assertTrue(editor.getCurrentObjects().contains(email));

        IMObjectEditor phoneEditor = editor.getEditor(phone2);
        assertNotNull(phoneEditor);
        phoneEditor.getProperty("telephoneNumber").setValue("12345678");

        assertFalse(customer.getContacts().contains(phone2));
        assertTrue(editor.isValid());                             // validation triggers the addition
        assertTrue(customer.getContacts().contains(phone2));

        assertTrue(customer.getContacts().contains(phone1));
        assertFalse(customer.getContacts().contains(location));
        assertFalse(customer.getContacts().contains(email));

        editor.remove(email);
        assertFalse(editor.getCurrentObjects().contains(email));
        assertTrue(editor.getCurrentObjects().contains(phone1));
        assertTrue(editor.getCurrentObjects().contains(phone2));
        assertTrue(editor.getCurrentObjects().contains(location));

        editor.remove(phone2);
        assertFalse(editor.getCurrentObjects().contains(email));
        assertTrue(editor.getCurrentObjects().contains(phone1));
        assertFalse(editor.getCurrentObjects().contains(phone2));
        assertTrue(editor.getCurrentObjects().contains(location));
        assertTrue(customer.getContacts().contains(phone1));
        assertFalse(customer.getContacts().contains(phone2));
        assertFalse(customer.getContacts().contains(location));
        assertFalse(customer.getContacts().contains(email));
    }
}
