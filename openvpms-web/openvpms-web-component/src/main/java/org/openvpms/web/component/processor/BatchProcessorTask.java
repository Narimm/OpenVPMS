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

package org.openvpms.web.component.processor;

import org.openvpms.archetype.component.processor.BatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;


/**
 * Adapts a {@link BatchProcessor} to the {@link Task} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BatchProcessorTask extends AbstractTask {

    /**
     * The processor.
     */
    private final BatchProcessor processor;

    /**
     * Determines if the task should terminate on error.
     */
    private boolean terminate = true;


    /**
     * Creates a new <tt>BatchProcessorTask</tt>.
     *
     * @param processor the processor
     */
    public BatchProcessorTask(BatchProcessor processor) {
        this.processor = processor;
    }

    /**
     * Returns the processor.
     *
     * @return the processor
     */
    public BatchProcessor getProcessor() {
        return processor;
    }

    /**
     * Determines if the task should terminate on error.
     * <p/>
     * Defaults to <tt>true</tt>.
     *
     * @param terminate if <tt>true</tt> terminates on error, otherwise ignores the error
     */
    public void setTerminateOnError(boolean terminate) {
        this.terminate = terminate;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    public void start(TaskContext context) {
        processor.setListener(new BatchProcessorListener() {
            public void completed() {
                notifyCompleted();
            }

            public void error(Throwable exception) {
                if (terminate) {
                    notifyCancelledOnError(exception);
                }
            }
        });
        processor.process();
    }
}
