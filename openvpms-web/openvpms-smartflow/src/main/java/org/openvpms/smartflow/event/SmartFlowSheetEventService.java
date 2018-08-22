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

package org.openvpms.smartflow.event;

import org.openvpms.component.business.domain.im.party.Party;

/**
 * The Smart Flow Sheet event service is used to receive events from Smart Flow Sheet.
 *
 * @author Tim Anderson
 */
public interface SmartFlowSheetEventService {

    /**
     * Sets the interval between poll events.
     *
     * @param interval the interval, in seconds
     */
    void setPollInterval(int interval);

    /**
     * Triggers a poll for events.
     */
    void poll();

    /**
     * Restarts event processing.
     */
    void restart();

    /**
     * Returns the status of events at the specified location.
     *
     * @param location the location
     * @return the event status
     */
    EventStatus getStatus(Party location);
}
