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

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.component.app.GlobalContext;


/**
 * Transfers a task from one worklist to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TransferWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;


    /**
     * Constructs a new <tt>TransferWorkflow</tt>.
     *
     * @param task the task
     */
    public TransferWorkflow(Act task) {
        initial = new DefaultTaskContext(false);

        // make sure there is a user, to populate empty author nodes
        initial.setUser(GlobalContext.getInstance().getUser());

        addTask(new SelectIMObjectTask<Party>("party.organisationWorkList", initial));
        addTask(new UpdateWorkListTask(task));
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Sets the work list on an <em>act.customerTask</em>.
     */
    private static class UpdateWorkListTask extends EditIMObjectTask {

        /**
         * Creates a new <tt>UpdateWorkListTask</tt>.
         * The object is saved on update.
         *
         * @param act the task to update
         */
        public UpdateWorkListTask(Act act) {
            super(act, false);
        }

        /**
         * Edits an object in the background.
         *
         * @param editor  the editor
         * @param context the task context
         */
        @Override
        protected void edit(IMObjectEditor editor, TaskContext context) {
            super.edit(editor, context);
            if (editor instanceof TaskActEditor) {
                ((TaskActEditor) editor).setWorkList(context.getWorkList());
            }
        }
    }

}
