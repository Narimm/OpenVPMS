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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.bound;

import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;


/**
 * Tests the {@link BoundTextField} class.
 *
 * @author Tim Anderson
 */
public class BoundTimeFieldTestCase extends AbstractBoundTextComponentTest {

    /**
     * Constructs an <tt>BoundFormattedFieldTestCase</tt>.
     */
    public BoundTimeFieldTestCase() {
        super("01:30", "12:30");
    }

    /**
     * Returns the value of the property.
     *
     * @param property the property
     * @return the value of the property
     */
    @Override
    protected String getValue(Property property) {
        return DateFormatter.formatTime((Date) property.getValue(), true);
    }

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected TextField createField(Property property) {
        PropertyTransformer transformer = new TimePropertyTransformer(property);
        property.setTransformer(transformer);
        return new BoundTimeField(property);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    @Override
    protected Property createProperty() {
        return new SimpleProperty("time", String.class);
    }
}