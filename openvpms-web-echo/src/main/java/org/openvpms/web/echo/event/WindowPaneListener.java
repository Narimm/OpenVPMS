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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.echo.event;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.web.echo.error.ErrorHandler;


/**
 * An <em>nextapp.echo2.app.event.WindowPaneListener</em> that catches any unhandled exceptions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class WindowPaneListener implements nextapp.echo2.app.event.WindowPaneListener {

    /**
     * Invoked when a user attempts to close a <tt>WindowPane</tt>.
     *
     * @param event the <tt>WindowPaneEvent</tt> describing the change
     */
    public final void windowPaneClosing(WindowPaneEvent event) {
        try {
            onClose(event);
        } catch (Throwable exception) {
            ErrorHandler.getInstance().error(exception);
        }
    }

    /**
     * Invoked when a user attempts to close a <tt>WindowPane</tt>.
     *
     * @param event the <tt>WindowPaneEvent</tt> describing the change
     */
    public abstract void onClose(WindowPaneEvent event);

}
