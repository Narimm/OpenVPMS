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

import net.sf.jasperreports.engine.util.ObjectUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
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

/**
 * Appointment series.
 *
 * @author Tim Anderson
 */
public class AppointmentSeries {

    /**
     * Appointment start and end times.
     */
    public static class Times implements Comparable<Times> {

        private final Date startTime;
        private final Date endTime;

        public Times(Date startTime, Date endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Times) {
                return compareTo((Times) obj) == 0;
            }
            return false;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * @param object the object to be compared.
         */
        @Override
        public int compareTo(Times object) {
            Date startTime2 = object.getStartTime();
            Date endTime2 = object.getEndTime();
            if (DateRules.compareTo(startTime, startTime2) < 0 && DateRules.compareTo(endTime, startTime2) <= 0) {
                return -1;
            }
            if (DateRules.compareTo(startTime, endTime2) >= 0 && DateRules.compareTo(endTime, endTime2) > 0) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * Used to indicate overlapping appointments.
     */
    public static class Overlap {
        private final Times appointment1;

        private final Times appointment2;

        public Overlap(Times appointment1, Times appointment2) {
            this.appointment1 = appointment1;
            this.appointment2 = appointment2;
        }

        public Times getAppointment1() {
            return appointment1;
        }

        public Times getAppointment2() {
            return appointment2;
        }
    }

    /**
     * The default maximum number of appointments.
     */
    public static final int DEFAULT_MAX_APPOINTMENTS = 365;

    /**
     * The appointment.
     */
    private final Act appointment;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The series or {@code null}, if the appointment isn't associated with a series.
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
     * The maximum no. of appointments that can be created.
     */
    private final int maxAppointments;


    /**
     * Constructs an {@link AppointmentSeries}.
     *
     * @param appointment the appointment
     * @param service     the archetype service
     */
    public AppointmentSeries(Act appointment, IArchetypeService service) {
        this(appointment, service, DEFAULT_MAX_APPOINTMENTS);
    }

    /**
     * Constructs an {@link AppointmentSeries}.
     *
     * @param appointment     the appointment
     * @param service         the archetype service
     * @param maxAppointments the maximum no. of appointments in a series
     */
    public AppointmentSeries(Act appointment, IArchetypeService service, int maxAppointments) {
        this.appointment = appointment;
        this.service = service;
        this.maxAppointments = maxAppointments;
        ActBean bean = new ActBean(appointment, service);
        series = (Act) bean.getNodeSourceObject("repeat");
        if (series != null) {
            previous = new State(bean);
            ActBean seriesBean = new ActBean(series, service);
            acts = getAppointments(appointment, seriesBean);

            int interval = seriesBean.getInt("interval", -1);
            DateUnits units = DateUnits.fromString(seriesBean.getString("units"));
            if (interval != -1 && units != null) {
                previous.setExpression(new CalendarRepeatExpression(interval, units));
            } else {
                String expression = seriesBean.getString("expression");
                if (!StringUtils.isEmpty(expression)) {
                    previous.setExpression(CronRepeatExpression.parse(expression));
                }
            }

            int times = seriesBean.getInt("times", -1);
            Date endTime = series.getActivityEndTime();
            if (times > 0) {
                int index = acts.indexOf(appointment);
                if (index > 0) {
                    // if the series isn't being edited from the start, adjust the no. of repeats
                    times -= index;
                }
                previous.setCondition(Repeats.times(times));
            } else if (endTime != null) {
                previous.setCondition(Repeats.until(endTime));
            }
            current = new State(previous);
        } else {
            current = new State(bean);
            acts = new ArrayList<Act>();
        }
    }

    /**
     * Invoked to notify this of any appointment changes.
     */
    public void refresh() {
        current.update(new ActBean(appointment, service));
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
     * Returns the first overlapping appointments.
     *
     * @return the first overlapping appointments, or {@code null} if none overlap
     */
    public Overlap getFirstOverlap() {
        return calculateSeries(new ArrayList<Times>());
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
     *
     * @return {@code true} if any changes were made
     */
    public boolean save() {
        boolean result = false;
        refresh();
        if (isModified()) {
            if (previous != null && !current.repeats()) {
                result = deleteSeries();
            } else if (previous != null && current.repeats()) {
                result = updateSeries();
            } else if (current.repeats()) {
                List<Times> series = new ArrayList<Times>();
                Overlap overlap = calculateSeries(series);
                if (overlap == null) {
                    createAppointments(series);
                    result = true;
                }
            }
            if (result) {
                previous = new State(current);
            }
        }
        return result;
    }

    /**
     * Returns the series act.
     *
     * @return the series act, or {@code null} if the appointment isn't associated with a series
     */
    public Act getSeries() {
        return series;
    }

    /**
     * Returns the appointments that make up the series.
     *
     * @return the appointments
     */
    public List<Act> getAppointments() {
        if (series != null) {
            ActBean bean = new ActBean(series, service);
            return ActHelper.sort(bean.getNodeActs("items"));
        }
        return Collections.emptyList();
    }

    /**
     * Returns the time that the series starts.
     *
     * @return the time
     */
    public Date getStartTime() {
        return series != null ? series.getActivityStartTime() : appointment.getActivityStartTime();
    }

    /**
     * Calculates the times for the appointment series.
     *
     * @param series used to collect the times
     * @return the first overlapping appointment, or {@code null} if there are no overlaps
     */
    private Overlap calculateSeries(List<Times> series) {
        Date startTime = appointment.getActivityStartTime();
        Date endTime = appointment.getActivityEndTime();
        Duration duration = new Duration(new DateTime(startTime), new DateTime(endTime));
        Overlap overlap = null;
        IMObjectReference schedule = current.getSchedule();
        IMObjectReference appointmentType = current.getAppointmentType();

        if (current.repeats() && schedule != null && appointmentType != null) {
            RepeatExpression expression = current.getExpression();
            RepeatCondition condition = current.getCondition();
            Predicate<Date> max = new TimesPredicate<Date>(maxAppointments - 1);
            Predicate<Date> predicate = PredicateUtils.andPredicate(max, condition.create());
            while ((startTime = expression.getRepeatAfter(startTime, predicate)) != null) {
                endTime = new DateTime(startTime).plus(duration).toDate();
                Times appointment = new Times(startTime, endTime);
                overlap = getOverlap(series, appointment);
                if (overlap != null) {
                    break;
                }
                series.add(appointment);
            }
        }
        return overlap;
    }

    private Overlap getOverlap(List<Times> series, Times appointment) {
        Overlap overlap = null;
        int index = Collections.binarySearch(series, appointment);
        if (index >= 0) {
            overlap = new Overlap(series.get(index), appointment);
        }
        return overlap;
    }

    /**
     * Creates appointments corresponding to the expression.
     *
     * @param times the appointment times
     */
    private void createAppointments(List<Times> times) {
        acts.clear();
        acts.add(appointment);
        series = createSeries();
        ActBean seriesBean = populateSeries(series, 0);

        List<Act> toSave = new ArrayList<Act>();
        seriesBean.addNodeRelationship("items", appointment);
        toSave.add(appointment);

        toSave.add(series);
        for (Times appointment : times) {
            Act act = create(appointment, seriesBean);
            acts.add(act);
            toSave.add(act);
        }
        service.save(toSave);
    }

    private Act createSeries() {
        series = (Act) service.create(ScheduleArchetypes.APPOINTMENT_SERIES);
        series.setActivityStartTime(appointment.getActivityStartTime());
        return series;
    }

    /**
     * Creates a new appointment linked to the series.
     *
     * @param times      the appointment times
     * @param seriesBean the series
     * @return the appointment
     */
    private Act create(Times times, ActBean seriesBean) {
        Act act = (Act) service.create(ScheduleArchetypes.APPOINTMENT);
        ActBean bean = update(act, times);
        bean.setNodeParticipant("author", current.getAuthor());
        seriesBean.addNodeRelationship("items", act);
        return act;
    }

    /**
     * Updates an appointment.
     *
     * @param act   the appointment
     * @param times the appointment times
     * @return the appointment
     */
    private ActBean update(Act act, Times times) {
        act.setActivityStartTime(times.getStartTime());
        act.setActivityEndTime(times.getEndTime());
        return update(act);
    }

    /**
     * Updates an appointment.
     *
     * @param act the appointment
     * @return the appointment
     */
    private ActBean update(Act act) {
        ActBean bean = new ActBean(act, service);
        bean.setNodeParticipant("schedule", current.getSchedule());
        bean.setNodeParticipant("appointmentType", current.getAppointmentType());
        bean.setNodeParticipant("customer", current.getCustomer());
        bean.setNodeParticipant("patient", current.getPatient());
        bean.setNodeParticipant("clinician", current.getClinician());
        bean.setValue("reason", current.getReason());
        bean.setValue("description", current.getNotes());
        return bean;
    }

    /**
     * Updates an appointment series.
     *
     * @return {@code true} if changes were made
     */
    private boolean updateSeries() {
        boolean result;
        List<Times> times = new ArrayList<Times>();
        Overlap overlap = calculateSeries(times);
        if (overlap != null) {
            result = false;
        } else {
            int index = acts.indexOf(appointment);
            if (index >= 0) {
                List<Act> future = Collections.emptyList();
                if (index + 1 < acts.size()) {
                    future = acts.subList(index + 1, acts.size());
                }
                updateSeries(future, times, index);
                acts = new ArrayList<Act>(acts.subList(index, acts.size()));
                result = true;
            } else {
                // shouldn't occur
                result = false;
            }
        }
        return result;
    }

    /**
     * Updates an appointment series.
     *
     * @param acts                   the acts to update
     * @param times                  the new times
     * @param actsPriorToAppointment the no. of acts in the series prior to the appointment
     */
    private void updateSeries(List<Act> acts, List<Times> times, int actsPriorToAppointment) {
        Act oldSeries = series;
        boolean createSeries = !current.repeatEquals(previous);
        // create a new series if the repeat expression or condition has changed

        Act currentSeries = (createSeries) ? createSeries() : series;
        ActBean bean = populateSeries(currentSeries, actsPriorToAppointment);
        ActBean oldBean = (createSeries) ? new ActBean(oldSeries, service) : bean;

        acts = new ArrayList<Act>(acts);                 // copy to avoid modifying source
        Iterator<Times> timesIterator = times.iterator();
        Iterator<Act> iterator = acts.listIterator();
        List<Act> toSave = new ArrayList<Act>();

        toSave.add(appointment);

        while (timesIterator.hasNext()) {
            Act act;
            if (iterator.hasNext()) {
                act = iterator.next();
                iterator.remove();
                update(act, timesIterator.next());
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
            oldBean.removeNodeRelationships("items", appointment);
            bean.addNodeRelationship("items", appointment);
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
     * Deletes the appointments after the current appointment, and unlinks it from the series.
     * If no acts reference the series act, it is also removed.
     *
     * @return {@code true} if changes were made
     */
    private boolean deleteSeries() {
        boolean result = false;
        int index = acts.indexOf(appointment);
        if (index >= 0) {
            List<Act> future = Collections.emptyList();
            if (index + 1 < acts.size()) {
                // delete the acts after the appointment.
                future = acts.subList(index + 1, acts.size());
            }
            deleteSeries(future);
            result = true;
            acts.clear();
        }
        return result;
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
        bean.removeNodeRelationships("items", appointment);
        List<Act> toSave = new ArrayList<Act>(acts);
        toSave.add(series);
        toSave.add(appointment);
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
     * @param series                 the series to populate
     * @param actsPriorToAppointment the no. of acts in the series prior to the appointment
     * @return the series bean
     */
    private ActBean populateSeries(Act series, int actsPriorToAppointment) {
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
            times = ((RepeatNTimesCondition) condition).getTimes() + actsPriorToAppointment;
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
     * @param appointment the appointment
     * @param series      the series
     * @return all of the acts in the series
     */
    private List<Act> getAppointments(Act appointment, ActBean series) {
        List<IMObjectReference> items = series.getNodeTargetObjectRefs("items");
        items.remove(appointment.getObjectReference());
        List<Act> result;
        result = ActHelper.getActs(items);
        result.add(appointment);
        return ActHelper.sort(result);
    }

    /**
     * Appointment series state.
     */
    private static class State {

        /**
         * The appointment type.
         */
        private IMObjectReference appointmentType;

        /**
         * The schedule.
         */
        private IMObjectReference schedule;

        /**
         * The customer.
         */
        private IMObjectReference customer;

        /**
         * The patient.
         */
        private IMObjectReference patient;

        /**
         * The author.
         */
        private IMObjectReference author;

        /**
         * The clinician.
         */
        private IMObjectReference clinician;

        /**
         * The appointment reason.
         */
        private String reason;

        /**
         * The appointment notes.
         */
        private String notes;

        /**
         * The expression.
         */
        private RepeatExpression expression;

        /**
         * The condition.
         */
        private RepeatCondition condition;


        /**
         * Initialises the state from an appointment.
         *
         * @param appointment the appointment
         */
        public State(ActBean appointment) {
            update(appointment);
        }

        /**
         * Updates the state from an appointment.
         *
         * @param appointment the appointment
         */
        public void update(ActBean appointment) {
            schedule = appointment.getNodeParticipantRef("schedule");
            appointmentType = appointment.getNodeParticipantRef("appointmentType");
            customer = appointment.getNodeParticipantRef("customer");
            patient = appointment.getNodeParticipantRef("patient");
            clinician = appointment.getNodeParticipantRef("clinician");
            author = appointment.getNodeParticipantRef("author");
            reason = appointment.getString("reason");
            notes = appointment.getString("description");
        }

        /**
         * Copy constructor.
         *
         * @param state the state to copy
         */
        public State(State state) {
            this.schedule = state.schedule;
            this.appointmentType = state.appointmentType;
            this.customer = state.customer;
            this.patient = state.patient;
            this.clinician = state.clinician;
            this.author = state.author;
            this.reason = state.reason;
            this.notes = state.notes;
            this.expression = state.expression;
            this.condition = state.condition;
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

        public IMObjectReference getAppointmentType() {
            return appointmentType;
        }

        public IMObjectReference getAuthor() {
            return author;
        }

        public IMObjectReference getCustomer() {
            return customer;
        }

        public IMObjectReference getPatient() {
            return patient;
        }

        public IMObjectReference getClinician() {
            return clinician;
        }

        public String getReason() {
            return reason;
        }

        public String getNotes() {
            return notes;
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
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof State)) {
                return false;
            }
            State other = (State) obj;
            return new EqualsBuilder()
                    .append(schedule, other.schedule)
                    .append(appointmentType, other.appointmentType)
                    .append(customer, other.customer)
                    .append(patient, other.patient)
                    .append(clinician, other.clinician)
                    .append(author, other.author)
                    .append(reason, other.reason)
                    .append(notes, other.notes)
                    .append(expression, other.expression)
                    .append(condition, other.condition)
                    .isEquals();
        }

    }

}
