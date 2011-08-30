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

package org.openvpms.web.app.workflow.otc;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.workflow.EditAccountActTask;
import org.openvpms.web.component.workflow.TaskContext;


/**
 * Task for editing OTC payments. This ensures that the payment amount equals
 * the charge amount.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class OTCPaymentTask extends EditAccountActTask {

    /**
     * Constructs a new <tt>OTCPaymentTask</tt>.
     */
    public OTCPaymentTask() {
        super(CustomerAccountArchetypes.PAYMENT, true);
    }

    /**
     * Creates a new editor for an object.
     *
     * @param object  the object to edit
     * @param context the task context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object,
                                          TaskContext context) {
        IMObjectEditor editor = super.createEditor(object, context);
        FinancialAct charge = (FinancialAct) context.getObject(
                CustomerAccountArchetypes.COUNTER);
        if (editor instanceof CustomerPaymentEditor && charge != null) {
            CustomerPaymentEditor payment = (CustomerPaymentEditor) editor;
            payment.setExpectedAmount(charge.getTotal());
        }
        return editor;
    }
}
