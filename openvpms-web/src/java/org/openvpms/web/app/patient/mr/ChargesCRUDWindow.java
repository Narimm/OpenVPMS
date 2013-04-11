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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.resource.util.Messages;


/**
 * @author tony
 */
public class ChargesCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * Reminder and alert shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES =
            {"act.customerAccountInvoiceItem",
                    "act.customerAccountCreditItem"};

    /**
     * Create a new <tt>ChargesCRUDWindow</tt>.
     *
     * @param help the help context
     */
    public ChargesCRUDWindow(HelpContext help) {
        super(Archetypes.create(SHORT_NAMES, Act.class, Messages.get("patient.charges.createtype")),
              DefaultActActions.getInstance(), help);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
    }

}
