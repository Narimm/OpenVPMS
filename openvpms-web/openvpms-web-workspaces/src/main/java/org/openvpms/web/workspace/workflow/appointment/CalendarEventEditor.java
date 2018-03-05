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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Label;
import org.openvpms.archetype.i18n.time.DateDurationFormatter;
import org.openvpms.archetype.i18n.time.DurationFormatter;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeriesEditor;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeriesViewer;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatCondition;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatExpression;
import org.openvpms.web.workspace.workflow.scheduling.AbstractScheduleActEditor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * An editor for <em>act.customerAppointment</em> and <em>act.calendarBlock</em> acts.
 *
 * @author Tim Anderson
 */
public abstract class CalendarEventEditor extends AbstractScheduleActEditor {

    /**
     * The event slot size.
     */
    private int slotSize;

    /**
     * The event series.
     */
    private final CalendarEventSeries series;

    /**
     * The series editor.
     */
    private final CalendarEventSeriesEditor seriesEditor;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * The event duration.
     */
    private Label duration = LabelFactory.create();

    /**
     * The duration formatter.
     */
    private static DurationFormatter formatter = DateDurationFormatter.create(false, false, false, true, true, true);

    /**
     * Constructs an {@link CalendarEventEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public CalendarEventEditor(Act act, IMObject parent, LayoutContext context) {
        this(act, parent, false, context);
    }

    /**
     * Constructs an {@link CalendarEventEditor}.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be {@code null}
     * @param editSeries if {@code true}, edit the series
     * @param context    the layout context
     */
    public CalendarEventEditor(Act act, IMObject parent, boolean editSeries, LayoutContext context) {
        super(act, parent, context);
        rules = ServiceHelper.getBean(AppointmentRules.class);
        if (act.isNew()) {
            initParticipant("schedule", context.getContext().getSchedule());
        }

        Date startTime = getStartTime();
        if (startTime == null) {
            Date scheduleDate = context.getContext().getScheduleDate();
            if (scheduleDate != null) {
                startTime = getDefaultStartTime(scheduleDate);
                setStartTime(startTime, true);
            }
        }

        series = createSeries();
        if (editSeries) {
            seriesEditor = createSeriesEditor(series);
            seriesEditor.addModifiableListener(new ModifiableListener() {
                @Override
                public void modified(Modifiable modifiable) {
                    resetValid(false);
                }
            });
        } else {
            seriesEditor = null;
        }
        updateRelativeDate();
        updateDuration();
    }

    /**
     * Sets the schedule.
     *
     * @param schedule the schedule
     */
    public void setSchedule(Entity schedule) {
        if (setParticipant("schedule", schedule)) {
            onScheduleChanged(schedule);
        }
        calculateEndTime();
    }

    /**
     * Returns the schedule.
     *
     * @return the schedule. May be {@code null}
     */
    public Entity getSchedule() {
        return (Entity) getParticipant("schedule");
    }

    /**
     * Returns the event series.
     *
     * @return the series
     */
    public CalendarEventSeries getSeries() {
        return (seriesEditor != null) ? seriesEditor.getSeries() : series;
    }

    /**
     * Sets the series repeat expression.
     *
     * @param expression the expression. May be {@code null}
     */
    public void setExpression(RepeatExpression expression) {
        if (seriesEditor != null) {
            seriesEditor.setExpression(expression);
        }
    }

    /**
     * Sets the series repeat condition.
     *
     * @param condition the condition. May be {@code null}
     */
    public void setCondition(RepeatCondition condition) {
        if (seriesEditor != null) {
            seriesEditor.setCondition(condition);
        }
    }

    /**
     * Determines if the object has been changed.
     *
     * @return {@code true} if the object has been changed
     */
    @Override
    public boolean isModified() {
        return super.isModified() || (seriesEditor != null && seriesEditor.isModified());
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        super.clearModified();
        if (seriesEditor != null) {
            seriesEditor.clearModified();
        }
    }

    /**
     * Calculates the series.
     * <p/>
     * If only a single act is being edited, this returns the act time.
     *
     * @return the series, or {@code null} if events overlap
     */
    public List<Times> getEventTimes() {
        List<Times> result;
        if (seriesEditor != null) {
            result = series.getEventTimes();
        } else {
            result = Collections.singletonList(Times.create(getObject()));
        }
        return result;
    }

    /**
     * Creates a new event series.
     *
     * @return a new event series
     */
    protected CalendarEventSeries createSeries() {
        return new CalendarEventSeries(getObject(), ServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new {@link CalendarEventSeriesEditor}.
     *
     * @param series the series to edit
     * @return a new editor
     */
    protected CalendarEventSeriesEditor createSeriesEditor(CalendarEventSeries series) {
        return new CalendarEventSeriesEditor(series);
    }

    /**
     * Returns the appointment rules.
     *
     * @return the appointment rules
     */
    protected AppointmentRules getRules() {
        return rules;
    }

    /**
     * Returns the series editor.
     *
     * @return the series editor. May be {@code null}
     */
    protected CalendarEventSeriesEditor getSeriesEditor() {
        return seriesEditor;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && (seriesEditor == null || seriesEditor.validate(validator));
    }

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        super.doSave();
        if (seriesEditor != null) {
            seriesEditor.save();
        }
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        Entity schedule = getSchedule();
        initSchedule(schedule);

        if (getEndTime() == null) {
            calculateEndTime();
        }
    }

    /**
     * Invoked when the start time changes. Calculates the end time.
     */
    @Override
    protected void onStartTimeChanged() {
        Date start = getStartTime();
        if (start != null && slotSize != 0) {
            Date rounded = rules.getSlotTime(start, slotSize, false);
            if (DateRules.compareTo(start, rounded) != 0) {
                setStartTime(rounded, true);
            }
            if (seriesEditor != null) {
                seriesEditor.refresh();
            }
        }

        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        updateRelativeDate();
        updateDuration();
    }

    /**
     * Invoked when the end time changes. Recalculates the end time if it is less than the start time.
     */
    @Override
    protected void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                calculateEndTime();
            } else if (slotSize != 0) {
                Date rounded = rules.getSlotTime(end, slotSize, true);
                if (DateRules.compareTo(end, rounded) != 0) {
                    setEndTime(rounded, true);
                }
            }
        }
        updateDuration();
    }

    /**
     * Updates the end-time editor to base relative dates on the start time.
     */
    protected void updateRelativeDate() {
        getEndTimeEditor().setRelativeDate(getStartTime());
    }

    /**
     * Calculates the end time.
     */
    protected abstract void calculateEndTime();

    /**
     * Updates the duration display.
     */
    private void updateDuration() {
        Date startTime = getStartTime();
        Date endTime = getEndTime();
        if (startTime != null && endTime != null) {
            duration.setText(formatter.format(startTime, endTime));
        } else {
            duration.setText(null);
        }
    }

    /**
     * Invoked when the schedule is updated. This gets the new slot size.
     *
     * @param schedule the schedule. May be {@code null}
     */
    protected void onScheduleChanged(Entity schedule) {
        initSchedule(schedule);
    }

    /**
     * Initialises the editor with the schedule.
     *
     * @param schedule the schedule. May be {@code null}
     */
    protected void initSchedule(Entity schedule) {
        if (schedule != null) {
            slotSize = rules.getSlotSize(schedule);
        }
    }

    /**
     * Returns the slot size.
     *
     * @return the slot size, in minutes
     */
    protected int getSlotSize() {
        return slotSize;
    }

    /**
     * Calculates the default start time of an event, using the supplied date and current time.
     * The start time is rounded to the next nearest 'slot-size' interval.
     *
     * @param date the start date
     * @return the start time
     */
    private Date getDefaultStartTime(Date date) {
        int slotSize = 0;
        Party schedule = (Party) getParticipant("schedule");
        if (schedule != null) {
            slotSize = rules.getSlotSize(schedule);
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Calendar timeCal = new GregorianCalendar();
        timeCal.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (slotSize != 0) {
            int mins = calendar.get(Calendar.MINUTE);
            mins = ((mins / slotSize) * slotSize) + slotSize;
            calendar.set(Calendar.MINUTE, mins);
        }
        return calendar.getTime();
    }

    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Dummy property used to ensure the duration is displayed after the endTime node.
         */
        private SimpleProperty durationProperty = new SimpleProperty(
                "duration", null, String.class, Messages.get("workflow.scheduling.appointment.duration"));

        /**
         * Constructs a {@link LayoutStrategy}.
         */
        public LayoutStrategy() {
            super(new ArchetypeNodes());
            addComponent(new ComponentState(getStartTimeEditor()));
            addComponent(new ComponentState(getEndTimeEditor()));
            addComponent(new ComponentState(duration, durationProperty));
        }

        /**
         * Lays out components in a grid.
         *
         * @param object     the object to lay out
         * @param properties the properties
         * @param context    the layout context
         */
        @Override
        protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context) {
            ArchetypeNodes.insert(properties, END_TIME, durationProperty);
            ComponentGrid grid = super.createGrid(object, properties, context);

            Property repeat = getProperty("repeat");
            if (seriesEditor != null) {
                ComponentState repeatState = new ComponentState(seriesEditor.getRepeatEditor(), repeat,
                                                                seriesEditor.getRepeatFocusGroup());
                grid.add(repeatState, 2);
                ComponentState untilState = new ComponentState(seriesEditor.getUntilEditor(),
                                                               seriesEditor.getUntilFocusGroup());
                untilState.setLabel(new Label());
                grid.add(untilState);
            } else {
                CalendarEventSeriesViewer viewer = new CalendarEventSeriesViewer(series);
                grid.add(new ComponentState(viewer.getComponent(), repeat), 2);
            }

            return grid;
        }
    }

}
