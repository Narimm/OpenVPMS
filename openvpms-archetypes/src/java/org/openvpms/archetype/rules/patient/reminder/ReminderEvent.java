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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ObjectSet;


/**
 * Reminder event.
 * <p>
 * This extends {@link ObjectSet} so reminder information can be supplied to reports.
 *
 * @author Tim Anderson
 */
public class ReminderEvent extends ObjectSet {

    /**
     * The reminder.
     */
    private static final String REMINDER = "reminder";

    /**
     * The reminder item.
     */
    private static final String ITEM = "item";

    /**
     * The patient.
     */
    private static final String PATIENT = "patient";

    /**
     * The customer.
     */
    private static final String CUSTOMER = "customer";

    /**
     * The reminder type.
     */
    private static final String REMINDER_TYPE = "reminderType";

    /**
     * The reminder start time. This is its next due date.
     */
    private static final String START_TIME = "startTime";

    /**
     * The reminder end time. This is its first due date.
     */
    private static final String END_TIME = "endTime";

    /**
     * The reminder count.
     */
    private static final String REMINDER_COUNT = "reminderCount";

    /**
     * The product.
     */
    private static final String PRODUCT = "product";

    /**
     * The clinician.
     */
    private static final String CLINICIAN = "clinician";

    /**
     * The contact.
     */
    private static final String CONTACT = "contact";

    /**
     * The practice location.
     */
    private static final String LOCATION = "location";

    /**
     * The document template.
     */
    private static final String DOCUMENT_TEMPLATE = "documentTemplate";

    /**
     * Constructs a {@link ReminderEvent} from an existing set.
     *
     * @param set the set
     */
    public ReminderEvent(ObjectSet set) {
        this((Act) set.get(REMINDER), (Act) set.get(ITEM), (Party) set.get(PATIENT), (Party) set.get(CUSTOMER));
    }

    /**
     * Constructs a {@link ReminderEvent}.
     *
     * @param reminder the reminder
     * @param item     the reminder item
     * @param patient  the patient
     * @param customer the customer
     */
    public ReminderEvent(Act reminder, Act item, Party patient, Party customer) {
        set(REMINDER, reminder);
        set(ITEM, item);
        set(PATIENT, patient);
        set(CUSTOMER, customer);
        set(START_TIME, reminder.getActivityStartTime());
        set(END_TIME, reminder.getActivityEndTime());
    }

    /**
     * Constructs a {@link ReminderEvent}.
     *
     * @param reminder the reminder
     * @param item     the reminder item
     * @param patient  the patient
     * @param customer the customer
     * @param contact  the contact. May be {@code null}
     */
    public ReminderEvent(Act reminder, Act item, Party patient, Party customer, Contact contact) {
        this(reminder, item, patient, customer);
        setContact(contact);
    }

    /**
     * Returns the reminder.
     *
     * @return the reminder
     */
    public Act getReminder() {
        return (Act) get(REMINDER);
    }

    /**
     * Returns the reminder item.
     *
     * @return the reminder item
     */
    public Act getItem() {
        return (Act) get(ITEM);
    }

    /**
     * Returns the reminder type.
     *
     * @return the reminder type. May be {@code null}
     */
    public Entity getReminderType() {
        return safeGet(REMINDER_TYPE, null);
    }

    /**
     * Sets the reminder type.
     *
     * @param reminderType the reminder type
     */
    public void setReminderType(Entity reminderType) {
        set(REMINDER_TYPE, reminderType);
    }

    /**
     * Returns the product.
     *
     * @return the product. May be {@code null}
     */
    public Product getProduct() {
        return safeGet(PRODUCT, null);
    }

    /**
     * Sets the product.
     *
     * @param product the product
     */
    public void setProduct(Product product) {
        set(PRODUCT, product);
    }

    /**
     * Returns the clinician.
     *
     * @return the clinician. May be {@code null}
     */
    public User getClinician() {
        return safeGet(CLINICIAN, null);
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician
     */
    public void setClinician(User clinician) {
        set(CLINICIAN, clinician);
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return (Party) get(PATIENT);
    }

    /**
     * Returns the customer.
     *
     * @return the customer. May be {@code null}
     */
    public Party getCustomer() {
        return (Party) get(CUSTOMER);
    }

    /**
     * Returns the contact.
     *
     * @return the contact. May be {@code null}
     */
    public Contact getContact() {
        return (Contact) safeGet(CONTACT, null);
    }

    /**
     * Sets the contact.
     *
     * @param contact the contact. May be {@code null}
     */
    public void setContact(Contact contact) {
        set("contact", contact);
    }

    /**
     * Returns the document template.
     *
     * @return the document template. May be {@code null}
     */
    public Entity getDocumentTemplate() {
        return (Entity) safeGet(DOCUMENT_TEMPLATE, null);
    }

    /**
     * Returns the reminder count.
     *
     * @return the reminder count
     */
    public int getReminderCount() {
        return safeGet(REMINDER_COUNT, 0);
    }

    /**
     * Sets the reminder count.
     *
     * @param count the reminder count
     */
    public void setReminderCount(int count) {
        set(REMINDER_COUNT, count);
    }

    /**
     * Returns the customer's practice location.
     *
     * @return the customer's practice location. May be {@code null}
     */
    public Party getLocation() {
        return (Party) get(LOCATION);
    }

    /**
     * Sets the customers practice location.
     *
     * @param location the location
     */
    public void setLocation(Party location) {
        set(LOCATION, location);
    }

    /**
     * Returns the named object if it exists, or {@code defaultValue} if it doesn't.
     *
     * @param name         the object name
     * @param defaultValue the default value
     * @return the named object if it exists, or {@code defaultValue} if it doesn't
     */
    @SuppressWarnings("unchecked")
    private <T> T safeGet(String name, T defaultValue) {
        return exists(name) ? (T) get(name) : defaultValue;
    }
}
