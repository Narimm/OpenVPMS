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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.component.property.DatePropertyTransformer;
import org.openvpms.web.component.property.DefaultPropertyTransformer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.util.DateFieldImpl;
import org.openvpms.web.resource.util.DateHelper;

import java.util.Date;


/**
 * Binds a {@link Property} to a <code>DateField</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundDateField extends DateFieldImpl {

    /**
     * A 'sensible' minimum for dates.
     */
    public static final Date MIN_DATE = java.sql.Date.valueOf("1970-01-01");

    /**
     * The bound property.
     */
    private final DateBinder binder;

    /**
     * If <tt>true</tt>, include the current time if the date is today.
     */
    private boolean includeTimeForToday = true;


    /**
     * Construct a new <code>BoundDateField</code>.
     * <p/>
     * If the property doesn't already have a {@link PropertyTransformer} registered, one will be added that
     * restricts entered dates to the range <tt>{@link #MIN_DATE}..now + 100 years</tt>.
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
     * For all other days, the time is set to <tt>0:0:0</tt>.
     * Defaults to <tt>true</tt>.
     *
     * @param include if <tt>true</tt>, include the current time if the date is
     *                today; otherwise set it to <tt>0:0:0</tt>
     */
    public void setIncludeTimeForToday(boolean include) {
        includeTimeForToday = include;
    }

    /**
     * Returns the minimum date allowed for this field.
     *
     * @return the minimum date, or <tt>null</tt> if there is no minimum date
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
     * @return the maximum date, or <tt>null</tt> if there is no maximum date
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
     * @param date the date. May be <tt>null</tt>
     */
    public void setDate(Date date) {
        binder.getProperty().setValue(date);
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
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
                        result = DateHelper.getDatetimeIfToday(result);
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

}
