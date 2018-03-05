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

package org.openvpms.smartflow.event;

import org.openvpms.component.system.common.event.Listener;
import org.openvpms.smartflow.model.event.Event;

/**
 * Smart Flow Sheet event dispatcher.
 *
 * @author Tim Anderson
 */
public interface EventDispatcher {

    /**
     * Monitors for a single event with the specified id.
     * <p/>
     * The listener is automatically removed, once the event is handled.
     *
     * @param id       the event identifier
     * @param listener the listener
     */
    void addListener(String id, Listener<Event> listener);

    /**
     * Removes a listener for an event identifier.
     *
     * @param id the event identifier
     */
    void removeListener(String id);

    /**
     * Dispatches an event.
     *
     * @param event the event
     */
    void dispatch(Event event);
}
