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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.dispatcher.quartz;

import org.openvpms.esci.adapter.dispatcher.ESCIDispatcher;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import javax.annotation.Resource;


/**
 * Integrates {@link ESCIDispatcher} with Quartz so that it may be scheduled.
 * <p/>
 * This implemements <tt>StatefulJob</tt> so that ESCIDispatcher won't be scheduled concurrently.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ESCIDispatcherJob implements InterruptableJob, StatefulJob {

    /**
     * The dispatcher.
     */
    private ESCIDispatcher dispatcher;


    /**
     * Registers the dispatcher.
     *
     * @param dispatcher the dispatcher
     */
    @Resource
    public void setESCIDispatcher(ESCIDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     *
     * @param context the execution context
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (dispatcher == null) {
            throw new JobExecutionException("ESCIDispatcher has not been registered");
        }
        try {
            dispatcher.dispatch();
        } catch (Throwable exception) {
            throw new JobExecutionException(exception);
        }
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a user interrupts the <code>Job</code>.
     */
    public void interrupt() {
        dispatcher.stop();
    }
}
