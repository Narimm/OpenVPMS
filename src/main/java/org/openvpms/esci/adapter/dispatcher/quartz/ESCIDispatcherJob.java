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

package org.openvpms.esci.adapter.dispatcher.quartz;

import org.openvpms.esci.adapter.dispatcher.ESCIDispatcher;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;


/**
 * Integrates {@link ESCIDispatcher} with Quartz so that it may be scheduled.
 * <p/>
 * This implements {@code StatefulJob} so that ESCIDispatcher won't be scheduled concurrently.
 *
 * @author Tim Anderson
 */
public class ESCIDispatcherJob implements InterruptableJob, StatefulJob {


    /**
     * The dispatcher.
     */
    private final ESCIDispatcher dispatcher;


    /**
     * Constructs an {@link ESCIDispatcherJob}.
     *
     * @param dispatcher the dispatcher
     */
    public ESCIDispatcherJob(ESCIDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }


    /**
     * Called by the {@code Scheduler} when a {@code Trigger} fires that is associated with the {@code Job}.
     *
     * @param context the execution context
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            dispatcher.dispatch();
        } catch (Throwable exception) {
            throw new JobExecutionException(exception);
        }
    }

    /**
     * Called by the {@code Scheduler} when a user interrupts the {@code Job}.
     */
    public void interrupt() {
        dispatcher.stop();
    }

}

