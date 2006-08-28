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

import echopointng.DateField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.edit.Property;

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
public class BoundDateField extends DateField {

    /**
     * The bound property.
     */
    private final Binder _binder;

    /**
     * Date change listener.
     */
    private final PropertyChangeListener _listener;


    /**
     * Construct a new <code>BoundDateField</code>.
     *
     * @param property the property to bind
     */
    public BoundDateField(Property property) {
        _listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String name = event.getPropertyName();
                if ("selectedDate".equals(name)) {
                    _binder.setProperty();
                }
            }
        };

        // Register an action listener to ensure document update events
        // are triggered in a timely fashion
        getTextField().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // no-op.
            }
        });

        _binder = new Binder(property) {
            protected Object getFieldValue() {
                return getDateChooser().getSelectedDate().getTime();
            }

            protected void setFieldValue(Object value) {
                getDateChooser().removePropertyChangeListener(_listener);
                Date date = (Date) value;
                Calendar calendar = null;
                if (date != null) {
                    calendar = Calendar.getInstance();
                    calendar.setTime(date);
                }
                getDateChooser().setSelectedDate(calendar);
                getDateChooser().addPropertyChangeListener(_listener);
            }
        };
        _binder.setField();
    }

}
