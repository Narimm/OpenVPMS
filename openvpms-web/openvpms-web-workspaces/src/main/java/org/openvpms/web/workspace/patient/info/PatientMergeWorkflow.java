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

package org.openvpms.web.workspace.patient.info;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.merge.MergeWorkflow;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.openvpms.component.system.common.query.Constraints.not;


/**
 * Patient merge workflow.
 *
 * @author Tim Anderson
 */
class PatientMergeWorkflow extends MergeWorkflow<Party> {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * Constructs a {@code PatientMergeWorkflow}.
     *
     * @param patient  the patient to merge to
     * @param customer the customer
     * @param help     the help context
     */
    public PatientMergeWorkflow(Party patient, Party customer, HelpContext help) {
        super(patient, help);
        this.customer = customer;
        init();
    }

    /**
     * Creates the task context.
     *
     * @param help the help context
     * @return a new task context
     */
    @Override
    protected TaskContext createContext(HelpContext help) {
        TaskContext context = super.createContext(help);
        context.setCustomer(customer);
        return context;
    }

    /**
     * Creates a task to select the object to merge.
     *
     * @param context the context
     * @return a new select task
     */
    @Override
    protected SelectIMObjectTask<Party> createSelectTask(Context context) {
        Party patient = getObject();
        String[] shortNames = {patient.getArchetypeId().getShortName()};
        PatientQuery query = new PatientQuery(shortNames, context);
        query.setShowAllPatients(true);

        // exclude the patient being merged from the search
        query.setConstraints(not(new ObjectRefConstraint("patient", patient.getObjectReference())));
        return new SelectIMObjectTask<Party>(query, getHelpContext().topic("patient"));
    }

    /**
     * Creates the task to perform the merge.
     *
     * @return a new task
     */
    protected Task createMergeTask() {
        return new SynchronousTask() {
            public void execute(TaskContext context) {
                Party from = context.getPatient();
                merge(from);
            }
        };
    }

    /**
     * Merges from the specified patient.
     *
     * @param from the patient to merge from
     */
    private void merge(final Party from) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                PatientRules rules = new PatientRules(ServiceHelper.getArchetypeService(),
                                                      ServiceHelper.getLookupService());
                rules.mergePatients(from, getObject());
                return true;
            }
        });
    }

}
