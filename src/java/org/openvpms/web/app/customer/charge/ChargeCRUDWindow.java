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
import org.openvpms.web.app.workflow.payment.PaymentWorkflow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.ReloadTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.Tasks;


/**
 * CRUD window for customer charges.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ChargeCRUDWindow extends CustomerActCRUDWindow<FinancialAct> {

    /**
     * Creates a new <tt>ChargeCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public ChargeCRUDWindow(Archetypes<FinancialAct> archetypes) {
        super(archetypes);
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
     * <p/>
     * This prompts to pay the account, and pops up a dialog to print the act.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(FinancialAct act) {
        Tasks tasks = new Tasks();
        TaskContext context = new DefaultTaskContext();
        context.addObject(act);
        PaymentWorkflow payment = new PaymentWorkflow();
        payment.setRequired(false);
        tasks.addTask(payment);

        // need to reload the act as it may be changed via the payment workflow
        // as part of the CustomerAccountRules
        String shortName = act.getArchetypeId().getShortName();
        tasks.addTask(new ReloadTask(shortName));
        PrintActTask print = new PrintActTask(shortName);
        print.setRequired(false);
        print.setEnableSkip(false);
        tasks.addTask(print);
        tasks.start(context);
    }

    /**
     * Creates a new edit dialog with Apply button disabled for <em>POSTED</em>
     * acts, to workaround OVPMS-733.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new ActEditDialog(editor);
    }

}
