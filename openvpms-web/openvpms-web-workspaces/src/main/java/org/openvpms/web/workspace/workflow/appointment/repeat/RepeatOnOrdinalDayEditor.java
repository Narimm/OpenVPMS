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
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;

/**
 * A {@link RepeatExpressionEditor} that supports expressions that repeat on the first..fifth/last Sunday-Saturday of the month.
 *
 * @author Tim Anderson
 */
class RepeatOnOrdinalDayEditor implements RepeatExpressionEditor {

    /**
     * The repeat start time.
     */
    private final Date startTime;

    private SimpleProperty ordinal = new SimpleProperty("ordinal", String.class);
    private SimpleProperty day = new SimpleProperty("day", String.class);
    private SimpleProperty interval = new SimpleProperty("interval", Integer.class);


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
        if (expression != null) {
            DayOfWeek dayOfWeek = expression.getDayOfWeek();
            day.setValue(dayOfWeek.getDay());
            if (dayOfWeek.getOrdindal() == DayOfWeek.LAST) {
                ordinal.setValue("L");
            } else {
                ordinal.setValue(dayOfWeek.getOrdindal());
            }
        }
        interval.setValue(1);
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        PairListModel ordinalModel = new PairListModel();
        ordinalModel.add(1, "First");
        ordinalModel.add(2, "Second");
        ordinalModel.add(3, "Third");
        ordinalModel.add(4, "Fourth");
        ordinalModel.add(5, "Fifth");
        ordinalModel.add("L", "Last");
        PairListModel dayModel = new PairListModel();
        String[] weekdays = DateFormatSymbols.getInstance().getWeekdays();
        for (int i = 0; i < 7; ++i) {
            int id = Calendar.SUNDAY + i;
            String name = weekdays[id];
            dayModel.add(id, name);
        }
        Label the = LabelFactory.create();
        the.setText("The");
        Label every = LabelFactory.create();
        every.setText("every");
        Label months = LabelFactory.create();
        months.setText("months");
        SelectField ordinalField = BoundSelectFieldFactory.create(ordinal, ordinalModel);
        ordinalField.setWidth(new Extent(10, Extent.EM));
        ordinalField.setSelectedIndex(0);
        ordinalField.setCellRenderer(PairListModel.RENDERER);
        SelectField dayField = BoundSelectFieldFactory.create(day, dayModel);
        dayField.setCellRenderer(PairListModel.RENDERER);
        dayField.setWidth(new Extent(10, Extent.EM));
        dayField.setSelectedIndex(0);
        return RowFactory.create(WIDE_CELL_SPACING, the, ordinalField, dayField, every,
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
            return new CronRepeatExpression(startTime, dayOfWeek);
        }
        return null;
    }

    /**
     * Determines if the editor is valid.
     *
     * @return {@code true} if the editor is valid
     */
    @Override
    public boolean isValid() {
        return false;
    }

    public static boolean supports(CronRepeatExpression expression) {
        DayOfWeek dayOfWeek = expression.getDayOfWeek();
        CronRepeatExpression.DayOfMonth dayOfMonth = expression.getDayOfMonth();
        return dayOfWeek.isOrdinal() && dayOfMonth.isAll();
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
