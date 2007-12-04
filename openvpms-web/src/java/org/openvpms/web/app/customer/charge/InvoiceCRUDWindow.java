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
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.charge;


import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.app.workflow.payment.PaymentWorkflow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.Tasks;


/**
 * CRUD window for invoices.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InvoiceCRUDWindow extends CustomerActCRUDWindow<FinancialAct> {

    /**
     * Creates a new <tt>InvoiceCRUDWindow</tt>.
     *
     * @param type      display name for the types of objects that this may
     *                  create
     * @param shortName the archetype short name
     */
    public InvoiceCRUDWindow(String type, String shortName) {
        super(type, new ShortNameList(shortName));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        enableButtons(buttons, true);
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
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPostButton());
            buttons.add(getPreviewButton());
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when posting of an act is complete.
     * Pops up a dialog to print the act, if one hasn't already been displayed,
     * and prompts to pay the account.
     *
     * @param act           the act
     * @param printPrompted determines if a print dialog has been displayed to
     *                      print the act
     */
    @Override
    protected void onPosted(FinancialAct act, boolean printPrompted) {
        Tasks tasks = new Tasks();
        if (!printPrompted) {
            PrintActTask print = new PrintActTask(act);
            print.setRequired(false);
            tasks.addTask(print);
        }
        tasks.addTask(new PaymentWorkflow());
        tasks.start();
    }

}
