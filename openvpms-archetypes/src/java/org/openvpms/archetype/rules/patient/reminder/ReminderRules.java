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

package org.openvpms.archetype.rules.patient.reminder;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;


/**
 * Reminder and alert rules.
 *
 * @author Tim Anderson
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
     * The reminder type cache. May be {@code null}.
     */
    private final ReminderTypes reminderTypes;

    /**
     * Reminder due indicator.
     */
    public enum DueState {
        NOT_DUE,      // indicates the reminder is in the future, outside the sensitivity period
        DUE,          // indicates the reminder is inside the sensitivity period
        OVERDUE       // indicates the reminder is overdue
    }

    /**
     * Constructs a {@link ReminderRules}.
     *
     * @param service      the archetype service
     * @param patientRules the patient rules
     */
    public ReminderRules(IArchetypeService service, PatientRules patientRules) {
        this(service, null, patientRules);
    }

    /**
     * Constructs a {@link ReminderRules}.
     * <p/>
     * A reminder type cache can be specified to cache reminders. By default, no cache is used.
     *
     * @param service       the archetype service
     * @param reminderTypes a cache for reminder types. If {@code null}, no caching is used
     * @param rules         the patient rules
     */
    public ReminderRules(IArchetypeService service, ReminderTypes reminderTypes, PatientRules rules) {
        this.service = service;
        this.rules = rules;
        this.reminderTypes = reminderTypes;
    }

    /**
     * Returns the reminder configuration associated with a practice.
     *
     * @param practice the practice
     * @return the reminder configuration, or {@code null} if none is configured
     */
    public ReminderConfiguration getConfiguration(Party practice) {
        IMObjectBean bean = new IMObjectBean(practice, service);
        IMObject object = bean.getNodeTargetObject("reminderConfiguration");
        return object != null ? new ReminderConfiguration(object, service) : null;
    }

    /**
     * Sets any IN_PROGRESS reminders that have the same patient and matching reminder group and/or type as that in
     * the supplied reminders to COMPLETED.
     * <p/>
     * This only has effect if the reminders have IN_PROGRESS status.
     * <p/>
     * This method should be used in preference to {@link #markMatchingRemindersCompleted(Act)} if multiple reminders
     * are being saved which may contain duplicates. The former won't mark duplicates completed if they are all saved
     * within the same transaction.
     * <p/>
     * Reminders are processed in the order they appear in the list. If later reminders match earlier ones, the later
     * ones will be marked COMPLETED.
     *
     * @param reminders the reminders
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void markMatchingRemindersCompleted(List<Act> reminders) {
        if (!reminders.isEmpty()) {
            reminders = new ArrayList<>(reminders);  // copy it so it can be modified
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
                    doMarkMatchingRemindersCompleted(reminder, type, patient);
                }
            }
        }
    }

    /**
     * Sets any IN_PROGRESS reminders that have the same patient and matching reminder group and/or type as that in
     * the supplied reminder to COMPLETED.
     * <p/>
     * This only has effect if the reminder is new and has IN_PROGRESS status.
     *
     * @param reminder the reminder
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void markMatchingRemindersCompleted(Act reminder) {
        doMarkMatchingRemindersCompleted(reminder);
    }

    /**
     * Sets any IN_PROGRESS alert that have the same patient and matching alert type as that in the supplied reminder to
     * COMPLETED.
     * <p/>
     * This only has effect if the alert is new and has IN_PROGRESS status.
     *
     * @param alert the alert
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void markMatchingAlertsCompleted(Act alert) {
        if (ReminderStatus.IN_PROGRESS.equals(alert.getStatus())) {
            ActBean bean = new ActBean(alert, service);
            IMObjectReference patient = bean.getNodeParticipantRef("patient");
            IMObjectReference alertType = bean.getNodeParticipantRef("alertType");
            if (alertType != null && patient != null) {
                markMatchingAlertsCompleted(alert, patient, alertType);
            }
        }
    }

    /**
     * Sets any IN_PROGRESS alerts that have the same patient and matching alert type as that in the supplied alerts
     * to COMPLETED.
     * <p/>
     * This only has effect if the alerts have IN_PROGRESS status.
     * <p/>
     * This method should be used in preference to {@link #markMatchingAlertsCompleted(Act)} if multiple alerts
     * are being saved which may contain duplicates. The former won't mark duplicates completed if they are all saved
     * within the same transaction.
     * <p/>
     * Alerts are processed in the order they appear in the list. If later alerts match earlier ones, the later
     * ones will be marked COMPLETED.
     *
     * @param alerts the reminders
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void markMatchingAlertsCompleted(List<Act> alerts) {
        if (!alerts.isEmpty()) {
            alerts = new ArrayList<>(alerts);  // copy it so it can be modified
            while (!alerts.isEmpty()) {
                Act alert = alerts.remove(0);
                if (ActStatus.IN_PROGRESS.equals(alert.getStatus())) {
                    ActBean bean = new ActBean(alert, service);
                    IMObjectReference alertType = bean.getNodeParticipantRef("alertType");
                    IMObjectReference patient = bean.getNodeParticipantRef("patient");
                    if (alertType != null && patient != null) {
                        // compare this alert with the others, to handle matching instances of these first
                        for (Act other : alerts.toArray(new Act[alerts.size()])) {
                            ActBean otherBean = new ActBean(other, service);
                            if (ObjectUtils.equals(patient, otherBean.getNodeParticipantRef("patient"))
                                && ObjectUtils.equals(alertType, otherBean.getNodeParticipantRef("alertType"))) {
                                markAlertCompleted(other);
                                alerts.remove(other);
                            }
                        }
                    }
                    // now mark any persistent matching reminders completed
                    markMatchingAlertsCompleted(alert, patient, alertType);
                }
            }
        }
    }

    /**
     * Calculates the due date for a reminder.
     *
     * @param date         the date to calculate the due date from
     * @param reminderType the reminder type
     * @return the end time for a reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date calculateReminderDueDate(Date date, Entity reminderType) {
        ReminderType type = new ReminderType(reminderType, service);
        return type.getDueDate(date);
    }

    /**
     * Calculates the due date for a product reminder.
     *
     * @param date         the date to calculate the due date from
     * @param relationship the product reminder relationship
     * @return the due date for the reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date calculateProductReminderDueDate(Date date, EntityRelationship relationship) {
        IMObjectBean bean = new IMObjectBean(relationship, service);
        int period = bean.getInt("period");
        String uom = bean.getString("periodUom", "YEARS");
        return DateRules.getDate(date, period, DateUnits.valueOf(uom));
    }

    /**
     * Determines if a reminder needs to be cancelled, based on its due
     * date and the specified date. Reminders should be cancelled if:
     * <p/>
     * <ul>
     * <li>{@code dueDate + (reminderType.cancelInterval * reminderType.cancelUnits) &lt;= date}</li>
     * <li>the patient is deceased or inactive</li>
     * </ul>
     *
     * @param reminder the reminder
     * @param date     the date
     * @return {@code true} if the reminder needs to be cancelled,
     * otherwise {@code false}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean shouldCancel(Act reminder, Date date) {
        boolean result = true;
        ActBean bean = new ActBean(reminder, service);
        ReminderType reminderType = getReminderType(bean);
        if (reminderType != null) {
            Date due = reminder.getActivityStartTime();
            result = reminderType.shouldCancel(due, date);
        }
        if (!result) {
            Party patient = (Party) bean.getNodeParticipant("patient");
            result = patient == null || rules.isDeceased(patient);
        }
        return result;
    }

    /**
     * Updates a reminder if it has no PENDING or ERROR items besides that supplied.
     * <p/>
     * This increments the reminder count.
     * <p/>
     * The caller is responsible for saving the reminder.
     *
     * @param reminder the reminder
     * @param item     the reminder item
     * @return {@code true} if the reminder was updated
     */
    public boolean updateReminder(Act reminder, Act item) {
        boolean result = false;
        ActBean bean = new ActBean(reminder, service);
        if (!hasOutstandingItems(bean, item)) {
            ActBean itemBean = new ActBean(item, service);
            int count = itemBean.getInt("count");
            if (count == bean.getInt("reminderCount")) {
                count++;
                bean.setValue("reminderCount", count);
                result = true;
                ReminderType reminderType = getReminderType(bean);
                if (reminderType != null) {
                    Date dueDate = reminderType.getNextDueDate(reminder.getActivityEndTime(), count);
                    if (dueDate != null) {
                        reminder.setActivityStartTime(dueDate);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the reminder types associated with a product.
     *
     * @param product the product
     * @return the associated reminder types, keyed on their <em>entityRelationship.productReminder</em>
     */
    public Map<EntityRelationship, Entity> getReminderTypes(Product product) {
        Map<EntityRelationship, Entity> result;
        IMObjectBean bean = new IMObjectBean(product, service);
        if (bean.hasNode("reminders")) {
            result = bean.getNodeTargetObjects("reminders", Entity.class, EntityRelationship.class);
        } else {
            result = Collections.emptyMap();
        }
        return result;
    }

    /**
     * Returns a reminder associated with an <em>act.patientDocumentForm</em>.
     * <p/>
     * For forms linked to an invoice item (via <em>actRelationship.invoiceItemDocument)</em>, this
     * uses the invoice item to get the reminder. If there are multiple reminders for the invoice item,
     * the one with the nearest due date will be returned.
     * <br/>
     * If there are multiple reminders with the same due date, the reminder with the lesser id will be used.
     * <p/>
     * For forms not linked to an invoice item that have a product with reminders, a reminder with the nearest due date
     * to that of the form's start time will be returned.
     * <p/>
     * For forms that don't meet the above, {@code null} is returned.
     *
     * @param form the form
     * @return the reminder, or {@code null} if there are no associated reminders
     */
    public Act getDocumentFormReminder(DocumentAct form) {
        Act result;
        ActBean formBean = new ActBean(form, service);
        Act invoiceItem = formBean.getSourceAct("actRelationship.invoiceItemDocument");
        if (invoiceItem != null) {
            result = getInvoiceReminder(invoiceItem);
        } else {
            result = getProductReminder(formBean);
        }
        return result;
    }

    /**
     * Determines the due state of a reminder relative to the current date.
     *
     * @param reminder the reminder
     * @return the due state
     */
    public DueState getDueState(Act reminder) {
        return getDueState(reminder, new Date());
    }

    /**
     * Determines the due state of a reminder relative to the specified date.
     *
     * @param reminder the reminder
     * @param date     the date
     * @return the due state
     */
    public DueState getDueState(Act reminder, Date date) {
        ActBean act = new ActBean(reminder, service);
        DueState result = DueState.NOT_DUE;
        Entity reminderType = act.getParticipant(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION);
        if (reminderType != null) {
            EntityBean bean = new EntityBean(reminderType);
            String sensitivityUnits = bean.getString("sensitivityUnits");
            if (sensitivityUnits == null) {
                sensitivityUnits = DateUnits.DAYS.toString();
            }
            int interval = bean.getInt("sensitivityInterval");
            DateUnits units = DateUnits.valueOf(sensitivityUnits);
            Date from = DateRules.getDate(date, -interval, units);
            Date to = DateRules.getDate(date, interval, units);
            Date dueDate = act.getAct().getActivityEndTime();
            if (dueDate != null) {
                if (DateRules.compareTo(dueDate, from) < 0) {
                    result = DueState.OVERDUE;
                } else if (DateRules.compareTo(dueDate, to) <= 0) {
                    result = DueState.DUE;
                }
            }
        }
        return result;
    }

    /**
     * Returns all reminders for a patient starting in the specified date range.
     *
     * @param patient the patient
     * @param from    the start of the date range, inclusive
     * @param to      the end of the date range, exclusive
     * @return all reminders for the patient in the date range
     */
    public Iterable<Act> getReminders(Party patient, Date from, Date to) {
        ArchetypeQuery query = createQuery(patient);
        return getReminders(query, from, to);
    }

    /**
     * Returns all reminders for a patient starting in the specified date range.
     *
     * @param patient     the patient
     * @param productType the product type to match. May contain wildcards
     * @param from        the start of the date range, inclusive
     * @param to          the end of the date range, exclusive
     * @return all reminders for the patient in the date range
     */
    public Iterable<Act> getReminders(Party patient, String productType, Date from, Date to) {
        ArchetypeQuery query = createQuery(patient);
        query.add(join("product").add(join("entity").add(join("type").add(join("target").add(
                eq("name", productType))))));
        return getReminders(query, from, to);
    }

    /**
     * Returns a reminder item for a reminder count and contact.
     *
     * @param reminder the reminder
     * @param count    the reminder count
     * @param contact  the contact
     * @return the reminder, or {@code null} if none is found
     */
    public Act getReminderItem(Act reminder, int count, Contact contact) {
        Act result = null;
        boolean email = TypeHelper.isA(contact, ContactArchetypes.EMAIL);
        boolean phone = TypeHelper.isA(contact, ContactArchetypes.PHONE);
        boolean location = TypeHelper.isA(contact, ContactArchetypes.LOCATION);
        if (email || phone || location) {
            ActBean bean = new ActBean(reminder, service);
            for (Act item : bean.getNodeActs("items")) {
                ActBean itemBean = new ActBean(item, service);
                if (itemBean.getInt("count") == count) {
                    if ((TypeHelper.isA(item, ReminderArchetypes.EMAIL_REMINDER) && email)
                        || (TypeHelper.isA(item, ReminderArchetypes.SMS_REMINDER) && phone)
                        || (TypeHelper.isA(item, ReminderArchetypes.PRINT_REMINDER) && location)) {
                        result = item;
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected List<Act> getReminders(ArchetypeQuery query, Date from, Date to) {
        List<Act> result = new ArrayList<>();
        for (Act act : new IterableIMObjectQuery<Act>(service, query)) {
            ActBean bean = new ActBean(act, service);
            Date date = bean.getDate("createdTime");
            if (date != null && DateRules.between(date, from, to)) {
                result.add(act);
            }
        }
        return result;
    }

    /**
     * Creates a query for all reminders for a patient.
     *
     * @param patient the patient
     * @return a new query
     */
    private ArchetypeQuery createQuery(Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(ReminderArchetypes.REMINDER);
        query.add(join("patient").add(eq("entity", patient)));
        return query;
    }

    /**
     * Returns a reminder associated with an invoice item.
     * <p/>
     * If there are multiple reminders for the invoice item, the one with the nearest due date will be returned.
     *
     * @param invoiceItem the invoice item
     * @return the reminder, or {@code null} if there are no associated reminders
     */
    private Act getInvoiceReminder(Act invoiceItem) {
        Act result = null;
        Date resultDueDate = null;
        ActBean bean = new ActBean(invoiceItem, service);
        List<Act> reminders = bean.getNodeActs("reminders");
        for (Act reminder : reminders) {
            Date dueDate = reminder.getActivityEndTime();
            if (dueDate != null) {
                boolean found = false;
                if (result == null) {
                    found = true;
                } else {
                    int compare = DateRules.compareTo(dueDate, resultDueDate);
                    if (compare < 0 || (compare == 0 && reminder.getId() < result.getId())) {
                        found = true;
                    }
                }
                if (found) {
                    result = reminder;
                    resultDueDate = dueDate;
                }
            }
        }
        return result;
    }

    /**
     * Returns a product reminder with the nearest due date to that of the forms start time will be returned.
     *
     * @param formBean the <em>act.patientDocumentForm</em> bean
     * @return the reminder, or {@code null} if there are no reminders associated with the product
     */
    private Act getProductReminder(ActBean formBean) {
        Act result = null;
        Date resultDueDate = null;
        Product product = (Product) formBean.getNodeParticipant("product");
        if (product != null) {
            Party patient = (Party) formBean.getNodeParticipant("patient");
            Date startTime = formBean.getDate("startTime");
            Map<EntityRelationship, Entity> reminderTypes = getReminderTypes(product);
            for (Map.Entry<EntityRelationship, Entity> entry : reminderTypes.entrySet()) {
                EntityRelationship relationship = entry.getKey();
                Entity reminderType = entry.getValue();
                Date dueDate = calculateProductReminderDueDate(startTime, relationship);
                if (resultDueDate == null || DateRules.compareTo(dueDate, resultDueDate) < 1) {
                    result = createReminder(reminderType, startTime, dueDate, patient, product);
                    resultDueDate = dueDate;
                }
            }
        }
        return result;
    }

    /**
     * Determines if a reminder is associated with an <em>entity.reminderType</em> that is the same as that specified
     * or has one or more <em>lookup.reminderGroup</em> classifications the same as those specified.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @return {@code true} if the reminder has a matching type or group
     * @throws ArchetypeServiceException for any archetype service error
     */
    private boolean hasMatchingTypeOrGroup(Act reminder, ReminderType reminderType) {
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
     * @return the associated reminder type, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private ReminderType getReminderType(Act act) {
        return getReminderType(new ActBean(act, service));
    }

    /**
     * Returns the reminder type associated with an act.
     *
     * @param bean the act bean
     * @return the associated reminder type, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private ReminderType getReminderType(ActBean bean) {
        ReminderType reminderType = null;
        if (reminderTypes != null) {
            reminderType = reminderTypes.get(bean.getNodeParticipantRef("reminderType"));
        } else {
            Entity entity = bean.getNodeParticipant("reminderType");
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
    private void markCompleted(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        bean.setStatus(ReminderStatus.COMPLETED);
        bean.setValue("completedDate", new Date());
        bean.save();
    }

    /**
     * Sets any IN_PROGRESS reminders that have the same patient and matching reminder group and/or type as that in
     * the supplied reminder to COMPLETED.
     * <p/>
     * This only has effect if the reminder has IN_PROGRESS status.
     * <p/>
     * If the reminder is set to expire, it is also marked COMPLETED.
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
                doMarkMatchingRemindersCompleted(reminder, reminderType, patient);
            }
        }
    }

    /**
     * Sets any IN_PROGRESS reminders that have the same patient and matching reminder group and/or type as that in
     * the supplied reminder to COMPLETED.
     * <p/>
     * This only has effect if the reminder has IN_PROGRESS status.
     * <p/>
     * If the reminder is set to expire, it is also marked COMPLETED.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @param patient      the patient reference
     * @throws ArchetypeServiceException for any archetype service exception
     */
    private void doMarkMatchingRemindersCompleted(Act reminder, ReminderType reminderType, IMObjectReference patient) {
        ArchetypeQuery query = new ArchetypeQuery(ReminderArchetypes.REMINDER, false, true);
        query.add(eq("status", ReminderStatus.IN_PROGRESS));
        query.add(join("patient").add(eq("entity", patient)));
        if (!reminder.isNew()) {
            query.add(Constraints.ne("id", reminder.getId()));
        }
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS); // must query all, otherwise the iteration would change
        IMObjectQueryIterator<Act> reminders = new IMObjectQueryIterator<>(service, query);
        while (reminders.hasNext()) {
            Act act = reminders.next();
            if (hasMatchingTypeOrGroup(act, reminderType)) {
                markCompleted(act);
            }
        }
        // if the reminder is set to expire immediately, mark it COMPLETED
        if (reminderType.shouldCancel(reminder.getActivityEndTime(), new Date())) {
            markCompleted(reminder);
        }
    }

    /**
     * Marks alerts with the same patient and alert type as that supplied, COMPLETED.
     * <p/>
     * If the alert has expired, it is also marked COMPLETED.
     *
     * @param alert     the alert
     * @param patient   the patient
     * @param alertType the alert type
     */
    private void markMatchingAlertsCompleted(Act alert, IMObjectReference patient, IMObjectReference alertType) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.ALERT, false, true);
        query.add(eq("status", ActStatus.IN_PROGRESS));
        query.add(join("patient").add(eq("entity", patient)));
        query.add(join("alertType").add(eq("entity", alertType)));
        if (!alert.isNew()) {
            query.add(Constraints.ne("id", alert.getId()));
        }
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS); // must query all, otherwise the iteration would change
        IMObjectQueryIterator<Act> alerts = new IMObjectQueryIterator<>(service, query);
        while (alerts.hasNext()) {
            Act next = alerts.next();
            markAlertCompleted(next);
        }
        Date endTime = alert.getActivityEndTime();
        if (endTime != null && DateRules.compareTo(endTime, new Date()) < 0) {
            markAlertCompleted(alert);
        }
    }

    /**
     * Marks an alert as {@link ActStatus#COMPLETED}, setting the end time to the current time.
     *
     * @param alert the alert
     */
    private void markAlertCompleted(Act alert) {
        alert.setStatus(ActStatus.COMPLETED);
        alert.setActivityEndTime(new Date());
        service.save(alert);
    }

    /**
     * Creates a reminder.
     *
     * @param reminderType the reminder type
     * @param date         the reminder created time
     * @param dueDate      the reminder due date
     * @param patient      the patient
     * @param product      the product. May be {@code null}
     * @return a new reminder
     * @throws ArchetypeServiceException for any error
     */
    private Act createReminder(Entity reminderType, Date date, Date dueDate, Party patient, Product product) {
        Act result = (Act) service.create(ReminderArchetypes.REMINDER);
        ActBean bean = new ActBean(result, service);
        bean.setValue("createdTime", date);
        bean.addNodeParticipation("reminderType", reminderType);
        bean.addNodeParticipation("patient", patient);
        if (product != null) {
            bean.addNodeParticipation("product", product);
        }
        result.setActivityStartTime(dueDate);
        result.setActivityEndTime(dueDate);
        return result;
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

}
