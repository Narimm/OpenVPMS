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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.bound;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link BoundAbsoluteTimeField}.
 *
 * @author Tim Anderson
 */
public class BoundAbsoluteTimeFieldTestCase extends AbstractBoundFieldTest<BoundAbsoluteTimeField, Date> {

    /**
     * The first test value.
     */
    private static final Date value1 = Time.valueOf("09:00:00");

    /**
     * The second test value.
     */
    private static final Date value2 = Time.valueOf("12:00:00");

    /**
     * Default constructor.
     */
    public BoundAbsoluteTimeFieldTestCase() {
        super(value1, value2);
    }

    /**
     * Verifies that times are restricted to a range.
     */
    @Test
    public void testTimeRange() {
        Property property1 = createProperty();
        BoundAbsoluteTimeField field1 = new BoundAbsoluteTimeField(property1);
        field1.setText("00:00");
        assertTrue(property1.isValid());
        assertEquals(Time.valueOf("00:00:00"), property1.getDate());
        field1.setText("24:00");
        checkError(property1, "Time must be < than the maximum time 24:00");

        field1.setText("23:59");
        assertTrue(property1.isValid());
        assertEquals(Time.valueOf("23:59:00"), property1.getDate());

        // make sure midnight can be included as an upper bound
        Property property2 = createProperty();
        BoundAbsoluteTimeField field2 = new BoundAbsoluteTimeField(property2, false);
        field2.setText("24:00");
        assertTrue(property2.isValid());
        assertEquals(Time.valueOf("24:00:00"), property2.getDate());
    }

    /**
     * Verifies a property has the expected error.
     *
     * @param property the property
     * @param error    the expected error
     */
    private void checkError(Property property, String error) {
        DefaultValidator validator = new DefaultValidator();
        assertFalse(property.validate(validator));
        List<ValidatorError> errors = validator.getErrors(property);
        assertEquals(1, errors.size());
        assertEquals(error, errors.get(0).getMessage());
    }

    /**
     * Returns the value of the field.
     *
     * @param field the field
     * @return the value of the field
     */
    @Override
    protected Date getValue(BoundAbsoluteTimeField field) {
        String text = field.getText();
        return (!StringUtils.isEmpty(text)) ? Time.valueOf(text + ":00") : null;
    }

    /**
     * Sets the value of the field.
     *
     * @param field the field
     * @param value the value to set
     */
    @Override
    protected void setValue(BoundAbsoluteTimeField field, Date value) {
        field.setText(DateFormatter.formatTime(value, true));
    }

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    @Override
    protected BoundAbsoluteTimeField createField(Property property) {
        return new BoundAbsoluteTimeField(property);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    @Override
    protected Property createProperty() {
        return new SimpleProperty("time", Date.class);
    }
}
