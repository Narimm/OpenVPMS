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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.event;


/**
 * <tt>Vetoable</tt> enables {@link VetoListener}s to veto actions asynchronously.
 *
 * @author Tim Anderson
 * @see VetoListener
 */
public interface Vetoable {

    /**
     * Indicates wether the action should be vetoed or not.
     *
     * @param veto if <tt>true</tt>, veto the action, otherwise allow it
     */
    void veto(boolean veto);
}
