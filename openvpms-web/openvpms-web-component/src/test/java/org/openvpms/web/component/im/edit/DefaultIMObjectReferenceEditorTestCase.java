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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.test.AbstractAppTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DefaultIMObjectReferenceEditor}.
 *
 * @author Tim Anderson
 */
public class DefaultIMObjectReferenceEditorTestCase extends AbstractAppTest {

    /**
     * The property being edited.
     */
    private Property property;

    /**
     * The parent object
     */
    private IMObject parent;

    /**
     * Tracks the number of times the property is modified.
     */
    private int modifiedCount;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        parent = create(PatientArchetypes.PATIENT_OWNER);
        property = new IMObjectProperty(parent, new IMObjectBean(parent).getDescriptor("target"));
        property.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                ++modifiedCount;
            }
        });
        super.setUp();
    }

    /**
     * Tests behaviour when the text field is populated with text for which there is only one match.
     */
    @Test
    public void testSetTextForOneMatch() {
        TestReferenceEditor editor = createEditor();
        Party patient = createPatient("Fido");

        editor.setQueryObjects(patient);
        TextField textField = editor.getSelector().getTextField();

        // check the preconditions
        assertTrue(editor.isNull());
        assertFalse(editor.isModified());
        assertEquals(0, editor.getOnUpdated());
        assertNull(property.getReference());
        assertEquals(0, editor.getOnUpdated());
        assertEquals(0, modifiedCount);
        assertFalse(editor.isValid());

        // now set the text field with a partial name. It should resolve to the patient.
        textField.setText("Fi");

        // check the post conditions
        assertEquals("Fido", textField.getText());
        assertFalse(editor.isNull());
        assertTrue(editor.isModified());
        assertEquals(patient.getObjectReference(), property.getReference());
        assertEquals(1, modifiedCount);
        assertTrue(editor.isValid());

        // now clear the text field. The property should be set to null and be invalid
        textField.setText(null);

        // check the post conditions
        assertTrue(editor.isNull());
        assertTrue(editor.isModified());
        assertNull(property.getReference());
        assertEquals(2, modifiedCount);
        assertFalse(editor.isValid());
    }

    /**
     * Tests behaviour when the text field is populated with text for which there are two matches.
     */
    @Test
    public void testSetTextForTwoMatches() {
        TestReferenceEditor editor = createEditor();
        Party patient1 = createPatient("Fido");
        Party patient2 = createPatient("Fifi");

        editor.setQueryObjects(patient1, patient2);
        editor.setSelection(patient2); // select patient2 when the browser is displayed
        TextField textField = editor.getSelector().getTextField();

        textField.setText("Fi");

        // check the post conditions
        assertEquals("Fifi", textField.getText());
        assertFalse(editor.isNull());
        assertTrue(editor.isModified());
        assertEquals(patient2.getObjectReference(), property.getReference());
        assertEquals(1, editor.getOnUpdated());
        assertEquals(1, modifiedCount);
        assertTrue(editor.isValid());

        // now enter a partial match, this time selecting patient1
        editor.setSelection(patient1);
        textField.setText("Fi");

        // check the post conditions
        assertEquals("Fido", textField.getText());
        assertFalse(editor.isNull());
        assertTrue(editor.isModified());
        assertEquals(patient1.getObjectReference(), property.getReference());
        assertEquals(2, editor.getOnUpdated());
        assertEquals(2, modifiedCount);
        assertTrue(editor.isValid());
    }

    /**
     * Tests updating the property from within the modification callback.
     * <p>
     * This happens when replacing a template product with a medication product for example.
     */
    @Test
    public void testUpdatePropertyFromWithinCallback() {
        TestReferenceEditor editor = createEditor();
        final Party patient1 = createPatient("Fido");
        final Party patient2 = createPatient("Fifi");

        editor.setQueryObjects(patient1, patient2);
        TextField textField = editor.getSelector().getTextField();

        property.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                // NOTE: this listener will be invoked twice
                property.setValue(patient2.getObjectReference());
            }
        });

        // set patient1. It should be replaced by patient2
        textField.setText("Fido");

        // check the post conditions
        assertEquals("Fifi", textField.getText());
        assertFalse(editor.isNull());
        assertTrue(editor.isModified());
        assertEquals(patient2.getObjectReference(), property.getReference());
        assertEquals(2, editor.getOnUpdated());
        assertEquals(2, modifiedCount);
        assertTrue(editor.isValid());
    }

    /**
     * Creates a patient with the specified name.
     *
     * @param name the patient name
     * @return a new patient
     */
    private Party createPatient(String name) {
        Party patient = TestHelper.createPatient(false);
        patient.setName(name);
        save(patient);
        return patient;
    }

    /**
     * Creates a new editor.
     *
     * @return a new editor
     */
    private TestReferenceEditor createEditor() {
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        TestReferenceEditor editor = new TestReferenceEditor(property, parent, context);
        editor.getComponent();
        return editor;
    }

    private class TestReferenceEditor extends DefaultIMObjectReferenceEditor<Party> {

        private List<Party> queryObjects = new ArrayList<>();

        private Party selection;
        private int onUpdated;

        public TestReferenceEditor(Property property, IMObject parent, LayoutContext context) {
            super(property, parent, context);
        }

        public void setQueryObjects(Party... objects) {
            queryObjects = Arrays.asList(objects);
        }

        public void setSelection(Party selection) {
            this.selection = selection;
        }

        public int getOnUpdated() {
            return onUpdated;
        }

        /**
         * Returns the selector.
         *
         * @return the selector
         */
        @Override
        public IMObjectSelector<Party> getSelector() {
            return super.getSelector();
        }

        /**
         * Creates a new selector.
         *
         * @param property    the property
         * @param context     the layout context
         * @param allowCreate determines if objects may be created
         * @return a new selector
         */
        @Override
        protected IMObjectSelector<Party> createSelector(Property property, LayoutContext context, boolean allowCreate) {
            return new IMObjectSelector<Party>(property, allowCreate, context) {
                @Override
                protected Query<Party> createQuery(String name) {
                    return TestReferenceEditor.this.createQuery(name);
                }

                @Override
                protected Browser<Party> createBrowser(Query<Party> query) {
                    return TestReferenceEditor.this.createBrowser(query);
                }

                @Override
                protected void onSelect(Query<Party> query, boolean runQuery) {
                    assertNotNull("selection cannot be null", selection);
                    Browser<Party> browser = createBrowser(query);
                    onSelected(selection, browser);
                }
            };
        }

        @Override
        protected Query<Party> createQuery(String name) {
            List<Party> objects = new ArrayList<>();
            if (name == null) {
                objects.addAll(queryObjects);
            } else {
                for (Party object : queryObjects) {
                    if (object.getName().startsWith(name)) {
                        objects.add(object);
                    }
                }
            }
            return new ListQuery<>(objects, PatientArchetypes.PATIENT, Party.class);
        }

        /**
         * Invoked when the underlying property updates.
         *
         * @param object the updated object. May be {@code null}
         */
        @Override
        protected void onUpdated(Party object) {
            ++onUpdated;
        }
    }
}
