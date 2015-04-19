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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.delivery;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Order selection dialog.
 *
 * @author Tim Anderson
 */
public class OrderSelectionBrowserDialog extends BrowserDialog<FinancialAct> {

    /**
     * Constructs a {@link OrderSelectionBrowserDialog}.
     *
     * @param title   the dialog title
     * @param message the dialog message. May be {@code null}
     * @param browser the browser
     */
    public OrderSelectionBrowserDialog(String title, String message, OrderTableBrowser browser, HelpContext help) {
        super(title, message, OK_CANCEL, browser, false, help);
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes the window.
     */
    @Override
    protected void onOK() {
        OrderTableBrowser browser = (OrderTableBrowser) getBrowser();
        if (browser.getSupplier() == null) {
            ErrorDialog.show(Messages.get("supplier.delivery.supplier.required"));
        } else if (browser.getStockLocation() == null) {
            ErrorDialog.show(Messages.get("supplier.delivery.stockLocation.required"));
        } else {
            super.onOK();
        }
    }

    /**
     * Determines if an object has been selected.
     *
     * @return {@code true} if an object has been selected, otherwise {@code false}
     */
    @Override
    public boolean isSelected() {
        return true;
    }
}
