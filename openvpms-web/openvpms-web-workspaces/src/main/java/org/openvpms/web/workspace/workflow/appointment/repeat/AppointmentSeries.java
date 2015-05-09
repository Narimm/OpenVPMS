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
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
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
     * The rule-based archetype service.
     */
    private final IArchetypeRuleService ruleService;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * The series or {@code null}, if the appointment isn't associated with a series.
     */
    private Act series;

    /**
     * The previous expression.
     */
    private RepeatExpression previous;

    /**
     * The current expression.
     */
    private RepeatExpression expression;

    /**
     * The repeat start time.
     */
    private Date startTime;

    /**
     * The appointment type.
     */
    private Entity appointmentType;

    /**
     * The schedule.
     */
    private Entity schedule;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The author.
     */
    private User author;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The maximum no. of appointments that can be created.
     */
    private final int maxAppointments;

    public static class Times {

        private final Date startTime;
        private final Date endTime;

        public Times(Date startTime, Date endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    /**
     * Constructs an {@link AppointmentSeries}.
     *
     * @param appointment the appointment
     * @param service     the archetype service
     * @param ruleService the rule-based archetype service
     * @param rules       the appointment rules
     */
    public AppointmentSeries(Act appointment, IArchetypeService service, IArchetypeRuleService ruleService,
                             AppointmentRules rules) {
        this(appointment, service, ruleService, rules, DEFAULT_MAX_APPOINTMENTS);
    }

    /**
     * Constructs an {@link AppointmentSeries}.
     *
     * @param appointment     the appointment
     * @param service         the archetype service
     * @param ruleService     the rule-based archetype service
     * @param rules           the appointment rules
     * @param maxAppointments the maximum no. of appointments that may be created
     */
    public AppointmentSeries(Act appointment, IArchetypeService service, IArchetypeRuleService ruleService,
                             AppointmentRules rules, int maxAppointments) {
        if (service instanceof IArchetypeRuleService) {
            throw new IllegalArgumentException("Argument 'service' must not implement IArchetypeRuleService");
        }
        this.appointment = appointment;
        this.service = service;
        this.ruleService = ruleService;
        this.rules = rules;
        this.maxAppointments = maxAppointments;
        ActBean bean = new ActBean(appointment, service);
        schedule = bean.getNodeParticipant("schedule");
        appointmentType = bean.getNodeParticipant("appointmentType");
        customer = (Party) bean.getNodeParticipant("customer");
        patient = (Party) bean.getNodeParticipant("patient");
        clinician = (User) bean.getNodeParticipant("clinician");
        author = (User) bean.getNodeParticipant("author");
        series = (Act) bean.getNodeSourceObject("repeat");
        if (series != null) {
            startTime = series.getActivityStartTime();
            ActBean seriesBean = new ActBean(series, service);
            int interval = seriesBean.getInt("interval", -1);
            DateUnits units = DateUnits.fromString(seriesBean.getString("units"));
            if (interval != -1 && units != null) {
                previous = new CalendarRepeatExpression(interval, units);
            } else {
                String expression = seriesBean.getString("expression");
                if (!StringUtils.isEmpty(expression)) {
                    previous = CronRepeatExpression.parse(expression);
                }
            }
            expression = previous;
        } else {
            startTime = appointment.getActivityStartTime();
        }
    }

    /**
     * Sets the schedule.
     *
     * @param schedule the schedule
     */
    public void setSchedule(Entity schedule) {
        this.schedule = schedule;
    }

    /**
     * Sets the appointment type.
     *
     * @param appointmentType the appointment type
     */
    public void setAppointmentType(Entity appointmentType) {
        this.appointmentType = appointmentType;
    }

    /**
     * Sets the customer.
     *
     * @param customer the customer
     */
    public void setCustomer(Party customer) {
        this.customer = customer;
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient
     */
    public void setPatient(Party patient) {
        this.patient = patient;
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician
     */
    public void setClinician(User clinician) {
        this.clinician = clinician;
    }

    /**
     * Sets the author.
     *
     * @param author the author
     */
    public void setAuthor(User author) {
        this.author = author;
    }

    /**
     * Returns the repeat expression for this series.
     *
     * @return the repeat expression, or {@code null} if none has been configured
     */
    public RepeatExpression getExpression() {
        return expression;
    }

    /**
     * Sets the repeat expression.
     *
     * @param expression the repeat expression. May be [@code null}
     */
    public void setExpression(RepeatExpression expression) {
        this.expression = expression;
    }

    public boolean isModified() {
        return !ObjectUtils.equals(previous, expression);
    }

    public boolean save() {
        boolean result = true;
        if (isModified()) {
            if (previous != null && expression == null) {
                result = deleteSeries();
            } else if (previous != null) {
                result = updateNonExpiredAppointments();
            } else if (expression != null) {
                createAppointments();
                result = true;
            }
            previous = expression;
        }
        return result;
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
     * Determines if an appointment has expired.
     *
     * @param startTime the appointment start time
     * @param now       the current time
     * @return {@code true} if the appointment has expired
     */
    protected boolean isExpired(Date startTime, Date now) {
        return DateRules.compareTo(startTime, now) <= 0;
    }

    /**
     * Creates appointments corresponding to the expression.
     */
    private void createAppointments() {
        series = (Act) service.create(ScheduleArchetypes.APPOINTMENT_SERIES);
        series.setActivityStartTime(startTime);
        ActBean seriesBean = updateSeries();

        List<Act> toSave = new ArrayList<Act>();
        seriesBean.addNodeRelationship("items", appointment);
        toSave.add(appointment);

        int i = 1;
        Date from = startTime;
        toSave.add(series);
        while (i < maxAppointments && (from = expression.getRepeatAfter(from)) != null) {
            Act act = create(from, seriesBean);
            toSave.add(act);
            ++i;
        }
        service.save(toSave);
    }

    private ActBean updateSeries() {
        ActBean seriesBean = new ActBean(series, service);
        String expr = null;
        Integer interval = null;
        String units = null;
        if (expression instanceof CalendarRepeatExpression) {
            CalendarRepeatExpression calendar = (CalendarRepeatExpression) expression;
            interval = calendar.getInterval();
            units = calendar.getUnits().toString();
        } else {
            expr = ((CronRepeatExpression) expression).getExpression();
        }
        seriesBean.setValue("interval", interval);
        seriesBean.setValue("units", units);
        seriesBean.setValue("expression", expr);
        return seriesBean;
    }

    /**
     * Creates a new appointment linked to the series.
     *
     * @param startTime  the appointment start time
     * @param seriesBean the series
     * @return the appointment
     */
    private Act create(Date startTime, ActBean seriesBean) {
        Act act = (Act) service.create(ScheduleArchetypes.APPOINTMENT);
        ActBean bean = update(act, startTime);
        bean.setNodeParticipant("author", author);
        seriesBean.addNodeRelationship("items", act);
        return act;
    }

    /**
     * Updates an appointment.
     *
     * @param act       the appointment
     * @param startTime the appointment start time
     * @return the appointment
     */
    private ActBean update(Act act, Date startTime) {
        Date endTime = rules.calculateEndTime(startTime, (Party) schedule, appointmentType);
        act.setActivityStartTime(startTime);
        act.setActivityEndTime(endTime);
        ActBean bean = new ActBean(act, service);
        bean.setNodeParticipant("schedule", schedule);
        bean.setNodeParticipant("appointmentType", appointmentType);
        bean.setNodeParticipant("customer", customer);
        bean.setNodeParticipant("patient", patient);
        bean.setNodeParticipant("clinician", clinician);
        return bean;
    }

    private boolean updateNonExpiredAppointments() {
        boolean result = false;

        Date now = new Date();
        ActBean bean = updateSeries();
        List<Act> acts = new ArrayList<Act>();
        int expired = 0;
        Date lastExpired = null;
        for (Act item : bean.getNodeActs("items")) {
            Date startTime = item.getActivityStartTime();
            if (isExpired(startTime, now)) {
                if (lastExpired == null || DateRules.compareTo(startTime, lastExpired) > 0) {
                    lastExpired = startTime;
                }
                expired++;
            } else {
                acts.add(item);
            }
        }
        ActHelper.sort(acts);
        int i = acts.size() + expired;
        Date from = lastExpired != null ? lastExpired : series.getActivityStartTime();
        Iterator<Act> iterator = acts.listIterator();
        List<Act> toUpdate = new ArrayList<Act>();
        List<Act> toSave = new ArrayList<Act>();
        while (i < maxAppointments && (from = expression.getRepeatAfter(from)) != null) {
            if (iterator.hasNext()) {
                Act act = iterator.next();
                iterator.remove();
                update(act, from);
                toUpdate.add(act);
            } else {
                Act act = create(from, bean);
                toSave.add(act);
            }
            ++i;
        }
        for (Act act : acts) {
            ActRelationship relationship = bean.getRelationship(act);
            bean.removeRelationship(relationship);
            act.removeActRelationship(relationship);
            toSave.add(act);
        }

        if (!toSave.isEmpty()) {
            toSave.add(series);
            service.save(toSave);
            result = true;
        }
        if (!toUpdate.isEmpty()) {
            ruleService.save(toUpdate); // don't fire rules for updated appointments
            result = true;
        }
        if (!acts.isEmpty()) {
            for (Act act : acts) {
                service.remove(act);
            }
            result = true;
        }
        return result;
    }

    /**
     * Deletes a series.
     * <p/>
     * Any non-expired appointments bar that passed at construction will be removed.
     *
     * @return {@code true} if changes were made
     */
    private boolean deleteSeries() {
        ActBean bean = new ActBean(series, service);
        Date now = new Date();
        int expired = 0;
        List<Act> toSave = new ArrayList<Act>();
        List<Act> toRemove = new ArrayList<Act>();
        for (ActRelationship relationship : bean.getValues("items", ActRelationship.class)) {
            Act item;
            if (!ObjectUtils.equals(appointment.getObjectReference(), relationship.getObjectReference())) {
                item = get(relationship.getTarget());
                if (item != null) {
                    if (isExpired(item.getActivityStartTime(), now)) {
                        expired++;
                    } else {
                        toRemove.add(item);
                    }
                }
            } else {
                item = appointment;
            }
            if (item != null) {
                bean.removeRelationship(relationship);
                item.removeActRelationship(relationship);
                toSave.add(item);
            }
        }
        if (!toSave.isEmpty()) {
            toSave.add(series);
            if (expired == 0) {
                toRemove.add(series);
            }
            service.save(toSave);
            for (Act act : toRemove) {
                service.remove(act);
            }
        }
        return !toRemove.isEmpty();
    }

    private Act get(IMObjectReference target) {
        return target != null ? (Act) service.get(target) : null;
    }

}
