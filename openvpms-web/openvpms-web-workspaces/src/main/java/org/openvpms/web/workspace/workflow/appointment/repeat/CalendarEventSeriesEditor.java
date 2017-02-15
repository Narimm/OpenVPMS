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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
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
 * An editor for {@link CalendarEventSeries}.
 *
 * @author Tim Anderson
 */
public class CalendarEventSeriesEditor extends AbstractModifiable {

    /**
     * The event series.
     */
    private final CalendarEventSeries series;

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
     * The listeners.
     */
    private final ModifiableListeners listeners = new ModifiableListeners();

    /**
     * Listener for changes to the expression and condition editors.
     */
    private final ModifiableListener listener;

    /**
     * Constructs an {@link CalendarEventSeriesEditor}.
     *
     * @param series the event series
     */
    public CalendarEventSeriesEditor(CalendarEventSeries series) {
        this.series = series;
        listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                resetValid(false);
                listeners.notifyListeners(modifiable);
            }
        };
        setExpression(series.getExpression());
        setCondition(series.getCondition());
    }

    /**
     * Sets the repeat expression.
     *
     * @param expression the expression. May be {@code null}
     */
    public void setExpression(RepeatExpression expression) {
        RepeatExpressionEditor editor = null;
        if (expression instanceof CalendarRepeatExpression) {
            CalendarRepeatExpression calendar = (CalendarRepeatExpression) expression;
            if (calendar.getInterval() == 1) {
                editor = new SimpleRepeatEditor(calendar);
            } else {
                editor = new RepeatEveryEditor(calendar);
            }
        } else if (expression instanceof CronRepeatExpression) {
            CronRepeatExpression cron = (CronRepeatExpression) expression;
            if (RepeatOnWeekdaysEditor.supports(cron)) {
                editor = new RepeatOnWeekdaysEditor();
            } else if (RepeatOnDaysEditor.supports(cron)) {
                editor = new RepeatOnDaysEditor(cron);
            } else if (RepeatOnNthDayEditor.supports(cron)) {
                editor = new RepeatOnNthDayEditor(cron);
            } else if (RepeatOnDaysOfMonthEditor.supports(cron)) {
                editor = new RepeatOnDaysOfMonthEditor(cron);
            } else if (RepeatOnDateEditor.supports(cron)) {
                editor = new RepeatOnDateEditor(cron);
            } else if (RepeatOnNthDayInMonthEditor.supports(cron)) {
                editor = new RepeatOnNthDayInMonthEditor(cron);
            }
        }
        setRepeatEditor(editor);
    }

    /**
     * Sets the repeat condition.
     *
     * @param condition the repeat condition. May be {@code null}
     */
    public void setCondition(RepeatCondition condition) {
        RepeatUntilEditor editor = null;

        if (condition instanceof RepeatUntilDateCondition) {
            editor = new RepeatUntilDateEditor((RepeatUntilDateCondition) condition);
        } else if (condition instanceof RepeatNTimesCondition) {
            editor = new RepeatNTimesEditor((RepeatNTimesCondition) condition);
        }
        setUntilEditor(editor);
    }

    /**
     * Returns the current series.
     *
     * @return the series
     */
    public CalendarEventSeries getSeries() {
        updateSeries();
        return series;
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
        return series.isModified();
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
        listeners.addListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    @Override
    public void addModifiableListener(ModifiableListener listener, int index) {
        listeners.addListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
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
     * Saves the series.
     */
    public void save() {
        series.save();
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
        updateSeries();
        if (repeatEditor != null) {
            if (untilEditor != null) {
                valid = repeatEditor.validate(validator) && untilEditor.validate(validator) && noOverlaps(validator);
            }
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * Updates the underlying series.
     */
    protected void updateSeries() {
        series.refresh();
        if (repeatEditor != null) {
            repeatEditor.setStartTime(series.getStartTime());
            series.setExpression(repeatEditor.getExpression());
        } else {
            series.setExpression(null);
        }
        if (untilEditor != null) {
            series.setCondition(untilEditor.getCondition());
        } else {
            series.setCondition(null);
        }
    }

    private boolean noOverlaps(Validator validator) {
        boolean result;
        CalendarEventSeries.Overlap overlap = series.getFirstOverlap();
        if (overlap != null) {
            result = false;
            Times event1 = overlap.getEvent1();
            Times event2 = overlap.getEvent2();
            String displayName = DescriptorHelper.getDisplayName(series.getEvent());
            String startTime1 = formatDateTime(event1.getStartTime());
            String endTime1 = formatDateTimeAbbrev(event1.getEndTime(), event1.getStartTime());
            String startTime2 = formatDateTime(event2.getStartTime());
            String endTime2 = formatDateTimeAbbrev(event2.getEndTime(), event2.getStartTime());
            String message = Messages.format("workflow.scheduling.appointment.overlap",
                                             displayName, startTime1, endTime1, startTime2, endTime2);
            validator.add(this, new ValidatorError(message));
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
        listeners.notifyListeners(this);
    }

    /**
     * Sets the repeat editor.
     *
     * @param editor the editor. May be {@code null}
     */
    private void setRepeatEditor(RepeatExpressionEditor editor) {
        if (repeatEditor != null) {
            repeatEditor.removeModifiableListener(listener);
            repeatGroup.remove(repeatEditor.getFocusGroup());
        }
        this.repeatEditor = editor;
        if (repeatContainer != null) {
            repeatContainer.removeAll();
            if (repeatEditor != null) {
                repeatContainer.add(repeatEditor.getComponent());
                repeatGroup.add(repeatEditor.getFocusGroup());
                repeatContainer.add(repeatSelector);
                repeatEditor.addModifiableListener(listener);

                if (untilEditor == null) {
                    setUntilEditor(new RepeatNTimesEditor());
                }
            } else {
                repeatContainer.add(LabelFactory.create("workflow.scheduling.appointment.norepeat"));
                setUntilEditor(null);
            }
            repeatContainer.add(repeatSelector);
        }
        resetValid();
    }

    private void onSelected(RepeatUntilEditor editor) {
        untilSelector.setExpanded(false);
        setUntilEditor(editor);
        refreshUntil();
        // need to refresh the drop-down otherwise the editor appears both selected and in the drop-down
        listeners.notifyListeners(this);
    }

    /**
     * Sets the repeat-until editor.
     *
     * @param editor the editor. May be {@code null}
     */
    private void setUntilEditor(RepeatUntilEditor editor) {
        if (untilEditor != null) {
            untilEditor.removeModifiableListener(listener);
            untilGroup.remove(untilEditor.getFocusGroup());
        }
        untilEditor = editor;
        if (untilContainer != null) {
            untilContainer.removeAll();
            if (untilEditor != null) {
                untilEditor.addModifiableListener(listener);
                untilContainer.add(untilEditor.getComponent());
                untilGroup.add(untilEditor.getFocusGroup());
                untilContainer.add(getUntilSelector());
            }
        }
        resetValid();
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
        ObjectTabPaneModel<ExpressionTab> model = new ObjectTabPaneModel<>(null);
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

        private List<RadioButton> buttons = new ArrayList<>();
        private List<Component> components = new ArrayList<>();

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
            model.add(new RepeatOnNthDayEditor(series.getStartTime()));
            model.add(new RepeatOnDaysOfMonthEditor(series.getStartTime()));
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
            model.add(new RepeatOnDateEditor(series.getStartTime()));
            model.add(new RepeatOnNthDayInMonthEditor(series.getStartTime()));
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
