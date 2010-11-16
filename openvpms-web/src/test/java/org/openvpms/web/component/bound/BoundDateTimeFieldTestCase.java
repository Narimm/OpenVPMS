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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.bound;

import nextapp.echo2.app.Component;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;

import java.sql.Timestamp;
import java.util.Date;


/**
 * Tests the {@link BoundDateTimeField} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundDateTimeFieldTestCase extends AbstractBoundFieldTest<BoundDateTimeField, Date> {

    /**
     * The first test value.
     */
    private static final Date value1 = new Date(Timestamp.valueOf("2009-01-01 10:30:00").getTime());

    /**
     * The second test value.
     */
    private static final Date value2 = new Date(Timestamp.valueOf("2010-12-31 23:59:00").getTime());

    /**
     * Constructs a <tt>BoundDateField</tt>.
     */
    public BoundDateTimeFieldTestCase() {
        super(value1, value2);
    }

    /**
     * Verifies that the date and time can be updated independently.
     */
    @Test
    public void testSetDateAndTime() {
        Property property = createProperty();
        BoundDateTimeField field = createField(property);
        field.setDate(java.sql.Date.valueOf("2010-12-31"));
        field.getTimeField().setText("10:30");

        Date expected1 = new Date(Timestamp.valueOf("2010-12-31 10:30:00").getTime());
        assertEquals(expected1, field.getProperty().getValue());

        field.setDate(java.sql.Date.valueOf("2010-10-11"));
        Date expected2 = new Date(Timestamp.valueOf("2010-10-11 10:30:00").getTime());
        assertEquals(expected2, field.getProperty().getValue());

        field.getTimeField().setText("11:30");
        Date expected3 = new Date(Timestamp.valueOf("2010-10-11 11:30:00").getTime());
        assertEquals(expected3, field.getProperty().getValue());
    }

    /**
     * Returns the value of the field.
     *
     * @param field the field
     * @return the value of the field
     */
    protected Date getValue(BoundDateTimeField field) {
        return field.getDatetime();
    }

    /**
     * Sets the value of the field.
     *
     * @param field the field
     * @param value the value to set
     */
    protected void setValue(BoundDateTimeField field, Date value) {
        field.setDatetime(value);
    }

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected BoundDateTimeField createField(Property property) {
        return new BoundDateTimeField(property);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    protected Property createProperty() {
        return new SimpleProperty("datetime", Date.class);
    }

    /**
     * Adds a component to the container.
     *
     * @param container the container
     * @param field     the field to add
     */
    @Override
    protected void addComponent(Component container, BoundDateTimeField field) {
        container.add(field.getComponent());
    }

    /**
     * Removes a component from the container.
     *
     * @param container the container
     * @param field     thhe field to remove
     */
    @Override
    protected void removeComponent(Component container, BoundDateTimeField field) {
        container.remove(field.getComponent());
    }
}