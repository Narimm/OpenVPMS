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
import org.quartz.DisallowConcurrentExecution;

/**
 * A job that shouldn't execute concurrently when launched from any configuration.
 *
 * @author Tim Anderson
 */
@DisallowConcurrentExecution
class SingletonTestJob extends TestJob implements SingletonJob {

    /**
     * The job state.
     */
    public static final JobState state = new JobState();

    /**
     * Constructs a {@link SingletonTestJob}.
     *
     * @param configuration the job configuration
     */
    public SingletonTestJob(Entity configuration) {
        super(state, configuration);
    }
}
