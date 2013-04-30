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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.edit;

import nextapp.echo2.app.Component;
import org.junit.Test;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link Editors} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class EditorsTestCase {

    /**
     * Tests the {@link Editors#isModified()} and {@link Editors#clearModified()} methods.
     */
    @Test
    public void testModified() {
        SimpleProperty property1 = new SimpleProperty("p1", String.class);
        SimpleProperty property2 = new SimpleProperty("p2", String.class);
        PropertySet set = new PropertySet(property1, property2);
        ModifiableListeners listeners = new ModifiableListeners();
        Editors editors = new Editors(set, listeners);

        assertFalse(editors.isModified());
        property1.setValue("foo");

        assertTrue(editors.isModified());
        assertTrue(property1.isModified());
        assertFalse(property2.isModified());

        editors.clearModified();
        assertFalse(editors.isModified());
        assertFalse(property1.isModified());
        assertFalse(property2.isModified());
    }

    /**
     * Verifies that when an editor is registered for a property, only a single event is generated when that property
     * changes.
     */
    @Test
    public void testPropertyListeners() {
        SimpleProperty property = new SimpleProperty("property", String.class);

        PropertySet set = new PropertySet(property);
        ModifiableListeners listeners = new ModifiableListeners();
        Editors editors = new Editors(set, listeners);

        CountingListener editorsListener = new CountingListener();
        editors.addModifiableListener(editorsListener);

        property.setValue("foo");
        assertEquals(1, editorsListener.getCount());

        // register an editor for the property, and verify that a subsequent update to the property still only triggers
        // one event
        Editor editor = new SimplePropertyEditor(property);
        editors.add(editor);
        property.setValue("bar");
        assertEquals(2, editorsListener.getCount());

        // remove the editor and verify that another update generates a single event
        editors.remove(editor);

        property.setValue("foo");
        assertEquals(3, editorsListener.getCount());
    }


    private static class CountingListener implements ModifiableListener {

        /**
         * The invocation count.
         */
        private int count;

        /**
         * Returns the invocation count.
         *
         * @return the invocation count
         */
        public int getCount() {
            return count;
        }

        /**
         * Invoked when a {@link Modifiable} changes.
         *
         * @param modifiable the modifiable
         */
        public void modified(Modifiable modifiable) {
            ++count;
        }
    }

    private static class SimplePropertyEditor extends AbstractPropertyEditor {
        public SimplePropertyEditor(SimpleProperty property1) {
            super(property1);
        }

        /**
         * Returns the component.
         *
         * @return <tt>null</tt>
         */
        public Component getComponent() {
            return null;
        }

        /**
         * Returns the focus group.
         *
         * @return <tt>null</tt>
         */
        public FocusGroup getFocusGroup() {
            return null;
        }

    }
}
