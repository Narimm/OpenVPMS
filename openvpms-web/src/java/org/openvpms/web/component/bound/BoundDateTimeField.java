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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.property.DateTimePropertyTransformer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.TimeFieldFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Bound date/time field.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Constructs a new <tt>BoundDateTimeField</tt>.
     *
     * @param property the property to bind
     */
    public BoundDateTimeField(Property property) {
        super(property);
        property.setTransformer(new DateTimePropertyTransformer(property));

        date = new DateField(property);
        time = TimeFieldFactory.create(property);
        component = RowFactory.create("CellSpacing", date, time);
        group = new FocusGroup(property.getName());
        group.add(date);
        group.add(time);
    }

    /**
     * Sets the date portion of the date/time.
     *
     * @param date the date. May be <tt>null</tt>
     */
    public void setDate(Date date) {
        getProperty().setValue(getDatetime(date));
    }

    /**
     * Returns the date portion of the date/time.
     *
     * @return the date. May be <tt>null</tt>
     */
    public Date getDate() {
        Date result = null;
        Calendar calendar = getDateField().getDateChooser().getSelectedDate();
        if (calendar != null) {
            result = DateHelper.getDayMonthYear(calendar.getTime());
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
        time.setText(DateHelper.formatTime(datetime, true));
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
     * Adds the time field a date.
     *
     * @param date the date
     * @return the date time
     */
    private Date getDatetime(Date date) {
        Date result = date;
        try {
            Date timePart = DateHelper.parseTime(time.getText());
            result = DateHelper.addDateTime(date, timePart);
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
                 * @param property the propery to update
                 */
                @Override
                protected void setProperty(Property property) {
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
                    if (property.setValue(date)) {
                        Object propertyValue = property.getValue();
                        if (!ObjectUtils.equals(date, propertyValue)) {
                            setField();
                        }
                    }
                }
            };
        }
    }

}
