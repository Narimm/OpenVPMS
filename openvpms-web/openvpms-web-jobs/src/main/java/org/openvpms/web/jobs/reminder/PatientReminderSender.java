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

package org.openvpms.web.jobs.reminder;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.party.ContactMatcher;
import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.archetype.rules.party.PurposeMatcher;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderCount;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypeCache;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Base class for patient reminder senders.
 *
 * @author Tim Anderson
 */
public abstract class PatientReminderSender {

    public static final String PATIENT_REMINDER = "PATIENT_REMINDER";

    /**
     * The grouped reminder template.
     */
    private final DocumentTemplate groupTemplate;

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The reminder types.
     */
    private final ReminderTypeCache reminderTypes;

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
     * Constructs a {@link PatientReminderSender}.
     *
     * @param groupTemplate the grouped reminder template
     * @param reminderTypes the reminder types
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public PatientReminderSender(DocumentTemplate groupTemplate, ReminderTypeCache reminderTypes,
                                 Party practice, IArchetypeService service, ReminderConfiguration config,
                                 CommunicationLogger logger) {
        this.groupTemplate = groupTemplate;
        this.reminderTypes = reminderTypes;
        this.practice = practice;
        this.service = service;
        this.config = config;
        this.logger = logger;
    }

    /**
     * Processes reminders.
     *
     * @param events the reminder events
     * @param from   the date to use when determining if a reminder item should be cancelled
     * @return the statistics
     */
    public Stats process(List<ObjectSet> events, Date from) {
        List<Act> toSave = new ArrayList<>();
        List<ObjectSet> toProcess = new ArrayList<>();
        int cancelled = 0;
        int sent = 0;
        int errors = 0;
        for (ObjectSet event : events) {
            Act item = (Act) event.get("item");
            if (shouldCancel(item, from, config)) {
                updateItem(item, ReminderItemStatus.CANCELLED, "Reminder not processed in time");
                toSave.add(item);
                Act reminder = updateReminder(event, item);
                if (reminder != null) {
                    toSave.add(reminder);
                }
                cancelled++;
            } else {
                toProcess.add(event);
            }
        }
        if (!toProcess.isEmpty()) {
            ObjectSet set = toProcess.get(0);
            Contact contact = getContact(set);
            if (contact != null) {
                process(contact, toProcess);
                for (ObjectSet event : toProcess) {
                    Act item = updateItem(event, ReminderItemStatus.COMPLETED, null);
                    toSave.add(item);
                    Act reminder = updateReminder(event, item);
                    if (reminder != null) {
                        toSave.add(reminder);
                    }
                    sent++;
                }
            } else {
                noContact(toProcess, toSave);
                errors += toProcess.size();
            }
        }
        service.save(toSave);
        return new Stats(sent, cancelled, errors);
    }

    /**
     * Determines if a reminder item should be cancelled.
     *
     * @param item   the item
     * @param from   the date to cancel from
     * @param config the reminder configuration
     * @return {@code true} if the reminder item should be cancelled
     */
    protected boolean shouldCancel(Act item, Date from, ReminderConfiguration config) {
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
     * Invoked when a customer doesn't have a contact.
     * Flags each reminder item as being in error.
     *
     * @param events the events
     * @param toSave the updated reminder items
     */
    protected void noContact(List<ObjectSet> events, List<Act> toSave) {
        String message = "No " + DescriptorHelper.getDisplayName(getContactArchetype());
        for (ObjectSet event : events) {
            Act item = updateItem(event, ReminderItemStatus.ERROR, message);
            toSave.add(item);
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
        Act result = null;
        Act reminder = (Act) event.get("reminder");
        ActBean bean = new ActBean(reminder, service);
        if (!hasOutstandingItems(bean, item)) {
            int reminderCount = event.getInt("reminderCount");
            if (reminderCount == bean.getInt("reminderCount")) {
                bean.setValue("reminderCount", reminderCount + 1);
                result = reminder;
            }
        }
        return result;
    }

    /**
     * Processes a list of reminder events.
     * <p/>
     * The events all belong to the same customer.
     * <p/>
     * This implementation delegates to {@link #process(Contact, List, String, DocumentTemplate, CommunicationLogger)}.
     *
     * @param contact the contact to send to
     * @param events  the reminder events
     */
    protected void process(Contact contact, List<ObjectSet> events) {
        String shortName;
        DocumentTemplate template = null;

        ObjectSet first = events.get(0);
        events = createObjectSets(events);

        if (events.size() > 1) {
            shortName = "GROUPED_REMINDERS";
            template = groupTemplate;
        } else {
            shortName = ReminderArchetypes.REMINDER;
            ReminderType reminderType = reminderTypes.get((Entity) first.get("reminderType"));
            if (reminderType != null) {
                int reminderCount = first.getInt("reminderCount");
                ReminderCount count = reminderType.getReminderCount(reminderCount);
                if (count != null) {
                    template = count.getTemplate();
                }
            }
        }
        process(contact, events, shortName, template, logger);
    }

    /**
     * Returns the contact to use.
     *
     * @param set the reminder event
     * @return the contact, or {@code null} if none is found
     */
    protected Contact getContact(ObjectSet set) {
        Act item = (Act) set.get("item");
        Party customer = (Party) set.get("customer");
        ActBean bean = new ActBean(item, service);

        boolean useReminderContact = bean.getBoolean("useReminderContact");
        return Contacts.find(Contacts.sort(customer.getContacts()), createContactMatcher(useReminderContact, service));
    }

    /**
     * Returns the contact archetype.
     *
     * @return the contact archetype
     */
    protected abstract String getContactArchetype();

    /**
     * Creates a contact matcher to locate the contact to send to.
     *
     * @param useReminderContact if {@code true}, the contact must have REMINDER purpose
     * @param service            the archetype service
     * @return a new contact matcher
     */
    protected ContactMatcher createContactMatcher(boolean useReminderContact, IArchetypeService service) {
        return new PurposeMatcher(getContactArchetype(), "REMINDER", useReminderContact, service);
    }

    /**
     * Processes a list of reminder events.
     *
     * @param contact   the contact to send to
     * @param events    the events
     * @param shortName the report archetype short name, used to select the document template if none specified
     * @param template  the document template to use. May be {@code null}
     * @param logger    the communication logger. May be {@code null}
     */
    protected abstract void process(Contact contact, List<ObjectSet> events, String shortName,
                                    DocumentTemplate template, CommunicationLogger logger);

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
     * Creates object sets for reporting on the specified events.
     *
     * @param events the events
     * @return a list of object sets corresponding to the events
     */
    protected List<ObjectSet> createObjectSets(List<ObjectSet> events) {
        List<ObjectSet> result = new ArrayList<>();
        for (ObjectSet event : events) {
            ObjectSet set = new ObjectSet();
            Act reminder = (Act) event.get("reminder");
            Party customer = (Party) event.get("customer");
            Party patient = (Party) event.get("patient");
            ActBean bean = new ActBean(reminder, service);
            set.set("act", reminder);
            set.set("customer", customer);
            set.set("patient", patient);
            IMObjectReference reminderTypeRef = bean.getNodeParticipantRef("reminderType");
            ReminderType reminderType = reminderTypes.get(reminderTypeRef);
            set.set("reminderType", reminderType != null ? reminderType.getEntity() : null);
            set.set("patient", patient);
            set.set("product", bean.getNodeParticipant("product"));
            set.set("clinician", bean.getNodeParticipant("clinician"));
            set.set("startTime", reminder.getActivityStartTime());
            set.set("endTime", reminder.getActivityEndTime());
            set.set("reminderCount", bean.getInt("count"));
            result.add(set);
        }
        return result;
    }

    /**
     * Creates a context for a reminder event.
     *
     * @param event the reminder event
     * @return a new context
     */
    protected Context createContext(ObjectSet event) {
        Party patient = (Party) event.get("patient");
        Party customer = (Party) event.get("customer");
        Party location = getCustomerLocation(customer);
        Context context = new LocalContext();
        context.setPatient(patient);
        context.setCustomer(customer);
        context.setLocation(location);
        context.setPractice(practice);
        return context;
    }

    /**
     * Returns a formatted note containing the reminder type and count.
     *
     * @param event the reminder
     * @return the note
     */
    protected String getNote(ObjectSet event) {
        int reminderCount = event.getInt("reminderCount");
        Entity reminderType = (Entity) event.get("reminderType");
        String name = (reminderType != null) ? reminderType.getName() : null;
        return Messages.format("reminder.log.note", name, reminderCount);
    }

    /**
     * Determines if a reminder has any PENDING or ERROR items outstanding, besides that supplied.
     *
     * @param reminder the reminder
     * @param item     the item
     * @return {@code true} if the reminder has outstanding items
     */
    private boolean hasOutstandingItems(ActBean reminder, Act item) {
        Predicate targetEquals = RefEquals.getTargetEquals(item.getObjectReference());
        for (Act act : reminder.getNodeTargetObjects("items", NotPredicate.getInstance(targetEquals), Act.class)) {
            String status = act.getStatus();
            if (ReminderItemStatus.PENDING.equals(status) || ReminderItemStatus.ERROR.equals(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates a reminder item.
     *
     * @param event   the reminder event
     * @param status  the item status
     * @param message the error message. May be {@code null}
     * @return the reminder item
     */
    private Act updateItem(ObjectSet event, String status, String message) {
        Act item = (Act) event.get("item");
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
    private void updateItem(Act item, String status, String message) {
        item.setStatus(status);
        ActBean bean = new ActBean(item, service);
        bean.setValue("error", message);
    }

}
