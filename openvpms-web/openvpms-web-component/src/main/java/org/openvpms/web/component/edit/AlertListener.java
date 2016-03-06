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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.edit;

/**
 * A listener to receive alerts during editing.
 *
 * @author Tim Anderson
 */
public interface AlertListener {

    /**
     * Invoked to notify of an alert.
     *
     * @param message the message
     * @return an identifier that may be used to subsequently cancel the alert
     */
    long onAlert(String message);

    /**
     * Cancels an alert.
     *
     * @param id the alert identifier
     */
    void cancel(long id);
}
