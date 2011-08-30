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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.info;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.workflow.merge.MergeWorkflow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Patient merge workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientMergeWorkflow extends MergeWorkflow<Party> {

    /**
     * Constructs a new <tt>MergeWorkflow</tt>.
     *
     * @param patient the patient to merge to
     */
    public PatientMergeWorkflow(Party patient) {
        super(patient);
    }

    /**
     * Creates the task context.
     * <p/>
     * This implementation propagates the current customer from the global
     * context.
     *
     * @return a new task context
     */
    @Override
    protected TaskContext createContext() {
        TaskContext context = super.createContext();
        context.setCustomer(GlobalContext.getInstance().getCustomer());
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
        String[] shortNames = {getObject().getArchetypeId().getShortName()};
        Query<Party> query = QueryFactory.create(shortNames, context,
                                                 Party.class);
        if (query instanceof PatientQuery) {
            ((PatientQuery) query).setShowAllPatients(true);
        }
        return new SelectIMObjectTask<Party>(query);
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
        TransactionTemplate template = new TransactionTemplate(
                ServiceHelper.getTransactionManager());
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                PatientRules rules = new PatientRules();
                rules.mergePatients(from, getObject());
                return true;
            }
        });
    }

}
