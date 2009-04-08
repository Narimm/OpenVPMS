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

import org.openvpms.archetype.component.processor.AbstractActionProcessor;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import static org.openvpms.archetype.rules.patient.reminder.ReminderEvent.Action;
import static org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException.ErrorCode.NoPatient;
import static org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException.ErrorCode.NoReminderType;
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
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;


/**
 * Reminder processor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderProcessor
        extends AbstractActionProcessor<Action, Act, ReminderEvent> {

    /**
     * The processing date, used to determine when reminders should be
     * cancelled.
     */
    private final Date processingDate;

    /**
     * The 'from' date. If non-null, only process reminders that have a next-due
     * date >= from.
     */
    private final Date from;

    /**
     * The 'to' date. If non-null, only process reminders that have a next-due
     * date <= to.
     */
    private final Date to;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Reminder rules.
     */
    private final ReminderRules rules;

    /**
     * Patient rules.
     */
    private final PatientRules patientRules;

    /**
     * Reminder type cache.
     */
    private final ReminderTypeCache reminderTypes;


    /**
     * Constructs a new <tt>DefaultReminderProcessor</tt>, using the current
     * time as the processing date.
     *
     * @param from the 'from' date. May be <tt>null</tt>
     * @param to   the 'to' date. Nay be <tt>null</tt>
     */
    public ReminderProcessor(Date from, Date to) {
        this(from, to, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>ReminderProcessor</tt>, using the current time
     * as the processing date.
     *
     * @param from    the 'from' date. May be <tt>null</tt>
     * @param to      the 'to' date. Nay be <tt>null</tt>
     * @param service the archetype service
     */
    public ReminderProcessor(Date from, Date to, IArchetypeService service) {
        this(from, to, new Date(), service);
    }

    /**
     * Constructs a new <tt>ReminderProcessor</tt>.
     *
     * @param from           the 'from' date. May be <tt>null</tt>
     * @param to             the 'to' date. Nay be <tt>null</tt>
     * @param processingDate the processing date
     * @param service        the archetype service
     */
    public ReminderProcessor(Date from, Date to, Date processingDate, IArchetypeService service) {
        this.from = from;
        this.to = to;
        this.processingDate = processingDate;
        this.service = service;
        rules = new ReminderRules(service, new ReminderTypeCache());
        patientRules = new PatientRules(service);
        reminderTypes = new ReminderTypeCache(service);
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
        int reminderCount = bean.getInt("reminderCount");
        process(reminder, reminderCount, bean);
    }

    /**
     * Process a reminder for a particular reminder count.
     *
     * @param reminder      the reminder
     * @param reminderCount the reminder count
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    public ReminderEvent process(Act reminder, int reminderCount) {
        ActBean bean = new ActBean(reminder, service);
        return process(reminder, reminderCount, bean);
    }

    /**
     * Generates a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @param template     the template. An instance of
     *                     <em>entityRelationship.reminderTypeTemplate</em>,
     *                     or <tt>null</tt> if there is no template
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent generate(Act reminder, ReminderType reminderType, EntityRelationship template) {
        ReminderEvent result;
        ActBean bean = new ActBean(reminder);
        Party patient = (Party) bean.getParticipant("participation.patient");
        if (patient == null) {
            throw new ReminderProcessorException(NoPatient);
        }
        Entity documentTemplate = null;
        if (template != null) {
            IMObjectReference target = template.getTarget();
            if (target != null) {
                documentTemplate = (Entity) service.get(target);
            }
        }

        Contact contact = null;
        Party owner = patientRules.getOwner(patient);
        if (owner != null) {
            if (documentTemplate == null) {
                // no document template, so can't send email or print. Use the
                // customer's phone contact.
                contact = rules.getPhoneContact(owner.getContacts());
            } else {
                contact = rules.getContact(owner.getContacts());
            }
        }
        if (TypeHelper.isA(contact, ContactArchetypes.LOCATION)) {
            result = print(reminder, reminderType, owner, contact, documentTemplate);
        } else if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
            result = phone(reminder, reminderType, owner, contact, documentTemplate);
        } else if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
            result = email(reminder, reminderType, owner, contact, documentTemplate);
        } else {
            // no/unrecognised contact
            result = list(reminder, reminderType, owner, contact, documentTemplate);
        }
        return result;
    }

    /**
     * Notifies listeners to skip a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent skip(Act reminder, ReminderType reminderType) {
        ReminderEvent event = new ReminderEvent(Action.SKIP, reminder, reminderType);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Notifies listeners to cancel a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent cancel(Act reminder, ReminderType reminderType) {
        ReminderEvent event = new ReminderEvent(Action.CANCEL, reminder, reminderType);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Notifies listeners to email a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param customer         the customer
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent email(Act reminder, ReminderType reminderType, Party customer, Contact contact,
                                  Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.EMAIL, reminder, reminderType, customer, contact,
                                                documentTemplate);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Notifies listeners to phone a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param customer         the customer
     * @param contact          the reminder contact
     * @param documentTemplate the document template. May be <tt>null</tt>   @return the reminder event
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent phone(Act reminder, ReminderType reminderType, Party customer, Contact contact,
                                  Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.PHONE, reminder, reminderType, customer, contact,
                                                documentTemplate);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Notifies listeners to print a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param customer         the customer
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent print(Act reminder, ReminderType reminderType, Party customer, Contact contact,
                                  Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.PRINT, reminder, reminderType, customer, contact,
                                                documentTemplate);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Notifies listeners to list a reminder. This is for reminders that
     * have no contact, or a contact that is not one of
     * <em>contact.location<em>, <em>contact.phoneNumber</em>,
     * or <em>contact.email</em>
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param customer         the customer
     * @param contact          the reminder contact. May be <tt>null</tt>
     * @param documentTemplate the document template
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent list(Act reminder, ReminderType reminderType, Party customer, Contact contact,
                                 Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.LIST, reminder, reminderType, customer, contact,
                                                documentTemplate);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Process a reminder for a particular reminder count.
     *
     * @param reminder      the reminder to process
     * @param reminderCount the reminder count
     * @param bean          contains the reminder
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    private ReminderEvent process(Act reminder, int reminderCount, ActBean bean) {
        ReminderEvent result;
        ReminderType reminderType = reminderTypes.get(bean.getParticipantRef("participation.reminderType"));
        if (reminderType == null) {
            throw new ReminderProcessorException(NoReminderType);
        }
        Date dueDate = reminder.getActivityEndTime();
        if (rules.shouldCancel(reminder, processingDate)) {
            result = cancel(reminder, reminderType);
        } else {
            if (reminderType.isDue(dueDate, reminderCount, from, to)) {
                EntityRelationship template = reminderType.getTemplateRelationship(reminderCount);
                result = generate(reminder, reminderType, template);
            } else {
                result = skip(reminder, reminderType);
            }
        }
        return result;
    }

}
