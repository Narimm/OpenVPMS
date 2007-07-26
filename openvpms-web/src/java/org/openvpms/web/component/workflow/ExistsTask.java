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

package org.openvpms.web.component.workflow;


/**
 * A {@link ConditionalTask} that executes conditions based on the existence
 * of a context object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ExistsTask extends ConditionalTask {

    /**
     * Constructs a new <tt>ExistsTask</tt>.
     *
     * @param key  the context key
     * @param task the task to execute if the condition evaluates <tt>true</tt>
     */
    public ExistsTask(String key, Task task) {
        this(key, true, task);
    }

    /**
     * Constructs a new <tt>ExistsTask</tt>.
     *
     * @param key    the context key
     * @param exists if <tt>true</tt> the object must exist for the task to be
     *               executed
     * @param task   the task to execute if the condition evaluates
     *               <tt>true</tt>
     */
    public ExistsTask(String key, boolean exists, Task task) {
        this(key, exists, task, null);
    }

    /**
     * Constructs a new <tt>ExistsTask</tt>.
     *
     * @param key      the context key
     * @param task     the task to execute if the condition evaluates true
     * @param elseTask the task to execute if the condition evalates false.
     *                 May be <tt>null</tt>
     */
    public ExistsTask(String key, Task task, Task elseTask) {
        this(key, true, task, elseTask);
    }

    /**
     * Constructs a new <tt>ExistsTask</tt>.
     *
     * @param key      the context key
     * @param exists   if <tt>true</tt> the object must exist for the task to be
     *                 executed
     * @param task     the task to execute if the condition evaluates true
     * @param elseTask the task to execute if the condition evalates false.
     *                 May be <tt>null</tt>
     */
    public ExistsTask(String key, boolean exists, Task task, Task elseTask) {
        super(new Exists(key, exists), task, elseTask);
    }

    private static class Exists extends EvalTask<Boolean> {

        /**
         * The context key.
         */
        private final String key;

        /**
         * Determines if the key must exist or not.
         */
        private final boolean exists;

        /**
         * Creates a new <tt>Exists</tt> condition.
         *
         * @param key    the context key
         * @param exists determines if the object must exist or not
         */
        public Exists(String key, boolean exists) {
            this.key = key;
            this.exists = exists;
        }

        /**
         * Starts the task.
         * <p/>
         * The registered {@link TaskListener} will be notified on completion or
         * failure.
         *
         * @param context the task context
         */
        public void start(TaskContext context) {
            boolean result;
            if (exists) {
                result = context.getObject(key) != null;
            } else {
                result = context.getObject(key) == null;
            }
            setValue(result);
        }
    }
}
