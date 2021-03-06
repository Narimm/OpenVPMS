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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.patient.history.PatientHistoryCRUDWindow;


/**
 * The CRUD window for editing events and their items.
 *
 * @author Tim Anderson
 */
public class VisitCRUDWindow extends PatientHistoryCRUDWindow {

    /**
     * Constructs a {@code VisitCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public VisitCRUDWindow(Context context, HelpContext help) {
        super(context, help);
    }

    /**
     * Lays out the component.
     * <p/>
     * This implementation just returns a dummy component, as the display of the component is managed by the parent
     * dialog.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        return new Row();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createNewButton());
        buttons.add(createEditButton());
        buttons.add(createDeleteButton());
        buttons.add(createPrintButton());
        buttons.add(createMailButton());
        buttons.add(createExternalEditButton());
    }
}
