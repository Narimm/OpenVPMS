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
 * A generic modal dialog that displays a message.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class MessageDialog extends PopupDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "MessageDialog";

    /**
     * Content label style.
     */
    private static final String LABEL_STYLE = "MessageDialog.Label";


    /**
     * Creates a new <code>MessageDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the buttons to display
     */
    public MessageDialog(String title, String message, Buttons buttons) {
        super(title, STYLE, buttons);
        setClosable(false);
        setModal(true);

        Label content = LabelFactory.create(null, LABEL_STYLE);
        content.setText(message);
        getLayout().add(content);
    }

}
