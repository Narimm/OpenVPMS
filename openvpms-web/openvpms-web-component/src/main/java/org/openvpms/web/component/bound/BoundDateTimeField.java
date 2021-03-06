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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.property.DateTimePropertyTransformer;
import org.openvpms.web.component.property.DefaultPropertyTransformer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Bound date/time field.
 *
 * @author Tim Anderson
 */
public class BoundDateTimeField extends AbstractPropertyEditor {

    /**
     * The date field.
     */
    private final BoundDateField date;

    /**
     * The time field.
     */
    private final BoundTimeField time;

    /**
     * The focus group.
     */
    private final FocusGroup group;

    /**
     * The date/time row.
     */
    private final Row component;

    /**
     * Constructs a {@link BoundDateTimeField}.
     * <p>
     * If the property doesn't already have a {@link PropertyTransformer} registered, one will be added that
     * restricts entered dates to the range {@code {@link BoundDateField#MIN_DATE}..now + 100 years}.
     * This a workaround for OVPMS-1006.
     *
     * @param property the property to bind
     */
    public BoundDateTimeField(Property property) {
        super(property);
        if (property.getTransformer() == null || property.getTransformer() instanceof DefaultPropertyTransformer) {
            // register a transformer that restricts dates
            Date maxDate = DateRules.getDate(DateRules.getDate(new Date()), 100, DateUnits.YEARS);
            property.setTransformer(new DateTimePropertyTransformer(property, BoundDateField.MIN_DATE, maxDate));
        }

        date = new DateField(property);
        time = BoundTimeFieldFactory.create(property);
        component = RowFactory.create("CellSpacing", date, time);
        group = new FocusGroup(property.getName());
        group.add(date);
        group.add(time);
    }

    /**
     * Sets the date portion of the date/time.
     *
     * @param date the date. May be {@code null}
     */
    public void setDate(Date date) {
        getProperty().setValue(getDatetime(date));
    }

    /**
     * Returns the minimum date allowed for this field.
     *
     * @return the minimum date, or {@code null} if there is no minimum date
     */
    public Date getMinDate() {
        Date result = null;
        if (getProperty().getTransformer() instanceof DateTimePropertyTransformer) {
            DateTimePropertyTransformer transformer = (DateTimePropertyTransformer) getProperty().getTransformer();
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
        if (getProperty().getTransformer() instanceof DateTimePropertyTransformer) {
            DateTimePropertyTransformer transformer = (DateTimePropertyTransformer) getProperty().getTransformer();
            result = transformer.getMaxDate();
        }
        return result;
    }

    /**
     * Returns the date portion of the date/time.
     *
     * @return the date. May be {@code null}
     */
    public Date getDate() {
        Date result = null;
        Calendar calendar = getDateField().getDateChooser().getSelectedDate();
        if (calendar != null) {
            result = DateRules.getDate(calendar.getTime());
        }
        return result;
    }

    /**
     * Sets the date and time.
     *
     * @param datetime the date and time
     */
    public void setDatetime(Date datetime) {
        setDate(datetime);
        time.setText(DateFormatter.formatTime(datetime, true));
    }

    /**
     * Returns the date and time.
     *
     * @return the date and time
     */
    public Date getDatetime() {
        Date result = getDate();
        return getDatetime(result);
    }

    /**
     * Sets the date to base relative dates on.
     *
     * @param date the date. If {@code null}, relative dates will be based on the current date/time
     */
    public void setRelativeDate(Date date) {
        this.date.setRelativeDate(date);
    }

    /**
     * Returns the date field,
     *
     * @return the date field
     */
    public BoundDateField getDateField() {
        return date;
    }

    /**
     * Returns the time field.
     *
     * @return the time field
     */
    public BoundTimeField getTimeField() {
        return time;
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }

    /**
     * Sets the component style name.
     *
     * @param styleName the style name
     */
    public void setStyleName(String styleName) {
        date.getDateChooser().setStyleName(styleName);
        date.getTextField().setStyleName(styleName);
        time.setStyleName(styleName);
    }

    /**
     * Adds the time field a date.
     *
     * @param date the date
     * @return the date time
     */
    private Date getDatetime(Date date) {
        Date result = date;
        try {
            Date timePart = DateFormatter.parseTime(time.getText());
            result = DateRules.addDateTime(date, timePart);
        } catch (Throwable ignore) {
            // no-op
        }
        return result;
    }


    private class DateField extends BoundDateField {

        public DateField(Property property) {
            super(property);
        }

        @Override
        protected DateBinder createBinder(Property property) {
            return new DateBinder(this, property) {

                /**
                 * Updates the property from the field.
                 *
                 * @param property the property to update
                 * @return {@code true} if the property was updated
                 */
                @Override
                protected boolean setProperty(Property property) {
                    boolean result;
                    Date date = getFieldValue();
                    Date currentDate = (Date) property.getValue();
                    if (date != null && currentDate != null) {
                        // preserve the existing time
                        Calendar current = new GregorianCalendar();
                        current.setTime(currentDate);

                        Calendar calendar = new GregorianCalendar();
                        calendar.setTime(date);
                        calendar.set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE, current.get(Calendar.MINUTE));
                        calendar.set(Calendar.SECOND, current.get(Calendar.SECOND));
                        date = calendar.getTime();
                    }
                    setDate(date);
                    result = property.setValue(date);
                    if (result) {
                        Object propertyValue = property.getValue();
                        if (!ObjectUtils.equals(date, propertyValue)) {
                            setField();
                        }
                    }
                    return result;
                }
            };
        }
    }

}
