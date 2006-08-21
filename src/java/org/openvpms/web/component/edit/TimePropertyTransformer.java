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

package org.openvpms.web.component.edit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import static org.openvpms.component.business.service.archetype.ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.TimeFormatter;
import org.openvpms.web.resource.util.Messages;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;


/**
 * Handler for time nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class TimePropertyTransformer extends PropertyTransformer {

    /**
     * The parent object archetype id.
     */
    private ArchetypeId _id;

    /**
     * The date component of the time. May be <code>null</code>.
     */
    private Date _date;


    /**
     * Construct a new <code>TimePropertyTransformer</code>.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     */
    public TimePropertyTransformer(IMObject object, NodeDescriptor descriptor) {
        super(descriptor);
        _id = object.getArchetypeId();
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
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws ValidationException if the object is invalid
     */
    public Object apply(Object object) throws ValidationException {
        Object result;
        try {
            if (object instanceof String) {
                String value = (String) object;
                if (StringUtils.isEmpty(value)) {
                    result = null;
                } else {
                    result = parse(value);
                }
            } else if (object instanceof Date) {
                result = object;
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
        DateFormat format = TimeFormatter.getFormat(true);
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
        if (_date != null) {
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
        int hours;
        try {
            hours = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw getException(exception);
        }
        if (hours < 0 || hours > 23) {
            throw getException(null);
        }
        return new Date(hours * DateUtils.MILLIS_IN_HOUR);
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
        Date hours = parseHours(hourPart);
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
        long time = hours.getTime() + (mins * DateUtils.MILLIS_IN_MINUTE);
        return new Date(time);
    }

    /**
     * Adds the date to a time.
     *
     * @param time the time to add
     * @return the date+time
     */
    private Date addDate(Date time) {
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime(_date);
        GregorianCalendar timeCal = new GregorianCalendar(
                TimeZone.getTimeZone("GMT"));
        timeCal.setTime(time);

        dateCal.add(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.add(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        return dateCal.getTime();
    }

    /**
     * Helper to create a {@link ValidationException} from an exception.
     *
     * @param exception the exception. May be <code>null</code>
     * @return a new <code>ValidationException</code>
     */
    private ValidationException getException(Throwable exception) {
        NodeDescriptor node = getDescriptor();
        String message = Messages.get("node.error.invalidtime",
                                      node.getDisplayName());
        ValidationError error = new ValidationError(node.getName(),
                                                    message);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        errors.add(error);
        ValidationException.ErrorCode code
                = FailedToValidObjectAgainstArchetype;
        if (exception != null) {
            return new ValidationException(errors, code, new Object[]{_id},
                                           exception);
        }
        return new ValidationException(errors, code, new Object[]{_id});
    }

}
