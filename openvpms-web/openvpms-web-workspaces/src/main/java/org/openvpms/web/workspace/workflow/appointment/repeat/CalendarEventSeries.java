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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import net.sf.jasperreports.engine.util.ObjectUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.act.ActHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Calendar event series.
 *
 * @author Tim Anderson
 */
public class CalendarEventSeries {

    /**
     * Used to indicate overlapping events.
     */
    public static class Overlap {
        private final Times event1;

        private final Times event2;

        public Overlap(Times event1, Times event2) {
            this.event1 = event1;
            this.event2 = event2;
        }

        public Times getEvent1() {
            return event1;
        }

        public Times getEvent2() {
            return event2;
        }
    }

    /**
     * The default maximum number of events.
     */
    public static final int DEFAULT_MAX_EVENTS = 365;

    /**
     * The event.
     */
    private final Act event;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The maximum no. of events that can be created.
     */
    private final int maxEvents;

    /**
     * The series or {@code null}, if the event isn't associated with a series.
     */
    private Act series;

    /**
     * The acts in the series.
     */
    private List<Act> acts;

    /**
     * The prior state.
     */
    private State previous;

    /**
     * The current state.
     */
    private State current;

    /**
     * If {@code true} only update the times of existing events in the series.
     */
    private boolean updateTimesOnly;

    /**
     * Constructs an {@link CalendarEventSeries}.
     *
     * @param event   the event. An appointment or calendar block
     * @param service the archetype service
     */
    public CalendarEventSeries(Act event, IArchetypeService service) {
        this(event, service, DEFAULT_MAX_EVENTS);
    }

    /**
     * Constructs an {@link CalendarEventSeries}.
     *
     * @param event     the event. An appointment or calendar block
     * @param service   the archetype service
     * @param maxEvents the maximum no. of appointments in a series
     */
    public CalendarEventSeries(Act event, IArchetypeService service, int maxEvents) {
        this.event = event;
        this.service = service;
        this.maxEvents = maxEvents;
        ActBean bean = new ActBean(event, service);
        series = (Act) bean.getNodeSourceObject("repeat");
        if (series != null) {
            previous = createState(bean);
            ActBean seriesBean = new ActBean(series, service);
            acts = getEvents(event, seriesBean);

            previous.setExpression(RepeatHelper.getExpression(seriesBean));
            int index = acts.indexOf(event);
            previous.setCondition(RepeatHelper.getCondition(seriesBean, index));
            current = copy(previous);
        } else {
            current = createState(bean);
            acts = new ArrayList<>();
        }
    }

    /**
     * Returns the event.
     *
     * @return the event
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Invoked to notify this of any event changes.
     */
    public void refresh() {
        current.update(new ActBean(event, service));
    }

    /**
     * Returns the repeat expression for this series.
     *
     * @return the repeat expression, or {@code null} if none has been configured
     */
    public RepeatExpression getExpression() {
        return current.getExpression();
    }

    /**
     * Sets the repeat expression.
     *
     * @param expression the repeat expression. May be [@code null}
     */
    public void setExpression(RepeatExpression expression) {
        current.setExpression(expression);
    }

    /**
     * Returns the repeat-until condition for this series.
     *
     * @return the condition, or {@code null} if none has been configured
     */
    public RepeatCondition getCondition() {
        return current.getCondition();
    }

    /**
     * Sets the repeat condition.
     *
     * @param condition the condition. May be {@code null}
     */
    public void setCondition(RepeatCondition condition) {
        current.setCondition(condition);
    }

    /**
     * Returns the first overlapping events.
     *
     * @return the first overlapping events, or {@code null} if none overlap
     */
    public Overlap getFirstOverlap() {
        return calculateSeries(new ArrayList<Times>());
    }

    /**
     * Determines if only the times of existing events should be updated.
     * <p>
     * This should be set {@code true} when moving a series.
     *
     * @param updateTimesOnly if {@code true}, only update the times, otherwise update all event fields
     */
    public void setUpdateTimesOnly(boolean updateTimesOnly) {
        this.updateTimesOnly = updateTimesOnly;
    }

    /**
     * Determines if the expression or condition has been modified.
     *
     * @return {@code true} if the expression or condition has been modified
     */
    public boolean isModified() {
        refresh();
        return !ObjectUtils.equals(previous, current);
    }

    /**
     * Saves the series.
     */
    public void save() {
        refresh();
        if (isModified()) {
            if (previous != null && !current.repeats()) {
                deleteSeries();
            } else if (previous != null) {
                if (!previous.repeats() && current.repeats()) {
                    createEvents();
                } else {
                    updateSeries();
                }
            } else if (current.repeats()) {
                createEvents();
            }
            previous = copy(current);
        }
    }

    /**
     * Returns the series act.
     *
     * @return the series act, or {@code null} if the event isn't associated with a series
     */
    public Act getSeries() {
        return series;
    }

    /**
     * Returns the events that make up the series.
     *
     * @return the events
     */
    public List<Act> getEvents() {
        if (series != null) {
            ActBean bean = new ActBean(series, service);
            return ActHelper.sort(bean.getNodeActs("items"));
        }
        return Collections.emptyList();
    }

    /**
     * Calculates the series.
     *
     * @return the series, or {@code null} if the events overlap
     */
    public List<Times> getEventTimes() {
        ArrayList<Times> result = new ArrayList<>();
        result.add(Times.create(event));
        Overlap overlap = calculateSeries(result);
        return overlap == null ? result : null;
    }

    /**
     * Returns the time that the series starts.
     *
     * @return the time
     */
    public Date getStartTime() {
        return current.getStartTime();
    }

    /**
     * Copies state.
     *
     * @param state the state to copy
     * @return a copy of {@code state}
     */
    protected State copy(State state) {
        return new State(state);
    }

    /**
     * Creates state from an act.
     *
     * @param bean the act bean
     * @return a new state
     */
    protected State createState(ActBean bean) {
        return new State(bean);
    }

    /**
     * Creates a new event linked to the series.
     *
     * @param times      the event times
     * @param seriesBean the series
     * @return the appointment
     */
    protected Act create(Times times, ActBean seriesBean) {
        Act act = (Act) service.create(event.getArchetypeId());
        ActBean bean = populate(act, times, current);
        bean.setNodeParticipant("author", current.getAuthor());
        seriesBean.addNodeRelationship("items", act);
        return act;
    }

    /**
     * Updates an event.
     * <p>
     * If {@link #updateTimesOnly} is {@code false}, then {@link #populate(ActBean, State)} will be invoked to
     * populate the event with the {@code state}.
     *
     * @param act   the event
     * @param times the event times
     * @param state the state to populate the event from
     * @return the event
     */
    protected ActBean populate(Act act, Times times, State state) {
        act.setActivityStartTime(times.getStartTime());
        act.setActivityEndTime(times.getEndTime());

        ActBean bean = new ActBean(act, service);
        bean.setNodeParticipant("schedule", state.getSchedule());
        if (!updateTimesOnly) {
            populate(bean, state);
        }
        return bean;
    }

    /**
     * Populates an event from state. This is invoked after the event times and schedule have been set.
     *
     * @param bean  the event bean
     * @param state the state
     */
    protected void populate(ActBean bean, State state) {
    }

    /**
     * Determines if the series can be calculated.
     *
     * @param state the current event state
     * @return {@code true} if the series can be calculated
     */
    protected boolean canCalculateSeries(State state) {
        return state.getSchedule() != null;
    }

    /**
     * Returns the archetype service.
     *
     * @return the service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Calculates the times for the event series.
     *
     * @param series used to collect the times
     * @return the first overlapping event, or {@code null} if there are no overlaps
     */
    private Overlap calculateSeries(List<Times> series) {
        Overlap overlap = null;
        int index = acts.indexOf(event);
        if (current.repeats() && (acts.isEmpty() || index >= 0)) {
            Date startTime = event.getActivityStartTime();
            Date endTime = event.getActivityEndTime();
            Duration duration = new Duration(new DateTime(startTime), new DateTime(endTime));

            if (canCalculateSeries(current)) {
                List<Times> times = new ArrayList<>();
                times.add(Times.create(event));
                ListIterator<Act> iterator = (index + 1 < acts.size()) ? acts.listIterator(index + 1) : null;
                RepeatExpression expression = current.getExpression();
                RepeatCondition condition = current.getCondition();
                Predicate<Date> max = new TimesPredicate<>(maxEvents - 1);
                Predicate<Date> predicate = PredicateUtils.andPredicate(max, condition.create());
                while ((startTime = expression.getRepeatAfter(startTime, predicate)) != null) {
                    endTime = new DateTime(startTime).plus(duration).toDate();
                    IMObjectReference reference = null;
                    if (iterator != null && iterator.hasNext()) {
                        Act act = iterator.next();
                        reference = act.getObjectReference();
                    }
                    Times newEvent = new Times(reference, startTime, endTime);
                    overlap = getOverlap(times, newEvent);
                    if (overlap != null) {
                        break;
                    }
                    times.add(newEvent);
                    series.add(newEvent);
                }
            }
        }
        return overlap;
    }

    private Overlap getOverlap(List<Times> series, Times event) {
        Overlap overlap = null;
        int index = Collections.binarySearch(series, event);
        if (index >= 0) {
            overlap = new Overlap(series.get(index), event);
        }
        return overlap;
    }

    /**
     * Creates events corresponding to the expression.
     */
    private void createEvents() {
        List<Times> times = new ArrayList<>();
        calculateSeries(times);
        acts.clear();
        acts.add(event);
        series = createSeries();
        ActBean seriesBean = populateSeries(series, 0);

        List<Act> toSave = new ArrayList<>();
        seriesBean.addNodeRelationship("items", event);
        toSave.add(event);

        toSave.add(series);
        for (Times t : times) {
            Act act = create(t, seriesBean);
            acts.add(act);
            toSave.add(act);
        }
        service.save(toSave);
    }

    /**
     * Creates a new calendar event series.
     *
     * @return a new <em>act.calendarEventSeries</em> act
     */
    private Act createSeries() {
        series = (Act) service.create(ScheduleArchetypes.CALENDAR_EVENT_SERIES);
        series.setActivityStartTime(event.getActivityStartTime());
        return series;
    }

    /**
     * Updates a series.
     *
     * @return {@code true} if changes were made
     */
    private boolean updateSeries() {
        boolean result;
        List<Times> times = new ArrayList<>();
        Overlap overlap = calculateSeries(times);
        if (overlap != null) {
            result = false;
        } else {
            int index = acts.indexOf(event);
            if (index >= 0) {
                List<Act> future = Collections.emptyList();
                if (index + 1 < acts.size()) {
                    future = acts.subList(index + 1, acts.size());
                }
                updateSeries(future, times, index);
                acts = new ArrayList<>(acts.subList(index, acts.size()));
                result = true;
            } else {
                // shouldn't occur
                result = false;
            }
        }
        return result;
    }

    /**
     * Updates a series.
     *
     * @param acts             the acts to update
     * @param times            the new times
     * @param actsPriorToEvent the no. of acts in the series prior to the event
     */
    private void updateSeries(List<Act> acts, List<Times> times, int actsPriorToEvent) {
        Act oldSeries = series;
        boolean createSeries = !current.repeatEquals(previous);
        // create a new series if the repeat expression or condition has changed

        Act currentSeries = (createSeries) ? createSeries() : series;
        ActBean bean = populateSeries(currentSeries, actsPriorToEvent);
        ActBean oldBean = (createSeries) ? new ActBean(oldSeries, service) : bean;

        acts = new ArrayList<>(acts);                 // copy to avoid modifying source
        Iterator<Times> timesIterator = times.iterator();
        Iterator<Act> iterator = acts.listIterator();
        List<Act> toSave = new ArrayList<>();

        toSave.add(event);

        while (timesIterator.hasNext()) {
            Act act;
            if (iterator.hasNext()) {
                act = iterator.next();
                iterator.remove();
                populate(act, timesIterator.next(), current);
                if (oldSeries != currentSeries) {
                    oldBean.removeNodeRelationships("items", act);
                    bean.addNodeRelationship("items", act);
                }
            } else {
                act = create(timesIterator.next(), bean);
            }
            toSave.add(act);
        }

        if (oldSeries != currentSeries) {
            oldBean.removeNodeRelationships("items", event);
            bean.addNodeRelationship("items", event);
        }

        // any remaining acts need to be removed. Detach them from their series
        for (Act act : acts) {
            oldBean.removeNodeRelationships("items", act);
            toSave.add(act);
        }

        if (!toSave.isEmpty()) {
            if (oldSeries != currentSeries) {
                toSave.add(oldSeries);
            }
            toSave.add(currentSeries);
            service.save(toSave);
        }
        if (!acts.isEmpty()) {
            for (Act act : acts) {
                service.remove(act);
            }
        }
    }

    /**
     * Deletes the events after the current event, and unlinks it from the series.
     * If no acts reference the series act, it is also removed.
     */
    private void deleteSeries() {
        int index = acts.indexOf(event);
        if (index >= 0) {
            List<Act> future = Collections.emptyList();
            if (index + 1 < acts.size()) {
                // delete the acts after the event.
                future = acts.subList(index + 1, acts.size());
            }
            deleteSeries(future);
            acts.clear();
        }
    }

    /**
     * Deletes the series.
     *
     * @param acts the acts to delete
     */
    private void deleteSeries(List<Act> acts) {
        ActBean bean = new ActBean(series, service);
        for (Act act : acts) {
            bean.removeNodeRelationships("items", act);
        }
        bean.removeNodeRelationships("items", event);
        List<Act> toSave = new ArrayList<>(acts);
        toSave.add(series);
        toSave.add(event);
        service.save(toSave);
        for (Act act : acts) {
            service.remove(act);
        }
        if (bean.getValues("items", ActRelationship.class).isEmpty()) {
            service.remove(series);
        }
        series = null;
    }

    /**
     * Populates the series with the current expression and condition.
     *
     * @param series           the series to populate
     * @param actsPriorToEvent the no. of acts in the series prior to the event
     * @return the series bean
     */
    private ActBean populateSeries(Act series, int actsPriorToEvent) {
        ActBean seriesBean = new ActBean(series, service);
        String expr = null;
        Integer interval = null;
        String units = null;
        Date endTime = null;
        Integer times = null;
        RepeatExpression expression = current.getExpression();
        RepeatCondition condition = current.getCondition();
        if (expression instanceof CalendarRepeatExpression) {
            CalendarRepeatExpression calendar = (CalendarRepeatExpression) expression;
            interval = calendar.getInterval();
            units = calendar.getUnits().toString();
        } else {
            expr = ((CronRepeatExpression) expression).getExpression();
        }
        if (condition instanceof RepeatUntilDateCondition) {
            endTime = ((RepeatUntilDateCondition) condition).getDate();
        } else {
            times = ((RepeatNTimesCondition) condition).getTimes() + actsPriorToEvent;
        }
        seriesBean.setValue("interval", interval);
        seriesBean.setValue("units", units);
        seriesBean.setValue("expression", expr);
        seriesBean.setValue("endTime", endTime);
        seriesBean.setValue("times", times);
        return seriesBean;
    }

    /**
     * Returns all of the acts in the series.
     *
     * @param event  the event
     * @param series the series
     * @return all of the acts in the series
     */
    private List<Act> getEvents(Act event, ActBean series) {
        List<IMObjectReference> items = series.getNodeTargetObjectRefs("items");
        items.remove(event.getObjectReference());
        List<Act> result;
        result = ActHelper.getActs(items);
        result.add(event);
        return ActHelper.sort(result);
    }

    /**
     * Event series state.
     */
    protected static class State {

        /**
         * The event start time.
         */
        private Date startTime;

        /**
         * The event end time.
         */
        private Date endTime;

        /**
         * The schedule.
         */
        private IMObjectReference schedule;

        /**
         * The author.
         */
        private IMObjectReference author;

        /**
         * The expression.
         */
        private RepeatExpression expression;

        /**
         * The condition.
         */
        private RepeatCondition condition;


        /**
         * Initialises the state from an event.
         *
         * @param event the event
         */
        public State(ActBean event) {
            update(event);
        }

        /**
         * Copy constructor.
         *
         * @param state the state to copy
         */
        public State(State state) {
            this.startTime = state.startTime;
            this.endTime = state.endTime;
            this.schedule = state.schedule;
            this.author = state.author;
            this.expression = state.expression;
            this.condition = state.condition;
        }

        /**
         * Updates the state from an event.
         *
         * @param event the event
         */
        public void update(ActBean event) {
            Act act = event.getAct();
            startTime = act.getActivityStartTime();
            endTime = act.getActivityEndTime();
            schedule = event.getNodeParticipantRef("schedule");
            author = event.getNodeParticipantRef("author");
        }

        /**
         * Returns the event start time.
         *
         * @return the start time
         */
        public Date getStartTime() {
            return startTime;
        }

        /**
         * Returns the expression.
         *
         * @return the expression. May be {@code null}
         */
        public RepeatExpression getExpression() {
            return expression;
        }

        /**
         * Sets the expression.
         *
         * @param expression the expression. May be {@code null}
         */
        public void setExpression(RepeatExpression expression) {
            this.expression = expression;
        }

        /**
         * Sets the condition.
         *
         * @param condition the condition. May be {@code null}
         */
        public void setCondition(RepeatCondition condition) {
            this.condition = condition;
        }

        /**
         * Returns the condition.
         *
         * @return the condition. May be {@code null}
         */
        public RepeatCondition getCondition() {
            return condition;
        }

        public boolean repeats() {
            return expression != null && condition != null;
        }

        public IMObjectReference getSchedule() {
            return schedule;
        }

        public IMObjectReference getAuthor() {
            return author;
        }

        public boolean repeatEquals(State other) {
            return new EqualsBuilder()
                    .append(expression, other.expression)
                    .append(condition, other.condition)
                    .isEquals();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         */
        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (obj == this) {
                result = true;
            } else if (!(obj instanceof State)) {
                result = false;
            } else {
                State other = (State) obj;
                if (DateRules.compareTo(startTime, other.startTime) != 0
                    || DateRules.compareTo(endTime, other.endTime) != 0) {
                    result = false;
                } else {
                    result = new EqualsBuilder()
                            .append(schedule, other.schedule)
                            .append(author, other.author)
                            .append(expression, other.expression)
                            .append(condition, other.condition)
                            .isEquals();
                }
            }
            return result;
        }

    }

}
