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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.bound;

import echopointng.DateChooser;
import echopointng.DateField;
import org.openvpms.web.component.property.Property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;


/**
 * Binds a date field to a property.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-10-17 07:23:38Z $
 */
class DateBinder extends Binder {

    /**
     * The date field.
     */
    private final DateField field;

    /**
     * Date change listener.
     */
    private final PropertyChangeListener listener;


    /**
     * Constructs a new <code>DateBinder</code>.
     *
     * @param field    the field to bind
     * @param property the property to bind to
     */
    public DateBinder(DateField field, Property property) {
        super(property);
        this.field = field;
        listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String name = event.getPropertyName();
                if ("selectedDate".equals(name)) {
                    setProperty();
                }
            }
        };
    }

    /**
     * Returns the date field.
     *
     * @return the date field
     */
    protected DateField getField() {
        return field;
    }

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    protected Date getFieldValue() {
        Calendar calendar = field.getDateChooser().getSelectedDate();
        return (calendar != null) ? calendar.getTime() : null;
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value to set
     */
    protected void setFieldValue(Object value) {
        DateChooser chooser = field.getDateChooser();
        chooser.removePropertyChangeListener(listener);
        Date date = (Date) value;
        Calendar calendar = null;
        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }
        chooser.setSelectedDate(calendar);
        chooser.addPropertyChangeListener(listener);
    }
}
