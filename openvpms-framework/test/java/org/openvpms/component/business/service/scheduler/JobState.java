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

import java.util.HashSet;
import java.util.Set;

/**
 * {@link TestJob} state.
 *
 * @author Tim Anderson
 */
class JobState {

    /**
     * Determines if the job is running.
     */
    private boolean running;

    /**
     * Determines if multiple instances of the job were run concurrently.
     */
    private boolean concurrent;

    /**
     * The no. of times the job was run.
     */
    private int runs;

    /**
     * The configurations used for the runs.
     */
    private Set<Entity> config = new HashSet<>();


    /**
     * Adds a configuration.
     *
     * @param config the job configuration
     */
    public synchronized void add(Entity config) {
        this.config.add(config);
    }

    /**
     * Indicates that the job is running.
     */
    public synchronized void running() {
        if (running) {
            concurrent = true;
        }
        running = true;
        runs++;
    }

    /**
     * Indicates that the job has completed.
     */
    public synchronized void completed() {
        running = false;
    }


    /**
     * Determines if multiple instances of the job were run concurrently.
     *
     * @return {@code true} if they were run concurrently, {@code false} if they ran sequentially
     */
    public synchronized boolean ranConcurrently() {
        return concurrent;
    }

    /**
     * Returns the no. of times the job ran.
     *
     * @return the count of runs
     */
    public synchronized int getRuns() {
        return runs;
    }

    /**
     * Resets the state.
     */
    public synchronized void reset() {
        running = false;
        concurrent = false;
        config.clear();
        runs = 0;
    }

    /**
     * Returns the configurations used in each run.
     *
     * @return the configurations
     */
    public synchronized Set<Entity> getConfig() {
        return config;
    }
}
