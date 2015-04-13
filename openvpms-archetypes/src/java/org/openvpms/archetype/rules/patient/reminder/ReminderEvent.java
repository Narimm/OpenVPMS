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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;


/**
 * Reminder event.
 *
 * @author Tim Anderson
 */
public class ReminderEvent {

    public enum Action {

        SKIP,        // indicates reminder should be skipped as it is not due
        CANCEL,      // indicates reminder should be cancelled
        EMAIL,       // indicates reminder should be emailed
        PHONE,       // indicates reminder should be phoned
        SMS,         // indicates reminder should be sent via SMS
        PRINT,       // indicates reminder should be printed
        EXPORT,      // indicates reminder should be exported
        LIST         // indicates reminder has no or unrecognised contact
    }

    /**
     * The reminder action.
     */
    private final Action action;

    /**
     * The reminder.
     */
    private final Act reminder;

    /**
     * The reminder type.
     */
    private final ReminderType reminderType;

    /**
     * The patient. May be {@code null}
     */
    private final Party patient;

    /**
     * The customer. May be {@code null}
     */
    private final Party customer;

    /**
     * The contact. May be {@code null}.
     */
    private final Contact contact;

    /**
     * The document template. May be {@code null}.
     */
    private final Entity documentTemplate;

    /**
     * Constructs a new {@code ReminderEvent}.
     *
     * @param action       the reminder action
     * @param reminder     the reminder
     * @param reminderType the reminder type
     */
    public ReminderEvent(Action action, Act reminder, ReminderType reminderType) {
        this(action, reminder, reminderType, null, null);
    }

    /**
     * Constructs a new {@code ReminderEvent}.
     *
     * @param action       the reminder action
     * @param reminder     the reminder
     * @param reminderType the reminder type
     * @param patient      the patient. May be {@code null}
     * @param customer     the customer. May be {@code null}
     */
    public ReminderEvent(Action action, Act reminder, ReminderType reminderType, Party patient, Party customer) {
        this(action, reminder, reminderType, patient, customer, null, null);
    }

    /**
     * Constructs a new {@code ReminderEvent}.
     *
     * @param action           the reminder action
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param patient          the patient. May be {@code null}
     * @param customer         the customer. May be {@code null}
     * @param contact          the reminder contact. May be {@code null}
     * @param documentTemplate the document template. May be {@code null}
     */
    public ReminderEvent(Action action, Act reminder, ReminderType reminderType, Party patient, Party customer,
                         Contact contact, Entity documentTemplate) {
        this.action = action;
        this.reminder = reminder;
        this.reminderType = reminderType;
        this.patient = patient;
        this.customer = customer;
        this.contact = contact;
        this.documentTemplate = documentTemplate;
    }

    /**
     * Returns the reminder action.
     *
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the reminder.
     *
     * @return the reminder
     */
    public Act getReminder() {
        return reminder;
    }

    /**
     * Returns the reminder type.
     *
     * @return the reminder type
     */
    public ReminderType getReminderType() {
        return reminderType;
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Returns the customer.
     *
     * @return the customer. May be {@code null}
     */
    public Party getCustomer() {
        return customer;
    }

    /**
     * Returns the contact.
     *
     * @return the contact. May be {@code null}
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Returns the document template.
     *
     * @return the document template. May be {@code null}
     */
    public Entity getDocumentTemplate() {
        return documentTemplate;
    }

}
