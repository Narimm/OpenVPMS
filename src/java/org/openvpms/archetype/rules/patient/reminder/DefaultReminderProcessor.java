/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient.reminder;

import static org.openvpms.archetype.rules.patient.reminder.ReminderEvent.Action;
import static org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException.ErrorCode.*;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of the {@link ReminderProcessor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultReminderProcessor implements ReminderProcessor {

    /**
     * The processing date.
     */
    private final Date processingDate;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Reminder rules.
     */
    private final ReminderRules rules;

    /**
     * The listeners. Listen to all events.
     */
    private final List<ReminderProcessorListener> listeners;

    /**
     * The listeners, keyed on action type.
     */
    private final Map<ReminderEvent.Action, List<ReminderProcessorListener>>
            actionListeners;


    /**
     * Constructs a new <tt>DefaultReminderProcessor</tt>, using the current
     * time as the processing date.
     */
    public DefaultReminderProcessor() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>ReminderProcessor</code>, using the current time
     * as the processing date.
     *
     * @param service the archetype service
     */
    public DefaultReminderProcessor(IArchetypeService service) {
        this(new Date(), service);
    }

    /**
     * Constructs a new <code>ReminderProcessor</code>.
     *
     * @param processingDate the processing date
     * @param service        the archetype service
     */
    public DefaultReminderProcessor(Date processingDate,
                                    IArchetypeService service) {
        this.processingDate = processingDate;
        this.service = service;
        listeners = new ArrayList<ReminderProcessorListener>();
        actionListeners = new HashMap<ReminderEvent.Action,
                List<ReminderProcessorListener>>();
        rules = new ReminderRules(service);
    }

    /**
     * Process a reminder.
     *
     * @param reminder the reminder to process
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    public void process(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        Entity reminderType = bean.getParticipant("participation.reminderType");
        if (reminderType == null) {
            throw new ReminderProcessorException(NoReminderType);
        }
        Date endTime = reminder.getActivityEndTime();
        if (rules.shouldCancel(endTime, reminderType, processingDate)) {
            cancel(reminder, reminderType);
        } else {
            int reminderCount = bean.getInt("reminderCount");
            EntityRelationship template = rules.getReminderTypeTemplate(
                    reminderCount, reminderType);
            if (template == null || !rules.isDue(endTime, template)) {
                skip(reminder, reminderType);
            } else {
                generate(reminder, reminderType, template);
            }
        }
    }

    /**
     * Adds a listener.
     *
     * @param listener
     */
    public void addListener(ReminderProcessorListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener
     */
    public void removeListener(ReminderProcessorListener listener) {
        listeners.remove(listener);
    }

    /**
     * Adds a listener.
     *
     * @param listener
     */
    public void addListener(Action action, ReminderProcessorListener listener) {
        List<ReminderProcessorListener> list = actionListeners.get(action);
        if (list == null) {
            list = new ArrayList<ReminderProcessorListener>();
            actionListeners.put(action, list);
        }
        list.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener
     */
    public void removeListener(Action action,
                               ReminderProcessorListener listener) {
        List<ReminderProcessorListener> list = actionListeners.get(action);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Generates a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type. An instance of
     *                     <em>entity.reminderType</em>
     * @param template     the template. An instance of
     *                     <em>entityRelationship.reminderTypeTemplate</em>
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected void generate(Act reminder, Entity reminderType,
                            EntityRelationship template) {
        ActBean bean = new ActBean(reminder);
        Party patient = (Party) bean.getParticipant("participation.patient");
        if (patient == null) {
            throw new ReminderProcessorException(NoPatient);
        }
        Contact contact = rules.getContact(patient);
        if (contact == null) {
            throw new ReminderProcessorException(NoContact);
        }

        IMObjectReference target = template.getTarget();
        Entity documentTemplate = null;
        if (target != null) {
            documentTemplate =
                    (Entity) ArchetypeQueryHelper.getByObjectReference(
                            service, target);
        }

        if (documentTemplate != null) {
            if (TypeHelper.isA(contact, "contact.location")) {
                print(reminder, reminderType, contact, documentTemplate);
            } else if (TypeHelper.isA(contact, "contact.phoneNumber")) {
                phone(reminder, reminderType, contact, documentTemplate);
            } else if (TypeHelper.isA(contact, "contact.email")) {
                email(reminder, reminderType, contact, documentTemplate);
            } else {
                // shouldn't occur
                throw new ReminderProcessorException(NoContact);
            }
        } else {
            skip(reminder, reminderType);
        }
    }

    /**
     * Notifies listeners to skip a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder to skip
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected void skip(Act reminder, Entity reminderType) {
        ReminderEvent event = new ReminderEvent(Action.SKIP, reminder,
                                                reminderType);
        notifyListeners(event);
    }

    /**
     * Notifies listeners to cancel a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected void cancel(Act reminder, Entity reminderType) {
        ReminderEvent event = new ReminderEvent(Action.CANCEL, reminder,
                                                reminderType);
        notifyListeners(event);
    }

    /**
     * Notifies listeners to email a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected void email(Act reminder, Entity reminderType, Contact contact,
                         Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.EMAIL, reminder,
                                                reminderType, contact,
                                                documentTemplate);
        notifyListeners(event);
    }

    /**
     * Notifies listeners to phone a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected void phone(Act reminder, Entity reminderType, Contact contact,
                         Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.PHONE, reminder,
                                                reminderType, contact,
                                                documentTemplate);
        notifyListeners(event);
    }

    /**
     * Notifies listeners to print a reminder.
     * <p/>
     * This implementation simply updates the statistics.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected void print(Act reminder, Entity reminderType, Contact contact,
                         Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.PRINT, reminder,
                                                reminderType, contact,
                                                documentTemplate);
        notifyListeners(event);
    }

    /**
     * Notifies listeners of an event.
     *
     * @param event the event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    private void notifyListeners(ReminderEvent event) {
        notifyListeners(listeners, event);
        List<ReminderProcessorListener> list
                = actionListeners.get(event.getAction());
        if (list != null) {
            notifyListeners(list, event);
        }
    }

    /**
     * Notifies listeners of an event.
     *
     * @param list  the listeners to notify
     * @param event the event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    private void notifyListeners(List<ReminderProcessorListener> list,
                                 ReminderEvent event) {
        for (ReminderProcessorListener listener : list) {
            listener.process(event);
        }
    }

}
