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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.scheduler;

import org.openvpms.component.model.entity.Entity;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;

/**
 * Test job.
 *
 * @author Tim Anderson
 */
abstract class TestJob implements Job {

    /**
     * The job state.
     */
    private final JobState state;

    /**
     * Constructs a {@link TestJob}.
     *
     * @param state         the job state
     * @param configuration the job configuration
     */
    TestJob(JobState state, Entity configuration) {
        this.state = state;
        state.add(configuration);
    }

    /**
     * Called by the {@link Scheduler} when a {@link Trigger} fires that is associated with the {@code Job}.
     *
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        state.running();
        try {
            Thread.sleep(1000);
        } catch (Throwable exception) {
            throw new JobExecutionException(exception);
        } finally {
            state.completed();
        }
    }
}
