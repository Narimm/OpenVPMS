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

import echopointng.DateChooser;
import echopointng.DateField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.util.DateFieldImpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;


/**
 * Binds a {@link Property} to a <code>DateField</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundDateField extends DateFieldImpl {

    /**
     * The bound property.
     */
    private final DateBinder binder;


    /**
     * Construct a new <code>BoundDateField</code>.
     *
     * @param property the property to bind
     */
    public BoundDateField(Property property) {
        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        getTextField().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // no-op.
            }
        });

        binder = createBinder(property);
        binder.setField();
    }

    /**
     * Creates a new {@link DateBinder}.
     *
     * @param property the property to bind
     * @return a new binder
     */
    protected DateBinder createBinder(Property property) {
        return new DateBinder(this, property);
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
     * Binds a date field to a property.
     */
    protected static class DateBinder extends Binder {

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

        protected Object getFieldValue() {
            return field.getDateChooser().getSelectedDate().getTime();
        }

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
}
