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

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.FinancialAct;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.insurance.claim.ClaimEditDialog;

/**
 * A task that prompts the user to claim against a patient's insurance policy.
 *
 * @author Tim Anderson
 */
public class InsuranceClaimTask extends Tasks {

    /**
     * The policy.
     */
    private final Act policy;

    /**
     * Determines if gap claims can be submitted.
     */
    private final boolean gapClaim;

    /**
     * Constructs an {@link InsuranceClaimTask}.
     *
     * @param policy   the policy
     * @param gapClaim if {@code true}, gap claims can be submitted
     * @param help     the help context
     */
    public InsuranceClaimTask(Act policy, boolean gapClaim, HelpContext help) {
        super(help);
        this.policy = policy;
        this.gapClaim = gapClaim;
    }

    /**
     * Initialise any tasks.
     *
     * @param context the task context
     */
    @Override
    protected void initialise(TaskContext context) {
        InsuranceRules rules = ServiceHelper.getBean(InsuranceRules.class);
        Party insurer = rules.getInsurer(policy);
        if (insurer != null) {
            String title = Messages.get("patient.insurance.claim.title");
            String message = Messages.format("patient.insurance.claim.message", insurer.getName());
            ConfirmationTask confirm = new ConfirmationTask(title, message, ConfirmationTask.Type.YES_NO,
                                                            context.getHelpContext());
            addTask(new ConditionalTask(confirm, new EditClaimTask(rules)));
        }
    }

    private class EditClaimTask extends EditIMObjectTask {


        private final InsuranceRules rules;

        /**
         * Constructs a new {@code EditIMObjectTask} to edit an object
         * in the {@link TaskContext}.
         *
         * @param rules the insurance rules
         */
        public EditClaimTask(InsuranceRules rules) {
            super(InsuranceArchetypes.CLAIM);
            this.rules = rules;
        }

        /**
         * Starts the task.
         *
         * @param context the task context
         */
        @Override
        public void start(TaskContext context) {
            FinancialAct claim = rules.createClaim(policy);
            if (gapClaim) {
                IMObjectBean bean = ServiceHelper.getArchetypeService().getBean(claim);
                bean.setValue("gapClaim", true);
            }
            context.setObject(InsuranceArchetypes.CLAIM, (IMObject) claim);
            super.start(context);
        }

        /**
         * Invoked when the edit dialog closes to complete the task.
         *
         * @param action  the dialog action
         * @param editor  the editor
         * @param context the task context
         */
        @Override
        protected void onDialogClose(String action, IMObjectEditor editor, TaskContext context) {
            if (ClaimEditDialog.SUBMIT_ID.equals(action)) {
                super.onEditCompleted();
            } else {
                super.onDialogClose(action, editor, context);
            }
        }
    }
}
