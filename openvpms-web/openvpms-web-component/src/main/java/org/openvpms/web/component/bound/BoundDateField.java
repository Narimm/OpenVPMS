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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.bound;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.component.property.DatePropertyTransformer;
import org.openvpms.web.component.property.DefaultPropertyTransformer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.echo.date.DateFieldImpl;

import java.util.Calendar;
import java.util.Date;


/**
 * Binds a {@link Property} to a {@code DateField}.
 *
 * @author Tim Anderson
 */
public class BoundDateField extends DateFieldImpl implements BoundProperty {

    /**
     * A 'sensible' minimum for dates.
     */
    public static final Date MIN_DATE = java.sql.Date.valueOf("1970-01-01");

    /**
     * The bound property.
     */
    private final DateBinder binder;

    /**
     * If {@code true}, include the current time if the date is today.
     */
    private boolean includeTimeForToday = true;


    /**
     * Constructs a {@link BoundDateField}.
     * <p/>
     * If the property doesn't already have a {@link PropertyTransformer} registered, one will be added that
     * restricts entered dates to the range {@code {@link #MIN_DATE}..now + 100 years}.
     * This a workaround for OVPMS-1006.
     *
     * @param property the property to bind
     */
    public BoundDateField(Property property) {
        binder = createBinder(property);
        if (property.getTransformer() == null || property.getTransformer() instanceof DefaultPropertyTransformer) {
            // register a transformer that restricts dates
            Date maxDate = DateRules.getDate(new Date(), 100, DateUnits.YEARS);
            property.setTransformer(new DatePropertyTransformer(property, MIN_DATE, maxDate));
        }
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }
        setAllowNulls(!property.isRequired());
    }

    /**
     * Includes the current time if the selected date is today.
     * For all other days, the time is set to {@code 0:0:0}.
     * Defaults to {@code true}.
     *
     * @param include if {@code true}, include the current time if the date is
     *                today; otherwise set it to {@code 0:0:0}
     */
    public void setIncludeTimeForToday(boolean include) {
        includeTimeForToday = include;
    }

    /**
     * Returns the minimum date allowed for this field.
     *
     * @return the minimum date, or {@code null} if there is no minimum date
     */
    public Date getMinDate() {
        Date result = null;
        Property property = binder.getProperty();
        if (property.getTransformer() instanceof DatePropertyTransformer) {
            DatePropertyTransformer transformer = (DatePropertyTransformer) property.getTransformer();
            result = transformer.getMinDate();
        }
        return result;
    }

    /**
     * Returns the maximum date allowed for this field.
     *
     * @return the maximum date, or {@code null} if there is no maximum date
     */
    public Date getMaxDate() {
        Date result = null;
        Property property = binder.getProperty();
        if (property.getTransformer() instanceof DatePropertyTransformer) {
            DatePropertyTransformer transformer = (DatePropertyTransformer) property.getTransformer();
            result = transformer.getMaxDate();
        }
        return result;
    }

    /**
     * Sets the date.
     *
     * @param date the date. May be {@code null}
     */
    public void setDate(Date date) {
        binder.getProperty().setValue(date);
    }

    /**
     * Life-cycle method invoked when the {@code Component} is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the {@code Component} is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }

    /**
     * Returns the property.
     *
     * @return the property
     */
    @Override
    public Property getProperty() {
        return binder.getProperty();
    }

    /**
     * Creates a new {@link DateBinder}.
     *
     * @param property the property to bind
     * @return a new binder
     */
    protected DateBinder createBinder(Property property) {
        return new DateBinder(this, property) {
            @Override
            protected Date getFieldValue() {
                Date result = super.getFieldValue();
                if (result != null) {
                    Date current = (Date) getProperty().getValue();
                    if (current != null && DateRules.getDate(current).equals(DateRules.getDate(result))) {
                        // preserve the existing date/time, to avoid spurious modification notifications
                        result = current;
                    } else if (includeTimeForToday) {
                        result = getDatetimeIfToday(result);
                    }
                }
                return result;
            }
        };
    }

    /**
     * Returns the binder.
     *
     * @return the binder
     */
    protected DateBinder getBinder() {
        return binder;
    }

    /**
     * Returns the current date/time if the date falls on the
     * current date, otherwise returns the date unchanged.
     *
     * @param date the date
     * @return the current date/time if {@code date} falls on the current date.
     *         If not, returns {@code date} unchanged.
     */
    private Date getDatetimeIfToday(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar d = Calendar.getInstance();
        d.setTime(date);
        if (now.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)
            && now.get(Calendar.YEAR) == d.get(Calendar.YEAR)) {
            return now.getTime();
        }
        return date;
    }

}
