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

import nextapp.echo2.app.TextField;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Tests the {@link org.openvpms.web.component.bound.BoundTextField} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundFormattedFieldTestCase extends AbstractBoundTextComponentTest {

    /**
     * Test the field using a date format.
     */
    private static final DateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructs an <tt>BoundFormattedFieldTestCase</tt>.
     */
    public BoundFormattedFieldTestCase() {
        super("01/01/2009", "31/01/2010");
    }

    /**
     * Returns the value of the property.
     *
     * @param property the property
     * @return the value of the property
     */
    @Override
    protected String getValue(Property property) {
        Date date = (Date) property.getValue();
        return FORMAT.format(date);
    }

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected TextField createField(Property property) {
        return new BoundFormattedField(property, 10, FORMAT);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    @Override
    protected Property createProperty() {
        return new SimpleProperty("date", Date.class);
    }
}