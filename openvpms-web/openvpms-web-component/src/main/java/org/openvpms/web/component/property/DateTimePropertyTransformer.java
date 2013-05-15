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

import java.text.ParseException;
import java.util.Date;


/**
 * Handler for date/time nodes.
 * <p/>
 * This assumes that all date changes will be as {@link java.util.Date}, and all time inputs will be as
 * strings.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class DateTimePropertyTransformer extends AbstractDateTimePropertyTransformer {

    /**
     * Constructs a <tt>DateTimePropertyTransformer</tt>.
     *
     * @param property the property
     */
    public DateTimePropertyTransformer(Property property) {
        this(property, null, null);
    }

    /**
     * Constructs a <tt>DateTimePropertyTransformer</tt>.
     *
     * @param property the property
     * @param min      the minimum value for the date. If <tt>null</tt>, the date has no minimum
     * @param max      the maximum value for the date. If <tt>null</tt>, the date has no maximum
     */
    public DateTimePropertyTransformer(Property property, Date min, Date max) {
        super(property, min, max, Format.DATE_TIME);
    }


    /**
     * Converts the supplied value to a date/time.
     * <p/>
     * This implemetation expects the value to be a time, which is added to the current date, using
     * {@link #addTime}.
     *
     * @param value the time string
     * @return the date/time
     * @throws ParseException if the value can't be parsed as a date/time
     */
    protected Date getDateTime(String value) throws ParseException {
        return addTime(value);
    }
}