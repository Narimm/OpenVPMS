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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import echopointng.DateField;
import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.bound.BoundDateFieldFactory;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ComponentHelper;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;


/**
 * Component for selecting a range of dates.
 *
 * @author Tim Anderson
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
     * Indicates if all dates should be selected. If so, the from and to dates are ignored.
     */
    private SimpleProperty all = new SimpleProperty("all", true, Boolean.class, Messages.get("daterange.all"));

    /**
     * The from date.
     */
    private SimpleProperty from = new SimpleProperty("from", null, Date.class, Messages.get("daterange.from"));

    /**
     * The to date.
     */
    private SimpleProperty to = new SimpleProperty("to", null, Date.class, Messages.get("daterange.to"));

    /**
     * The all-dates component.
     */
    private ComponentState allDates;
    /**
     * The from-date component.
     */
    private ComponentState fromDate;

    /**
     * The to-date component.
     */
    private ComponentState toDate;

    /**
     * The component.
     */
    private Component component;


    /**
     * Constructs a {@link DateRange}.
     *
     * @param focus the focus group
     */
    public DateRange(FocusGroup focus) {
        this(focus, true);
    }

    /**
     * Constructs a {@link DateRange}.
     *
     * @param focus   the focus group
     * @param showAll determines if an 'all' checkbox should be displayed to select all dates
     */
    public DateRange(FocusGroup focus, boolean showAll) {
        this.focus = focus;
        this.showAll = showAll;
        if (showAll) {
            all.addModifiableListener(new ModifiableListener() {
                @Override
                public void modified(Modifiable modifiable) {
                    onAllDatesChanged();
                }
            });
        }
        from.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onFromChanged();
            }
        });
        to.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onToChanged();
            }
        });
        Date today = DateRules.getToday();
        setFrom(today);
        setTo(today);
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or {@code null} to query all dates
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
        from.setValue(date);
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or {@code null} to query all dates
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
        to.setValue(date);
    }

    /**
     * Sets the state of the <em>allDates</em> checkbox, if present.
     *
     * @param selected the state of the <em>allDates</em> checkbox
     */
    public void setAllDates(boolean selected) {
        if (showAll) {
            all.setValue(selected);
        }
    }

    /**
     * Sets the enabled state of the date range.
     *
     * @param enabled if {@code true} enable the component otherwise disable it
     */
    public void setEnabled(boolean enabled) {
        if (allDates != null) {
            allDates.getComponent().setEnabled(enabled);
        }
        if (enabled && !getAllDates()) {
            setDateFieldsEnabled(true);
        } else {
            setDateFieldsEnabled(false);
        }
    }

    /**
     * Determines if all dates are being selected.
     *
     * @return {@code true} if all dates are being selected
     */
    public boolean getAllDates() {
        return showAll && all.getValue() != null && (Boolean) all.getValue();
    }

    /**
     * Renders the component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            component = doLayout();
        }
        return component;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    protected Component doLayout() {
        fromDate = createFromDate(from);
        toDate = createToDate(to);
        if (showAll) {
            allDates = createAllDates(all);
        } else {
            allDates = null;
        }
        Component container = getContainer();
        if (allDates != null) {
            container.add(allDates.getLabel());
            container.add(allDates.getComponent());
            focus.add(allDates.getComponent());
        }
        container.add(fromDate.getLabel());
        container.add(fromDate.getComponent());
        container.add(toDate.getLabel());
        container.add(toDate.getComponent());

        onAllDatesChanged();

        focus.add(fromDate.getComponent());
        focus.add(toDate.getComponent());
        return container;
    }

    /**
     * Returns a container to render the component.
     *
     * @return the container
     */
    protected Component getContainer() {
        return RowFactory.create(Styles.CELL_SPACING);
    }

    /**
     * Creates a component to render the "all dates" property.
     *
     * @param allDates the "all dates" property
     * @return a new component
     */
    protected ComponentState createAllDates(Property allDates) {
        return new ComponentState(new BoundCheckBox(allDates), allDates);
    }

    /**
     * Creates a component to render the "from date" property.
     *
     * @param from the "from date" property
     * @return a new component
     */
    protected ComponentState createFromDate(Property from) {
        return new ComponentState(BoundDateFieldFactory.create(from), from);
    }

    /**
     * Creates a component to render the "to date" property.
     *
     * @param to the "to date" property
     * @return a new component
     */
    protected ComponentState createToDate(Property to) {
        return new ComponentState(BoundDateFieldFactory.create(to), to);
    }

    /**
     * Invoked when the 'from' date changes. Sets the 'to' date = 'from' if 'from' is greater.
     */
    private void onFromChanged() {
        Date from = getFrom();
        Date to = getTo();
        if (from != null && to != null && DateRules.compareDates(from, to) > 0) {
            setTo(from);
        }
    }

    /**
     * Invoked when the 'to' date changes. Sets the 'from' date = 'to' if 'from' is greater.
     */
    private void onToChanged() {
        Date from = getFrom();
        Date to = getTo();
        if (from != null && to != null && DateRules.compareDates(from, to) > 0) {
            setFrom(to);
        }
    }

    /**
     * Invoked when the 'all dates' check box changes.
     */
    protected void onAllDatesChanged() {
        boolean enabled = !getAllDates();
        setDateFieldsEnabled(enabled);
    }

    /**
     * Enables/disables the date fields.
     *
     * @param enabled if {@code true}, enable them, else disable them
     */
    private void setDateFieldsEnabled(boolean enabled) {
        ComponentHelper.enable(fromDate.getLabel(), enabled);
        ComponentHelper.enable((DateField) fromDate.getComponent(), enabled);
        ComponentHelper.enable(toDate.getLabel(), enabled);
        ComponentHelper.enable((DateField) toDate.getComponent(), enabled);
    }

    /**
     * Returns the date of the given field.
     *
     * @param property the date property
     * @return the selected date
     */
    private Date getDate(SimpleProperty property) {
        Date date = (Date) property.getValue();
        return DateRules.getDate(date); // truncate any time component
    }

}
