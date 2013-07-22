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

package org.openvpms.web.workspace.workflow.merge;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Workflow for merging objects of the same type.
 *
 * @author Tim Anderson
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
     * Constructs a {@code MergeWorkflow.
     *
     * @param object the object to merge to
     * @param help   the help context
     */
    public MergeWorkflow(T object, HelpContext help) {
        super(help);
        this.object = object;
        initial = createContext(help);

        String displayName = DescriptorHelper.getDisplayName(object);
        String mergeTitle = Messages.format("workflow.merge.title", displayName);
        String mergeMsg = Messages.format("workflow.merge.message", displayName);

        addTask(new ConfirmationTask(mergeTitle, mergeMsg, false, help));
        SelectIMObjectTask select = createSelectTask(initial);
        select.setTitle(Messages.format("workflow.merge.select.title",
                                        displayName, object.getName()));
        select.setMessage(Messages.format("workflow.merge.select.message",
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
     * This implementation creates an {@link DefaultTaskContext} that doesn't inherit from the parent context
     *
     * @param help the help context
     * @return a new task context
     */
    protected TaskContext createContext(HelpContext help) {
        return new DefaultTaskContext(help);
    }

    /**
     * Creates a task to select the object to merge.
     *
     * @param context the context
     * @return a new select task
     */
    protected abstract SelectIMObjectTask<T> createSelectTask(Context context);

    /**
     * Creates the task to perform the merge.
     *
     * @return a new task
     */
    protected abstract Task createMergeTask();

}
