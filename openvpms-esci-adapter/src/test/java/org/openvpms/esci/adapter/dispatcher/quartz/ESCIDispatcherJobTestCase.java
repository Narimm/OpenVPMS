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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.esci.adapter.dispatcher.DocumentProcessor;
import org.openvpms.esci.adapter.dispatcher.ESCIDispatcher;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.security.User;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.SchedulerContext;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.JobListenerSupport;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import java.util.List;
import java.util.Properties;


/**
 * Tests Spring/Quartz integration of {@link ESCIDispatcherJob} and {@link ESCIDispatcher}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ESCIDispatcherJobTestCase extends AbstractESCITest {

    /**
     * Verifies that {@link ESCIDispatcher} can be triggered ad-hoc via the <tt>Scheduler.triggerJob</tt> method,
     * and doesn't execute concurrently.
     *
     * @throws SchedulerException if the scheduler can't be configured
     */
    @Test
    public void testTriggerJob() throws SchedulerException {
        TestESCIDispatcher dispatcher = new TestESCIDispatcher();
        GlobalListener listener = new GlobalListener();
        Scheduler scheduler = createScheduler(dispatcher, listener);

        // create a job
        JobDetail job = new JobDetail("esciDispatcherJob", ESCIDispatcherJob.class);
        job.setDurability(true);

        scheduler.addJob(job, false);
        int count = 10;
        for (int i = 0; i < count; ++i) {
            scheduler.triggerJob(job.getName(), null);
        }
        scheduler.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignore) {
        }

        scheduler.shutdown(true);

        // make sure it ran successfully, and wasn't invoked concurrently
        assertEquals(dispatcher.getRuns(), count);
        assertFalse(dispatcher.invokedConcurrently());
        assertEquals(dispatcher.getRuns(), listener.getRuns());
        assertEquals(0, listener.getExceptions());
    }

    /**
     * Verifies that {@link ESCIDispatcher} can be triggered repeatedly via a <tt>SimpleTrigger</tt>, and doesn't
     * execute concurrently.
     *
     * @throws SchedulerException if the scheduler can't be configured
     */
    @Test
    public void testTrigger() throws SchedulerException {
        TestESCIDispatcher dispatcher = new TestESCIDispatcher();
        GlobalListener listener = new GlobalListener();
        Scheduler scheduler = createScheduler(dispatcher, listener);

        // create a job detail
        JobDetail detail = new JobDetail("esciDispatcherJob", ESCIDispatcherJob.class);

        // create a trigger that repeats every 100ms
        SimpleTrigger trigger = new SimpleTrigger("mySimpleTrigger", Scheduler.DEFAULT_GROUP);
        trigger.setJobName(detail.getName());
        trigger.setJobGroup(detail.getGroup());
        trigger.setRepeatInterval(100);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);

        // register the detail and trigger
        scheduler.scheduleJob(detail, trigger);

        // start the scheduler, and wait, so the ESCIDispatcher is run several times
        scheduler.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignore) {
        }
        scheduler.shutdown(true);

        // make sure it ran successfully, and wasn't invoked concurrently
        assertTrue(dispatcher.getRuns() > 1);
        assertFalse(dispatcher.invokedConcurrently());
        assertEquals(dispatcher.getRuns(), listener.getRuns());
        assertEquals(0, listener.getExceptions());
    }

    /**
     * Creates a scheduler with a global listener and a thread pool with 10 threads.
     *
     * @param dispatcher the dispatcher
     * @param listener   the global listener
     * @return a new scheduler
     * @throws SchedulerException if the scheduler can't be created
     */
    private Scheduler createScheduler(ESCIDispatcher dispatcher, JobListener listener) throws SchedulerException {
        User user = TestHelper.createUser();
        StdSchedulerFactory factory = new StdSchedulerFactory();
        Properties properties = new Properties();
        properties.setProperty("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        factory.initialize(properties);
        Scheduler scheduler = factory.getScheduler();

        // register the dispatcher in the context. This will be used to populate ESCIDispatcherJob instances by
        // the jobFactory
        SchedulerContext context = scheduler.getContext();
        context.put("ESCIDispatcher", dispatcher);
        context.put("ArchetypeService", getArchetypeService());
        context.put("runAs", user.getUsername());

        // set up a job factory to populate the ESCIDispatcher on the ESCIDispatcherJob
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setSchedulerContext(scheduler.getContext());
        scheduler.setJobFactory(jobFactory);
        scheduler.addGlobalJobListener(listener);
        return scheduler;
    }

    private static class TestESCIDispatcher implements ESCIDispatcher {

        /**
         * Determines if dispatch is in progress.
         */
        private volatile boolean dispatching;

        /**
         * The no. of successful runs.
         */
        private volatile int runs;

        /**
         * Determines if dispatch was invoked concurrently.
         */
        private volatile boolean concurrentDispatch;


        public void setDocumentProcessors(List<DocumentProcessor> processors) {
        }

        /**
         * Dispatch documents.
         * <p/>
         * Dispatching may be stopped by invoking {@link #stop}.
         */
        public void dispatch() {
            if (dispatching) {
                concurrentDispatch = true;
            }
            dispatching = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
                // ignore
            }
            dispatching = false;
            ++runs;
        }

        /**
         * Flags the current dispatch to stop.
         */
        public void stop() {
        }

        /**
         * Determines if the dispatcher was invoked concurrently.
         *
         * @return <tt>true</tt> if the dispatcher was invoked concurrently
         */
        public boolean invokedConcurrently() {
            return concurrentDispatch;
        }

        /**
         * Returns the no. of times the dispatcher was run.
         *
         * @return the no. of times the {@link #dispatch} was invoked
         */
        public int getRuns() {
            return runs;
        }

    }

    private class GlobalListener extends JobListenerSupport {

        /**
         * The no. of successful runs.
         */
        private int runs;

        /**
         * The no. of exceptions
         */
        private int exceptions;

        /**
         * Returns the no. of times the jobs were successfully executed.
         *
         * @return the no. of times the jobs were successfully executed.
         */
        public int getRuns() {
            return runs;
        }

        /**
         * Returns the no. of exceptions.
         *
         * @return the no. of job exceptions
         */
        public int getExceptions() {
            return exceptions;
        }

        /**
         * Returns the listener name.
         *
         * @return the listener name
         */
        public String getName() {
            return "Listener";
        }

        /**
         * Invoked when a job completes.
         *
         * @param context      the job context
         * @param jobException an exception raised by the job. May be <tt>null</tt>
         */
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            if (jobException != null) {
                exceptions++;
                jobException.printStackTrace();
            } else {
                runs++;
            }
        }
    }
}
