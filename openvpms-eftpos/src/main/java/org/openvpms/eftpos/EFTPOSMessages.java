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

package org.openvpms.eftpos;

import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.i18n.Messages;

/**
 * Messages reported by the POS API.
 *
 * @author Tim Anderson
 */
public class EFTPOSMessages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("EFTPOS", EFTPOSMessages.class.getName());

    /**
     * Creates a message indicating that a terminal was not found.
     *
     * @param id   the terminal identifier
     * @param name the terminal name
     * @return the message
     */
    public static Message terminalNotFound(long id, String name) {
        return messages.getMessage(100, id, name);
    }

    /**
     * Creates a message indicating that a terminal is not available for use.
     *
     * @param id   the terminal identifier
     * @param name the terminal name
     * @return the message
     */
    public static Message terminalNotAvailable(long id, String name) {
        return messages.getMessage(101, id, name);
    }
}
