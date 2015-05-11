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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.AbstractListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.property.NumericPropertyTransformer;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Month;

/**
 * A {@link RepeatExpressionEditor} that supports expressions that repeat on the first..fifth/last Sunday-Saturday of
 * the month.
 *
 * @author Tim Anderson
 */
class RepeatOnOrdinalDayEditor extends AbstractRepeatExpressionEditor {

    /**
     * The repeat start time.
     */
    private final Date startTime;

    /**
     * "1".."5" or "L" for first...fifth or last.
     */
    private final SimpleProperty ordinal = new SimpleProperty("ordinal", String.class);

    /**
     * The day to repeat on.
     */
    private final SimpleProperty day = new SimpleProperty("day", String.class);

    /**
     * The month interval.
     */
    private final SimpleProperty interval
            = new SimpleProperty("interval", null, Integer.class,
                                 Messages.get("workflow.scheduling.appointment.interval"));


    /**
     * Constructs an {@link RepeatOnOrdinalDayEditor}.
     *
     * @param startTime the repeat start time
     */
    public RepeatOnOrdinalDayEditor(Date startTime) {
        this(startTime, null);
    }

    /**
     * Constructs an {@link RepeatOnOrdinalDayEditor}.
     *
     * @param startTime  the repeat start time
     * @param expression the source expression. May be {@code null}
     */
    public RepeatOnOrdinalDayEditor(Date startTime, CronRepeatExpression expression) {
        this.startTime = startTime;
        ordinal.setRequired(true);
        day.setRequired(true);
        interval.setRequired(true);
        interval.setTransformer(new NumericPropertyTransformer(interval, true));
        if (expression != null) {
            DayOfWeek dayOfWeek = expression.getDayOfWeek();
            day.setValue(dayOfWeek.getDay());
            if (dayOfWeek.getOrdindal() == DayOfWeek.LAST) {
                ordinal.setValue("L");
            } else {
                ordinal.setValue(dayOfWeek.getOrdindal());
            }
            Month month = expression.getMonth();
            interval.setValue(month.getInterval());
        } else {
            interval.setValue(1);
        }
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        PairListModel ordinalModel = new PairListModel();
        ordinalModel.add(1, Messages.get("workflow.scheduling.appointment.first"));
        ordinalModel.add(2, Messages.get("workflow.scheduling.appointment.second"));
        ordinalModel.add(3, Messages.get("workflow.scheduling.appointment.third"));
        ordinalModel.add(4, Messages.get("workflow.scheduling.appointment.fourth"));
        ordinalModel.add(5, Messages.get("workflow.scheduling.appointment.fifth"));
        ordinalModel.add("L", Messages.get("workflow.scheduling.appointment.last"));
        PairListModel dayModel = new PairListModel();
        String[] weekdays = DateFormatSymbols.getInstance().getWeekdays();
        for (int i = 0; i < 7; ++i) {
            int day = Calendar.SUNDAY + i;
            String name = weekdays[day];
            dayModel.add(DayOfWeek.getDay(day), name);
        }
        Label the = LabelFactory.create("workflow.scheduling.appointment.onthe");
        Label every = LabelFactory.create();
        every.setText(Messages.get("workflow.scheduling.appointment.every").toLowerCase());
        Label months = LabelFactory.create("workflow.scheduling.appointment.months");
        SelectField ordinalField = BoundSelectFieldFactory.create(ordinal, ordinalModel);
        ordinalField.setWidth(new Extent(10, Extent.EM));
        ordinalField.setSelectedIndex(0);
        ordinalField.setCellRenderer(PairListModel.RENDERER);
        SelectField dayField = BoundSelectFieldFactory.create(day, dayModel);
        dayField.setCellRenderer(PairListModel.RENDERER);
        dayField.setWidth(new Extent(10, Extent.EM));
        dayField.setSelectedIndex(0);
        return RowFactory.create(Styles.CELL_SPACING, the, ordinalField, dayField, every,
                                 BoundTextComponentFactory.create(interval, 5), months);
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    @Override
    public RepeatExpression getExpression() {
        String nth = ordinal.getString();
        String day = this.day.getString();
        if (nth != null && day != null) {
            DayOfWeek dayOfWeek = "L".equals(nth) ? DayOfWeek.lastDay(day) : new DayOfWeek(day, Integer.parseInt(nth));
            return new CronRepeatExpression(startTime, Month.every(interval.getInt()), dayOfWeek);
        }
        return null;
    }

    /**
     * Determines if the editor can edit the supplied expression.
     *
     * @param expression the expression
     * @return {@code true} if the editor can edit the expression
     */
    public static boolean supports(CronRepeatExpression expression) {
        DayOfWeek dayOfWeek = expression.getDayOfWeek();
        DayOfMonth dayOfMonth = expression.getDayOfMonth();
        return dayOfWeek.isOrdinal() && dayOfMonth.isAll();
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

    private static class PairListModel extends AbstractListModel {

        public static ListCellRenderer RENDERER = new ListCellRenderer() {
            @Override
            public Object getListCellRendererComponent(Component list, Object value, int index) {
                AbstractListComponent l = (AbstractListComponent) list;
                PairListModel model = (PairListModel) l.getModel();
                return model.getValue(index);
            }
        };

        private List<Pair> pairs = new ArrayList<Pair>();

        private static class Pair {
            final Object key;
            final Object value;

            public Pair(Object key, Object value) {
                this.key = key;
                this.value = value;
            }
        }

        /**
         * Returns the value at the specified index in the list.
         *
         * @param index the index
         * @return the value
         */
        @Override
        public Object get(int index) {
            return pairs.get(index).key;
        }

        public Object getValue(int index) {
            return pairs.get(index).value;
        }

        public void add(Object key, Object value) {
            pairs.add(new Pair(key, value));
        }

        /**
         * Returns the size of the list.
         *
         * @return the size
         */
        @Override
        public int size() {
            return pairs.size();
        }
    }

}
