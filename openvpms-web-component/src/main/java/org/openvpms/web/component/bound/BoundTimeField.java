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

import nextapp.echo2.app.Extent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.text.DateFormat;


/**
 * Bound time field.
 * <p/>
 * This should be used to represent times linked to dates.
 * <p/>
 * For absolute
 *
 * @author Tim Anderson
 */
public class BoundTimeField extends BoundFormattedField {

    /**
     * Construct a new {@code BoundTimeField}.
     *
     * @param property the property to bind
     */
    public BoundTimeField(Property property) {
        super(property, DateFormatter.getTimeFormat(true));
        DateFormat format = (DateFormat) getFormat();
        int columns = DateFormatter.getLength(format);
        setWidth(new Extent(columns, Extent.EX));
    }

    /**
     * Parses the field value. This implementation is a no-op, as field
     * parsing is handled via {@link TimePropertyTransformer}.
     *
     * @return the parsed value, or {@code value} if it can't be parsed
     */
    @Override
    protected Object parse(String value) {
        return value;
    }
}
