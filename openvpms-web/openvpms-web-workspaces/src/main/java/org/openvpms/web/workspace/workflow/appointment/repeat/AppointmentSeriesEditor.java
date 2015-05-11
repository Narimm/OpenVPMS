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
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.popup.DropDown;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.StyleTableCellRenderer;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.web.resource.i18n.format.DateFormatter.formatDateTime;
import static org.openvpms.web.resource.i18n.format.DateFormatter.formatDateTimeAbbrev;

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
     * Container for the repeat selector.
     */
    private Component repeatContainer;

    /**
     * The repeat selector drop-down.
     */
    private DropDown repeatSelector;

    /**
     * The selected repeat editor. May be {@code null}
     */
    private RepeatExpressionEditor repeatEditor;

    /**
     * The tab pane model.
     */
    private ObjectTabPaneModel<ExpressionTab> repeatModel;

    /**
     * Container for the until selector, if there is a repeat expression. Empty otherwise
     */
    private Component untilContainer;

    /**
     * The until selector drop-down.
     */
    private DropDown untilSelector;

    /**
     * The selected until editor. May be {@code null}
     */
    private RepeatUntilEditor untilEditor;

    /**
     * The repeat until table.
     */
    private Table untilTable;

    /**
     * The repeat expression editor focus group.
     */
    private final FocusGroup repeatGroup = new FocusGroup(RepeatExpressionEditor.class.getSimpleName());

    /**
     * The repeat until editor focus group.
     */
    private final FocusGroup untilGroup = new FocusGroup(RepeatUntilEditor.class.getSimpleName());


    /**
     * Constructs an {@link AppointmentSeriesEditor}.
     *
     * @param series the appointment series
     */
    public AppointmentSeriesEditor(AppointmentSeries series) {
        this.series = series;

        RepeatExpression expression = series.getExpression();
        if (expression instanceof CalendarRepeatExpression) {
            CalendarRepeatExpression calendar = (CalendarRepeatExpression) expression;
            if (calendar.getInterval() == 1) {
                repeatEditor = new SimpleRepeatEditor(calendar);
            } else {
                repeatEditor = new RepeatEveryEditor(calendar);
            }
        } else if (expression instanceof CronRepeatExpression) {
            CronRepeatExpression cron = (CronRepeatExpression) expression;
            if (RepeatOnDaysEditor.supports(cron)) {
                repeatEditor = new RepeatOnDaysEditor(series.getStartTime(), cron);
            } else if (RepeatOnOrdinalDayEditor.supports(cron)) {
                repeatEditor = new RepeatOnOrdinalDayEditor(series.getStartTime(), cron);
            }
        }
        RepeatCondition condition = series.getCondition();
        if (condition instanceof RepeatUntilDateCondition) {
            untilEditor = new RepeatUntilDateEditor((RepeatUntilDateCondition) condition);
        } else if (condition instanceof RepeatNTimesCondition) {
            untilEditor = new RepeatNTimesEditor((RepeatNTimesCondition) condition);
        }
        if (repeatEditor != null) {
            repeatGroup.add(repeatEditor.getFocusGroup());
        }
        if (untilEditor != null) {
            untilGroup.add(untilEditor.getFocusGroup());
        }
    }

    /**
     * Refreshes the editor.
     * <p/>
     * This should be invoked if the appointment start time changes.
     */
    public void refresh() {
        refreshRepeat();
    }

    /**
     * Returns the editor component.
     *
     * @return the component
     */
    public Component getRepeatEditor() {
        if (repeatContainer == null) {
            repeatContainer = RowFactory.create(Styles.CELL_SPACING);
            if (repeatSelector == null) {
                repeatModel = createRepeatModel();
                TabbedPane tabs = TabbedPaneFactory.create(repeatModel);
                repeatSelector = createDropDown(null, RowFactory.create(tabs));
                // need to put the tabs in a row for Chrome, otherwise it renders to the width of the dialog.
                repeatSelector.setBorder(BorderEx.NONE);
                repeatSelector.setRolloverBorder(BorderEx.NONE);
                repeatSelector.setPopUpAlwaysOnTop(true);
                repeatSelector.setFocusOnExpand(true);
            }
            Component component = (repeatEditor == null)
                                  ? LabelFactory.create("workflow.scheduling.appointment.norepeat")
                                  : repeatEditor.getComponent();
            repeatContainer.add(component);
            repeatContainer.add(repeatSelector);
        }
        return repeatContainer;
    }

    /**
     * Returns the focus group for the repeat expression editor.
     *
     * @return the focus group
     */
    public FocusGroup getRepeatFocusGroup() {
        return repeatGroup;
    }

    /**
     * Returns the condition editor component.
     *
     * @return the condition editor component
     */
    public Component getUntilEditor() {
        if (untilContainer == null) {
            untilContainer = RowFactory.create(Styles.CELL_SPACING);
            if (untilEditor != null) {
                untilContainer.add(untilEditor.getComponent());
                untilContainer.add(getUntilSelector());
            }
        }
        return untilContainer;
    }

    /**
     * Returns the condition editor focus group.
     *
     * @return the focus group
     */
    public FocusGroup getUntilFocusGroup() {
        return untilGroup;
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
        boolean valid = false;
        if (repeatEditor == null) {
            series.setExpression(null);
            series.setCondition(null);
        } else {
            series.refresh();
            series.setExpression(repeatEditor.getExpression());
            series.setCondition(untilEditor.getCondition());
        }
        if (repeatEditor == null) {
            valid = true;
        } else if (untilEditor != null) {
            valid = repeatEditor.validate(validator) && untilEditor.validate(validator) && noOverlaps(validator);
        }
        return valid;
    }

    private boolean noOverlaps(Validator validator) {
        boolean result;
        AppointmentSeries.Overlap overlap = series.getFirstOverlap();
        if (overlap != null) {
            result = false;
            AppointmentSeries.Times appointment1 = overlap.getAppointment1();
            AppointmentSeries.Times appointment2 = overlap.getAppointment2();
            String startTime1 = formatDateTime(appointment1.getStartTime(), false);
            String endTime1 = formatDateTimeAbbrev(appointment1.getEndTime(), appointment1.getStartTime());
            String startTime2 = formatDateTime(appointment2.getStartTime(), false);
            String endTime2 = formatDateTimeAbbrev(appointment2.getEndTime(), appointment2.getStartTime());
            validator.add(this, new ValidatorError(Messages.format("workflow.scheduling.appointment.overlap",
                                                                   startTime1, endTime1, startTime2, endTime2)));
        } else {
            result = true;
        }
        return result;
    }

    private Component getUntilSelector() {
        if (untilSelector == null) {
            untilTable = createTable(createUntilModel());
            if (untilEditor == null) {
                untilEditor = new RepeatUntilDateEditor(DateRules.getDate(series.getStartTime(), 1, DateUnits.YEARS));
            }
            untilSelector = createDropDown(null, untilTable);
        }
        return untilSelector;
    }


    private void onSelected(RepeatExpressionEditor editor) {
        repeatSelector.setExpanded(false);
        setRepeatEditor(editor);
        refreshRepeat();
        // need to refresh the drop-down otherwise the editor appears both selected and in the drop-down
    }

    /**
     * Sets the repeat editor.
     *
     * @param editor the editor. May be {@code null}
     */
    private void setRepeatEditor(RepeatExpressionEditor editor) {
        if (repeatEditor != null) {
            repeatGroup.remove(repeatEditor.getFocusGroup());
        }
        this.repeatEditor = editor;
        repeatContainer.removeAll();
        if (editor != null) {
            RepeatExpression expression = editor.getExpression();
            repeatContainer.add(editor.getComponent());
            repeatGroup.add(editor.getFocusGroup());
            series.setExpression(expression);
            repeatContainer.add(repeatSelector);

            if (untilEditor == null) {
                setUntilEditor(new RepeatNTimesEditor());
            }
        } else {
            repeatContainer.add(LabelFactory.create("workflow.scheduling.appointment.norepeat"));
            setUntilEditor(null);
        }
        repeatContainer.add(repeatSelector);
    }

    private void onSelected(RepeatUntilEditor editor) {
        untilSelector.setExpanded(false);
        setUntilEditor(editor);
        refreshUntil();
        // need to refresh the drop-down otherwise the editor appears both selected and in the drop-down
    }

    /**
     * Sets the repeat-until editor.
     *
     * @param editor the editor. May be {@code null}
     */
    private void setUntilEditor(RepeatUntilEditor editor) {
        if (untilEditor != null) {
            untilGroup.remove(untilEditor.getFocusGroup());
        }
        untilEditor = editor;
        untilContainer.removeAll();
        if (untilEditor != null) {
            untilContainer.add(editor.getComponent());
            untilGroup.add(editor.getFocusGroup());
            untilContainer.add(getUntilSelector());
        }
    }

    private void refreshRepeat() {
        if (repeatModel != null) {
            for (int i = 0; i < repeatModel.size(); ++i) {
                repeatModel.getObject(i).refresh();
            }
        }
    }

    private void refreshUntil() {
        if (untilTable != null) {
            untilTable.setModel(createUntilModel());
        }
    }

    private DropDown createDropDown(Component target, Component dropDown) {
        DropDown result = new DropDown(target, dropDown);
        result.setBorder(BorderEx.NONE);
        result.setRolloverBorder(BorderEx.NONE);
        result.setPopUpAlwaysOnTop(true);
        result.setFocusOnExpand(true);
        return result;
    }

    /**
     * Creates the repeat tab model.
     *
     * @return a new tab model
     */
    private ObjectTabPaneModel<ExpressionTab> createRepeatModel() {
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

    private UntilTableModel createUntilModel() {
        UntilTableModel model = new UntilTableModel();
        model.add(Messages.get("workflow.scheduling.appointment.once"), new RepeatNTimesEditor(1));
        model.add(Messages.get("workflow.scheduling.appointment.twice"), new RepeatNTimesEditor(2));
        model.add(new RepeatNTimesEditor());
        model.add(new RepeatUntilDateEditor(DateRules.getDate(series.getStartTime(), 1, DateUnits.YEARS)));
        return model;
    }

    private static Table createTable(TableModel model) {
        Table table = TableFactory.create(model, "plain");
        table.setHeaderVisible(false);
        table.setInsets(new Insets(10));
        table.setDefaultRenderer(Object.class, new StyleTableCellRenderer("Table.Row-Inset"));
        table.setSelectionEnabled(false);
        table.setRolloverEnabled(false);
        return table;
    }

    private static class ButtonTableModel extends AbstractTableModel {

        private final ButtonGroup group = new ButtonGroup();

        private List<RadioButton> buttons = new ArrayList<RadioButton>();
        private List<Component> components = new ArrayList<Component>();

        public RadioButton add(Component component) {
            RadioButton button = new RadioButton();
            button.setGroup(group);
            buttons.add(button);
            this.components.add(component);
            return button;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return components.size();
        }

        @Override
        public Object getValueAt(int column, int row) {
            switch (column) {
                case 0:
                    return buttons.get(row);
                case 1:
                    return components.get(row);
            }
            return null;
        }
    }

    private class RepeatTableModel extends ButtonTableModel {

        public RadioButton add(final RepeatExpressionEditor repeat) {
            RadioButton button = super.add(repeat.getComponent());
            button.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onSelected(repeat);
                }
            });
            return button;
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
        private final Table table;


        /**
         * Constructs an {@link ExpressionTab}.
         *
         * @param displayName the tab display name
         */
        public ExpressionTab(String displayName) {
            this.displayName = displayName;
            table = createTable(createTableModel());
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
            if (repeatEditor != null) {
                RadioButton noRepeat = model.add(LabelFactory.create("workflow.scheduling.appointment.norepeat"));
                noRepeat.addActionListener(new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        onSelected((RepeatExpressionEditor) null);
                    }
                });
            }
            model.add(new SimpleRepeatEditor(Repeats.daily()));
            model.add(new SimpleRepeatEditor(Repeats.weekdays(series.getStartTime())));
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
            model.add(new SimpleRepeatEditor(Repeats.weekly()));
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
            model.add(new SimpleRepeatEditor(Repeats.monthly()));
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
            model.add(new SimpleRepeatEditor(Repeats.yearly()));
            model.add(new RepeatEveryEditor(DateUnits.YEARS));
            return model;
        }
    }

    private class UntilTableModel extends ButtonTableModel {

        public RadioButton add(String displayName, final RepeatUntilEditor editor) {
            Label label = LabelFactory.create();
            label.setText(displayName);
            return add(editor, label);
        }

        public RadioButton add(final RepeatUntilEditor until) {
            return add(until, until.getComponent());
        }

        private RadioButton add(final RepeatUntilEditor editor, Component component) {
            RadioButton button = add(component);
            button.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onSelected(editor);
                }
            });
            return button;
        }

    }
}
