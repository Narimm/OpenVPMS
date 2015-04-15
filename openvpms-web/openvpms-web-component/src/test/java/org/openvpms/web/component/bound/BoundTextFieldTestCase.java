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
import org.openvpms.web.echo.text.TextField;


/**
 * Tests the {@link BoundTextField} class.
 *
 * @author Tim Anderson
 */
public class BoundTextFieldTestCase extends AbstractBoundTextComponentTest {

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected TextField createField(Property property) {
        return new BoundTextField(property, 10);
    }

}
