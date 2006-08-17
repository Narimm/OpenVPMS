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

import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.TimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * .
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BoundTimeField extends BoundFormattedField {

    /**
     * The date component of the time. May be <code>null</code>.
     */
    private Date _date;

    /**
     * Construct a new <code>BoundFormattedField</code>.
     *
     * @param property the property to bind
     */
    public BoundTimeField(Property property) {
        super(property, 5, TimeFormatter.getFormat(true));
    }

    /**
     * Sets the date part of the time.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        _date = DateFormatter.getDayMonthYear(date);
    }

    /**
     * Parses the field value.
     *
     * @return the parsed value, or <code>value</code> if it can't be parsed
     */
    @Override
    protected Object parse(String value) {
        Object parsed = super.parse(value);
        if (parsed instanceof Date && _date != null) {
            parsed = getAbsoluteDate((Date) parsed);
        }
        return parsed;
    }

    private Date getAbsoluteDate(Date time) {
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime(_date);
        GregorianCalendar timeCal = new GregorianCalendar();
        timeCal.setTime(time);

        dateCal.add(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.add(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        return dateCal.getTime();
    }
}
