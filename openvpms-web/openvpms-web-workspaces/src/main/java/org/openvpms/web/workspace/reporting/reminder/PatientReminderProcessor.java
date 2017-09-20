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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.party.ContactMatcher;
import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.archetype.rules.party.PurposeMatcher;
import org.openvpms.archetype.rules.patient.PatientRules;
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
import org.openvpms.web.component.error.ErrorFormatter;
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
    private final ReminderRules reminderRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PatientReminderProcessor.class);

    /**
     * Constructs a {@link PatientReminderProcessor}.
     *
     * @param reminderTypes the reminder types
     * @param reminderRules the reminder rules
     * @param patientRules  the patient rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public PatientReminderProcessor(ReminderTypes reminderTypes, ReminderRules reminderRules, PatientRules patientRules,
                                    Party practice, IArchetypeService service, ReminderConfiguration config,
                                    CommunicationLogger logger) {
        this.reminderTypes = reminderTypes;
        this.reminderRules = reminderRules;
        this.patientRules = patientRules;
        this.practice = practice;
        this.service = service;
        this.config = config;
        this.logger = logger;
    }

    /**
     * Prepares reminders for processing.
     * <p>
     * If reminders aren't being resent, this:
     * <ul>
     * <li>cancels any reminders that are no longer applicable</li>
     * <li>filters out any reminders that can't be processed due to missing data</li>
     * <li>adds meta-data for subsequent calls to {@link #process}</li>
     * </ul>
     * If reminders are being resent, due dates are ignored, and no cancellation will occur.
     * <p>
     * To specify the contact to use, pre-populate reminders via the {@link ReminderEvent#setContact(Contact)} method.
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
            if (!resend && isOutOfDate(item, cancelDate)) {
                // cancel out of date reminders, unless they are being resent
                cancel(event, Messages.get("reporting.reminder.outofdate"), updated, cancelled, false);
            } else if (isDeceased(event)) {
                // cancel reminders for deceased patients
                cancel(event, Messages.get("reporting.reminder.deceased"), updated, cancelled, resend);
            } else if (isPatientInactive(event)) {
                // cancel reminders for inactive patients
                cancel(event, Messages.get("reporting.reminder.patientinactive"), updated, cancelled, resend);
            } else if (isCustomerInactive(event)) {
                // cancel reminders for inactive customers. If the reminder is being resent, don't update the reminder.
                cancel(event, Messages.get("reporting.reminder.customerinactive"), updated, cancelled, resend);
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
     * @return {@code true} if any changes were saved
     */
    public boolean complete(PatientReminders reminders) {
        boolean result;
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
        result = save(reminders);
        CommunicationLogger logger = getLogger();
        if (logger != null && !reminders.getReminders().isEmpty()) {
            log(reminders, logger);
        }
        return result;
    }

    /**
     * Invoked when processing fails due to exception.
     * For reminders that are not being resent and are not in error cancelled, this sets the:
     * <ul>
     *     <li>status of each reminder to be sent to {@link ReminderItemStatus#ERROR}</li>
     *     <li>error node to the formatted message from the exception</li>
     * </ul>
     *
     * @param reminders the reminders
     * @param exception the exception
     * @return {@code true} if any reminders were updated
     */
    public boolean failed(PatientReminders reminders, Throwable exception) {
        boolean result = false;
        boolean resend = reminders.getResend();
        log.error("Failed to send reminders: " + exception.getMessage(), exception);
        if (!resend) {
            for (ReminderEvent event : reminders.getReminders()) {
                Act item = (Act) service.get(event.getItem().getObjectReference());
                if (item != null) {
                    result |= setError(item, exception);
                }
            }
        }
        return result;
    }

    /**
     * Collects statistics from the supplied state.
     *
     * @param state      the state
     * @param statistics the statistics to update
     */
    public void addStatistics(PatientReminders state, Statistics statistics) {
        for (ReminderEvent event : state.getReminders()) {
            Act item = event.getItem();
            ReminderType reminderType = reminderTypes.get(event.getReminderType());
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
     * <p>
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
     * Cancels a reminder item.
     * <p>
     * If the item is being resent it is changed, but not added to the set of updated acts. This is to ensure that
     * any original status and message is preserved, but that the status of the resend can be returned to the caller.
     *
     * @param event     the reminder
     * @param message   the cancellation reason
     * @param updated   the set of updated acts
     * @param cancelled the set of cancelled reminders
     * @param resend    determines if reminders are being resent. If {@code true}, the reminder itself isn't updated,
     *                  but is added to the set of cancelled reminders
     */
    protected void cancel(ReminderEvent event, String message, List<Act> updated, List<ReminderEvent> cancelled,
                          boolean resend) {
        Act item = event.getItem();
        updateItem(item, ReminderItemStatus.CANCELLED, message);
        if (!resend) {
            updated.add(item);
            Act reminder = updateReminder(event, item);
            if (reminder != null) {
                updated.add(reminder);
            }
        }
        cancelled.add(event);
    }

    /**
     * Determines if a reminder item should be cancelled as it is out of date
     *
     * @param item the item
     * @param from the date to cancel from
     * @return {@code true} if the reminder item should be cancelled
     */
    protected boolean isOutOfDate(Act item, Date from) {
        Date cancel = config.getCancelDate(item.getActivityStartTime(), getArchetype());
        return DateRules.compareDates(cancel, from) <= 0;
    }

    /**
     * Determines if the patient is deceased.
     *
     * @param event the reminder
     * @return {@code true} if the patient is deceased
     */
    protected boolean isDeceased(ReminderEvent event) {
        Party patient = event.getPatient();
        return patient != null && patientRules.isDeceased(patient);
    }

    /**
     * Determines if the patient is inactive.
     *
     * @param event the reminder
     * @return {@code true} if the patient is inactive
     */
    protected boolean isPatientInactive(ReminderEvent event) {
        Party patient = event.getPatient();
        return patient == null || !patient.isActive();
    }

    /**
     * Determines if the customer is inactive.
     *
     * @param event the reminder
     * @return {@code true} if the customer is inactive
     */
    protected boolean isCustomerInactive(ReminderEvent event) {
        Party customer = event.getCustomer();
        return customer == null || !customer.isActive();
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
     * @return {@code true} if any changes were saved
     */
    protected boolean save(PatientReminders state) {
        boolean result = false;
        List<Act> updated = state.getUpdated();
        if (!updated.isEmpty()) {
            service.save(updated);
            result = true;
        }
        return result;
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
     * Assigns a reminder item ERROR status, and populates the error with the exception message.
     *
     * @param item      them item
     * @param exception the cause of the error
     * @return {@code true} if the item was successfully updated
     */
    protected boolean setError(Act item, Throwable exception) {
        boolean result = false;
        String message = null;
        try {
            item.setStatus(ReminderItemStatus.ERROR);
            IMObjectBean bean = new IMObjectBean(item, service);
            message = ErrorFormatter.format(exception);
            if (message != null) {
                int maxLength = bean.getDescriptor("error").getMaxLength();
                message = StringUtils.abbreviate(message, maxLength);
            }
            bean.setValue("error", message);
            bean.save();
            result = true;
        } catch (Throwable error) {
            log.error("Failed to update reminder item=" + item.getId() + " with error=" + message + ": "
                      + error.getMessage(), exception);
        }
        return result;
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
     * @param customer       the reminder
     * @param matcher        the contact matcher
     * @param defaultContact the default contact, or {@code null} to select one from the customer
     * @return the contact, or {@code null} if none is found
     */
    protected Contact getContact(Party customer, ContactMatcher matcher, Contact defaultContact) {
        Contact contact;
        if (defaultContact != null && matcher.isA(defaultContact)) {
            contact = defaultContact;
        } else {
            contact = Contacts.find(Contacts.sort(customer.getContacts()), matcher);
        }
        return contact;
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
        item.setStatus(status);
        ActBean bean = new ActBean(item, service);
        bean.setValue("processed", new Date());
        bean.setValue("error", message);
    }

    /**
     * Updates a reminder if it has no PENDING or ERROR items besides that supplied.
     * <p>
     * This increments the reminder count.
     * <p>
     * The caller is responsible for saving the reminder.
     *
     * @param reminder the reminder
     * @param item     the reminder item
     * @return {@code true} if the reminder was updated
     */
    private boolean updateReminder(Act reminder, Act item) {
        return reminderRules.updateReminder(reminder, item);
    }

}
