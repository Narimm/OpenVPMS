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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.delivery;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.query.BrowserDialog;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderSelectionBrowserDialog extends BrowserDialog<FinancialAct> {

    /**
     * Construct a new <tt>BrowserDialog</tt>.
     *
     * @param title   the dialog title
     * @param message the dialog message. May be <tt>null</tt>
     * @param browser the browser
     */
    public OrderSelectionBrowserDialog(String title, String message,
                                       OrderTableBrowser browser) {
        super(title, message, OK_CANCEL, browser, false);
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        OrderTableBrowser browser = (OrderTableBrowser) getBrowser();
        if (browser.getSupplier() != null
                && browser.getStockLocation() != null) {
            super.onOK();
        }
    }
}
