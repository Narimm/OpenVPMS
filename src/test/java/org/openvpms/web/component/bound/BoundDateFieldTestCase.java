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

import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;

import java.util.Date;
import java.util.Calendar;


/**
 * Tests the {@link BoundDateField} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundDateFieldTestCase extends AbstractBoundFieldTest<BoundDateField, Date> {

    /**
     * The first test value.
     */
    private static final Date value1 = java.sql.Date.valueOf("2009-01-01");

    /**
     * The second test value.
     */
    private static final Date value2 = java.sql.Date.valueOf("2010-12-31");

    /**
     * Constructs a <tt>BoundDateField</tt>.
     */
    public BoundDateFieldTestCase() {
        super(value1, value2);
    }

    /**
     * Returns the value of the field.
     *
     * @param field the field
     * @return the value of the field
     */
    protected Date getValue(BoundDateField field) {
        return field.getSelectedDate().getTime();
    }

    /**
     * Sets the value of the field.
     *
     * @param field the field
     * @param value the value to set
     */
    protected void setValue(BoundDateField field, Date value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(value);
        field.setSelectedDate(calendar);
    }

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected BoundDateField createField(Property property) {
        return new BoundDateField(property);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    protected Property createProperty() {
        return new SimpleProperty("date", Date.class);
    }
}