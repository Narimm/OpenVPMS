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
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import echopointng.DateField;
import nextapp.echo2.app.Extent;
import org.openvpms.web.component.bound.BoundDateField;
import org.openvpms.web.component.property.Property;


/**
 * Factory for <code>DateFields</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DateFieldFactory extends ComponentFactory {

    /**
     * Creates a new date field with the default style.
     *
     * @return a new date field
     */
    public static DateField create() {
        return init(new DateFieldImpl());
    }

    /**
     * Creates a new bound date field with the default style.
     *
     * @param property the property to bind
     * @return a new bound date field
     */
    public static DateField create(Property property) {
        return init(new BoundDateField(property));
    }

    /**
     * Initialises a date field.
     *
     * @param date the date field
     * @return the date field
     */
    private static DateField init(DateField date) {
        setDefaultStyle(date.getDateChooser());
        setDefaultStyle(date.getTextField());
        int length = DateHelper.getLength(date.getDateFormat());
        date.getTextField().setWidth(new Extent(length, Extent.EX));
        return date;
    }


}

