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
import org.openvpms.web.component.util.ColourHelper;


/**
 * Tests the {@link BoundColorSelect} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundColorSelectTestCase extends AbstractBoundFieldTest<BoundColorSelect, String> {

    /**
     * Constructs a <tt>BoundColorSelectTestCase</tt>.
     */
    public BoundColorSelectTestCase() {
        super("0x0", "0xffffff");
    }

    /**
     * Returns the value of the field.
     *
     * @param field the field
     * @return the value of the field
     */
    protected String getValue(BoundColorSelect field) {
        return ColourHelper.getString(field.getColor());
    }

    /**
     * Sets the value of the field.
     *
     * @param field the field
     * @param value the value to set
     */
    protected void setValue(BoundColorSelect field, String value) {
        field.setColor(ColourHelper.getColor(value));
    }

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected BoundColorSelect createField(Property property) {
        return new BoundColorSelect(property);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    protected Property createProperty() {
        return new SimpleProperty("colour", String.class);
    }
}