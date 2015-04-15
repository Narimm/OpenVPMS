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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.echo.dialog.PopupDialog;


/**
 * Manages edit dialogs displayed during charging.
 *
 * @author Tim Anderson
 */
public class ChargeEditorQueue extends DefaultEditorQueue {

    /**
     * The current dialog.
     */
    private PopupDialog current;

    /**
     * Constructs a {@code ChargeEditorQueue}.
     */
    public ChargeEditorQueue() {
        super(new LocalContext());
    }

    /**
     * Returns the current popup dialog.
     *
     * @return the current popup dialog. May be {@code null}
     */
    public PopupDialog getCurrent() {
        return current;
    }

    /**
     * Displays a dialog.
     *
     * @param dialog the dialog to display
     */
    @Override
    protected void show(PopupDialog dialog) {
        super.show(dialog);
        current = dialog;
    }

    /**
     * Invoked when the edit is completed.
     */
    @Override
    protected void completed() {
        super.completed();
        current = null;
    }
}
