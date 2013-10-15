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

package org.openvpms.web.workspace.reporting.till;

import nextapp.echo2.app.Grid;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Start Clear Till dialog.
 *
 * @author Tim Anderson
 */
public class StartClearTillDialog extends AbstractClearTillDialog {

    /**
     * Constructs a {@link StartClearTillDialog}.
     *
     * @param help the help context
     */
    public StartClearTillDialog(HelpContext help) {
        super(Messages.get("till.startClear.title"), help);
        Grid grid = GridFactory.create(2);
        addAmount(grid);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, grid));
    }

    /**
     * Invoked when the OK button is pressed. Closes the window if the amount is valid
     */
    protected void onOK() {
        if (getCashFloat() != null) {
            super.onOK();
        }
    }

}
