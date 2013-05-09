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

import org.openvpms.archetype.rules.util.DateRules;

import java.text.ParseException;
import java.util.Date;


/**
 * Handler for time nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class TimePropertyTransformer extends AbstractDateTimePropertyTransformer {

    /**
     * The date component of the time. May be <tt>null</tt>.
     */
    private Date date;


    /**
     * Construct a new <tt>TimePropertyTransformer</tt>.
     *
     * @param property the property
     */
    public TimePropertyTransformer(Property property) {
        super(property);
    }

    /**
     * Sets the date part of the time.
     *
     * @param date the date. May be <tt>null</tt>
     */
    public void setDate(Date date) {
        this.date = (date != null) ? DateRules.getDate(date) : null;
    }

    /**
     * Returns the supplied value as a date/time.
     *
     * @param value the time
     * @return the date/time
     */
    protected Date getDateTime(Date value) {
        Date result;
        if (date != null) {
            result = DateRules.addDateTime(date, value);
        } else {
            result = value;
        }
        return result;
    }

    /**
     * Converts the supplied value to a date/time.
     * <p/>
     * This implementation expects the value to be a time, which is added to the current date, using
     * {@link #addTime}.
     *
     * @param value the time string
     * @return the date/time
     * @throws ParseException if the value can't be parsed as a date/time
     */
    protected Date getDateTime(String value) throws ParseException {
        return addTime(value);
    }

    /**
     * Returns the date.
     *
     * @return the date. May be <tt>null</tt>
     */
    protected Date getDate() {
        return date;
    }
}
