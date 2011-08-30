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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.print;

import java.util.EventListener;


/**
 * Listener for {@link Printer} events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface PrinterListener extends EventListener {

    /**
     * Notifies of a successful print.
     *
     * @param printer the printer that was used. May be <tt>null</tt>
     */
    void printed(String printer);

    /**
     * Notifies that the print was cancelled.
     */
    void cancelled();

    /**
     * Notifies that the print was skipped.
     */
    void skipped();

    /**
     * Invoked when a print fails.
     *
     * @param cause the reason for the failure
     */
    void failed(Throwable cause);
}
