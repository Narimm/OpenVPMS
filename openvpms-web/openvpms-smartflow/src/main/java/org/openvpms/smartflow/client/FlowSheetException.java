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

package org.openvpms.smartflow.client;

import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.i18n.Message;

/**
 * Smart Flow Sheet interface exception.
 *
 * @author Tim Anderson
 */
public class FlowSheetException extends OpenVPMSException {

    /**
     * Constructs a {@link FlowSheetException}.
     *
     * @param message the message
     */
    public FlowSheetException(Message message) {
        super(message);
    }

    /**
     * Constructs a {@link FlowSheetException}.
     *
     * @param message the message
     * @param cause   the root cause
     */
    public FlowSheetException(Message message, Throwable cause) {
        super(message, cause);
    }
}
