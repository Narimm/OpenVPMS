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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Reminder rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ReminderRules {

    /**
     * Patient reminder act short name.
     */
    public static final String PATIENT_REMINDER = "act.patientReminder";

    /**
     * Reminder type participation short name.
     */
    public static final String REMINDER_TYPE = "participation.reminderType";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <code>ReminderRules</code>.
     *
     * @param service the archetype service
     */
    public ReminderRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets any 'in progress' reminders that have the same patient and
     * reminder group as that in the supplied reminder to 'completed'.
     * This only has effect if the reminder is new, and has 'in progress'
     * status.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void markMatchingRemindersCompleted(Act reminder) {
        if (reminder.isNew() && ReminderStatus.IN_PROGRESS.equals(
                reminder.getStatus())) {
            ActBean bean = new ActBean(reminder, service);
            Entity reminderType = bean.getParticipant(
                    "participation.reminderType");
            Party patient = (Party) bean.getParticipant(
                    "participation.patient");
            if (reminderType != null && patient != null) {
                markMatchingRemindersCompleted(patient, reminderType);
            }
        }
    }

    /**
     * Sets any 'in progress' reminders that have the same patient and reminder
     * group as that being supplied to 'completed'.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @throws ArchetypeServiceException for any archetype service exception
     */
    @SuppressWarnings("unchecked")
    public void markMatchingRemindersCompleted(Party patient,
                                               Entity reminderType) {
        EntityBean bean = new EntityBean(reminderType);
        List<IMObject> groups = bean.getValues("groups");
        if (!groups.isEmpty()) {
            ArchetypeQuery query = new ArchetypeQuery(PATIENT_REMINDER,
                                                      false,
                                                      true);
            query.add(new NodeConstraint("status",
                                         ReminderStatus.IN_PROGRESS));
            IMObjectReference ref = patient.getObjectReference();
            CollectionNodeConstraint participations
                    = new CollectionNodeConstraint(
                    "patient", "participation.patient", false, true)
                    .add(new ObjectRefNodeConstraint("entity", ref));
            query.add(participations);
            query.setNumOfRows(ArchetypeQuery.ALL_ROWS);
            List acts = service.get(query).getRows();
            for (Act act : (List<Act>) acts) {
                if (hasMatchingGroup(groups, act)) {
                    markCompleted(act);
                }
            }
        }
    }

    /**
     * Calculate the due date for a reminder using the reminder's start date
     * plus the default interval and units from the associated reminder type.
     *
     * @param act the act
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void calculateReminderDueDate(Act act) {
        ActBean bean = new ActBean(act, service);
        if (bean.isA(PATIENT_REMINDER)) {
            Date startTime = act.getActivityStartTime();
            Entity reminderType = bean.getParticipant(REMINDER_TYPE);
            Date endTime = null;
            if (startTime != null && reminderType != null) {
                endTime = calculateReminderDueDate(startTime, reminderType);
            }
            act.setActivityEndTime(endTime);
        }
    }

    /**
     * Calculates the due date for a reminder.
     *
     * @param startTime    the start time
     * @param reminderType the reminder type
     * @return the end time for a reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date calculateReminderDueDate(Date startTime, Entity reminderType) {
        EntityBean bean = new EntityBean(reminderType, service);
        int interval = bean.getInt("defaultInterval");
        String units = bean.getString("defaultUnits");
        return calculateDate(startTime, interval, units);
    }

    /**
     * Determines if a reminder needs to be cancelled, based on its due date
     * and the specified date. Reminders should be cancelled if:
     * <em>endTime + (reminderType.cancelInterval * reminderType.cancelUnits)
     * &lt; date</em>
     *
     * @param endTime      the due date
     * @param reminderType the reminderType
     * @param date         the date
     * @return <code>true</code> if the reminder needs to be cancelled,
     *         otherwise <code> false
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean shouldCancel(Date endTime, Entity reminderType, Date date) {
        EntityBean bean = new EntityBean(reminderType, service);
        int interval = bean.getInt("cancelInterval");
        String units = bean.getString("cancelUnits");
        Date cancelDate = calculateDate(endTime, interval, units);
        return (cancelDate.compareTo(date) > 0);
    }

    /**
     * Returns a reminder type template, given the no. of times a reminder
     * has already been sent, and the reminder type.
     *
     * @param reminderCount the no. of times a reminder has been sent
     * @param reminderType  the reminder type
     * @return the corresponding reminder type template, or <code>null</code>
     *         if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EntityRelationship getReminderTypeTemplate(int reminderCount,
                                                      Entity reminderType) {
        EntityBean reminderBean = new EntityBean(reminderType, service);
        List<IMObject> templates = reminderBean.getValues("templates");
        for (IMObject template : templates) {
            IMObjectBean templateBean = new IMObjectBean(template, service);
            if (templateBean.getInt("reminderCount") == reminderCount) {
                return (EntityRelationship) template;
            }
        }
        return null;
    }

    /**
     * Determines if a reminder is due, given its due date, the no. of times
     * a reminder has been sent and the reminder type.
     *
     * @param endTime       the due date
     * @param reminderCount the no. of times a reminder has been sent
     * @param reminderType  the reminder type
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isDue(Date endTime, int reminderCount, Entity reminderType) {
        EntityRelationship template = getReminderTypeTemplate(reminderCount,
                                                              reminderType);
        return isDue(endTime, template);
    }

    /**
     * Determines if a reminder is due, given its due date and the reminder
     * type template.
     *
     * @param endTime              the due date
     * @param reminderTypeTemplate the reminder type template
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isDue(Date endTime,
                         EntityRelationship reminderTypeTemplate) {
        IMObjectBean templateBean = new IMObjectBean(reminderTypeTemplate,
                                                     service);
        int interval = templateBean.getInt("interval");
        String units = templateBean.getString("units");
        Date nextDate = calculateDate(endTime, interval, units);
        return (nextDate.compareTo(endTime) > 0);
    }

    /**
     * Calculates a date for a reminder.
     *
     * @param startTime the start time
     * @param interval  the time interval. May be negative to calculate a date
     *                  in the past
     * @param units     the interval units
     * @return the date for a reminder
     */
    public Date calculateDate(Date startTime, int interval, String units) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        if (units != null) {
            units = units.toLowerCase();
            if (units.equals("years")) {
                calendar.add(Calendar.YEAR, interval);
            } else if (units.equals("months")) {
                calendar.add(Calendar.MONTH, interval);
            } else if (units.equals("weeks")) {
                calendar.add(Calendar.DAY_OF_YEAR, interval * 7);
            } else if (units.equals("days")) {
                calendar.add(Calendar.DAY_OF_YEAR, interval);
            }
        }
        return calendar.getTime();
    }

    /**
     * Returns a contact for a patient.
     * Returns the first contact with classifictation 'Reminders' or the
     * preferred contact.location if no contact has this classification.
     *
     * @param patient the patient
     * @return a contact for the patient, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getContact(Party patient) {
        Contact reminders = null;
        Contact location = null;
        Party owner = new PatientRules(service).getOwner(patient);
        if (owner != null) {
            for (Contact contact : owner.getContacts()) {
                IMObjectBean contactBean = new IMObjectBean(contact, service);
                if (contactBean.isA("contact.location")
                        && contactBean.getBoolean("preferred")) {
                    location = contact;
                } else {
                    List<IMObject> purposes = contactBean.getValues("purposes");
                    for (IMObject purpose : purposes) {
                        // todo - will break if Classification name is changed
                        // will be fixed by OBF-134
                        if ("Reminders".equals(purpose.getName())) {
                            reminders = contact;
                            break;
                        }
                    }
                }
            }
        }
        return (reminders != null) ? reminders : location;
    }

    /**
     * Sets a reminder's status to completed, and updates its completedDate
     * to 'now' before saving it.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void markCompleted(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        bean.setStatus(ReminderStatus.COMPLETED);
        bean.setValue("completedDate", new Date());
        bean.save();
    }

    /**
     * Determines if a reminder is associated with an
     * <em>entity.reminderType</em> that has one or more
     * <em>classification.reminderGroup</em> classifications the same as those
     * specified.
     *
     * @param groups   the groups
     * @param reminder the reminder
     * @return <code>true</code> if the reminder has a matching group
     * @throws ArchetypeServiceException for any archetype service error
     */
    private boolean hasMatchingGroup(List<IMObject> groups, Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        Entity reminderType = bean.getParticipant("participation.reminderType");
        if (reminderType != null) {
            EntityBean typeBean = new EntityBean(reminderType, service);
            for (IMObject group : typeBean.getValues("groups")) {
                if (groups.contains(group)) {
                    return true;
                }
            }
        }
        return false;
    }

}
