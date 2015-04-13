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

package org.openvpms.web.workspace.workflow.otc;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;


/**
 * A task for editing over-the-counter payments.
 * <p/>
 * This ensures that the payment amount equals the charge amount.
 *
 * @author Tim Anderson
 */
class OTCPaymentTask extends EditIMObjectTask {

    /**
     * Constructs a {@link OTCPaymentTask}.
     */
    public OTCPaymentTask() {
        super(CustomerAccountArchetypes.PAYMENT, true);
    }


    /**
     * Edits an object.
     *
     * @param object  the object to edit
     * @param context the task context
     */
    @Override
    protected void edit(IMObject object, TaskContext context) {
        Act payment = (Act) object;
        payment.setStatus(ActStatus.IN_PROGRESS); // enables the Apply button
        super.edit(object, context);
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
        FinancialAct charge = (FinancialAct) context.getObject(CustomerAccountArchetypes.COUNTER);
        return new OTCPaymentEditor((FinancialAct) object, null, layout, charge.getTotal());
    }
}
