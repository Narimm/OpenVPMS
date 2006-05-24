/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.test.TestHelper;


/**
 * {@link DefaultCollectionPropertyEditor} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultCollectionPropertyEditorTestCase
        extends AbstractCollectionPropertyEditorTest {

    /**
     * Tests {@link CollectionPropertyEditor#getArchetypeRange()}.
     */
    public void testGetArchetypeRange() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        CollectionPropertyEditor editor = createEditor(property, parent);
        String[] range = editor.getArchetypeRange();
        assertEquals(4, range.length);
        assertEquals("contact.location", range[0]);
        assertEquals("contact.phoneNumber", range[1]);
        assertEquals("contact.email", range[2]);
        assertEquals("contact.faxNumber", range[3]);
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    protected IMObject createParent() {
        return TestHelper.createCustomer();
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
     * @param parent thhe parent of the collection
     * @return a new editor for the property
     */
    protected CollectionPropertyEditor createEditor(
            CollectionProperty property, IMObject parent) {
        return new DefaultCollectionPropertyEditor(property);
    }

    /**
     * Returns an object to add to the collection.
     *
     * @return a new object to add to the collection
     */
    protected IMObject createObject() {
        return TestHelper.create("contact.location");
    }

}
