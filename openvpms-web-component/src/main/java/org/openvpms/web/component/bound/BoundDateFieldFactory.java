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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.bound;

import echopointng.DateField;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.DateFieldFactory;


/**
 * Factory for {@link BoundDateField}s.
 *
 * @author Tim Anderson
 */
public class BoundDateFieldFactory extends DateFieldFactory {

    /**
     * Creates a new bound date field with the default style.
     *
     * @param property the property to bind
     * @return a new bound date field
     */
    public static DateField create(Property property) {
        return init(new BoundDateField(property));
    }

}

