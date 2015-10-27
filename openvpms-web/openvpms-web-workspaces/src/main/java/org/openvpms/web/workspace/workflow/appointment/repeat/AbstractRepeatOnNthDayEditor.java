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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import nextapp.echo2.app.SelectField;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.component.im.list.PairListModel;
import org.openvpms.web.component.property.NumericPropertyTransformer;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.resource.i18n.Messages;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;

/**
 * A {@link RepeatExpressionEditor} that supports expressions that repeat on the first..fifth/last Sunday-Saturday of
 * the month.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRepeatOnNthDayEditor extends AbstractRepeatExpressionEditor {

    /**
     * "1".."5" or "L" for first...fifth or last.
     */
    private final SimpleProperty ordinal = new SimpleProperty("ordinal", String.class);

    /**
     * The day to repeat on.
     */
    private final SimpleProperty day = new SimpleProperty("day", String.class);

    /**
     * The repeat interval.
     */
    private final SimpleProperty interval = new SimpleProperty(
            "interval", null, Integer.class, Messages.get("workflow.scheduling.appointment.interval"));

    /**
     * Constructs an {@link AbstractRepeatOnNthDayEditor}.
     *
     * @param startTime the expression start time. May be {@code null}
     */
    public AbstractRepeatOnNthDayEditor(Date startTime) {
        this();
        setStartTime(startTime);
        if (startTime != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTime);
            day.setValue(DayOfWeek.getDay(calendar.get(Calendar.DAY_OF_WEEK)));
            ordinal.setValue(calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
        }
    }

    /**
     * Constructs an {@link AbstractRepeatOnNthDayEditor}.
     *
     * @param expression the source expression
     */
    public AbstractRepeatOnNthDayEditor(CronRepeatExpression expression) {
        this();
        DayOfWeek dayOfWeek = expression.getDayOfWeek();
        day.setValue(dayOfWeek.getDay());
        if (dayOfWeek.getOrdinal() == DayOfWeek.LAST) {
            ordinal.setValue("L");
        } else {
            ordinal.setValue(dayOfWeek.getOrdinal());
        }
    }

    /**
     * Default constructor.
     */
    private AbstractRepeatOnNthDayEditor() {
        ordinal.setRequired(true);
        day.setRequired(true);
        interval.setRequired(true);
        interval.setTransformer(new NumericPropertyTransformer(interval, true));
        interval.setValue(1);
    }

    /**
     * Sets the repeat interval.
     *
     * @param interval the interval
     */
    public void setInterval(int interval) {
        this.interval.setValue(interval);
    }

    /**
     * Returns the repeat interval.
     *
     * @return the interval
     */
    public int getInterval() {
        return interval.getInt();
    }

    /**
     * Returns the Cron day-of-week specification.
     *
     * @return the day-of-week specification, or {@code null} if it is invalid
     */
    public DayOfWeek getDayOfWeek() {
        DayOfWeek result = null;
        String nth = ordinal.getString();
        String d = this.day.getString();
        if (nth != null && d != null) {
            result = "L".equals(nth) ? DayOfWeek.last(d) : DayOfWeek.nth(d, Integer.parseInt(nth));
        }
        return result;
    }

    /**
     * Creates an ordinal select field.
     *
     * @return a new select field
     */
    protected SelectField createOrdinalSelector() {
        PairListModel ordinalModel = new PairListModel();
        ordinalModel.add("1", Messages.get("workflow.scheduling.appointment.first"));
        ordinalModel.add("2", Messages.get("workflow.scheduling.appointment.second"));
        ordinalModel.add("3", Messages.get("workflow.scheduling.appointment.third"));
        ordinalModel.add("4", Messages.get("workflow.scheduling.appointment.fourth"));
        ordinalModel.add("5", Messages.get("workflow.scheduling.appointment.fifth"));
        ordinalModel.add("L", Messages.get("workflow.scheduling.appointment.last"));
        SelectField ordinalField = BoundSelectFieldFactory.create(ordinal, ordinalModel);
        ordinalField.setCellRenderer(PairListModel.RENDERER);
        return ordinalField;
    }

    /**
     * Creates a day select field.
     *
     * @return a new select field
     */
    protected SelectField createDaySelector() {
        PairListModel dayModel = new PairListModel();
        String[] weekdays = DateFormatSymbols.getInstance().getWeekdays();
        for (int i = 0; i < 7; ++i) {
            int day = Calendar.SUNDAY + i;
            String name = weekdays[day];
            dayModel.add(DayOfWeek.getDay(day), name);
        }

        SelectField dayField = BoundSelectFieldFactory.create(day, dayModel);
        dayField.setCellRenderer(PairListModel.RENDERER);
        return dayField;
    }

    /**
     * Creates an interval field.
     *
     * @return a new field
     */
    protected SpinBox createIntervalField() {
        return new SpinBox(interval, 1, 12);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return ordinal.validate(validator) && day.validate(validator) && interval.validate(validator);
    }
}
