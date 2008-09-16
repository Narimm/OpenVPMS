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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import echopointng.DateChooser;
import echopointng.DateField;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.RowFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * A date selector that provides:
 * <ul>
 * <li>an editable date field</li>
 * <li>buttons to add/subtract 1 and 7 days from the displayed date</li>
 * <li>a button to jump to the current date</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DateSelector {

    /**
     * The date.
     */
    private DateField date;

    /**
     * The last date processed by {@link #onDateChanged()}.
     * todo - this is a workaround for a bug/feature of the EPNG date field
     * which seems to generate 2 events for the one update to the text field.
     */
    private Date lastDate;

    /**
     * Listener to notify of date change events.
     */
    private ActionListener listener;

    /**
     * The component.
     */
    private Component component;

    /**
     * The focus group.
     */
    private FocusGroup focus;


    /**
     * Creates a new <tt>DateSelector</tt>.
     */
    public DateSelector() {
        date = DateFieldFactory.create();
        date.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if ("selectedDate".equals(event.getPropertyName())) {
                    onDateChanged();
                }
            }
        });
        focus = new FocusGroup("dateSelector");
    }

    /**
     * Set the displayed date
     *
     * @param date the date to set
     */
    public void setDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        this.date.getDateChooser().setSelectedDate(calendar);
    }

    /**
     * Returns the selected date.
     *
     * @return the selected date
     */
    public Date getDate() {
        Date datetime = date.getDateChooser().getSelectedDate().getTime();
        return DateRules.getDate(datetime);
    }

    /**
     * Sets a listener to be notified of date change events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the component representing the date selector.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            doLayout();
        }
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Lays out the component.
     */
    private void doLayout() {
        Button prevWeek = ButtonFactory.create(
                null, "date.previousWeek", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(-7);
            }
        });
        Button prevDay = ButtonFactory.create(
                null, "date.previousDay", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(-1);
            }
        });
        Button currentDay = ButtonFactory.create(
                null, "date.currentDay", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(0);
            }
        });
        Button nextDay = ButtonFactory.create(
                null, "date.nextDay", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(1);
            }
        });
        Button nextWeek = ButtonFactory.create(
                null, "date.nextWeek", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDays(7);
            }
        });

        focus.add(prevWeek);
        focus.add(prevDay);
        focus.add(date);
        focus.add(currentDay);
        focus.add(nextDay);
        focus.add(nextWeek);
        component = RowFactory.create("CellSpacing", prevWeek, prevDay, date,
                                      currentDay, nextDay, nextWeek);
    }

    /**
     * Invoked to change the selected date.
     *
     * @param days the no. of days to add. <code>0</code> indicates current date
     */
    private void addDays(int days) {
        DateChooser dateChooser = date.getDateChooser();
        Calendar calendar;
        if (days == 0) {
            calendar = new GregorianCalendar();
            calendar.setTime(new Date());
        } else {
            calendar = dateChooser.getSelectedDate();
            calendar.add(Calendar.DAY_OF_MONTH, days);
        }
        date.getDateChooser().setSelectedDate(calendar);
        onDateChanged();
    }

    /**
     * Invoked when the date is updated.
     */
    protected void onDateChanged() {
        Date date = getDate();
        if (!date.equals(lastDate)) {
            lastDate = date;
            if (listener != null) {
                listener.actionPerformed(new ActionEvent(this, "date"));
            }
        }
    }

}
