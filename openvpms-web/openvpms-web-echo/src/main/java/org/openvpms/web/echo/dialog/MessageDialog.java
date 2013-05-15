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
 */

package org.openvpms.web.echo.dialog;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusCommand;
import org.openvpms.web.echo.help.HelpContext;


/**
 * A generic modal dialog that displays a message.
 *
 * @author Tim Anderson
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
     * The focus, prior to the dialog being shown.
     */
    private FocusCommand focus;


    /**
     * Constructs a {@code MessageDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the buttons to display
     */
    public MessageDialog(String title, String message, String[] buttons) {
        this(title, message, STYLE, buttons);
    }

    /**
     * Constructs a {@code MessageDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the buttons to display
     * @param help    the help context. May be {@code null}
     */
    public MessageDialog(String title, String message, String[] buttons, HelpContext help) {
        this(title, message, STYLE, buttons, help);
    }

    /**
     * Constructs a {@code MessageDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param style   the dialog style
     * @param buttons the buttons to display
     */
    public MessageDialog(String title, String message, String style, String[] buttons) {
        this(title, message, style, buttons, null);
    }

    /**
     * Constructs a {@code MessageDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param style   the dialog style
     * @param buttons the buttons to display
     * @param help    the help context. May be {@code null}
     */
    public MessageDialog(String title, String message, String style, String[] buttons, HelpContext help) {
        super(title, style, buttons, help);
        this.message = message;
        setModal(true);
        focus = new FocusCommand();
    }

    /**
     * Processes a user request to close the window (via the close button).
     * <p/>
     * This restores the previous focus
     */
    @Override
    public void userClose() {
        focus.restore();
        super.userClose();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label content = LabelFactory.create(true, true);
        content.setText(message);
        Row row = RowFactory.create("Inset.Large", content);
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
