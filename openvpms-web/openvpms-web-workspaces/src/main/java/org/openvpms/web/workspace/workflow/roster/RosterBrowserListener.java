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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.web.component.im.query.BrowserListener;

/**
 * Roster browser listener.
 *
 * @author Tim Anderson
 */
interface RosterBrowserListener<T> extends BrowserListener<T> {

    /**
     * Invoked to create and edit a new event.
     */
    void create();

    /**
     * Invoked to edit an event.
     *
     * @param event the set representing the event
     */
    void edit(T event);
}
