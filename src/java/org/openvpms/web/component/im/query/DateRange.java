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

package org.openvpms.web.component.im.query;

import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.ComponentHelper;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;


/**
 * Component for selecting a range of dates.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DateRange {

    /**
     * The focus group.
     */
    private final FocusGroup focus;

    /**
     * Determines if the 'allDates' check box should be displayed.
     */
    private final boolean showAll;

    /**
     * Indicates if all dates should be selected. If so, the from and to dates
     * are ignored.
     */
    private CheckBox allDates;

    /**
     * The date-from label.
     */
    private Label fromLabel;

    /**
     * The date-from date.
     */
    private DateField from;

    /**
     * The date-to label.
     */
    private Label toLabel;

    /**
     * The date-to date.
     */
    private DateField to;

    /**
     * The component.
     */
    private Component component;

    /**
     * The comonent style.
     */
    private static final String STYLE = "CellSpacing";


    /**
     * Constructs a new <tt>DateRange</tt>
     *
     * @param focus the focus group
     */
    public DateRange(FocusGroup focus) {
        this(focus, true);
    }

    /**
     * Constructs a new <tt>DateRange</tt>
     *
     * @param focus   the focus group
     * @param showAll determines if an 'all' checkbox should be displayed to select all dates
     */
    public DateRange(FocusGroup focus, boolean showAll) {
        this.focus = focus;
        this.showAll = showAll;
        component = doLayout();
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or <tt>null</tt> to query all dates
     */
    public Date getFrom() {
        return getAllDates() ? null : getDate(from);
    }

    /**
     * Sets the 'from' date.
     *
     * @param date the 'from' date
     */
    public void setFrom(Date date) {
        setDate(from, date);
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or <tt>null</tt> to query all dates
     */
    public Date getTo() {
        return getAllDates() ? null : getDate(to);
    }

    /**
     * Sets the 'to' date.
     *
     * @param date the 'to' date
     */
    public void setTo(Date date) {
        setDate(to, date);
    }

    /**
     * Sets the state of the <em>allDates</em> checkbox, if present.
     *
     * @param selected the state of the <em>allDates</em> checkbox
     */
    public void setAllDates(boolean selected) {
        if (showAll) {
            allDates.setSelected(selected);
        }
    }

    /**
     * Determines if all dates are being selected.
     *
     * @return <tt>true</tt> if all dates are being seleced
     */
    public boolean getAllDates() {
        return showAll && allDates.isSelected();
    }


    /**
     * Renders the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    protected Component doLayout() {
        if (showAll) {
            allDates = CheckBoxFactory.create("daterange.all", true);
            allDates.addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onAllDatesChanged();
                }
            });
        }

        fromLabel = LabelFactory.create("daterange.from");
        from = DateFieldFactory.create();
        from.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onFromChanged();
                    }
                });

        toLabel = LabelFactory.create("daterange.to");
        to = DateFieldFactory.create();
        to.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onToChanged();
                    }
                });
        Row range = RowFactory.create(STYLE, fromLabel, from, toLabel, to);
        Row result = RowFactory.create(STYLE);
        if (showAll) {
            result.add(allDates);
        }
        result.add(range);

        onAllDatesChanged();

        if (showAll) {
            focus.add(allDates);
        }
        focus.add(from);
        focus.add(to);
        return result;
    }

    /**
     * Invoked when the 'from' date changes. Sets the 'to' date = 'from' if
     * 'from' is greater.
     */
    private void onFromChanged() {
        Date from = getFrom();
        Date to = getTo();
        if (from != null && from.compareTo(to) > 0) {
            setTo(from);
        }
    }

    /**
     * Invoked when the 'to' date changes. Sets the 'from' date = 'to' if
     * 'from' is greater.
     */
    private void onToChanged() {
        Date from = getFrom();
        Date to = getTo();
        if (from != null && from.compareTo(to) > 0) {
            setFrom(to);
        }
    }

    /**
     * Invoked when the 'all dates' check box changes.
     */
    private void onAllDatesChanged() {
        boolean enabled = !getAllDates();
        ComponentHelper.enable(fromLabel, enabled);
        ComponentHelper.enable(from, enabled);
        ComponentHelper.enable(toLabel, enabled);
        ComponentHelper.enable(to, enabled);
    }

    /**
     * Returns the date of the given field.
     *
     * @param field the date field
     * @return the selected date from <tt>field</tt>
     */
    private Date getDate(DateField field) {
        Date date = field.getDateChooser().getSelectedDate().getTime();
        return DateRules.getDate(date); // truncate any time component
    }

    /**
     * Sets the date of the given field.
     *
     * @param field the date field
     * @param date  the date to set
     */
    private void setDate(DateField field, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        field.getDateChooser().setSelectedDate(calendar);
    }

}
