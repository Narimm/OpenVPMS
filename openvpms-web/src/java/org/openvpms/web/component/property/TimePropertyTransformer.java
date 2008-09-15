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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.resource.util.Messages;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Handler for time nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class TimePropertyTransformer extends AbstractPropertyTransformer {

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
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <tt>object</tt> if no
     *         transformation is required
     * @throws PropertyException if the object is invalid
     */
    public Object apply(Object object) throws PropertyException {
        Object result;
        try {
            if (object instanceof String) {
                String value = (String) object;
                if (StringUtils.isEmpty(value)) {
                    Property property = getProperty();
                    if (!property.isRequired()) {
                        result = null;
                    } else {
                        String msg = Messages.get("property.error.required",
                                                  property.getDisplayName());
                        throw new PropertyException(property, msg);
                    }
                } else {
                    result = parse(value);
                }
            } else if (object instanceof Date) {
                if (date != null) {
                    result = addDate((Date) object);
                } else {
                    result = object;
                }
            } else if (object == null) {
                result = null;
            } else {
                throw getException(null);
            }
        } catch (ValidationException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw getException(exception);
        }

        return result;
    }


    /**
     * Parses a time from a string.
     *
     * @param value the string value
     * @return the parsed time
     * @throws ValidationException if the string can't be parsed
     */
    private Date parse(String value) throws ValidationException {
        Date result;
        DateFormat format = DateHelper.getTimeFormat(true);
        try {
            result = format.parse(value);
        } catch (ParseException exception) {
            if (value.length() <= 2) {
                result = parseHours(value);
            } else if (value.length() <= 4) {
                result = parseHoursMins(value);
            } else {
                throw getException(exception);
            }
        }
        if (date != null) {
            result = addDate(result);
        }
        return result;
    }

    /**
     * Parse a time from a string, expected to be in the range 0..23.
     *
     * @param value the string to parse
     * @return the parsed time
     * @throws ValidationException if the string can't be parsed
     */
    private Date parseHours(String value) throws ValidationException {
        int hours = getHours(value);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    /**
     * Parses hours from a string, expected to be in the range 0..23.
     *
     * @param value the string to parse
     * @throws ValidationException if the string can't be parsed
     */
    private int getHours(String value) {
        int hours;
        try {
            hours = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw getException(exception);
        }
        if (hours < 0 || hours > 23) {
            throw getException(null);
        }
        return hours;
    }

    /**
     * Parse a time from a string, expected to be of the form [H]HMM.
     *
     * @param value the string to parse
     * @return the parsed time
     * @throws ValidationException if the string can't be parsed
     */
    private Date parseHoursMins(String value) throws ValidationException {
        String hourPart;
        if (value.length() == 3) {
            hourPart = value.substring(0, 1);
        } else {
            hourPart = value.substring(0, 2);
        }
        int hours = getHours(hourPart);
        int mins;
        try {
            String minPart = value.substring(hourPart.length());
            mins = Integer.parseInt(minPart);
        } catch (NumberFormatException exception) {
            throw getException(exception);
        }
        if (mins < 0 || mins > 59) {
            throw getException(null);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, mins);
        return calendar.getTime();
    }

    /**
     * Adds the date to a time.
     *
     * @param time the time to add
     * @return the date+time
     */
    private Date addDate(Date time) {
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime(date);
        GregorianCalendar timeCal = new GregorianCalendar();
        timeCal.setTime(time);

        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        return dateCal.getTime();
    }

    /**
     * Helper to create a new property exception.
     *
     * @param cause the cause. May be <tt>null</tt>
     * @return a new property exception
     */
    private PropertyException getException(Throwable cause) {
        String message = Messages.get("property.error.invalidtime",
                                      getProperty().getDisplayName());
        return new PropertyException(getProperty(), message, cause);
    }

}
