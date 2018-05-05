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

import org.openvpms.component.i18n.Message;

/**
 * Smart Flow Sheet exception raised when access to a document has been denied.
 *
 * @author Tim Anderson
 */
public class AccessToDocumentDeniedException extends FlowSheetException {

    /**
     * Constructs a {@link AccessToDocumentDeniedException}.
     *
     * @param message the message
     */
    public AccessToDocumentDeniedException(Message message) {
        super(message);
    }
}
