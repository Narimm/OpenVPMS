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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.booking.impl;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.ws.rs.BadRequestException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ISO date helper methods.
 *
 * @author Tim Anderson
 */
class DateHelper {
    /**
     * Helper to convert a string query parameter to an ISO 8601 date.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the corresponding date
     * @throws BadRequestException if the value is invalid
     */
    static Date getDate(String name, String value) {
        if (value == null) {
            throw new BadRequestException("Missing '" + name + "' parameter");
        }
        DateTime result;
        try {
            result = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(value);
        } catch (IllegalArgumentException e) {
            try {
                result = ISODateTimeFormat.dateTime().parseDateTime(value);
            } catch (IllegalArgumentException nested) {
                throw new BadRequestException("Parameter '" + name + "' is not a valid ISO date/time: " + value);
            }
        }
        return result.toDate();
    }

    /**
     * Ensures a {code Timestamp} date instances are converted to {@code Date}.
     * <p/>
     * This is so all dates appear in the server's timezone.
     *
     * @param date the date
     * @return the date, converted if necessary
     */
    static Date convert(Date date) {
        return (date instanceof Timestamp) ? new Date(date.getTime()) : date;
    }
}
