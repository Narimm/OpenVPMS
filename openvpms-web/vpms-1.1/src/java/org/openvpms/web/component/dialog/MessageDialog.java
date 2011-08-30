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
import nextapp.echo2.app.Row;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * A generic modal dialog that displays a message.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class MessageDialog extends PopupDialog {

    /**
     * The message.
     */
    private final String message;

    /**
     * Dialog style name.
     */
    private static final String STYLE = "MessageDialog";


    /**
     * Creates a new <code>MessageDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the buttons to display
     */
    public MessageDialog(String title, String message, String[] buttons) {
        this(title, message, STYLE, buttons);
    }

    /**
     * Creates a new <code>MessageDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param style   the dialog style
     * @param buttons the buttons to display
     */
    public MessageDialog(String title, String message, String style,
                         String[] buttons) {
        super(title, style, buttons);
        this.message = message;
        setModal(true);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label content = LabelFactory.create();
        content.setText(message);
        Row row = RowFactory.create("Inset", content);
        getLayout().add(row);
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    protected String getMessage() {
        return message;
    }

}
