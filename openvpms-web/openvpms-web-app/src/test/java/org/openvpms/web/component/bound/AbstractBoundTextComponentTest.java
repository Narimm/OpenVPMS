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

import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;


/**
 * Tests text components that bind to properties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractBoundTextComponentTest extends AbstractBoundFieldTest<TextComponent, String> {

    /**
     * Constructs an <tt>AbstractBoundTextComponentTest</tt>.
     * <p/>
     * Two test values are passed to the superclass: "value1" and "value2".
     */
    public AbstractBoundTextComponentTest() {
        super("value1", "value2");
    }

    /**
     * Constructs an <tt>AbstractBoundTextComponentTest</tt>.
     *
     * @param value1 the first test value
     * @param value2 the second test value
     */
    public AbstractBoundTextComponentTest(String value1, String value2) {
        super(value1, value2);
    }

    /**
     * Returns the value of the field.
     *
     * @param field the field
     * @return the value of the field
     */
    protected String getValue(TextComponent field) {
        return field.getText();
    }

    /**
     * Sets the value of the field.
     *
     * @param field the field
     * @param value the value to set
     */
    protected void setValue(TextComponent field, String value) {
        field.setText(value);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    protected Property createProperty() {
        return new SimpleProperty("String", String.class);
    }
}
