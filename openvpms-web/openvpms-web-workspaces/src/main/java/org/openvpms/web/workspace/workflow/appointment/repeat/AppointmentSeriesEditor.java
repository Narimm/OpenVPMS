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
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.popup.DropDown;
import org.openvpms.web.echo.table.EvenOddTableCellRenderer;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.appointment.AppointmentActEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * An editor for {@link AppointmentSeries}.
 *
 * @author Tim Anderson
 */
public class AppointmentSeriesEditor extends AbstractModifiable {

    /**
     * The appointment series.
     */
    private final AppointmentSeries series;

    /**
     * The appointment editor
     */
    private final AppointmentActEditor appointmentEditor;

    /**
     * The repeat selector drop-down.
     */
    private DropDown dropDown;

    /**
     * The selected repeat editor. May be {@code null}
     */
    private RepeatExpressionEditor editor;

    /**
     * The label to use if no series is present.
     */
    private TextField label;

    /**
     * The tab pane model.
     */
    private ObjectTabPaneModel<ExpressionTab> model;

    /**
     * Constructs an {@link AppointmentSeriesEditor}.
     *
     * @param series the appointment series
     */
    public AppointmentSeriesEditor(AppointmentSeries series, AppointmentActEditor appointmentEditor) {
        this.series = series;
        this.appointmentEditor = appointmentEditor;
        label = TextComponentFactory.create();

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
    }

    /**
     * Refreshes the editor.
     * <p/>
     * This should be invoked if the appointment start time changes.
     */
    public void refresh() {
        if (model != null) {
            for (int i = 0; i < model.size(); ++i) {
                model.getObject(i).refresh();
            }
        }
    }

    /**
     * Returns the editor component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (dropDown == null) {
            model = createModel();
            TabbedPane tabs = TabbedPaneFactory.create(model);
            dropDown = new DropDown(label, tabs);
            if (editor != null) {
                dropDown.setTarget(editor.getComponent());
            }
            dropDown.setBorder(BorderEx.NONE);
            dropDown.setRolloverBorder(BorderEx.NONE);
            dropDown.setPopUpAlwaysOnTop(true);
            dropDown.setFocusOnExpand(true);
        }
        return dropDown;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        // no-op
    }

    /**
     * Adds a listener to be notified when this changes.
     * <p/>
     * Listeners will be notified in the order they were registered.
     *
     * @param listener the listener to add
     */
    @Override
    public void addModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    @Override
    public void addModifiableListener(ModifiableListener listener, int index) {
        // no-op
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        // no-op
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return null;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        if (editor == null) {
            series.setExpression(null);
        } else {
            series.setSchedule(appointmentEditor.getSchedule());
            series.setAppointmentType(appointmentEditor.getAppointmentType());
            series.setCustomer(appointmentEditor.getCustomer());
            series.setPatient(appointmentEditor.getPatient());
            series.setClinician(appointmentEditor.getClinician());
            series.setAuthor(appointmentEditor.getAuthor());
            series.setExpression(editor.getExpression());
        }
        return editor == null || (editor.validate(validator) && noOverlaps(validator));
    }

    private boolean noOverlaps(Validator validator) {
        RepeatExpression expression = series.getExpression();
        return true;
    }

    /**
     * Invoked when an editor is selected.
     *
     * @param editor the editor
     */
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
        refresh();  // need to refresh the drop-down otherwise the editor appears both selected and in the drop-down
    }

    /**
     * Creates the tab model.
     *
     * @return a new tab model
     */
    private ObjectTabPaneModel<ExpressionTab> createModel() {
        ObjectTabPaneModel<ExpressionTab> model = new ObjectTabPaneModel<ExpressionTab>(null);
        addTab(model, new Daily());
        addTab(model, new Weekly());
        addTab(model, new Monthly());
        addTab(model, new Yearly());
        return model;
    }

    /**
     * Helper to add a tab.
     *
     * @param model the tab model
     * @param tab   the tab to add
     */
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

    /**
     * Repeat expression tab.
     */
    private abstract class ExpressionTab {

        /**
         * The tab display name.
         */
        private final String displayName;

        /**
         * Table of repeat expressions.
         */
        private Table table;


        /**
         * Constructs an {@link ExpressionTab}.
         *
         * @param displayName the tab display name
         */
        public ExpressionTab(String displayName) {
            this.displayName = displayName;
            table = TableFactory.create(createTableModel());
            table.setHeaderVisible(false);
            table.setDefaultRenderer(Object.class, EvenOddTableCellRenderer.INSTANCE);
            table.setSelectionEnabled(false);
            table.setRolloverEnabled(false);
        }

        /**
         * Returns the tab display name.
         *
         * @return the tab display name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the tab component.
         *
         * @return the tab component
         */
        public Component getComponent() {
            return table;
        }

        /**
         * Refreshes the tab.
         */
        public void refresh() {
            table.setModel(createTableModel());
        }

        /**
         * Creates a new expression table model.
         *
         * @return a new model
         */
        protected abstract RepeatTableModel createTableModel();

    }

    /**
     * Tab for daily repeat expressions.
     */
    private class Daily extends ExpressionTab {

        public Daily() {
            super(Messages.get("workflow.scheduling.appointment.daily"));
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

    /**
     * Tab for weekly repeat expressions.
     */
    private class Weekly extends ExpressionTab {

        public Weekly() {
            super(Messages.get("workflow.scheduling.appointment.weekly"));
        }

        @Override
        protected RepeatTableModel createTableModel() {
            RepeatTableModel model = new RepeatTableModel();
            model.add(new SimpleRepeatEditor(RepeatExpressions.weekly()));
            model.add(new RepeatEveryEditor(DateUnits.WEEKS));
            return model;
        }
    }

    /**
     * Tab for monthly repeat expressions.
     */
    private class Monthly extends ExpressionTab {

        public Monthly() {
            super(Messages.get("workflow.scheduling.appointment.monthly"));
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

    /**
     * Tab for yearly repeat expressions.
     */
    private class Yearly extends ExpressionTab {

        public Yearly() {
            super(Messages.get("workflow.scheduling.appointment.yearly"));
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
