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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Reminder rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ReminderRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The reminder type cache. May be <tt>null</tt>.
     */
    private final ReminderTypeCache reminderTypes;


    /**
     * Creates a new <tt>ReminderRules</tt>.
     */
    public ReminderRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>ReminderRules</tt>.
     *
     * @param service the archetype service
     */
    public ReminderRules(IArchetypeService service) {
        this(service, null);
    }

    /**
     * Creates a new <tt>ReminderRules</tt>.
     * A reminder type cache can be specified to cache reminders. By default,
     * no cache is used.
     *
     * @param service       the archetype service
     * @param reminderTypes a cache for reminder types. If <tt>null</tt>, no
     *                      caching is used
     */
    public ReminderRules(IArchetypeService service,
                         ReminderTypeCache reminderTypes) {
        this.service = service;
        rules = new PatientRules(service, null, null);
        this.reminderTypes = reminderTypes;
    }

    /**
     * Sets any 'in progress' reminders that have the same patient and matching reminder group and/or type as that in
     * the supplied reminders to 'completed'.
     * <p/>
     * This only has effect if the reminders have 'in progress' status.
     * <p/>
     * This method should be used in preference to {@link #markMatchingRemindersCompleted(Act)} if multiple reminders
     * are being saved which may contain duplicates. The former won't mark duplicates completed if they are all saved
     * within the same transaction.
     * <p/>
     * Reminders are processed in the order they appear in the list. If later reminders match earlier ones, the later
     * ones will be marked 'completed'.
     *
     * @param reminders the reminders
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void markMatchingRemindersCompleted(List<Act> reminders) {
        if (!reminders.isEmpty()) {
            reminders = new ArrayList<Act>(reminders);  // copy it so it can be modified
            while (!reminders.isEmpty()) {
                Act reminder = reminders.remove(0);
                if (ReminderStatus.IN_PROGRESS.equals(reminder.getStatus())) {
                    ActBean bean = new ActBean(reminder, service);
                    ReminderType type = getReminderType(bean);
                    IMObjectReference patient = bean.getNodeParticipantRef("patient");
                    if (type != null && patient != null) {
                        // compare this reminder with the others, to handle matching instances of these first
                        for (Act other : reminders.toArray(new Act[reminders.size()])) {
                            ActBean otherBean = new ActBean(other, service);
                            if (ObjectUtils.equals(patient, otherBean.getNodeParticipantRef("patient"))
                                && hasMatchingTypeOrGroup(other, type)) {
                                markCompleted(other);
                                reminders.remove(other);
                            }
                        }
                    }
                    // now mark any persistent matching reminders completed
                    doMarkMatchingRemindersCompleted(reminder);
                }
            }
        }
    }

    /**
     * Sets any 'in progress' reminders that have the same patient and matching reminder group and/or type as that in
     * the supplied reminder to 'completed'.
     * <p/>
     * This only has effect if the reminder is new and has 'in progress' status.
     * <p/>
     * This method is intended to be invoked just prior to a new reminder being saved.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void markMatchingRemindersCompleted(Act reminder) {
        if (reminder.isNew()) {
            doMarkMatchingRemindersCompleted(reminder);
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
        Date startTime = act.getActivityStartTime();
        ReminderType reminderType = getReminderType(bean);
        Date endTime = null;
        if (startTime != null && reminderType != null) {
            endTime = reminderType.getDueDate(startTime);
        }
        act.setActivityEndTime(endTime);
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
        ReminderType type = new ReminderType(reminderType);
        return type.getDueDate(startTime);
    }

    /**
     * Calculates the due date for a product reminder.
     *
     * @param startTime    the start time
     * @param relationship the product reminder relationship
     * @return the due date for the reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date calculateProductReminderDueDate(Date startTime, EntityRelationship relationship) {
        IMObjectBean bean = new IMObjectBean(relationship, service);
        int period = bean.getInt("period");
        String uom = bean.getString("periodUom");
        return DateRules.getDate(startTime, period, DateUnits.valueOf(uom));
    }

    /**
     * Returns a count of 'in progress' reminders for a patient.
     *
     * @param patient the patient
     * @return the no. of 'in progress' reminders for <tt>patient</tt>
     * @throws ArchetypeServiceException for any error
     */
    public int countReminders(Party patient) {
        NamedQuery query = new NamedQuery("act.patientReminder-count",
                                          Arrays.asList("count"));
        query.setParameter("patientId", patient.getId());
        return count(query);
    }

    /**
     * Returns a count of 'in progress' alerts whose endTime is greater than
     * the specified date/time.
     *
     * @param patient the patient
     * @param date    the date/time
     * @return the no. of 'in progress' alerts for <tt>patient</tt>
     * @throws ArchetypeServiceException for any error
     */
    public int countAlerts(Party patient, Date date) {
        NamedQuery query = new NamedQuery("act.patientAlert-count",
                                          Arrays.asList("count"));
        query.setParameter("patientId", patient.getId());
        query.setParameter("date", date);
        return count(query);
    }

    /**
     * Determines if a reminder is due in the specified date range.
     *
     * @param reminder the reminder
     * @param from     the 'from' date. May be <tt>null</tt>
     * @param to       the 'to' date. Nay be <tt>null</tt>
     * @return <tt>true</tt> if the reminder is due
     */
    public boolean isDue(Act reminder, Date from, Date to) {
        ActBean bean = new ActBean(reminder, service);
        ReminderType reminderType = getReminderType(bean);
        if (reminderType != null) {
            int reminderCount = bean.getInt("reminderCount");
            return reminderType.isDue(reminder.getActivityEndTime(),
                                      reminderCount, from, to);
        }
        return false;
    }

    /**
     * Determines if a reminder needs to be cancelled, based on its due
     * date and the specified date. Reminders should be cancelled if:
     * <p/>
     * <tt>dueDate + (reminderType.cancelInterval * reminderType.cancelUnits) &lt;= date</tt>
     *
     * @param reminder the reminder
     * @param date     the date
     * @return <tt>true</tt> if the reminder needs to be cancelled,
     *         otherwise <tt>false</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean shouldCancel(Act reminder, Date date) {
        ActBean bean = new ActBean(reminder, service);
        // First check if Patient deceased and if so set to Cancel
        Party patient = (Party) bean.getParticipant("participation.patient");
        EntityBean patientBean = new EntityBean(patient, service);
        if (patientBean.getBoolean("deceased", false)) {
            return true;
        }
        // Otherwise get reminderType and check cancel period
        ReminderType reminderType = getReminderType(bean);
        if (reminderType != null) {
            Date due = reminder.getActivityEndTime();
            return reminderType.shouldCancel(due, date);
        }
        return false;
    }

    /**
     * Cancels a reminder.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void cancelReminder(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        bean.setStatus(ReminderStatus.CANCELLED);
        bean.save();
    }

    /**
     * Updates a reminder that has been successfully sent.
     * <p/>
     * This clears the <em>error</em> node.
     *
     * @param reminder the reminder
     * @param lastSent the date when the reminder was sent
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void updateReminder(Act reminder, Date lastSent) {
        ActBean bean = new ActBean(reminder, service);
        int count = bean.getInt("reminderCount");
        bean.setValue("reminderCount", count + 1);
        bean.setValue("lastSent", lastSent);
        bean.setValue("error", null);
        bean.save();
    }

    /**
     * Returns a reminder type template, given the no. of times a reminder
     * has already been sent, and the reminder type.
     *
     * @param reminderCount the no. of times a reminder has been sent
     * @param reminderType  the reminder type
     * @return the corresponding reminder type template, or <tt>null</tt>
     *         if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EntityRelationship getReminderTypeTemplate(int reminderCount,
                                                      Entity reminderType) {
        ReminderType type = new ReminderType(reminderType, service);
        return type.getTemplateRelationship(reminderCount);
    }

    /**
     * Calculates the next due date for a reminder.
     *
     * @param reminder the reminder
     * @return the next due date for the reminder, or <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date getNextDueDate(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        int count = bean.getInt("reminderCount");
        ReminderType reminderType = getReminderType(bean);
        if (reminderType != null) {
            return reminderType.getNextDueDate(reminder.getActivityEndTime(),
                                               count);
        }
        return null;
    }

    /**
     * Returns the contact for a reminder.
     *
     * @param reminder the reminder
     * @return the contact, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getContact(Act reminder) {
        Contact contact = null;
        ActBean bean = new ActBean(reminder, service);
        Party patient = (Party) bean.getParticipant("participation.patient");
        if (patient != null) {
            Party owner = rules.getOwner(patient);
            if (owner != null) {
                contact = getContact(owner, reminder);
            }
        }
        return contact;
    }

    /**
     * Returns the contact for a patient owner and reminder.
     *
     * @param owner    the patient owner
     * @param reminder the reminder
     * @return the contact, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getContact(Party owner, Act reminder) {
        Contact contact;
        ActBean bean = new ActBean(reminder, service);
        ReminderType reminderType = getReminderType(bean);
        EntityRelationship template = null;
        if (reminderType != null) {
            int reminderCount = bean.getInt("reminderCount");
            template = reminderType.getTemplateRelationship(reminderCount);
        }
        if (template != null && template.getTarget() != null) {
            contact = getContact(owner.getContacts());
        } else {
            // no document reminderTypeTemplate, so can't send email or print.
            // Use the customer's phone contact.
            contact = getPhoneContact(owner.getContacts());
        }
        return contact;
    }

    /**
     * Returns the first contact with classification 'REMINDER', or; the
     * preferred contact.location if no contact has this classification,
     * or; the first contact.location if none is preferred, or; the first
     * location if there are no contact.locations.
     *
     * @param contacts the contacts
     * @return a contact, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getContact(Set<Contact> contacts) {
        return getContact(contacts, true, ContactArchetypes.LOCATION);
    }

    /**
     * Returns the first phone contact with classification 'REMINDER' or
     * the preferred phone contact if no contact has this classification.
     *
     * @param contacts the contacts
     * @return a contact, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getPhoneContact(Set<Contact> contacts) {
        return getContact(contacts, false, ContactArchetypes.PHONE);
    }

    /**
     * Returns the first email contact with classification 'REMINDER' or the
     * preferred email contact if no contact has this classification.
     *
     * @param contacts the contacts
     * @return a contact, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getEmailContact(Set<Contact> contacts) {
        return getContact(contacts, false, ContactArchetypes.EMAIL);
    }

    /**
     * Determines if a reminder is associated with an <em>entity.reminderType</em> that is the same as that specified
     * or has one or more <em>lookup.reminderGroup</em> classifications the same as those specified.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @return <tt>true</tt> if the reminder has a matching type or group
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected boolean hasMatchingTypeOrGroup(Act reminder, ReminderType reminderType) {
        boolean result = false;
        ReminderType otherType = getReminderType(reminder);
        if (otherType != null) {
            if (otherType.getEntity().equals(reminderType.getEntity())) {
                result = true;
            } else {
                List<Lookup> groups = reminderType.getGroups();
                for (Lookup group : otherType.getGroups()) {
                    if (groups.contains(group)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the reminder type associated with an act.
     *
     * @param act the act
     * @return the associated reminder type, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected ReminderType getReminderType(Act act) {
        return getReminderType(new ActBean(act, service));
    }

    /**
     * Returns the reminder type associated with an act.
     *
     * @param bean the act bean
     * @return the associated reminder type, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected ReminderType getReminderType(ActBean bean) {
        ReminderType reminderType = null;
        if (reminderTypes != null) {
            reminderType = reminderTypes.get(
                    bean.getParticipantRef(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION));
        } else {
            Entity entity = bean.getParticipant(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION);
            if (entity != null) {
                reminderType = new ReminderType(entity, service);
            }
        }
        return reminderType;
    }

    /**
     * Sets a reminder's status to completed, and updates its completedDate
     * to 'now' before saving it.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void markCompleted(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        bean.setStatus(ReminderStatus.COMPLETED);
        bean.setValue("completedDate", new Date());
        bean.save();
    }

    /**
     * Sets any 'in progress' reminders that have the same patient and matching reminder group and/or type as that in
     * the supplied reminder to 'completed'.
     * <p/>
     * This only has effect if the reminder has 'in progress' status.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service exception
     */
    private void doMarkMatchingRemindersCompleted(Act reminder) {
        if (ReminderStatus.IN_PROGRESS.equals(reminder.getStatus())) {
            ActBean bean = new ActBean(reminder, service);
            ReminderType reminderType = getReminderType(bean);
            IMObjectReference patient = bean.getNodeParticipantRef("patient");
            if (reminderType != null && patient != null) {
                ArchetypeQuery query = new ArchetypeQuery(ReminderArchetypes.REMINDER, false, true);
                query.add(Constraints.eq("status", ReminderStatus.IN_PROGRESS));
                query.add(Constraints.join("patient").add(Constraints.eq("entity", patient)));
                if (!reminder.isNew()) {
                    query.add(Constraints.ne("id", reminder.getId()));
                }
                query.setMaxResults(ArchetypeQuery.ALL_RESULTS); // must query all, otherwise the iteration would change
                IMObjectQueryIterator<Act> reminders = new IMObjectQueryIterator<Act>(service, query);
                while (reminders.hasNext()) {
                    Act act = reminders.next();
                    if (hasMatchingTypeOrGroup(act, reminderType)) {
                        markCompleted(act);
                    }
                }
            }
        }
    }
    
    /**
     * Helper to return a count from a named query.
     *
     * @param query the query
     * @return the count
     * @throws ArchetypeServiceException for any error
     */
    private int count(NamedQuery query) {
        Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
        if (iter.hasNext()) {
            ObjectSet set = iter.next();
            Number count = (Number) set.get("count");
            return count != null ? count.intValue() : 0;
        }
        return 0;
    }

    /**
     * Returns the first contact with classification 'REMINDER' or the
     * preferred contact with the specified short name if no contact has this
     * classification.
     *
     * @param contacts   the contacts
     * @param anyContact if <tt>true</tt> any contact with a 'REMINDER'
     *                   classification will be returned. If there is
     *                   no 'REMINDER' contact, the first preferred contact
     *                   with the short name will be returned. If there is
     *                   no preferred contact then the first contact matching
     *                   the short name will be returned. If there is no
     *                   contact matching the short name, the first preferred
     *                   contact will be returned.
     *                   If <tt>false</tt> only those contacts of type
     *                   <em>shortName</em> will be returned
     * @param shortNames the archetype shortname of the preferred contact
     * @return a contact, or <tt>null</tt> if none is found
     */
    private Contact getContact(Set<Contact> contacts, boolean anyContact, String... shortNames) {
        Contact reminder = null;
        Contact preferred = null;
        Contact fallback = null;
        for (Contact contact : contacts) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            if (bean.isA(shortNames) || anyContact) {
                if (reminder == null || !TypeHelper.isA(reminder, shortNames)) {
                    List<Lookup> purposes = bean.getValues("purposes", Lookup.class);
                    for (Lookup purpose : purposes) {
                        if ("REMINDER".equals(purpose.getCode())) {
                            reminder = contact;
                            break;
                        }
                    }
                }

                if (preferred == null || !TypeHelper.isA(preferred, shortNames)) {
                    if (bean.hasNode("preferred") && bean.getBoolean("preferred")) {
                        preferred = contact;
                    }
                }
                if (fallback == null || !TypeHelper.isA(fallback, shortNames)) {
                    fallback = contact;
                }
            }
        }
        Contact result;
        if (reminder != null) {
            result = reminder;
        } else if (preferred != null && fallback != null) {
            if (TypeHelper.isA(preferred, shortNames)) {
                result = preferred;
            } else if (TypeHelper.isA(fallback, shortNames)) {
                result = fallback;
            } else {
                result = preferred;
            }
        } else if (preferred != null) {
            result = preferred;
        } else {
            result = fallback;
        }
        return result;
    }

}
