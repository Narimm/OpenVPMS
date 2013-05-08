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
package org.openvpms.web.echo.event;

import nextapp.echo2.app.event.ChangeEvent;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * An {@code nextapp.echo2.app.event.ChangeListener} that catches any unhandled exceptions.
 *
 * @author Tim Anderson
 */
public abstract class ChangeListener implements nextapp.echo2.app.event.ChangeListener {

    /**
     * Invoked when a state change occurs.
     * <p/>
     * Delegates to {@link #onChange}, catching and reporting any unhandled exceptions.
     *
     * @param e the fired <tt>ActionEvent</tt>
     */
    public void stateChanged(ChangeEvent e) {
        try {
            onChange(e);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when a state change occurs.
     *
     * @param event an event describing the change
     */
    public abstract void onChange(ChangeEvent event);

}
