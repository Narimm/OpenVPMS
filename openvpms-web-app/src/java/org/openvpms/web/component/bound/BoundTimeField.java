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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Extent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.component.util.DateHelper;

import java.text.DateFormat;


/**
 * Bound time field.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundTimeField extends BoundFormattedField {

    /**
     * Construct a new <code>BoundTimeField</code>.
     *
     * @param property the property to bind
     */
    public BoundTimeField(Property property) {
        super(property, DateHelper.getTimeFormat(true));
        DateFormat format = (DateFormat) getFormat();
        int columns = DateHelper.getLength(format);
        setWidth(new Extent(columns, Extent.EX));
    }

    /**
     * Parses the field value. This implementation is a no-op, as field
     * parsing is handled via {@link TimePropertyTransformer}.
     *
     * @return the parsed value, or <code>value</code> if it can't be parsed
     */
    @Override
    protected Object parse(String value) {
        return value;
    }
}
