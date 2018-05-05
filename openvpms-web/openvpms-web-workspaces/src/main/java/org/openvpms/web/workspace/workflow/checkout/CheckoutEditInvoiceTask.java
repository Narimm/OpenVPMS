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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;

/**
 * Charges all boarding appointments associated with the current patient.
 *
 * @author Tim Anderson
 */
public class CheckoutEditInvoiceTask extends EditIMObjectTask {

    /**
     * The boarding events.
     */
    private final Visits visits;

    /**
     * Constructs a {@link CheckoutEditInvoiceTask}.
     *
     * @param visits the visits
     */
    public CheckoutEditInvoiceTask(Visits visits) {
        super(CustomerAccountArchetypes.INVOICE);
        this.visits = visits;
    }

    /**
     * Creates a new editor for an object.
     *
     * @param object  the object to edit
     * @param context the task context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object, TaskContext context) {
        LayoutContext layout = new DefaultLayoutContext(true, context, context.getHelpContext());
        return new CheckoutChargeEditor((FinancialAct) object, visits, layout);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor  the editor
     * @param skip    if {@code true}, editing may be skipped
     * @param context the help context
     * @return a new edit dialog
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor, boolean skip, TaskContext context) {
        return new CheckoutEditDialog((CheckoutChargeEditor) editor, context);
    }
}
