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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.util;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.esci.adapter.i18n.Message;


/**
 * e-Supply Chain Interface Adapter exception.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ESCIAdapterException extends OpenVPMSException {

    /**
     * Constructs an <tt>ESCIAdapterException</tt>.
     *
     * @param message the message
     */
    public ESCIAdapterException(Message message) {
        super(message.toString());
    }

    /**
     * Constructs an <tt>ESCIAdapterException</tt>.
     *
     * @param message the message
     * @param cause   the root cause
     */
    public ESCIAdapterException(Message message, Throwable cause) {
        super(message.toString(), cause);
    }

}
