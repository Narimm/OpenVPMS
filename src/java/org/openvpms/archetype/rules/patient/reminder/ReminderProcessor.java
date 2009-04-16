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
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


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
     * Template cache.
     */
    private final Map<IMObjectReference, Entity> templates = new HashMap<IMObjectReference, Entity>();

    /**
     * Determines if evaluation of patients and customers should occur, even if the reminder must be cancelled or
     * skipped.
     */
    private boolean evaluateFully = false;


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
     * Determines if reminder events should be fully populated, even if the reminder is to be cancelled or skipped.
     * <p/>
     * For performance, defaults to <tt>false</tt>
     *
     * @param evaluateFully if <tt>true</tt> populate patient, customer and contact information for cancelled and
     *                      skipped reminders
     */
    public void setEvaluateFully(boolean evaluateFully) {
        this.evaluateFully = evaluateFully;
    }

    /**
     * Generates a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @param template     the template. An instance of <em>entityRelationship.reminderTypeTemplate</em>
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent generate(Act reminder, ReminderType reminderType, EntityRelationship template) {
        ReminderEvent result;
        Party patient = getPatient(reminder);
        if (patient != null) {
            Entity documentTemplate = null;
            IMObjectBean templateBean = new IMObjectBean(template, service);
            boolean list = templateBean.getBoolean("list");
            if (!list) {
                documentTemplate = getTemplate(template.getTarget());
            }
            if (!list && documentTemplate == null) {
                // no template, so can't process
                result = skip(reminder, reminderType, patient);
            } else {
                Party customer = getCustomer(patient);
                Contact contact = getContact(customer);
                if (list) {
                    result = list(reminder, reminderType, patient, customer, contact, documentTemplate);
                } else if (TypeHelper.isA(contact, ContactArchetypes.LOCATION)) {
                    result = print(reminder, reminderType, patient, customer, contact, documentTemplate);
                } else if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
                    result = phone(reminder, reminderType, patient, customer, contact, documentTemplate);
                } else if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                    result = email(reminder, reminderType, patient, customer, contact, documentTemplate);
                } else {
                    // no/unrecognised contact
                    result = list(reminder, reminderType, patient, customer, contact, documentTemplate);
                }
            }
        } else {
            // need a patient
            result = skip(reminder, reminderType);
        }
        return result;
    }

    /**
     * Notifies listeners to skip a reminder, when the patient cannot be determined.
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
     * Notifies listeners to skip a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @param patient      the patient. May be <tt>null</tt>
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent skip(Act reminder, ReminderType reminderType, Party patient) {
        return notifyListeners(Action.SKIP, reminder, reminderType, patient);
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
        return notifyListeners(Action.CANCEL, reminder, reminderType, null);
    }

    /**
     * Notifies listeners to email a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param patient          the patient
     * @param customer         the customer
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent email(Act reminder, ReminderType reminderType, Party patient, Party customer,
                                  Contact contact, Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.EMAIL, reminder, reminderType, patient, customer, contact,
                                                documentTemplate);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Notifies listeners to phone a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param patient          the patient
     * @param customer         the customer
     * @param contact          the reminder contact
     * @param documentTemplate the document template. May be <tt>null</tt>
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent phone(Act reminder, ReminderType reminderType, Party patient, Party customer,
                                  Contact contact, Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.PHONE, reminder, reminderType, patient, customer, contact,
                                                documentTemplate);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Notifies listeners to print a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param patient          the patient
     * @param customer         the customer
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent print(Act reminder, ReminderType reminderType, Party patient, Party customer,
                                  Contact contact, Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.PRINT, reminder, reminderType, patient, customer, contact,
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
     * @param patient          the patient
     * @param customer         the customer
     * @param contact          the reminder contact. May be <tt>null</tt>
     * @param documentTemplate the document template
     * @return the reminder event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected ReminderEvent list(Act reminder, ReminderType reminderType, Party patient, Party customer,
                                 Contact contact, Entity documentTemplate) {
        ReminderEvent event = new ReminderEvent(Action.LIST, reminder, reminderType, patient, customer, contact,
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
        IMObjectReference ref = bean.getParticipantRef(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION);
        ReminderType reminderType = reminderTypes.get(ref);
        if (reminderType == null) {
            throw new ReminderProcessorException(NoReminderType);
        }
        Date dueDate = reminder.getActivityEndTime();
        if (rules.shouldCancel(reminder, processingDate)) {
            result = cancel(reminder, reminderType);
        } else {
            if (reminderType.isDue(dueDate, reminderCount, from, to)) {
                EntityRelationship template = reminderType.getTemplateRelationship(reminderCount);
                if (template != null) {
                    result = generate(reminder, reminderType, template);
                } else {
                    // no template, so skip the reminder
                    result = skip(reminder, reminderType, null);
                }
            } else {
                result = skip(reminder, reminderType, null);
            }
        }
        return result;
    }

    /**
     * Notifies listeners of a reminder event.
     *
     * @param action       the event action
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @param patient      the patient. May be <tt>null</tt>
     * @return a new reminder event
     */
    private ReminderEvent notifyListeners(Action action, Act reminder, ReminderType reminderType, Party patient) {
        Party customer = null;
        Contact contact = null;
        if (evaluateFully) {
            if (patient == null) {
                patient = getPatient(reminder);
            }
            if (patient != null) {
                customer = getCustomer(patient);
                contact = getContact(customer);
            }
        }
        ReminderEvent event = new ReminderEvent(action, reminder, reminderType, patient, customer, contact, null);
        notifyListeners(event.getAction(), event);
        return event;
    }

    /**
     * Returns the template for the specified template reference.
     *
     * @param reference the reference. May be <tt>null</tt>
     * @return the corresponding template, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getTemplate(IMObjectReference reference) {
        Entity result = null;
        if (reference != null) {
            result = templates.get(reference);
            if (result == null) {
                result = (Entity) service.get(reference);
                if (result != null) {
                    templates.put(reference, result);
                }
            }
        }
        return result;
    }

    /**
     * Returns the patient of a reminder.
     *
     * @param reminder the reminder
     * @return the patient, or <tt>null</tt> if it cannot be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Party getPatient(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        return (Party) bean.getParticipant("participation.patient");
    }

    /**
     * Returns the customer associated with a patient.
     *
     * @param patient the patient
     * @return the corresponding customer, or <tt>null</tt> if it cannot be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Party getCustomer(Party patient) {
        return patientRules.getOwner(patient);
    }

    /**
     * Returns the default contact for a customer.
     *
     * @param customer the customer. May be <tt>null</tt>
     * @return the default contact, or <tt>null</tt>
     */
    private Contact getContact(Party customer) {
        return (customer != null) ? rules.getContact(customer.getContacts()) : null;
    }

}
