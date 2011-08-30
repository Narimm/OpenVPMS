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

package org.openvpms.web.component.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Tests the {@link SimpleProperty} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SimplePropertyTestCase extends AbstractPropertyTest {

    /**
     * Tests the {@link Property#getDescriptor()} method.
     */
    @Test
    public void testGetDescriptor() {
        Property property = createStringProperty("Foo");
        assertNull(property.getDescriptor());
    }

    /**
     * Tests the {@link Property#getDisplayName()} method.
     */
    public void testDisplayName() {
        SimpleProperty property = new SimpleProperty("foo", String.class);
        assertEquals("Foo", property.getDisplayName());
        property.setDisplayName("bar");
        assertEquals("bar", property.getDisplayName());
    }

    /**
     * Tests the {@link Property#getDescription()} method.
     */
    @Test
    public void testDescription() {
        SimpleProperty property = new SimpleProperty("foo", String.class);
        assertNull(property.getDescription());
        property.setDescription("bar");
        assertEquals("bar", property.getDescription());
    }

    /**
     * Tests the {@link Property#isLookup()} method.
     */
    public void testIsLookup() {
        SimpleProperty property = new SimpleProperty("foo", String.class);
        assertFalse(property.isLookup());      // unsupported
    }

    /**
     * Tests the {@link Property#isCollection()} method.
     */
    public void testIsCollection() {
        SimpleProperty property = new SimpleProperty("foo", Map.class);
        assertFalse(property.isCollection());      // unsupported
    }

    /**
     * Tests the {@link Property#getArchetypeRange()} method.
     */
    public void testGetArchetypeRange() {
        SimpleProperty property = new SimpleProperty("foo", String.class);
        assertEquals(0, property.getArchetypeRange().length);

        property.setArchetypeRange(new String[]{PatientArchetypes.PATIENT});
        assertEquals(1, property.getArchetypeRange().length);
        assertEquals(PatientArchetypes.PATIENT, property.getArchetypeRange()[0]);

        property.setArchetypeRange(new String[]{"act.customerAccountCharges*"});
        List<String> values = Arrays.asList(property.getArchetypeRange());
        assertEquals(3, values.size());
        assertTrue(values.contains(CustomerAccountArchetypes.INVOICE));
        assertTrue(values.contains(CustomerAccountArchetypes.CREDIT));
        assertTrue(values.contains(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests the {@link Property#isDerived()} method.
     */
    public void testIsDerived() {
        SimpleProperty property = new SimpleProperty("foo", String.class);
        assertFalse(property.isDerived());  // unsupported
    }

    /**
     * Tests the {@link Property#isReadOnly()} method.
     */
    public void testIsReadOnly() {
        SimpleProperty property = new SimpleProperty("foo", String.class);

        assertFalse(property.isReadOnly());
        property.setReadOnly(true);
        assertTrue(property.isReadOnly());
    }

    /**
     * Tests the {@link Property#isHidden()} method.
     */
    @Test
    public void testIsHidden() {
        SimpleProperty property = new SimpleProperty("foo", String.class);

        assertFalse(property.isHidden());
        property.setHidden(true);
        assertTrue(property.isHidden());
    }

    /**
     * Tests the {@link Property#isRequired()} method.
     */
    @Test
    public void testIsRequired() {
        SimpleProperty property = new SimpleProperty("foo", String.class);

        assertFalse(property.isRequired());
        property.setRequired(true);
        assertTrue(property.isRequired());
    }

    /**
     * Verifies a validation error matches that expected.
     *
     * @param property the property to check
     * @param message  the expected validation error message
     */
    @Override
    protected void checkValidationError(Property property, String message) {
        String expected = ValidatorError.format(property.getDisplayName(), message);
        super.checkValidationError(property, expected);
    }

    /**
     * Creates a boolean property.
     *
     * @param name the property name
     * @return a new boolean property
     */
    protected Property createBooleanProperty(String name) {
        return new SimpleProperty(name, Boolean.class);
    }

    /**
     * Creates a string property.
     *
     * @param name      the property name
     * @param minLength the minimum length
     * @param maxLength the maximum length
     * @return a new string property
     */
    protected Property createStringProperty(String name, int minLength, int maxLength) {
        SimpleProperty result = new SimpleProperty(name, String.class);
        result.setMinLength(minLength);
        result.setMaxLength(maxLength);
        return result;
    }

    /**
     * Creates a property of the specified type.
     *
     * @param name the property name
     * @param type the property type
     * @return a new property
     */
    protected Property createProperty(String name, Class type) {
        return new SimpleProperty(name, type);
    }

    /**
     * Creates a string property.
     *
     * @param minLength the minimum length
     * @param maxLength the maximum length
     * @return a new property
     */
    protected TestProperty createTestProperty(int minLength, int maxLength) {
        SimpleTestProperty result = new SimpleTestProperty("foo", String.class);
        result.setMinLength(minLength);
        result.setMaxLength(maxLength);
        return result;
    }

    private class SimpleTestProperty extends SimpleProperty implements TestProperty {

        /**
         * The count of validations.
         */
        private int count = 0;

        /**
         * Constructs a <tt>SimpleProperty</tt>.
         *
         * @param name the property name
         * @param type the property type
         */
        public SimpleTestProperty(String name, Class type) {
            super(name, type);
        }

        /**
         * Validates the object.
         *
         * @param validator the validator
         * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
         */
        @Override
        protected boolean doValidation(Validator validator) {
            ++count;
            return super.doValidation(validator);
        }

        /**
         * Returns the no. of times validation has been invoked.
         *
         * @return the no. of times validation has been invoked
         */
        public int getValidations() {
            return count;
        }
    }
}
