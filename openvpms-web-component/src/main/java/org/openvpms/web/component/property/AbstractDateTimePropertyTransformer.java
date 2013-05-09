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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.property;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateHelper;

import java.text.ParseException;
import java.util.Date;

/**
 * {@link PropertyTransformer} for date/time properties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractDateTimePropertyTransformer extends AbstractPropertyTransformer {

    /**
     * The format for date range validation errors.
     */
    protected enum Format {
        DATE,
        DATE_TIME
    }

    /**
     * The minimum value for the date. If <tt>null</tt>, the date has no minimum.
     */
    private Date minDate;

    /**
     * The maximum value for the date. If <tt>null</tt>, the date has no maximum.
     */
    private Date maxDate;

    /**
     * The format for date range validation errors.
     */
    private Format format;

    /**
     * Determines if seconds should be kept.
     */
    private boolean keepSeconds = true;


    /**
     * Constructs a <tt>AbstractDateTimePropertyTransformer</tt>.
     *
     * @param property the property
     */
    public AbstractDateTimePropertyTransformer(Property property) {
        this(property, null, null, Format.DATE_TIME);
    }

    /**
     * Constructs a <tt>AbstractDateTimePropertyTransformer</tt>.
     *
     * @param property the property
     * @param min      the minimum value for the date. If <tt>null</tt>, the date has no minimum
     * @param max      the maximum value for the date. If <tt>null</tt>, the date has no maximum
     * @param format   the format for date range validation errors
     */
    public AbstractDateTimePropertyTransformer(Property property, Date min, Date max, Format format) {
        super(property);
        minDate = min;
        maxDate = max;
        this.format = format;
    }

    /**
     * Returns the minimum value for the date.
     *
     * @return the minimum value for the date. If <tt>null</tt>, the date has no minimum
     */
    public Date getMinDate() {
        return minDate;
    }

    /**
     * Returns the maximum value for the date.
     *
     * @return the maximum value for the date. If <tt>null</tt>, the date has no maximum
     */
    public Date getMaxDate() {
        return maxDate;
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <tt>object</tt> if no
     *         transformation is required
     * @throws org.openvpms.web.component.property.PropertyException
     *          if the object is invalid
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
                        String msg = Messages.get("property.error.required", property.getDisplayName());
                        throw new PropertyException(property, msg);
                    }
                } else {
                    result = getDateTime(value);
                }
            } else if (object instanceof Date) {
                result = getDateTime((Date) object);
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
        if (result instanceof Date) {
            if (!keepSeconds) {
                result = removeSeconds((Date) result);
            }
            checkDateRange((Date) result, minDate, maxDate);
        }
        return result;
    }

    /**
     * Determines if seconds should be kept.
     *
     * @param keep if <true</tt> keep seconds, otherwise zero them out (along with milliseconds)
     */
    public void setKeepSeconds(boolean keep) {
        keepSeconds = keep;
    }

    /**
     * Converts the supplied value to a date/time.
     *
     * @param value the date/time string
     * @return the date/time
     * @throws ParseException if the value can't be parsed as a date/time
     */
    protected abstract Date getDateTime(String value) throws ParseException;

    /**
     * Returns the date.
     * <p/>
     * This implementation assumes that the property value is a date, and returns it unchanged.
     *
     * @return the date, or <tt>null</tt> if there is no date
     */
    protected Date getDate() {
        return (Date) getProperty().getValue();
    }

    /**
     * Returns the supplied value as a date/time.
     * <p/>
     * This implementation returns the value unchanged.
     *
     * @param value a date, time, or date/time
     * @return the date/time
     */
    protected Date getDateTime(Date value) {
        return value;
    }

    /**
     * Helper to parse a time string, and add it to the current date, if one is present.
     * <p/>
     * NOTE: This does not modify the current date/time.
     *
     * @param value the time string
     * @return the date/time
     * @throws ParseException if the value can't be parsed as a time
     */
    protected Date addTime(String value) throws ParseException {
        Date result;
        Date time = DateHelper.parseTime(value);
        Date date = getDate();
        if (date != null) {
            result = DateHelper.addDateTime(date, time);
        } else {
            result = time;
        }
        return result;
    }

    /**
     * Verifies that the date falls into the acceptable date range.
     *
     * @param date the date to check
     * @param min  the minimum date, or <tt>null</tt> if there is no minimum
     * @param max  the maximum date, or <tt>null</tt> if there is no maximum
     */
    protected void checkDateRange(Date date, Date min, Date max) {
        if (min != null && date.getTime() < min.getTime()) {
            String formatDate;
            String formatMin;
            if (format == Format.DATE) {
                formatDate = DateHelper.formatDate(date, false);
                formatMin = DateHelper.formatDate(min, false);
            } else {
                formatDate = DateHelper.formatDateTime(date, false);
                formatMin = DateHelper.formatDateTime(min, false);
            }
            String msg = Messages.get("property.error.minDate", formatDate, formatMin);
            throw new PropertyException(getProperty(), msg);
        }
        if (max != null && date.getTime() > max.getTime()) {
            String formatDate;
            String formatMax;
            if (format == Format.DATE) {
                formatDate = DateHelper.formatDate(date, false);
                formatMax = DateHelper.formatDate(max, false);
            } else {
                formatDate = DateHelper.formatDateTime(date, false);
                formatMax = DateHelper.formatDateTime(max, false);
            }
            String msg = Messages.get("property.error.maxDate", formatDate, formatMax);
            throw new PropertyException(getProperty(), msg);
        }
    }

    /**
     * Helper to create a new property exception.
     *
     * @param cause the cause. May be <tt>null</tt>
     * @return a new property exception
     */
    protected PropertyException getException(Throwable cause) {
        String message = Messages.get("property.error.invalidtime", getProperty().getDisplayName());
        return new PropertyException(getProperty(), message, cause);
    }


    /**
     * Zeroes any seconds (and milliseconds) from the date.
     *
     * @param date the date
     * @return the modified date
     */
    private Date removeSeconds(Date date) {
        long ms = date.getTime();
        long seconds = ms % (60 * 1000);
        if (seconds != 0) {
            date.setTime(ms - seconds);
        }
        return date;
    }
}