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
 *
 *  $Id$
 */
package org.openvpms.web.echo.event;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * An <em>nextapp.echo2.app.event.ActionListener</em> that catches any unhandled exceptions.
 *
 * @author Tim Anderson
 */
public abstract class ActionListener implements nextapp.echo2.app.event.ActionListener {

    private final HelpContext help;

    public ActionListener() {
        this(null);
    }

    public ActionListener(HelpContext help) {
        this.help = help;
    }

    /**
     * Invoked when an action occurs.
     * <p/>
     * Delegates to {@link #onAction}, catching and reporting any unhandled exceptions.
     *
     * @param event the fired <tt>ActionEvent</tt>
     */
    public final void actionPerformed(ActionEvent event) {
        try {
            onAction(event);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when an action occurs.
     *
     * @param event the fired <tt>ActionEvent</tt>
     */
    public abstract void onAction(ActionEvent event);
}
