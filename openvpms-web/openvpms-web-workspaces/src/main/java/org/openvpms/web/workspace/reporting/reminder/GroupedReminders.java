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

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;

import java.util.List;

/**
 * Patient reminders prepared for processing by an {@link PatientReminderProcessor}.
 * <p>
 * If there are multiple reminders, these are for a single customer.
 *
 * @author Tim Anderson
 */
public class GroupedReminders extends PatientReminders {

    /**
     * The customer. May be {@code null}
     */
    private final Party customer;

    /**
     * The contact. May be {@code null}
     */
    private final Contact contact;

    /**
     * The practice location. May be {@code null}
     */
    private final Party location;

    /**
     * The document template. May be {@code null}
     */
    private final DocumentTemplate template;

    /**
     * Constructs a {@link GroupedReminders}.
     *
     * @param reminders the reminders to send
     * @param groupBy   the reminder grouping policy. This is used to determine which document template, if any, is
     *                  selected to process reminders.
     * @param cancelled reminders that have been cancelled
     * @param errors    reminders that are in error
     * @param updated   reminders/reminder items that have been updated
     * @param resend    determines if reminders are being resent
     * @param customer  the customer the reminders are for. May be {@code null} if there are no reminders to send
     * @param contact   the contact to use. May be {@code null} if there are no reminders to send
     * @param location  the practice location. May be {@code null} if there are no reminders to send
     * @param template  the document template to use. May be {@code null} if there are no reminders to send
     */
    public GroupedReminders(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy,
                            List<ReminderEvent> cancelled,
                            List<ReminderEvent> errors, List<Act> updated, boolean resend,
                            Party customer, Contact contact, Party location, DocumentTemplate template) {
        super(reminders, groupBy, cancelled, errors, updated, resend);
        this.customer = customer;
        this.contact = contact;
        this.location = location;
        this.template = template;
    }

    /**
     * Returns the customer.
     *
     * @return the customer, or {@code null} if there are no reminders to send
     */
    public Party getCustomer() {
        return customer;
    }

    /**
     * Returns the contact.
     *
     * @return the contact, or {@code null} if there are no reminders to send
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Returns the practice location.
     *
     * @return the practice location, or {@code null} if there are no reminders to send
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Returns the document template.
     *
     * @return the document template, or {@code null} if there are no reminders to send
     */
    public DocumentTemplate getTemplate() {
        return template;
    }

    /**
     * Creates a context for the reminders.
     *
     * @param practice the practice
     * @return a new context
     */
    @Override
    public Context createContext(Party practice) {
        Context context = super.createContext(practice);
        context.setCustomer(customer);
        context.setLocation(location);
        List<ReminderEvent> reminders = getReminders();
        if (reminders.size() == 1 || getGroupBy() == ReminderType.GroupBy.PATIENT) {
            ReminderEvent reminder = reminders.get(0);
            context.setPatient(reminder.getPatient());
        }
        return context;
    }

}
