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

package org.openvpms.web.component.property;

import org.openvpms.web.component.util.DateHelper;

import java.util.Date;
import java.text.ParseException;


/**
 * Handler for date nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class DatePropertyTransformer extends AbstractDateTimePropertyTransformer {

    /**
     * Constructs a <tt>DateTimePropertyTransformer</tt>.
     *
     * @param property the property
     */
    public DatePropertyTransformer(Property property) {
        this(property, null, null);
    }

    /**
     * Constructs a <tt>DateTimePropertyTransformer</tt>.
     *
     * @param property the property
     * @param min      the minimum value for the date. If <tt>null</tt>, the date has no minimum
     * @param max      the maximum value for the date. If <tt>null</tt>, the date has no maximum
     */
    public DatePropertyTransformer(Property property, Date min, Date max) {
        super(property, min, max, Format.DATE);
    }

    /**
     * Converts the supplied value to a date.
     *
     * @param value the date/time string
     * @return the date
     * @throws ParseException if the value can't be parsed as a date
     */
    protected Date getDateTime(String value) throws ParseException {
        return DateHelper.getDateFormat(true).parse(value);
    }
}