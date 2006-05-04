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

package org.openvpms.web.component.dialog;

import nextapp.echo2.app.Label;

import org.openvpms.web.component.util.LabelFactory;


/**
 * A modal dialog that prompts the user to select an OK or Cancel button.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ConfirmationDialog extends PopupDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "ConfirmationDialog";

    /**
     * Content label style.
     */
    private static final String LABEL_STYLE = "ConfirmationDialog.Label";


    /**
     * Construct a new <code>ConfirmationDialog</code>
     *
     * @param title the window title
     */
    public ConfirmationDialog(String title, String message) {
        super(title, STYLE, Buttons.OK_CANCEL);
        Label prompt = LabelFactory.create(null, LABEL_STYLE);
        prompt.setText(message);
        getLayout().add(prompt);
    }

}
