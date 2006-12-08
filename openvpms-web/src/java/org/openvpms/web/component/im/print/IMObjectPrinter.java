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

package org.openvpms.web.component.im.print;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Prints an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IMObjectPrinter<T extends IMObject> {

    /**
     * Initiates printing of an object.
     */
    void print(T object);

    /**
     * Sets the listener for print events.
     *
     * @param listener the listener. May be <code>null</code>
     */
    void setListener(IMObjectPrinterListener<T> listener);

    /**
     * Determines if printing should occur interactively.
     *
     * @param interactive if <code>true</code>, prompt the user
     */
    void setInteractive(boolean interactive);
}
