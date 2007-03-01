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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderEvent {

    public enum Action {
        SKIP, CANCEL, EMAIL, PHONE, PRINT
    }

    private final Action action;

    private final Act reminder;

    private final Entity reminderType;

    private final Contact contact;

    private final Entity documentTemplate;

    public ReminderEvent(Action action, Act reminder, Entity reminderType) {
        this(action, reminder, reminderType, null, null);
    }

    public ReminderEvent(Action action, Act reminder, Entity reminderType,
                         Contact contact, Entity documentTemplate) {
        this.action = action;
        this.reminder = reminder;
        this.reminderType = reminderType;
        this.contact = contact;
        this.documentTemplate = documentTemplate;
    }

    public Action getAction() {
        return action;
    }

    public Act getReminder() {
        return reminder;
    }

    public Entity getReminderType() {
        return reminderType;
    }

    public Contact getContact() {
        return contact;
    }

    public Entity getDocumentTemplate() {
        return documentTemplate;
    }

}
