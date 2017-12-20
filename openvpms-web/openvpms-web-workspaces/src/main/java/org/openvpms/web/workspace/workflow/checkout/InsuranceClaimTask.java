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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.patient.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * A task that prompts the user to claim against a patient's current insurance policy.
 *
 * @author Tim Anderson
 */
public class InsuranceClaimTask extends Tasks {

    /**
     * Constructs an {@link InsuranceClaimTask}.
     *
     * @param help the help context
     */
    public InsuranceClaimTask(HelpContext help) {
        super(help);
    }

    /**
     * Initialise any tasks.
     *
     * @param context the task context
     */
    @Override
    protected void initialise(TaskContext context) {
        Party patient = context.getPatient();
        InsuranceRules rules = ServiceHelper.getBean(InsuranceRules.class);
        Act policy = rules.getCurrentPolicy(patient);
        if (policy != null) {
            Party insurer = rules.getInsurer(policy);
            if (insurer != null) {
                String title = Messages.get("patient.insurance.claim.title");
                String message = Messages.format("patient.insurance.claim.message", insurer.getName());
                ConfirmationTask confirm = new ConfirmationTask(title, message, ConfirmationTask.Type.YES_NO,
                                                                context.getHelpContext());
                addTask(new ConditionalTask(confirm, new EditClaimTask(policy, rules)));
            }
        }
    }

    private static class EditClaimTask extends EditIMObjectTask {

        private final Act policy;

        private final InsuranceRules rules;

        /**
         * Constructs a new {@code EditIMObjectTask} to edit an object
         * in the {@link TaskContext}.
         *
         * @param policy the policy to make the claim against
         * @param rules  the insurance rules
         */
        public EditClaimTask(Act policy, InsuranceRules rules) {
            super(InsuranceArchetypes.CLAIM);
            this.policy = policy;
            this.rules = rules;
        }

        /**
         * Starts the task.
         *
         * @param context the task context
         */
        @Override
        public void start(TaskContext context) {
            context.setObject(InsuranceArchetypes.CLAIM, rules.createClaim(policy));
            super.start(context);
        }
    }
}
