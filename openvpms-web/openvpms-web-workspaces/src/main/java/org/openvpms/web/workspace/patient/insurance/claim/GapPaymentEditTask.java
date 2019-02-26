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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.credit.CreditActEditDialog;

import java.math.BigDecimal;

/**
 * Edits a gap payment.
 * <p>
 * This updates the claim benefit status when payment is made.
 * <p>
 * It makes no attempt to notify the insurer; that is left to the caller. This is because the status needs to be updated
 * irrespective of whether or not notification succeeds.
 *
 * @author Tim Anderson
 */
class GapPaymentEditTask extends EditIMObjectTask {

    /**
     * The claim.
     */
    private final GapClaimImpl claim;

    /**
     * The amount to pay.
     */
    private final BigDecimal amount;

    /**
     * The amount already paid.
     */
    private final BigDecimal paid;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(GapPaymentEditTask.class);

    /**
     * Constructs a {@link GapPaymentEditTask}.
     *
     * @param claim  the claim
     * @param amount the amount to pay
     * @param paid   the amount already paid
     */
    public GapPaymentEditTask(GapClaimImpl claim, BigDecimal amount, BigDecimal paid) {
        super(CustomerAccountArchetypes.PAYMENT, true);
        this.claim = claim;
        this.amount = amount;
        this.paid = paid;
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
        return new ReloadingCreditActEditDialog(editor, claim, context);
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
        return new GapPaymentEditor((Act) object, null, layout, amount);
    }

    /**
     * A {@link ReloadingCreditActEditDialog} that reloads the claim if the payment fails to save.
     * <p/>
     * This can occur if full payment is being made on the claim, at the same time as the benefit amount is received.
     */
    private class ReloadingCreditActEditDialog extends CreditActEditDialog {

        private GapClaimImpl claim;

        ReloadingCreditActEditDialog(IMObjectEditor editor, GapClaimImpl claim, TaskContext context) {
            super(editor, claim.getInvoices(), context);
            this.claim = claim;
        }

        /**
         * Saves the current object.
         *
         * @param editor the editor
         * @throws OpenVPMSException if the save fails
         */
        @Override
        protected void doSave(IMObjectEditor editor) {
            super.doSave(editor);
            CustomerPaymentEditor paymentEditor = (CustomerPaymentEditor) editor;
            paymentEditor.setStatus(ActStatus.POSTED);
            FinancialAct payment = (FinancialAct) paymentEditor.getObject();
            BigDecimal total = payment.getTotal().add(paid);
            if (total.compareTo(claim.getTotal()) >= 0) {
                claim.fullyPaid();
            } else if (total.compareTo(claim.getGapAmount()) == 0) {
                Context context = getContext();
                String notes = Messages.format("customer.credit.gap.benefitadjustment", claim.getInsurerId());
                claim.gapPaid(context.getPractice(), context.getLocation(), context.getUser(), notes);
            }
        }

        /**
         * Invoked to reload the object being edited when save fails.
         * <p/>
         * This implementation reloads the editor, but returns {@code false} if the act is saved and has been POSTED.
         *
         * @param editor the editor
         * @return {@code true} if the editor was reloaded and the act is not now POSTED.
         */
        @Override
        protected boolean reload(IMObjectEditor editor) {
            boolean result = false;
            if (super.reload(editor)) {
                // reload the claim, as that is probably what triggered the rollback in the first place
                try {
                    claim = claim.reload();
                    result = true;
                } catch (Throwable exception) {
                    log.warn(exception, exception);
                }
            }
            return result;
        }
    }
}
