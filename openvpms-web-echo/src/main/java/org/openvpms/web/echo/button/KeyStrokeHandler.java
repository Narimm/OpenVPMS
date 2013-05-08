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

package org.openvpms.web.echo.button;


/**
 * This interface is to be implemented by <code>Component<code>s that listen
 * for keystroke events  using the EchoPointNG <code>KeyStrokeListener</code>
 * class.
 * This interface exists solely as a workaround for Firefox which appears to
 * deregister listeners on the parent component when a child contains listeners.
 * PThe problem is known to exist in Firefix 1.5.x.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface KeyStrokeHandler {

    /**
     * Re-registers keystroke listeners.
     */
    void reregisterKeyStrokeListeners();
}
