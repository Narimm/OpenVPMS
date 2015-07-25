/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.dialog;

import org.openvpms.web.echo.focus.FocusCommand;
import org.openvpms.web.echo.help.HelpContext;

/**
 * A modal dialog.
 * <p/>
 * This saves the focused component and restores it when the dialog is closed.
 *
 * @author Tim Anderson
 */
public abstract class ModalDialog extends PopupDialog {

    /**
     * The focus, prior to the dialog being shown.
     */
    private final FocusCommand focus;

    /**
     * Constructs a {@link ModalDialog}.
     *
     * @param title   the window title
     * @param buttons the buttons to display
     * @param help    the help context. May be {@code null}
     */
    public ModalDialog(String title, String[] buttons, HelpContext help) {
        this(title, null, buttons, help);
    }

    /**
     * Constructs a {@link ModalDialog}.
     *
     * @param title   the window title
     * @param style   the window style. May be {@code null}
     * @param buttons the buttons to display
     */
    public ModalDialog(String title, String style, String[] buttons) {
        this(title, style, buttons, null);
    }

    /**
     * Constructs a {@link ModalDialog}.
     *
     * @param title   the window title
     * @param style   the window style. May be {@code null}
     * @param buttons the buttons to display
     * @param help    the help context. May be {@code null}
     */
    public ModalDialog(String title, String style, String[] buttons, HelpContext help) {
        super(title, style, buttons, help);
        focus = new FocusCommand();
        setModal(true);
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

}
