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

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Base class for {@link Property} tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractPropertyTest extends AbstractAppTest {

    /**
     * Tests the {@link Property#isValid()}, {@link Property#validate} and {@link Property#resetValid()} methods.
     */
    @Test
    public void testValidation() {
        TestProperty property = createTestProperty(1, 10);
        assertFalse(property.isValid());
        assertEquals(1, property.getValidations());

        property.setValue("Foo");
        assertTrue(property.isValid());
        assertEquals(2, property.getValidations());

        // verify validation isn't performed again as the property is valid and hasn't changed
        assertTrue(property.isValid());
        assertEquals(2, property.getValidations());

        // verify validation is performed again as the property has changed
        property.setValue(null);
        assertFalse(property.isValid());
        assertEquals(3, property.getValidations());

        // verify validation is performed again as the property is invalid
        assertFalse(property.isValid());
        assertEquals(4, property.getValidations());

        // set the property to a valid value and verify validation only performed once
        property.setValue("Bar");
        assertTrue(property.isValid());
        assertEquals(5, property.getValidations());

        assertTrue(property.isValid());
        assertEquals(5, property.getValidations());

        // now reset the valid flag to force validation
        property.resetValid();
        assertTrue(property.isValid());
        assertEquals(6, property.getValidations());

        assertTrue(property.isValid());
        assertEquals(6, property.getValidations());

        // now refresh the property to force validation
        property.refresh();
        assertTrue(property.isValid());
        assertEquals(7, property.getValidations());

        assertTrue(property.isValid());
        assertEquals(7, property.getValidations());
    }

    /**
     * Tests the {@link Property#isModified()} and {@link Property#clearModified} methods.
     */
    @Test
    public void testModified() {
        Property property = createStringProperty("Foo");

        assertFalse(property.isModified());
        property.setValue("foo");
        assertTrue(property.isModified());
        property.clearModified();
        assertFalse(property.isModified());
    }

    /**
     * Tests the {@link Property#refresh()} method.
     */
    @Test
    public void testRefresh() {
        final int[] updates = new int[1];

        Property property = createStringProperty("Foo");
        property.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updates[0]++;
            }
        });
        assertFalse(property.isModified());
        property.refresh();
        assertEquals(1, updates[0]);
        assertTrue(property.isModified());

        property.refresh();
        assertEquals(2, updates[0]);
        assertTrue(property.isModified());
    }

    /**
     * Tests the {@link Property#addModifiableListener} and {@link Property#removeModifiableListener} methods.
     */
    @Test
    public void testListener() {
        Property property = createStringProperty("Foo");

        CountingListener listener1 = new CountingListener();
        CountingListener listener2 = new CountingListener();
        property.addModifiableListener(listener1);
        property.addModifiableListener(listener2);
        property.setValue("Foo");
        assertEquals(1, listener1.getCount());
        assertEquals(1, listener2.getCount());

        // remove listener2 - should no longer receive updates
        property.removeModifiableListener(listener2);

        property.setValue("Bar");
        assertEquals(2, listener1.getCount());
        assertEquals(1, listener2.getCount());

        property.setValue("Bar"); // same value - won't update
        assertEquals(2, listener1.getCount());
        assertEquals(1, listener2.getCount());

        // remove listener1 - should no longer receive updates
        property.removeModifiableListener(listener1);
        property.setValue("Foo");
        assertEquals(2, listener1.getCount());
        assertEquals(1, listener2.getCount());
    }

    /**
     * Verifies that a listener can be removed from a property while it is being invoked.
     */
    @Test
    public void testRemoveListenerInCallback() {
        final Property property = createStringProperty("Foo");

        final ModifiableListener[] container = new ModifiableListener[1];
        CountingListener listener1 = new CountingListener() {
            public void modified(Modifiable modifiable) {
                super.modified(modifiable);
                property.removeModifiableListener(container[0]); // remove the listener
            }
        };
        container[0] = listener1;

        CountingListener listener2 = new CountingListener();

        property.addModifiableListener(listener1);
        property.addModifiableListener(listener2);
        property.setValue("Bar");
        property.setValue("Foo");
        assertEquals(1, listener1.getCount());
        assertEquals(2, listener2.getCount());
    }

    /**
     * Tests the {@link Property#getDescriptor()} method.
     */
    @Test
    public abstract void testGetDescriptor();

    /**
     * Tests the {@link Property#getName()} method.
     */
    @Test
    public void testName() {
        Property property = createStringProperty("foo");
        assertEquals("foo", property.getName());
    }

    /**
     * Tests the {@link Property#getDisplayName()} method.
     */
    @Test
    public abstract void testDisplayName();

    /**
     * Tests the {@link Property#getDescription()} method.
     */
    @Test
    public abstract void testDescription();

    /**
     * Tests the {@link Property#getMinLength()} method.
     */
    public void testGetMinLength() {
        Property property = createStringProperty("foo", 2, 25);
        assertEquals(2, property.getMinLength());
    }

    /**
     * Tests the {@link Property#getMaxLength()} method.
     */
    public void testGetMaxLength() {
        Property property = createStringProperty("foo", 2, 25);
        assertEquals(25, property.getMaxLength());
    }

    /**
     * Tests the {@link Property#isBoolean()} method.
     */
    @Test
    public void testIsBoolean() {
        Property property = createBooleanProperty("property");
        assertTrue(property.isBoolean());
        assertFalse(property.isCollection());
        assertFalse(property.isDate());
        assertFalse(property.isLookup());
        assertFalse(property.isMoney());
        assertFalse(property.isNumeric());
        assertFalse(property.isString());
        assertFalse(property.isObjectReference());

        assertEquals(Boolean.class, property.getType());
    }

    /**
     * Tests the {@link Property#isString()} method.
     */
    @Test
    public void testIsString() {
        Property property = createStringProperty("property");
        assertFalse(property.isBoolean());
        assertFalse(property.isCollection());
        assertFalse(property.isDate());
        assertFalse(property.isLookup());
        assertFalse(property.isMoney());
        assertFalse(property.isNumeric());
        assertTrue(property.isString());
        assertFalse(property.isObjectReference());

        assertEquals(String.class, property.getType());
    }

    /**
     * Tests the {@link Property#isNumeric()} method.
     */
    @Test
    public void testIsNumeric() {
        checkNumeric(Byte.class);
        checkNumeric(Short.class);
        checkNumeric(Integer.class);
        checkNumeric(Long.class);
        checkNumeric(Float.class);
        checkNumeric(Double.class);
        checkNumeric(BigDecimal.class);
        checkNumeric(BigInteger.class);
        checkNumeric(Money.class);
    }

    /**
     * Tests the {@link Property#isDate()} method.
     */
    @Test
    public void testIsDate() {
        Property property = createProperty("property", Date.class);
        assertFalse(property.isBoolean());
        assertFalse(property.isCollection());
        assertTrue(property.isDate());
        assertFalse(property.isLookup());
        assertFalse(property.isMoney());
        assertFalse(property.isNumeric());
        assertFalse(property.isString());
        assertFalse(property.isObjectReference());

        assertEquals(Date.class, property.getType());
    }

    /**
     * Tests the {@link Property#isMoney()} method.
     */
    @Test
    public void testIsMoney() {
        Property property = createProperty("property", Money.class);
        assertFalse(property.isBoolean());
        assertFalse(property.isCollection());
        assertFalse(property.isDate());
        assertFalse(property.isLookup());
        assertTrue(property.isMoney());
        assertTrue(property.isNumeric());
        assertFalse(property.isString());
        assertFalse(property.isObjectReference());

        assertEquals(Money.class, property.getType());
    }

    /**
     * Tests the {@link Property#isObjectReference()} method.
     */
    @Test
    public void testIsObjectReference() {
        Property property = createProperty("property", IMObjectReference.class);
        assertFalse(property.isBoolean());
        assertFalse(property.isCollection());
        assertFalse(property.isDate());
        assertFalse(property.isLookup());
        assertFalse(property.isMoney());
        assertFalse(property.isNumeric());
        assertFalse(property.isString());
        assertTrue(property.isObjectReference());

        assertEquals(IMObjectReference.class, property.getType());
    }

    /**
     * Tests the {@link Property#isLookup()} method.
     */
    @Test
    public abstract void testIsLookup();

    /**
     * Tests the {@link Property#isCollection()} method.
     */
    @Test
    public abstract void testIsCollection();

    /**
     * Tests the {@link Property#getArchetypeRange()} method.
     */
    @Test
    public abstract void testGetArchetypeRange();

    /**
     * Tests the {@link Property#isDerived()} method.
     */
    @Test
    public abstract void testIsDerived();

    /**
     * Tests the {@link Property#isReadOnly()} method.
     */
    @Test
    public abstract void testIsReadOnly();

    /**
     * Tests the {@link Property#isHidden()} method.
     */
    @Test
    public abstract void testIsHidden();

    /**
     * Tests the {@link Property#isRequired()} method.
     */
    @Test
    public abstract void testIsRequired();

    /**
     * Checks validation of the minimum length.
     */
    @Test
    public void testMinLengthValidation() {
        Property property = createStringProperty("foo", 2, 255);
        property.setValue("X");
        assertFalse(property.isValid());
        String error = Messages.get("property.error.minLength", property.getMinLength());
        checkValidationError(property, error);

        property.setValue("XX");
        assertTrue(property.isValid());
    }

    /**
     * Checks validation of the maximum length.
     */
    @Test
    public void testMaxLengthValidation() {
        Property property = createStringProperty("foo", 0, 30);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < property.getMaxLength() + 1; i++) {
            builder.append("x");
        }
        property.setValue(builder.toString());
        assertFalse(property.isValid());
        checkValidationError(property, Messages.get("property.error.maxLength", property.getMaxLength()));
        property.setValue("X");
        assertTrue(property.isValid());
    }

    /**
     * Verifies that when a {@link PropertyTransformer} throws an exception, the property is marked invalid.
     */
    @Test
    public void testPropertyTransfomerValidation() {
        final Property property = createStringProperty("foo");
        property.setValue("Foo");
        assertTrue(property.isValid());
        property.setTransformer(new PropertyTransformer() {
            public Object apply(Object object) {
                throw new PropertyException(property, "Invalid");
            }
        });
        property.setValue("Bar");
        checkValidationError(property, "Invalid");
    }

    /**
     * Verifies a validation error matches that expected.
     *
     * @param property the property to check
     * @param message  the expected validation error message
     */
    protected void checkValidationError(Property property, String message) {
        Validator validator = new Validator();
        assertFalse(validator.validate(property));
        List<ValidatorError> list = validator.getErrors(property);
        assertEquals(1, list.size());
        ValidatorError error = list.get(0);
        assertEquals(message, error.toString());
    }

    /**
     * Creates a boolean property.
     *
     * @param name the property name
     * @return a new boolean property
     */
    protected abstract Property createBooleanProperty(String name);

    /**
     * Creates a string property.
     *
     * @param name the property name
     * @return a new string property
     */
    protected Property createStringProperty(String name) {
        return createStringProperty(name, 0, 255);
    }

    /**
     * Creates a string property.
     *
     * @param name      the property name
     * @param minLength the minimum length
     * @param maxLength the maximum length
     * @return a new string property
     */
    protected abstract Property createStringProperty(String name, int minLength, int maxLength);

    /**
     * Creates a property of the specified type.
     *
     * @param name the property name
     * @param type the property type
     * @return a new property
     */
    protected abstract Property createProperty(String name, Class type);

    /**
     * Creates a string property.
     *
     * @param minLength the minimum length
     * @param maxLength the maximum length
     * @return a new property
     */
    protected abstract TestProperty createTestProperty(int minLength, int maxLength);

    /**
     * Tests the {@link Property#isNumeric()} method.
     *
     * @param type the numeric type
     */
    private void checkNumeric(Class type) {
        Property property = createProperty("property", type);
        assertFalse(property.isBoolean());
        assertFalse(property.isCollection());
        assertFalse(property.isDate());
        assertFalse(property.isLookup());
        assertTrue(property.isNumeric());
        assertFalse(property.isString());

        assertEquals(type, property.getType());
    }

    protected interface TestProperty extends Property {

        /**
         * Returns the no. of times validation has been invoked.
         *
         * @return the no. of times validation has been invoked
         */
        int getValidations();

    }

    protected static class CountingListener implements ModifiableListener {

        /**
         * The counter.
         */
        private int count;

        /**
         * Invoked when a {@link Modifiable} changes.
         *
         * @param modifiable the modifiable
         */
        public void modified(Modifiable modifiable) {
            count++;
        }

        /**
         * Returns the count of invocations.
         *
         * @return the count
         */
        public int getCount() {
            return count;
        }
    }

}
