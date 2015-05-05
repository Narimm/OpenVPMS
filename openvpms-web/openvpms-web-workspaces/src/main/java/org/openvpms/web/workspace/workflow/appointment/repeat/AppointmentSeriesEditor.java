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

import echopointng.BorderEx;
import echopointng.TabbedPane;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.AbstractTableModel;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.popup.DropDown;
import org.openvpms.web.echo.table.EvenOddTableCellRenderer;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.echo.text.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * An editor for {@link AppointmentSeries}.
 *
 * @author Tim Anderson
 */
public class AppointmentSeriesEditor {

    /**
     * The appointment series.
     */
    private final AppointmentSeries series;

    private final TabbedPane tabs;

    private TextField label;

    private DropDown dropDown;

    /**
     * The selected repeat editor.
     */
    private RepeatExpressionEditor editor;

    /**
     * Constructs an {@link AppointmentSeriesEditor}.
     *
     * @param series the appointment series
     */
    public AppointmentSeriesEditor(AppointmentSeries series) {
        this.series = series;
        label = TextComponentFactory.create();
        ObjectTabPaneModel<ExpressionTab> model = createModel();

        tabs = TabbedPaneFactory.create(model);
        dropDown = new DropDown(label, tabs);

        RepeatExpression expression = series.getExpression();
        if (expression instanceof CalendarRepeatExpression) {
            CalendarRepeatExpression calendar = (CalendarRepeatExpression) expression;
            if (calendar.getInterval() == 1) {
                editor = new SimpleRepeatEditor(calendar);
            } else {
                editor = new RepeatEveryEditor(calendar);
            }
        } else if (expression instanceof CronRepeatExpression) {
            CronRepeatExpression cron = (CronRepeatExpression) expression;
            if (RepeatOnDaysEditor.supports(cron)) {
                editor = new RepeatOnDaysEditor(series.getStartTime(), cron);
            } else if (RepeatOnOrdinalDayEditor.supports(cron)) {
                editor = new RepeatOnOrdinalDayEditor(series.getStartTime(), cron);
            }
        }
        if (editor != null) {
            dropDown.setTarget(editor.getComponent());
        }
        dropDown.setBorder(BorderEx.NONE);
        dropDown.setRolloverBorder(BorderEx.NONE);
        dropDown.setPopUpAlwaysOnTop(true);
        dropDown.setFocusOnExpand(true);
    }

    public void refresh() {
        //   tabs.setModel(createModel());
    }

    public boolean isValid() {
        if (editor == null) {
            series.setExpression(null);
        } else {
            series.setExpression(editor.getExpression());
        }
        return editor == null || editor.isValid();
    }

    public Component getComponent() {
        return dropDown;
    }

    private void onSelected(RepeatExpressionEditor editor) {
        this.editor = editor;
        RepeatExpression expression = editor.getExpression();
        if (expression != null) {
            dropDown.setTarget(editor.getComponent());
            dropDown.setExpanded(false);
        } else {
            label.setText(null);
        }
        series.setExpression(expression);
    }

    private ObjectTabPaneModel<ExpressionTab> createModel() {
        ObjectTabPaneModel<ExpressionTab> model = new ObjectTabPaneModel<ExpressionTab>(null);
        addTab(model, new Daily());
        addTab(model, new Weekly());
        addTab(model, new Monthly());
        addTab(model, new Yearly());
        return model;
    }

    private void addTab(ObjectTabPaneModel<ExpressionTab> model, ExpressionTab tab) {
        model.addTab(tab, tab.getDisplayName(), tab.getComponent());
    }

    private class RepeatTableModel extends AbstractTableModel {

        private ButtonGroup group = new ButtonGroup();
        private List<RadioButton> buttons = new ArrayList<RadioButton>();
        private List<RepeatExpressionEditor> repeat = new ArrayList<RepeatExpressionEditor>();

        public void add(final RepeatExpressionEditor repeat) {
            RadioButton button = new RadioButton();
            button.setGroup(group);
            buttons.add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onSelected(repeat);
                }
            });
            this.repeat.add(repeat);
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return repeat.size();
        }

        @Override
        public Object getValueAt(int column, int row) {
            switch (column) {
                case 0:
                    return buttons.get(row);
                case 1:
                    return repeat.get(row).getComponent();
            }
            return null;
        }
    }

    private abstract class ExpressionTab {

        private final String displayName;

        private Table table;


        public ExpressionTab(String displayName) {
            this.displayName = displayName;
            table = TableFactory.create(createTableModel());
            table.setHeaderVisible(false);
            table.setDefaultRenderer(Object.class, EvenOddTableCellRenderer.INSTANCE);
            table.setSelectionEnabled(false);
            table.setRolloverEnabled(false);
        }

        public String getDisplayName() {
            return displayName;
        }

        public Component getComponent() {
            return table;
        }

        protected abstract RepeatTableModel createTableModel();
    }

    private class Daily extends ExpressionTab {

        public Daily() {
            super("Daily");
        }

        @Override
        protected RepeatTableModel createTableModel() {
            RepeatTableModel model = new RepeatTableModel();
            model.add(new SimpleRepeatEditor(RepeatExpressions.daily()));
            model.add(new SimpleRepeatEditor(RepeatExpressions.weekdays(series.getStartTime())));
            model.add(new RepeatEveryEditor(DateUnits.DAYS));
            model.add(new RepeatOnDaysEditor(series.getStartTime()));
            return model;
        }
    }

    private class Weekly extends ExpressionTab {

        public Weekly() {
            super("Weekly");
        }

        @Override
        protected RepeatTableModel createTableModel() {
            RepeatTableModel model = new RepeatTableModel();
            model.add(new SimpleRepeatEditor(RepeatExpressions.weekly()));
            model.add(new RepeatEveryEditor(DateUnits.WEEKS));
            return model;
        }
    }

    private class Monthly extends ExpressionTab {

        public Monthly() {
            super("Monthly");
        }

        @Override
        protected RepeatTableModel createTableModel() {
            RepeatTableModel model = new RepeatTableModel();
            model.add(new SimpleRepeatEditor(RepeatExpressions.monthly()));
            model.add(new RepeatEveryEditor(DateUnits.MONTHS));
            model.add(new RepeatOnOrdinalDayEditor(series.getStartTime()));
            return model;
        }
    }

    private class Yearly extends ExpressionTab {

        public Yearly() {
            super("Yearly");
        }

        @Override
        protected RepeatTableModel createTableModel() {
            RepeatTableModel model = new RepeatTableModel();
            model.add(new SimpleRepeatEditor(RepeatExpressions.yearly()));
            model.add(new RepeatEveryEditor(DateUnits.YEARS));
            return model;
        }
    }
}
