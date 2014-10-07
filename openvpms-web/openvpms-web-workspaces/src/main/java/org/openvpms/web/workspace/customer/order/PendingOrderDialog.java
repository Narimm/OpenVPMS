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

package org.openvpms.web.workspace.customer.order;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Browser dialog for orders requiring charging.
 *
 * @author Tim Anderson
 */
public class PendingOrderDialog extends BrowserDialog<Act> {

    /**
     * Constructs an {@link PendingOrderDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param help    the help context
     */
    public PendingOrderDialog(String title, PendingOrderBrowser browser, HelpContext help) {
        super(title, OK_CANCEL, browser, help);
        setCloseOnSelection(false);
    }
}
