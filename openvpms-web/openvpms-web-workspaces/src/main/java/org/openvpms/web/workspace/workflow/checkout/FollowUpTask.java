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

import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.workflow.worklist.FollowUpTaskEditor;

import java.util.List;

/**
 * Displays a dialog to create a follow-up task, if follow-up work lists are configured.
 *
 * @author Tim Anderson
 */
public class FollowUpTask extends Tasks {

    /**
     * The work lists.
     */
    private List<Entity> workLists;

    /**
     * Constructs a {@link FollowUpTask}.
     *
     * @param help the help context
     */
    public FollowUpTask(HelpContext help) {
        super(help);
    }

    /**
     * Initialise any tasks.
     *
     * @param context the task context
     */
    @Override
    protected void initialise(TaskContext context) {
        workLists = FollowUpTaskEditor.getWorkLists(context);
        if (!workLists.isEmpty()) {
            addTask(new AddFollowUpTask());
        }
    }

    private class AddFollowUpTask extends EditIMObjectTask {

        public AddFollowUpTask() {
            super(ScheduleArchetypes.TASK, true);
            setRequired(false);
            setSkip(true);
        }

        /**
         * Starts the task.
         *
         * @param context the task context
         */
        @Override
        public void start(TaskContext context) {
            // copy the context so that the follow-up act isn't added to the global task context, interfering with
            // the check-out task act
            TaskContext copy = new DefaultTaskContext(new LocalContext(context), context.getHelpContext());
            super.start(copy);
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
            return new FollowUpTaskEditor((Act) object, workLists, layout);
        }
    }
}
