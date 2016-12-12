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

package org.openvpms.web.component.workflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper for creating common {@link Task} instances.
 *
 * @author Tim Anderson
 */
public class TaskFactory {

    /**
     * Creates a task that determines if a node is a particular value.
     *
     * @param shortName the short name of the object to evaluate the node of
     * @param node      the node name
     * @param value     the expected value
     * @return a new task
     */
    public static <T> EvalTask<Boolean> eq(String shortName, String node, T value) {
        return new NodeConditionTask<>(shortName, node, value);
    }

    /**
     * Creates a task that determines if a node is not a particular value.
     *
     * @param shortName the short name of the object to evaluate the node of
     * @param node      the node name
     * @param value     the expected value
     * @return a new task
     */
    public static <T> EvalTask<Boolean> ne(String shortName, String node, T value) {
        return new NodeConditionTask<>(shortName, node, false, value);
    }

    /**
     * Creates a task that evaluates {@code true} if the tasks it executes all evaluate {@code true}.
     *
     * @param task1 the first task to execute
     * @param task2 the second task to execute
     * @return a new task
     */
    public static EvalTask<Boolean> and(final EvalTask<Boolean> task1, final EvalTask<Boolean> task2) {
        return new AndTask(task1, task2);
    }

    /**
     * Creates a task that executes another, when a condition evaluates {@code true}.
     *
     * @param condition the condition
     * @param task      the task to execute if the condition evaluates true
     * @return a new task
     */
    public static Task when(EvalTask<Boolean> condition, Task task) {
        return new ConditionalTask(condition, task);
    }

    /**
     * Creates a task that executes another task based on a condition,
     *
     * @param condition the condition
     * @param task      the task to execute if the condition evaluates {@code true}
     * @param elseTask  the task to execute if the condition evaluates {@code false}
     * @return a new task
     */
    public static Task when(EvalTask<Boolean> condition, Task task, Task elseTask) {
        return new ConditionalTask(condition, task, elseTask);
    }

    private static class AndTask extends EvalTask<Boolean> {

        List<EvalTask<Boolean>> tasks = new ArrayList<>();
        private EvalTask<Boolean> current;
        private Iterator<EvalTask<Boolean>> iterator;

        public AndTask(EvalTask<Boolean> task1, EvalTask<Boolean> task2) {
            tasks.add(task1);
            tasks.add(task2);
        }

        /**
         * Starts the task.
         * <p/>
         * The registered {@link TaskListener} will be notified on completion or failure.
         *
         * @param context the task context
         */
        @Override
        public void start(TaskContext context) {
            iterator = tasks.iterator();
            next(context);
        }

        /**
         * Runs the next task.
         *
         * @param context the task context
         */
        private void next(final TaskContext context) {
            TaskListener listener = new DefaultTaskListener() {
                @Override
                public void taskEvent(TaskEvent event) {
                    switch (event.getType()) {
                        case SKIPPED:
                            notifySkipped();
                            break;
                        case COMPLETED:
                            boolean value = current.getValue() != null && current.getValue();
                            if (!value) {
                                setValue(false);
                            } else if (iterator.hasNext()) {
                                next(context);
                            } else {
                                setValue(true);
                            }
                            break;
                        default:
                            notifyCancelled();
                            break;
                    }
                }
            };
            current = iterator.next();
            current.addTaskListener(listener);
            current.start(context);
        }
    }
}
