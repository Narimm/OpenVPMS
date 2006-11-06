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

import java.util.Date;


/**
 * Abstract implementation of the {@link ReminderProcessor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractReminderProcessor implements ReminderProcessor {

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
     * Processing statistics.
     */
    private final Statistics stats = new Statistics();


    /**
     * Constructs a new <code>ReminderProcessor</code>, using the current time
     * as the processing date.
     */
    public AbstractReminderProcessor() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>ReminderProcessor</code>, using the current time
     * as the processing date.
     *
     * @param service the archetype service
     */
    public AbstractReminderProcessor(IArchetypeService service) {
        this(new Date(), service);

    }

    /**
     * Constructs a new <code>ReminderProcessor</code>.
     *
     * @param processingDate the processing date
     * @param service        the archetype service
     */
    public AbstractReminderProcessor(Date processingDate,
                                     IArchetypeService service) {
        this.processingDate = processingDate;
        this.service = service;
        rules = new ReminderRules(service);
    }

    /**
     * Process a reminder.
     *
     * @param reminder the reminder to process
     */
    public void process(Act reminder) {
        ActBean bean = new ActBean(reminder);
        Entity reminderType = bean.getParticipant("participation.reminderType");
        if (reminderType != null) {
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
        } else {
            // need to flag in error?
        }
    }

    /**
     * Returns statistics.
     *
     * @return statistics
     */
    public Statistics getStatistics() {
        return stats;
    }

    /**
     * Generates a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type. An instance of
     *                     <em>entity.reminderType</em>
     * @param template     the template. An instance of
     *                     <em>entityRelationship.reminderTypeTemplate</em>
     */
    protected void generate(Act reminder, Entity reminderType,
                            EntityRelationship template) {
        Contact contact = null;
        ActBean bean = new ActBean(reminder);
        Party patient = (Party) bean.getParticipant("participation.patient");
        if (patient != null) {
            contact = rules.getContact(patient);
        }
        IMObjectReference target = template.getTarget();
        Entity documentTemplate = null;
        if (target != null) {
            documentTemplate = (Entity) ArchetypeQueryHelper.getByObjectReference(
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
                skip(reminder, reminderType);
            }
        }
    }

    /**
     * Skip a reminder.
     * <p/>
     * This implementation updates the statistics.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder to skip
     */
    protected void skip(Act reminder, Entity reminderType) {
        stats.increment(reminderType, Statistics.Type.SKIPPED);
    }

    /**
     * Cancels a reminder.
     * <p/>
     * This implementation sets the act status to CANCELLED, and updates the
     * statistics.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void cancel(Act reminder, Entity reminderType) {
        reminder.setStatus(ReminderStatus.CANCELLED);
        service.save(reminder);
        stats.increment(reminderType, Statistics.Type.CANCELLED);
    }

    /**
     * Email a reminder.
     * <p/>
     * This implementation simply updates the statistics.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     */
    protected void email(Act reminder, Entity reminderType, Contact contact,
                         Entity documentTemplate) {
        stats.increment(reminderType, Statistics.Type.EMAILED);
    }

    /**
     * Phone a reminder.
     * <p/>
     * This implementation simply updates the statistics.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     */
    protected void phone(Act reminder, Entity reminderType, Contact contact,
                         Entity documentTemplate) {
        stats.increment(reminderType, Statistics.Type.LISTED);
    }

    /**
     * Print a reminder.
     * <p/>
     * This implementation simply updates the statistics.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     */
    protected void print(Act reminder, Entity reminderType, Contact contact,
                         Entity documentTemplate) {
        stats.increment(reminderType, Statistics.Type.PRINTED);
    }
}
