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

import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
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

    /**
     * Verifies that a new contact is created after an new one is created, modified and the collection saved.
     * This tests the fix for OVPMS-1846.
     */
    @Test
    public void testExcludeUnmodifiedContactsWithSave() {
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");

        PropertySet set = new PropertySet(customer);
        CollectionProperty property = (CollectionProperty) set.get("contacts");
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        ContactCollectionEditor editor = new ContactCollectionEditor(property, customer, context);
        editor.setExcludeUnmodifiedContacts(true);
        editor.getComponent();

        IMObjectEditor contactEditor1 = editor.add(ContactArchetypes.LOCATION);
        assertNotNull(contactEditor1);

        // verify contact not added until it changes
        Contact location1 = (Contact) contactEditor1.getObject();
        assertFalse(customer.getContacts().contains(location1));

        // verifies that the same location is returned, as location1 hasn't been modified
        IMObjectEditor contactEditor2 = editor.add(ContactArchetypes.LOCATION);
        assertNotNull(contactEditor2);
        assertSame(contactEditor1, contactEditor2);

        // now change the contact, and verify it is added to the collection
        contactEditor1.getProperty("address").setValue("Test");
        assertTrue(editor.isValid());
        editor.save();
        assertTrue(customer.getContacts().contains(location1));

        // verifies a new location is returned
        IMObjectEditor contactEditor3 = editor.add(ContactArchetypes.LOCATION);
        assertNotNull(contactEditor3);
        assertNotSame(contactEditor1, contactEditor3);
    }

    /**
     * Verifies that if an existing contact is preferred, subsequent contacts of the same type aren't.
     */
    @Test
    public void testExistingPreferred() {
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

        checkPreferred(phone1, true);
        IMObjectEditor phone2Editor = editor.add(ContactArchetypes.PHONE);
        assertNotNull(phone2Editor);
        checkPreferred(phone2Editor.getObject(), false);

        IMObjectEditor locationEditor = editor.add(ContactArchetypes.LOCATION);
        assertNotNull(locationEditor);
        locationEditor.getProperty("address").setValue("123 Foo St");
        checkPreferred(locationEditor.getObject(), true);

        // make sure phone1 still preferred
        save(customer);
        phone1 = get(phone1);
        checkPreferred(phone1, true);
    }

    /**
     * Verifies that setting a contact to preferred turns off the preferred flag in other contacts of the same type.
     */
    @Test
    public void testSetPreferredTurnsOffExistingPreferred() {
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

        checkPreferred(phone1, true);

        // add a location, and verify it is preferred
        IMObjectEditor locationEditor = editor.add(ContactArchetypes.LOCATION);
        assertNotNull(locationEditor);
        locationEditor.getProperty("address").setValue("123 Bar St");
        checkPreferred(locationEditor.getObject(), true);

        // phone1 should still be preferred
        checkPreferred(phone1, true);

        // add another phone phone
        IMObjectEditor phone2Editor = editor.add(ContactArchetypes.PHONE);
        assertNotNull(phone2Editor);
        checkPreferred(phone2Editor.getObject(), false);

        // make it preferred. The phone1 contact should no longer be preferred.
        phone2Editor.getProperty("preferred").setValue(true);
        checkPreferred(phone2Editor.getObject(), true);
        checkPreferred(phone1, false);

        // location contact should still be preferred
        checkPreferred(locationEditor.getObject(), true);
    }

    /**
     * Checks that a contact's preferred flag matches that expected.
     *
     * @param contact   the contact
     * @param preferred the expected value of the preferred flag
     */
    private void checkPreferred(IMObject contact, boolean preferred) {
        IMObjectBean bean = new IMObjectBean(contact);
        assertEquals(preferred, bean.getBoolean("preferred"));
    }
}
