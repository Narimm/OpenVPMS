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

import nextapp.echo2.app.event.DocumentEvent;
import org.openvpms.web.echo.error.ErrorHandler;


/**
 * An <em>nextapp.echo2.app.event.DocumentListener</em> that catches any unhandled exceptions.
 *
 * @author Tim Anderson
 */
public abstract class DocumentListener implements nextapp.echo2.app.event.DocumentListener {

    /**
     * Invoked when an document update occurs.
     * <p/>
     * Delegates to {@link #onUpdate}, catching and reporting any unhandled exceptions.
     *
     * @param event an event describing the update
     */
    public void documentUpdate(DocumentEvent event) {
        try {
            onUpdate(event);
        } catch (Throwable exception) {
            ErrorHandler.getInstance().error(exception);
        }
    }

    /**
     * Invoked when an action occurs.
     *
     * @param event the fired <tt>ActionEvent</tt>
     */
    public abstract void onUpdate(DocumentEvent event);
}