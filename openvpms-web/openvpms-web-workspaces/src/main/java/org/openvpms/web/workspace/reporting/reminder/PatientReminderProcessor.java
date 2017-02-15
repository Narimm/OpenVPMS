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

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.party.ContactMatcher;
import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.archetype.rules.party.PurposeMatcher;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypeCache;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Patient reminder processor.
 * <p/>
 * This processes {@link ObjectSet} returned by queries created by {@link ReminderItemObjectSetQuery}.
 *
 * @author Tim Anderson
 */
public abstract class PatientReminderProcessor {

    public static class State {

        private final List<ObjectSet> reminders;

        private final List<ObjectSet> cancelled;

        private final List<ObjectSet> errors;

        private final List<Act> updated;

        public State(List<ObjectSet> reminders, List<ObjectSet> cancelled, List<ObjectSet> errors, List<Act> updated) {
            this.reminders = reminders;
            this.cancelled = cancelled;
            this.errors = errors;
            this.updated = updated;
        }

        public List<ObjectSet> getReminders() {
            return reminders;
        }

        public void updated(Act act) {
            updated.add(act);
        }

        public List<Act> getUpdated() {
            return updated;
        }

        public List<ObjectSet> getCancelled() {
            return cancelled;
        }

        public List<ObjectSet> getErrors() {
            return errors;
        }

        public int getProcessed() {
            Set<ObjectSet> sets = new HashSet<>();
            sets.addAll(reminders);
            sets.removeAll(cancelled);
            sets.removeAll(errors);
            return sets.size();
        }
    }

    /**
     * The reminder contact purpose.
     */
    protected static final String CONTACT_PURPOSE = "REMINDER";

    /**
     * The reason to use when logging communications.
     */
    protected static final String COMMUNICATION_REASON = "PATIENT_REMINDER";

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The reminder types.
     */
    private final ReminderTypeCache reminderTypes;

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The reminder configuration.
     */
    private final ReminderConfiguration config;

    /**
     * The communication logger.
     */
    private final CommunicationLogger logger;

    /**
     * Constructs a {@link PatientReminderProcessor}.
     *
     * @param reminderTypes the reminder types
     * @param rules         the reminder rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public PatientReminderProcessor(ReminderTypeCache reminderTypes, ReminderRules rules, Party practice,
                                    IArchetypeService service, ReminderConfiguration config,
                                    CommunicationLogger logger) {
        this.reminderTypes = reminderTypes;
        this.rules = rules;
        this.practice = practice;
        this.service = service;
        this.config = config;
        this.logger = logger;
    }

    /**
     * Prepares reminders for processing.
     * <p/>
     * This:
     * <ul>
     * <li>cancels any reminders that are no longer applicable</li>
     * <li>filters out any reminders that can't be processed due to missing data</li>
     * <li>adds meta-data for subsequent calls to {@link #process}</li>
     * </ul>
     *
     * @param reminders  the reminders
     * @param cancelDate the date to use when determining if a reminder item should be cancelled
     * @return the reminders to process
     */
    public State prepare(List<ObjectSet> reminders, Date cancelDate) {
        List<ObjectSet> toProcess = new ArrayList<>();
        List<ObjectSet> cancelled = new ArrayList<>();
        List<ObjectSet> errors = new ArrayList<>();
        List<Act> updated = new ArrayList<>();
        for (ObjectSet set : reminders) {
            Act item = getItem(set);
            if (shouldCancel(item, cancelDate)) {
                updateItem(item, ReminderItemStatus.CANCELLED, "Reminder not processed in time");
                updated.add(item);
                Act reminder = updateReminder(set, item);
                if (reminder != null) {
                    updated.add(reminder);
                }
                cancelled.add(set);
            } else {
                toProcess.add(set);
            }
        }
        if (!toProcess.isEmpty()) {
            toProcess = prepare(toProcess, updated, errors);
        }
        return new State(toProcess, cancelled, errors, updated);
    }

    /**
     * Processes reminders.
     *
     * @param state the reminder state
     */
    public abstract void process(State state);

    /**
     * Completes processing.
     *
     * @param state the reminder state
     */
    public abstract void complete(State state);

    /**
     * Determines if reminder processing is performed asynchronously.
     *
     * @return {@code true} if reminder processing is performed asynchronously
     */
    public abstract boolean isAsynchronous();

    /**
     * Returns the reminder types.
     *
     * @return the reminder types
     */
    public ReminderTypeCache getReminderTypes() {
        return reminderTypes;
    }

    /**
     * Prepares reminders for processing.
     * <p/>
     * This:
     * <ul>
     * <li>filters out any reminders that can't be processed due to missing data</li>
     * <li>adds meta-data for subsequent calls to {@link #process}</li>
     * </ul>
     *
     * @param reminders the reminders
     * @param updated   acts that need to be saved on completion
     * @param errors    reminders that can't be processed due to error
     * @return the reminders to process
     */
    protected abstract List<ObjectSet> prepare(List<ObjectSet> reminders, List<Act> updated, List<ObjectSet> errors);

    /**
     * Determines if a reminder item should be cancelled.
     *
     * @param item the item
     * @param from the date to cancel from
     * @return {@code true} if the reminder item should be cancelled
     */
    protected boolean shouldCancel(Act item, Date from) {
        Date cancel = getCancelDate(item.getActivityStartTime(), config);
        return DateRules.compareDates(cancel, from) <= 0;
    }

    /**
     * Returns the date from which a reminder item should be cancelled.
     *
     * @param startTime the item start time
     * @param config    the reminder configuration
     * @return the date when the item should be cancelled
     */
    protected abstract Date getCancelDate(Date startTime, ReminderConfiguration config);

    /**
     * Saves the state.
     *
     * @param state the state
     */
    protected void save(State state) {
        service.save(state.getUpdated());
    }

    /**
     * Returns the customer's preferred practice location.
     *
     * @param customer the customer
     * @return the customer's preferred practice location, or {@code null} if none is found
     */
    protected Party getCustomerLocation(Party customer) {
        return (Party) new IMObjectBean(customer, service).getNodeTargetObject("practice");
    }

    /**
     * Returns the practice.
     *
     * @return the practice
     */
    protected Party getPractice() {
        return practice;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the communication logger.
     *
     * @return the communication logger, or {@code null} if logging is disabled.
     */
    protected CommunicationLogger getLogger() {
        return logger;
    }

    /**
     * Returns the reminder configuration.
     *
     * @return the reminder configuration
     */
    protected ReminderConfiguration getConfig() {
        return config;
    }

    /**
     * Adds meta-data to reminders.
     *
     * @param reminders the reminders
     */
    protected void populate(List<ObjectSet> reminders) {
        for (ObjectSet set : reminders) {
            Act reminder = getReminder(set);
            Act item = getItem(set);
            ActBean bean = new ActBean(reminder, service);
            ActBean itemBean = new ActBean(item, service);
            IMObjectReference reminderTypeRef = bean.getNodeParticipantRef("reminderType");
            ReminderType reminderType = reminderTypes.get(reminderTypeRef);
            set.set("reminderType", reminderType != null ? reminderType.getEntity() : null);
            set.set("product", bean.getNodeParticipant("product"));
            set.set("clinician", bean.getNodeParticipant("clinician"));
            set.set("startTime", reminder.getActivityStartTime());
            set.set("endTime", reminder.getActivityEndTime());
            set.set("reminderCount", itemBean.getInt("count"));
        }
    }

    /**
     * Creates a context for a reminder.
     *
     * @param reminder the reminder set
     * @return a new context
     */
    protected Context createContext(ObjectSet reminder) {
        Party patient = getPatient(reminder);
        Party customer = getCustomer(reminder);
        Party location = getCustomerLocation(customer);
        Context context = new LocalContext();
        context.setPatient(patient);
        context.setCustomer(customer);
        context.setLocation(location);
        context.setPractice(practice);
        return context;
    }

    /**
     * Returns the contact to use.
     *
     * @param reminder the reminder set
     * @return the contact, or {@code null} if none is found
     */
    protected Contact getContact(ObjectSet reminder, ContactMatcher matcher) {
        Party customer = getCustomer(reminder);
        return Contacts.find(Contacts.sort(customer.getContacts()), matcher);
    }

    /**
     * Creates a contact matcher to locate the contact to send to.
     *
     * @param shortName the contact archetype short name
     * @return a new contact matcher
     */
    protected ContactMatcher createContactMatcher(String shortName) {
        return new PurposeMatcher(shortName, CONTACT_PURPOSE, false, service);
    }

    /**
     * Returns the reminder act associated with a reminder set.
     *
     * @param reminder the reminder set
     * @return the reminder act
     */
    protected Act getReminder(ObjectSet reminder) {
        return (Act) reminder.get("reminder");
    }

    /**
     * Returns the reminder item associated with a reminder set.
     *
     * @param reminder the reminder set
     * @return the reminder item
     */
    protected Act getItem(ObjectSet reminder) {
        return (Act) reminder.get("item");
    }

    /**
     * Returns the customer associated with a reminder set.
     *
     * @param reminder the reminder set
     * @return the customer
     */
    protected Party getCustomer(ObjectSet reminder) {
        return (Party) reminder.get("customer");
    }

    /**
     * Returns the patient associated with a reminder set.
     *
     * @param reminder the reminder set
     * @return the patient
     */
    protected Party getPatient(ObjectSet reminder) {
        return (Party) reminder.get("patient");
    }

    /**
     * Returns the reminder type associated with a reminder set.
     *
     * @param reminder the reminder set
     * @return the reminder type, or {@code null} if none exists
     */
    protected ReminderType getReminderType(ObjectSet reminder) {
        return reminderTypes.get((Entity) reminder.get("reminderType"));
    }

    /**
     * Returns a formatted note containing the reminder type and count.
     *
     * @param reminder the reminder set
     * @return the note
     */
    protected String getNote(ObjectSet reminder) {
        int reminderCount = reminder.getInt("reminderCount");
        Entity reminderType = (Entity) reminder.get("reminderType");
        return getNote(reminderCount, reminderType);
    }

    /**
     * Returns a formatted note containing the reminder type and count.
     *
     * @param reminderCount the reminder count
     * @param reminderType  the reminder type. May be {@code null}
     * @return the note
     */
    protected String getNote(int reminderCount, Entity reminderType) {
        String name = (reminderType != null) ? reminderType.getName() : null;
        return Messages.format("reminder.log.note", name, reminderCount);
    }

    /**
     * Completes a reminder item, and updates the associated reminder if it has no PENDING or ERROR items.
     *
     * @param event the reminder event
     * @param state the processing state
     */
    protected void complete(ObjectSet event, State state) {
        Act item = updateItem(event, ReminderItemStatus.COMPLETED, null);
        state.updated(item);
        Act reminder = updateReminder(event, item);
        if (reminder != null) {
            state.updated(reminder);
        }
    }

    /**
     * Updates a reminder if it has no PENDING or ERROR items besides that supplied.
     *
     * @param event the reminder event
     * @param item  the reminder item
     * @return the updated reminder, or {@code null} if it wasn't updated
     */
    protected Act updateReminder(ObjectSet event, Act item) {
        Act reminder = (Act) event.get("reminder");
        if (rules.updateReminder(reminder, item)) {
            return reminder;
        }
        return null;
    }

    /**
     * Updates a reminder item.
     *
     * @param reminder the reminder set
     * @param status   the item status
     * @param message  the error message. May be {@code null}
     * @return the reminder item
     */
    protected Act updateItem(ObjectSet reminder, String status, String message) {
        Act item = getItem(reminder);
        updateItem(item, status, message);
        return item;
    }

    /**
     * Updates a reminder item.
     *
     * @param item    the item
     * @param status  the item status
     * @param message the error message. May be {@code null}
     */
    protected void updateItem(Act item, String status, String message) {
        item.setStatus(status);
        ActBean bean = new ActBean(item, service);
        bean.setValue("error", message);
    }

}
