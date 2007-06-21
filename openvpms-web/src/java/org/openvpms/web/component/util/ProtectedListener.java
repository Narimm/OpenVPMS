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
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;


/**
 * An <tt>ActionListener</tt> that catches any exceptions and displays them
 * in a dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ProtectedListener implements ActionListener {

    /**
     * The dialog title. May be <tt>null</tt>.
     */
    private final String title;


    /**
     * Constructs a new <tt>ProtectedListener</tt>.
     */
    public ProtectedListener() {
        this(null);
    }

    /**
     * Constructs a new <tt>ProtectedListener</tt>.
     *
     * @param title the dialog title. May be <tt>null<tt>.
     */
    public ProtectedListener(String title) {
        this.title = title;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param event the fired <tt>ActionEvent</tt>
     */
    public void actionPerformed(ActionEvent event) {
        try {
            onAction(event);
        } catch (Throwable exception) {
            if (title != null) {
                ErrorHelper.show(title, exception);
            } else {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Invoked when an action occurs. Catches and displays any exception.
     *
     * @param event the fired <tt>ActionEvent</tt>
     * @throws Throwable for any error
     */
    protected abstract void onAction(ActionEvent event) throws Throwable;
}
