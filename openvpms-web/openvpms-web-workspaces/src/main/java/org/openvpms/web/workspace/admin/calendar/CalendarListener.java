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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.calendar;

import org.openvpms.component.business.domain.im.act.Act;

import java.util.Date;

/**
 * Listener for calendar events.
 *
 * @author Tim Anderson
 */
public interface CalendarListener {

    /**
     * Create a new event starting at the specified date.
     *
     * @param date the date
     */
    void create(Date date);

    /**
     * Edits an event.
     *
     * @param event the event to edit
     */
    void edit(Act event);
}
