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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.plugin.docload;

import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class DocumentLoaderException extends OpenVPMSException {

    /**
     * Delegate to the super class
     *
     * @param msg       the message
     * @param exception the exception
     */
    public DocumentLoaderException(String msg, Throwable exception) {
        super(msg, exception);
    }
}
