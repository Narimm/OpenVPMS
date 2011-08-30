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

package org.openvpms.web.app.workflow.merge;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;


/**
 * Workflow for merging objects of the same type.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class MergeWorkflow<T extends IMObject> extends WorkflowImpl {

    /**
     * The object to merge to.
     */
    private final T object;

    /**
     * The initial context.
     */
    protected final TaskContext initial;


    /**
     * Constructs a new <tt>MergeWorkflow</tt>.
     *
     * @param object the object to merge to
     */
    public MergeWorkflow(T object) {
        this.object = object;
        initial = createContext();

        String displayName = DescriptorHelper.getDisplayName(object);
        String mergeTitle = Messages.get("workflow.merge.title", displayName);
        String mergeMsg = Messages.get("workflow.merge.message", displayName);

        addTask(new ConfirmationTask(mergeTitle, mergeMsg, false));
        SelectIMObjectTask select = createSelectTask(initial);
        select.setTitle(Messages.get("workflow.merge.select.title",
                                     displayName, object.getName()));
        select.setMessage(Messages.get("workflow.merge.select.message",
                                       displayName));
        addTask(select);
        addTask(createMergeTask());
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        start(initial);
    }

    /**
     * Returns the object to merge to.
     *
     * @return the object to merge to
     */
    protected T getObject() {
        return object;
    }

    /**
     * Creates the task context.
     * <p/>
     * This implementation creates the an {@link DefaultTaskContext} that
     * doesn't inherit from the global context
     *
     * @return a new task context
     */
    protected TaskContext createContext() {
        return new DefaultTaskContext(false);
    }

    /**
     * Creates a task to select the object to merge.
     *
     * @param context the context
     * @return a new select task
     */
    protected SelectIMObjectTask<T> createSelectTask(Context context) {
        String shortName = object.getArchetypeId().getShortName();
        return new SelectIMObjectTask<T>(shortName, context);
    }

    /**
     * Creates the task to perform the merge.
     *
     * @return a new task
     */
    protected abstract Task createMergeTask();

}
