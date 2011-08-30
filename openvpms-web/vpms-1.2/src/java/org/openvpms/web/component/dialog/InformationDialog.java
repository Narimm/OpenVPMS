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

package org.openvpms.web.component.dialog;

import org.openvpms.web.resource.util.Messages;


/**
 * Modal information dialog box.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-29 04:00:51Z $
 */
public class InformationDialog extends MessageDialog {

    /**
     * Construct a new <tt>InformationDialog</tt>.
     *
     * @param message the message to display
     */
    public InformationDialog(String message) {
        this(Messages.get("informationdialog.title"), message);
    }

    /**
     * Construct a new <tt>InformationDialog</tt>.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    public InformationDialog(String title, String message) {
        super(title, message, "InformationDialog", OK);
        setDefaultButton(OK_ID);
    }

    /**
     * Helper to show a new information dialog.
     *
     * @param message dialog message
     */
    public static void show(String message) {
        InformationDialog dialog = new InformationDialog(message);
        dialog.show();
    }

    /**
     * Helper to show a new information dialog.
     *
     * @param title   the dialog title
     * @param message dialog message
     */
    public static void show(String title, String message) {
        InformationDialog dialog = new InformationDialog(title, message);
        dialog.show();
    }

}
