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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.appointment;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Period;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.util.SMSLengthCalculator;
import org.openvpms.web.component.service.SMSService;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.workflow.appointment.reminder.AppointmentReminderEvaluator;
import org.openvpms.web.workspace.workflow.appointment.reminder.AppointmentReminderException;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A job that sends SMS appointment reminders.
 * <p/>
 * It is configured by an <em>entity.jobAppointmentReminder</em>.
 *
 * @author Tim Anderson
 */
public class AppointmentReminderJob implements InterruptableJob, StatefulJob {

    /**
     * The job configuration.
     */
    private final Entity configuration;

    /**
     * The SMS service.
     */
    private final SMSService service;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService archetypeService;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The location rules.
     */
    private final LocationRules locationRules;

    /**
     * The appointment reminder evaluator.
     */
    private final AppointmentReminderEvaluator evaluator;

    /**
     * The interval prior to an appointment when reminders can be sent.
     */
    private final Period fromPeriod;

    /**
     * The interval prior to an appointment when reminders should no longer be sent.
     */
    private final Period toPeriod;

    /**
     * Communications logging subject.
     */
    private final String subject;

    /**
     * Determines if reminding should stop.
     */
    private volatile boolean stop;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

    /**
     * Determines the minimum appointment start time.
     */
    private Date minStartTime;

    /**
     * Determines the maximum appointment start time.
     */
    private Date maxStartTime;

    /**
     * The total no. of reminders processed.
     */
    private int total;

    /**
     * The no. of reminders sent.
     */
    private int sent;

    /**
     * The maximum no. of message parts supported by the provider.
     */
    private final int maxParts;

    /**
     * Schedules that failed to have reminders sent, and the corresponding dates, ordered on schedule name.
     */
    private Map<Entity, Set<Date>> errors = new TreeMap<>(new Comparator<Entity>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Entity o1, Entity o2) {
            return ComparatorUtils.nullLowComparator(null).compare(o1.getName(), o2.getName());
        }
    });

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AppointmentReminderJob.class);

    /**
     * Communication logging reason code.
     */
    private static final String REASON = "APPOINTMENT_REMINDER";

    /**
     * Constructs an {@link AppointmentReminderJob}.
     *
     * @param configuration    the job configuration
     * @param service          the SMS service
     * @param archetypeService the archetype service
     * @param customerRules    the customer rules
     * @param patientRules     the patient rules
     * @param practiceService  the practice service
     * @param locationRules    the location rules
     * @param evaluator        the appointment reminder evaluator
     */
    public AppointmentReminderJob(Entity configuration, SMSService service, IArchetypeRuleService archetypeService,
                                  CustomerRules customerRules, PatientRules patientRules,
                                  PracticeService practiceService, LocationRules locationRules,
                                  AppointmentReminderEvaluator evaluator) {
        this.configuration = configuration;
        this.service = service;
        this.archetypeService = archetypeService;
        this.customerRules = customerRules;
        this.patientRules = patientRules;
        this.practiceService = practiceService;
        this.locationRules = locationRules;
        this.evaluator = evaluator;
        maxParts = service.getMaxParts();
        IMObjectBean bean = new IMObjectBean(configuration, archetypeService);
        fromPeriod = getPeriod(bean, "smsFrom", "smsFromUnits", DateUnits.WEEKS);
        toPeriod = getPeriod(bean, "smsTo", "smsToUnits", DateUnits.DAYS);
        notifier = new JobCompletionNotifier(archetypeService);
        subject = Messages.get("sms.log.appointment.subject");
    }

    /**
     * Called by the {@link Scheduler} when a user interrupts the {@code Job}.
     *
     * @throws UnableToInterruptJobException if there is an exception while interrupting the job.
     */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        stop = true;
    }

    /**
     * Called by the {@link Scheduler} when a {@link Trigger} fires that is associated with the {@code Job}.
     *
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            execute();
            complete(null);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(exception);
        }
    }

    /**
     * Sends appointment reminders.
     */
    protected void execute() {
        total = 0;
        sent = 0;
        Party practice = practiceService.getPractice();
        if (practice == null) {
            throw new IllegalStateException("No current practice");
        }
        List<Party> locations = practiceService.getLocations();
        Entity defaultTemplate = practiceService.getAppointmentSMSTemplate();
        Map<Party, Entity> templates = new HashMap<>();
        for (Party location : locations) {
            addTemplate(location, templates);
        }
        if (defaultTemplate == null && templates.isEmpty()) {
            throw new IllegalStateException("No Appointment Reminder SMS Templates have been configured");
        }
        NamedQuery query = new NamedQuery("AppointmentReminderJob.getReminders", "id");
        Date date = getStartDate();
        minStartTime = DateRules.plus(date, toPeriod);
        maxStartTime = DateRules.plus(date, fromPeriod);

        if (log.isInfoEnabled()) {
            log.info("Sending reminders for appointments between " + DateFormatter.formatDateTime(minStartTime)
                     + " and " + DateFormatter.formatDateTime(maxStartTime));
        }

        query.setParameter("from", minStartTime);
        query.setParameter("to", maxStartTime);
        int pageSize = getPageSize();
        query.setMaxResults(pageSize);
        // pull in count results at a time. Note that sending updates the appointment, which affects paging, so the
        // query needs to be re-issued from the start if any have updated
        boolean done = false;
        Set<Long> exclude = new HashSet<>();
        while (!stop && !done) {
            IPage<ObjectSet> page = archetypeService.getObjects(query);
            boolean updated = false;  // flag to indicate if any reminders were updated
            for (ObjectSet set : page.getResults()) {
                long id = set.getLong("id");
                ActBean bean = getAppointment(id);
                if (bean != null && !exclude.contains(id) && canSend(bean)) {
                    ++total;
                    if (send(bean, practice, defaultTemplate, templates)) {
                        ++sent;
                        updated = true;
                    } else {
                        // failed to send the reminder, so flag the act for exclusion if a query retrieves it again
                        exclude.add(id);
                    }
                }
            }
            if (page.getResults().size() < pageSize) {
                done = true;
            } else if (!updated) {
                // nothing updated, so pull in the next page
                query.setFirstResult(query.getFirstResult() + page.getResults().size());
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Sent " + sent + " of " + total + " appointment reminders");
        }
    }

    /**
     * Returns the date/time to base date calculations on.
     *
     * @return the current date/time
     */
    protected Date getStartDate() {
        return new Date();
    }

    /**
     * Determines the no. of appointments to process at once.
     *
     * @return the page size
     */
    protected int getPageSize() {
        return 1000;
    }

    /**
     * Sets an appointment reminder SMS.
     *
     * @param bean            the appointment
     * @param practice        the practice
     * @param defaultTemplate the default reminder template
     * @param templates       the location specific reminder templates
     * @return {@code true} if the reminder was sent
     * @throws SMSException if the send fails
     */
    protected boolean send(ActBean bean, Party practice, Entity defaultTemplate, Map<Party, Entity> templates) {
        boolean sent = false;
        Party customer = (Party) bean.getNodeParticipant("customer");
        if (customer != null && isCustomerValid(customer, bean) && isPatientValid(bean)) {
            Contact contact = getSMSContact(customer);
            if (contact == null) {
                addError(bean, Messages.get("sms.appointment.nocontact"));
            } else {
                Party location = getLocation(bean);
                if (location == null) {
                    addError(bean, Messages.get("sms.appointment.nolocation"));
                } else {
                    Entity template = templates.get(location);
                    if (template == null) {
                        template = defaultTemplate;
                    }
                    if (template == null) {
                        addError(bean, Messages.format("sms.appointment.notemplate", location.getName()));
                    } else {
                        try {
                            String message = evaluator.evaluate(template, bean.getAct(), location, practice);
                            if (StringUtils.isEmpty(message)) {
                                addError(bean, Messages.get("sms.appointment.empty"));
                            } else if (SMSLengthCalculator.getParts(message) > maxParts) {
                                addError(bean, Messages.format("sms.appointment.toolong", message));
                            } else {
                                service.send(message, contact, customer, subject, REASON, location);
                                bean.setValue("reminderSent", new Date());
                                bean.setValue("reminderError", null);
                                bean.save();
                                sent = true;
                            }
                        } catch (AppointmentReminderException exception) {
                            log.error(exception, exception);
                            addError(bean, exception.getMessage());
                        }
                    }
                }
            }
        }
        return sent;
    }

    /**
     * Determines if a reminder can be sent for an appointment.
     *
     * @param bean the appointment bean
     * @return {@code true} if the startTime isn't in the past, a reminder is flagged to be sent, and no reminder has
     * already been sent
     */
    protected boolean canSend(ActBean bean) {
        return !isPast(bean) && bean.getBoolean("sendReminder") && bean.getDate("reminderSent") == null;
    }

    /**
     * Determines if an appointment is past.
     *
     * @param bean the appointment bean
     * @return {@code true} if the appointment is past
     */
    protected boolean isPast(ActBean bean) {
        return DateRules.compareTo(bean.getDate("startTime"), new Date()) <= 0;
    }

    /**
     * Returns the location associated with an appointment.
     *
     * @param bean the appointment bean
     * @return the location, or {@code null} if none can be determined
     */
    protected Party getLocation(ActBean bean) {
        Party location = null;
        Entity schedule = bean.getNodeParticipant("schedule");
        if (schedule != null) {
            IMObjectBean scheduleBean = new IMObjectBean(schedule, archetypeService);
            location = (Party) scheduleBean.getNodeTargetObject("location");
            if (location == null) {
                log.warn("Cannot determine the practice location for: " + schedule.getName());
            }
        }
        return location;
    }

    /**
     * Determines if the appointment customer is valid.
     *
     * @param customer the customer
     * @param bean     the appointment bean
     * @return {@code true} if the customer is valid
     */
    private boolean isCustomerValid(Party customer, ActBean bean) {
        boolean valid = customer.isActive();
        if (!valid) {
            addError(bean, Messages.get("sms.appointment.customerinactive"));
        }
        return valid;
    }

    /**
     * Determines if the appointment patient is valid. It is valid if no patient is present, or it is active and
     * not deceased.
     *
     * @param bean the appointment bean
     * @return {@code true} if the patient is valid
     */
    private boolean isPatientValid(ActBean bean) {
        boolean valid;
        Party patient = (Party) bean.getNodeParticipant("patient");
        if (patient == null) {
            valid = true;
        } else if (patientRules.isDeceased(patient)) {
            addError(bean, Messages.get("sms.appointment.patientdeceased"));
            valid = false;
        } else if (!patient.isActive()) {
            addError(bean, Messages.get("sms.appointment.patientinactive"));
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * Returns an appointment given its identifier.
     *
     * @param id the appointment identifier
     * @return the appointment, or {@code null} if it doesn't exist
     */
    private ActBean getAppointment(long id) {
        Act act = (Act) archetypeService.get(new IMObjectReference(ScheduleArchetypes.APPOINTMENT, id));
        return act != null ? new ActBean(act, archetypeService) : null;
    }

    /**
     * Adds an error.
     * <p/>
     * This populates the reminderError node, ensuring the contents don't exceed the maximum length, and
     * logs the schedule and date of the reminder for reporting by {@link #notifyUsers}.
     *
     * @param bean    the reminder bean
     * @param message the error message
     */
    private void addError(ActBean bean, String message) {
        log.error("Failed to send reminder, id=" + bean.getAct().getId() + ", message=" + message);
        int maxLength = bean.getDescriptor("reminderError").getMaxLength();
        bean.setValue("reminderError", StringUtils.abbreviate(message, maxLength));
        bean.save();
        Entity schedule = bean.getNodeParticipant("schedule");
        if (schedule != null) {
            Set<Date> dates = errors.get(schedule);
            if (dates == null) {
                dates = new TreeSet<>();
                errors.put(schedule, dates);
            }
            dates.add(DateRules.getDate(bean.getDate("startTime")));
        }
    }

    /**
     * Returns the SMS contact for a customer.
     *
     * @param customer the customer
     * @return the SMS contact, or {@code null} if none exists
     */
    private Contact getSMSContact(Party customer) {
        Contact contact = customerRules.getSMSContact(customer);
        if (contact != null) {
            IMObjectBean contactBean = new IMObjectBean(contact, archetypeService);
            if (!StringUtils.isEmpty(contactBean.getString("telephoneNumber"))) {
                return contact;
            }
        }
        return null;
    }

    /**
     * Adds a template for a location to the supplied cache, if one exists.
     *
     * @param location  the practice location
     * @param templates the template cache
     */
    private void addTemplate(Party location, Map<Party, Entity> templates) {
        Entity template = locationRules.getAppointmentSMSTemplate(location);
        if (template != null) {
            templates.put(location, template);
        }
    }

    /**
     * Invoked on completion of a job. Sends a message notifying the registered users of completion or failure of the
     * job if required.
     *
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void complete(Throwable exception) {
        if (exception != null || sent != 0 || total != 0) {
            Set<User> users = notifier.getUsers(configuration);
            if (!users.isEmpty()) {
                notifyUsers(users, exception);
            }
        }
    }

    /**
     * Notifies users of completion or failure of the job.
     *
     * @param users     the users to notify
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void notifyUsers(Set<User> users, Throwable exception) {
        String subject;
        String reason;
        StringBuilder text = new StringBuilder();
        if (exception != null) {
            reason = SystemMessageReason.ERROR;
            subject = Messages.format("appointmentreminder.subject.exception", configuration.getName());
            text.append(Messages.format("appointmentreminder.exception", exception.getMessage()));
        } else {
            if (sent != total || !errors.isEmpty()) {
                reason = SystemMessageReason.ERROR;
                subject = Messages.format("appointmentreminder.subject.errors", configuration.getName(), total - sent);
            } else {
                reason = SystemMessageReason.COMPLETED;
                subject = Messages.format("appointmentreminder.subject.success", configuration.getName(), sent);
            }
        }
        if (minStartTime != null && maxStartTime != null) {
            text.append(Messages.format("appointmentreminder.period", DateFormatter.formatDateTime(minStartTime),
                                        DateFormatter.formatDateTime(maxStartTime)));
            text.append("\n");
        }
        text.append(Messages.format("appointmentreminder.sent", sent, total));

        if (!errors.isEmpty()) {
            text.append("\n\n");
            text.append(Messages.get("appointmentreminder.error"));
            text.append("\n");
            for (Map.Entry<Entity, Set<Date>> entry : errors.entrySet()) {
                for (Date date : entry.getValue()) {
                    text.append(Messages.format("appointmentreminder.error.item", entry.getKey().getName(),
                                                DateFormatter.formatDate(date, false)));
                    text.append("\n");
                }
            }
        }
        notifier.send(users, subject, reason, text.toString());
    }

    /**
     * Helper to return a {@code Period} for an inverval and units node.
     *
     * @param bean         the bean
     * @param intervalName the interval node name
     * @param unitsName    the interval units node name
     * @param defaultUnits the default units, if none is present
     * @return the corresponding period
     */
    private Period getPeriod(IMObjectBean bean, String intervalName, String unitsName, DateUnits defaultUnits) {
        int interval = bean.getInt(intervalName);
        DateUnits units = DateUnits.fromString(bean.getString(unitsName), defaultUnits);
        return units.toPeriod(interval);
    }
}
