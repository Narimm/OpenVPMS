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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;


/**
 * Reminder event.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see AbstractReminderProcessorListener
 */
public class ReminderEvent {

    public enum Action {
        SKIP, CANCEL, EMAIL, PHONE, PRINT
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
    private final Entity reminderType;

    /**
     * The contact. May be <tt>null</tt>.
     */
    private final Contact contact;

    /**
     * The document template. May be <tt>null</tt>.
     */
    private final Entity documentTemplate;


    /**
     * Constructs a new <tt>ReminderEvent</tt>.
     *
     * @param action       the reminder action
     * @param reminder     the reminder
     * @param reminderType the reminder type
     */
    public ReminderEvent(Action action, Act reminder, Entity reminderType) {
        this(action, reminder, reminderType, null, null);
    }

    /**
     * Constructs a new <tt>ReminderEvent</tt>.
     *
     * @param action           the reminder action
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact. May be <tt>null</tt>
     * @param documentTemplate the document template. May be <tt>null</tt>
     */
    public ReminderEvent(Action action, Act reminder, Entity reminderType,
                         Contact contact, Entity documentTemplate) {
        this.action = action;
        this.reminder = reminder;
        this.reminderType = reminderType;
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
    public Entity getReminderType() {
        return reminderType;
    }

    /**
     * Returns the contact.
     *
     * @return the contact. May be <tt>null</tt>
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Returns the document template.
     *
     * @return the document template. May be <tt>null</tt>
     */
    public Entity getDocumentTemplate() {
        return documentTemplate;
    }

}
