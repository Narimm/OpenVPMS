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

package org.openvpms.web.app.customer.payment;


import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * CRUD window for payments.
 *
 * @author Tim Anderson
 */
public class PaymentCRUDWindow extends CustomerActCRUDWindow<FinancialAct> {

    /**
     * Constructs a {@code PaymentCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param help       the help context
     */
    public PaymentCRUDWindow(Archetypes<FinancialAct> archetypes, HelpContext help) {
        super(archetypes, DefaultActActions.<FinancialAct>getInstance(), help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPostButton());
        buttons.add(createPreviewButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(POST_ID, enable);
        buttons.setEnabled(PREVIEW_ID, enable);
    }

    /**
     * Creates a new edit dialog with Apply button disabled for <em>POSTED</em>
     * acts, to workaround OVPMS-733.
     *
     * @param editor the editor
     * @param help
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor, HelpContext help) {
        return new ActEditDialog(editor, getHelpContext());
    }

}
