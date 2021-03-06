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

package org.openvpms.web.component.im.edit;

import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * {@link DefaultCollectionPropertyEditor} test case.
 *
 * @author Tim Anderson
 */
public class DefaultCollectionPropertyEditorTestCase extends AbstractCollectionPropertyEditorTest {

    /**
     * Tests {@link CollectionPropertyEditor#getArchetypeRange()}.
     */
    @Test
    public void testGetArchetypeRange() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        CollectionPropertyEditor editor = createEditor(property, parent);
        String[] range = editor.getArchetypeRange();
        assertEquals(4, range.length);
        Set<String> set = new HashSet<>(Arrays.asList(range));
        assertTrue(set.contains(ContactArchetypes.LOCATION));
        assertTrue(set.contains(ContactArchetypes.PHONE));
        assertTrue(set.contains(ContactArchetypes.EMAIL));
        assertTrue(set.contains(ContactArchetypes.WEBSITE));
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    protected IMObject createParent() {
        Party party = (Party) create("party.customerperson");
        // remove default contacts
        for (Contact contact : party.getContacts().toArray(new Contact[party.getContacts().size()])) {
            party.removeContact(contact);
        }
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", "foo");
        bean.setValue("lastName", "xyz");
        return party;
    }

    /**
     * Returns the name of the collection node.
     *
     * @return the node name
     */
    protected String getCollectionNode() {
        return "contacts";
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param parent   the parent of the collection
     * @return a new editor for the property
     */
    protected CollectionPropertyEditor createEditor(
            CollectionProperty property, IMObject parent) {
        return new DefaultCollectionPropertyEditor(property);
    }

    /**
     * Returns an object to add to the collection.
     *
     * @param parent the parent of the collection
     * @return a new object to add to the collection
     */
    protected IMObject createObject(IMObject parent) {
        return create(ContactArchetypes.LOCATION);
    }

    /**
     * Makes an object valid or invalid.
     *
     * @param object the object
     * @param valid  if {@code true}, make it valid, otherwise make it invalid
     */
    @Override
    protected void makeValid(IMObject object, boolean valid) {
        IMObjectBean bean = new IMObjectBean(object);
        bean.setValue("startDate", valid ? new Date() : null);
    }
}
