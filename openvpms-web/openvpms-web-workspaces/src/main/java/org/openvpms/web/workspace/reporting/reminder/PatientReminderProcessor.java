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
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Patient reminder processor.
 *
 * @author Tim Anderson
 */
public abstract class PatientReminderProcessor {

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
    private final ReminderTypes reminderTypes;

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
    public PatientReminderProcessor(ReminderTypes reminderTypes, ReminderRules rules, Party practice,
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
     * If reminders aren't being resent, this:
     * <ul>
     * <li>cancels any reminders that are no longer applicable</li>
     * <li>filters out any reminders that can't be processed due to missing data</li>
     * <li>adds meta-data for subsequent calls to {@link #process}</li>
     * </ul>
     * If reminders are being resent, due dates are ignored, and no cancellation will occur.
     *
     * @param reminders  the reminders
     * @param groupBy    the reminder grouping policy. This determines which document template is selected
     * @param cancelDate the date to use when determining if a reminder item should be cancelled
     * @param resend     if {@code true}, reminders are being resent
     * @return the reminders to process
     */
    public PatientReminders prepare(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy, Date cancelDate,
                                    boolean resend) {
        List<ReminderEvent> toProcess = new ArrayList<>();
        List<ReminderEvent> cancelled = new ArrayList<>();
        List<ReminderEvent> errors = new ArrayList<>();
        List<Act> updated = new ArrayList<>();
        for (ReminderEvent event : reminders) {
            Act item = event.getItem();
            if (!resend && shouldCancel(item, cancelDate)) {
                updateItem(item, ReminderItemStatus.CANCELLED, Messages.get("reporting.reminder.outofdate"));
                updated.add(item);
                Act reminder = updateReminder(event, item);
                if (reminder != null) {
                    updated.add(reminder);
                }
                cancelled.add(event);
            } else {
                toProcess.add(event);
            }
        }
        return prepare(toProcess, groupBy, cancelled, errors, updated, resend);
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the archetype
     */
    public abstract String getArchetype();

    /**
     * Processes reminders.
     *
     * @param reminders the reminders
     */
    public abstract void process(PatientReminders reminders);

    /**
     * Completes processing.
     *
     * @param reminders the reminders
     */
    public void complete(PatientReminders reminders) {
        boolean resend = reminders.getResend();
        for (ReminderEvent event : reminders.getReminders()) {
            if (!resend) {
                complete(event, reminders);
            } else {
                // if its a resend of a reminder, but the reminder item is new, complete it, if it isn't in error
                Act item = event.getItem();
                if (item.isNew() && !ReminderItemStatus.ERROR.equals(item.getStatus())) {
                    completeItem(event, reminders);
                }
            }
        }
        save(reminders);
        CommunicationLogger logger = getLogger();
        if (logger != null && !reminders.getReminders().isEmpty()) {
            log(reminders, logger);
        }
    }

    /**
     * Collects statistics from the supplied state.
     *
     * @param state      the state
     * @param statistics the statistics to update
     */
    public void addStatistics(PatientReminders state, Statistics statistics) {
        for (ReminderEvent event : state.getReminders()) {
            Act reminder = event.getReminder();
            Act item = event.getItem();
            ReminderType reminderType = reminderTypes.get(new ActBean(reminder).getNodeParticipantRef("reminderType"));
            if (ReminderItemStatus.ERROR.equals(item.getStatus())) {
                statistics.addErrors(1);
            } else if (ReminderItemStatus.CANCELLED.equals(item.getStatus())) {
                statistics.addCancelled(1);
            } else {
                if (reminderType != null) {
                    statistics.increment(event, reminderType);
                }
            }
        }
        statistics.addErrors(state.getErrors().size());
        statistics.addCancelled(state.getCancelled().size());
    }

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
    public ReminderTypes getReminderTypes() {
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
     * @param reminders the reminders to prepare
     * @param groupBy   the reminder grouping policy. This determines which document template is selected
     * @param cancelled reminder items that will be cancelled
     * @param errors    reminders that can't be processed due to error
     * @param updated   acts that need to be saved on completion
     * @param resend    if {@code true}, reminders are being resent
     * @return the reminders to process
     */
    protected abstract PatientReminders prepare(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy,
                                                List<ReminderEvent> cancelled, List<ReminderEvent> errors,
                                                List<Act> updated, boolean resend);

    /**
     * Determines if a reminder item should be cancelled.
     *
     * @param item the item
     * @param from the date to cancel from
     * @return {@code true} if the reminder item should be cancelled
     */
    protected boolean shouldCancel(Act item, Date from) {
        Date cancel = config.getCancelDate(item.getActivityStartTime(), getArchetype());
        return DateRules.compareDates(cancel, from) <= 0;
    }

    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    protected abstract void log(PatientReminders state, CommunicationLogger logger);

    /**
     * Saves the state.
     *
     * @param state the state
     */
    protected void save(PatientReminders state) {
        List<Act> updated = state.getUpdated();
        if (!updated.isEmpty()) {
            service.save(updated);
        }
    }

    /**
     * Returns the practice location to use when sending reminders to a customer.
     *
     * @param customer the customer
     * @return the customer's preferred practice location, or the fallback reminder configuration location if the
     * customer has none. May be {@code null}
     */
    protected Party getLocation(Party customer) {
        Party location = (Party) new IMObjectBean(customer, service).getNodeTargetObject("practice");
        if (location == null) {
            location = config.getLocation();
        }
        return location;
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
     * @param contact   the customer contact. May be {@code null}
     * @param location  the customer location. May be {@code null}
     */
    protected void populate(List<ReminderEvent> reminders, Contact contact, Party location) {
        for (ReminderEvent reminder : reminders) {
            populate(reminder, contact, location);
        }
    }

    /**
     * Adds meta-data to a reminder.
     *
     * @param event    the reminder event
     * @param contact  the customer contact. May be {@code null}
     * @param location the customer location. May be {@code null}
     */
    protected void populate(ReminderEvent event, Contact contact, Party location) {
        Act reminder = event.getReminder();
        Act item = event.getItem();
        ActBean bean = new ActBean(reminder, service);
        ActBean itemBean = new ActBean(item, service);
        IMObjectReference reminderTypeRef = bean.getNodeParticipantRef("reminderType");
        ReminderType reminderType = reminderTypes.get(reminderTypeRef);
        event.setContact(contact);
        event.setReminderType(reminderType != null ? reminderType.getEntity() : null);
        event.setProduct((Product) bean.getNodeParticipant("product"));
        event.setClinician((User) bean.getNodeParticipant("clinician"));
        event.setReminderCount(itemBean.getInt("count"));
        event.setLocation(location);
    }

    /**
     * Creates a context for a reminder.
     *
     * @param reminder the reminder set
     * @param location the customer location. May be {@code null}
     * @return a new context
     */
    protected Context createContext(ReminderEvent reminder, Party location) {
        Context context = new LocalContext();
        context.setPatient(reminder.getPatient());
        context.setCustomer(reminder.getCustomer());
        context.setLocation(location);
        context.setPractice(practice);
        return context;
    }

    /**
     * Returns the contact to use.
     *
     * @param customer the reminder
     * @return the contact, or {@code null} if none is found
     */
    protected Contact getContact(Party customer, ContactMatcher matcher) {
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
     * Returns the reminder type associated with a reminder set.
     *
     * @param reminder the reminder set
     * @return the reminder type, or {@code null} if none exists
     */
    protected ReminderType getReminderType(ReminderEvent reminder) {
        return reminderTypes.get(reminder.getReminderType());
    }

    /**
     * Returns a formatted note containing the reminder type and count.
     *
     * @param reminder the reminder
     * @return the note
     */
    protected String getNote(ReminderEvent reminder) {
        int reminderCount = reminder.getReminderCount();
        Entity reminderType = reminder.getReminderType();
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
    protected void complete(ReminderEvent event, PatientReminders state) {
        Act item = completeItem(event, state);
        Act reminder = event.getReminder();
        updateReminder(reminder, item);
        ActBean bean = new ActBean(reminder);
        bean.setValue("lastSent", new Date());
        state.updated(reminder);
    }

    /**
     * Completes a reminder item.
     *
     * @param event the reminder event
     * @param state the processing state
     */
    protected Act completeItem(ReminderEvent event, PatientReminders state) {
        Act item = updateItem(event, ReminderItemStatus.COMPLETED, null);
        state.updated(item);
        return item;
    }

    /**
     * Updates a reminder if it has no PENDING or ERROR items besides that supplied.
     *
     * @param event the reminder event
     * @param item  the reminder item
     * @return the updated reminder, or {@code null} if it wasn't updated
     */
    protected Act updateReminder(ReminderEvent event, Act item) {
        Act reminder = event.getReminder();
        if (updateReminder(reminder, item)) {
            return reminder;
        }
        return null;
    }

    private boolean updateReminder(Act reminder, Act item) {
        return rules.updateReminder(reminder, item);
    }

    /**
     * Updates a reminder item.
     *
     * @param event   the reminder event
     * @param status  the item status
     * @param message the error message. May be {@code null}
     * @return the reminder item
     */
    protected Act updateItem(ReminderEvent event, String status, String message) {
        Act item = event.getItem();
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
        item.setActivityStartTime(new Date()); // update the send time to when it was actually sent
        item.setStatus(status);
        ActBean bean = new ActBean(item, service);
        bean.setValue("error", message);
    }

}
