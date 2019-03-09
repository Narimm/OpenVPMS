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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link JobScheduler}.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("jobscheduler-appcontext.xml")
public class JobSchedulerTestCase extends AbstractArchetypeServiceTest {

    /**
     * The job scheduler/
     */
    private JobScheduler scheduler;

    /**
     * The test user.
     */
    private User user;

    /**
     * Factory to configure Quartz.
     */
    private SchedulerFactoryBean factory;

    /**
     * Job archetype.
     */
    private static final String JOB_TEST = "entity.jobTest";

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception {
        // delete any existing jobs
        List<IMObject> jobs = get(new ArchetypeQuery(JobScheduler.JOB_ARCHETYPE));
        for (IMObject job : jobs) {
            if (job.isA(JOB_TEST)) {
                remove(job);
            }
        }

        // set up the scheduler
        factory = new SchedulerFactoryBean();
        factory.setJobFactory(new SpringBeanJobFactory());
        factory.setApplicationContext(applicationContext);
        factory.afterPropertiesSet();
        factory.start();

        Scheduler quartz = factory.getObject();
        scheduler = new JobScheduler(quartz, getArchetypeService());
        scheduler.afterPropertiesSet();

        // create a user to run jobs as
        user = (User) create("security.user");
        user.setUsername("u" + Math.abs(new Random().nextInt()));
        user.setName(user.getUsername());
        save(user);
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    @After
    public void tearDown() throws Exception {
        scheduler.destroy();
        factory.destroy();
    }

    /**
     * Verifies that a {@link SingletonJob} does not run concurrently if launched manually twice.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDisallowConcurrentExecutionForSingletonSingleConfig() throws Exception {
        Entity jobA = createJob("jobA", SingletonTestJob.class, user);
        List<Entity> jobs = scheduler.getJobs(JOB_TEST);
        assertEquals(1, jobs.size());

        JobState state = SingletonTestJob.state;
        state.reset();

        scheduler.run(jobA);
        scheduler.run(jobA);

        Thread.sleep(5000);
        checkState(state, 2, false, jobA);
    }

    /**
     * Verifies that if two jobs are configured for a {@link SingletonJob}
     * the last instance registered is the one that is scheduled.
     * <p/>
     * This is because the last to be saved unschedules the previous instance.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDisallowConcurrentExecutionForSingletonMultipleConfigs() throws Exception {
        Entity jobA = createJob("jobA", SingletonTestJob.class, user);
        Entity jobB = createJob("jobB", SingletonTestJob.class, user);
        List<Entity> jobs = scheduler.getJobs(JOB_TEST);
        assertEquals(2, jobs.size());

        JobState state = SingletonTestJob.state;
        state.reset();

        scheduleIn2Seconds(jobA);
        scheduleIn2Seconds(jobB);

        Thread.sleep(5000);
        checkState(state, 1, false, jobB); // last one scheduled ran
    }

    /**
     * Verifies that if the same job with  {@link DisallowConcurrentExecution} is run manually twice,
     * the executions happen sequentially, rather than concurrently.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMultipleRunForDisallowConcurrentExecutionWithSameConfig() throws Exception {
        // same job, configured via different entities
        Entity jobA = createJob("jobA", NonConcurrentTestJob.class, user);
        List<Entity> jobs = scheduler.getJobs(JOB_TEST);
        assertEquals(1, jobs.size());

        JobState state = NonConcurrentTestJob.state;
        state.reset();
        scheduler.run(jobA);
        scheduler.run(jobA);
        Thread.sleep(3000);
        checkState(state, 2, false, jobA);
    }

    /**
     * Verifies that if the same job with  {@link DisallowConcurrentExecution} is run manually twice,
     * via a different configuration, the executions happen concurrently.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMultipleRunForDisallowConcurrentExecutionWithDifferentConfig() throws Exception {
        // same job, configured via different entities
        Entity jobA = createJob("jobA", NonConcurrentTestJob.class, user);
        Entity jobB = createJob("jobB", NonConcurrentTestJob.class, user);
        List<Entity> jobs = scheduler.getJobs(JOB_TEST);
        assertEquals(2, jobs.size());

        JobState state = NonConcurrentTestJob.state;
        state.reset();
        scheduler.run(jobA);
        scheduler.run(jobB);
        Thread.sleep(3000);
        checkState(state, 2, true, jobA, jobB);
    }

    /**
     * Verifies that if two job are configured for the same class and it isn't stateful and doesn't have
     * the {@link DisallowConcurrentExecution} annotation, it can run concurrently.
     *
     * @throws Exception for any error
     */
    @Test
    public void testConcurrencyForSameJobDifferentConfig() throws Exception {
        Entity jobA = createJob("jobA", ConcurrentTestJob.class, user);
        Entity jobB = createJob("jobB", ConcurrentTestJob.class, user);
        List<Entity> jobs = scheduler.getJobs(JOB_TEST);
        assertEquals(2, jobs.size());

        JobState state = ConcurrentTestJob.state;
        state.reset();
        scheduler.run(jobA);
        scheduler.run(jobB);
        Thread.sleep(3000);
        checkState(state, 2, true, jobA, jobB);
    }

    /**
     * Verifies that if two job are configured for the same class and it isn't stateful and doesn't have
     * the {@link DisallowConcurrentExecution} annotation, it can run concurrently.
     *
     * @throws Exception for any error
     */
    @Test
    public void testConcurrencyForSameJobSameConfig() throws Exception {
        Entity jobA = createJob("jobA", ConcurrentTestJob.class, user);
        List<Entity> jobs = scheduler.getJobs(JOB_TEST);
        assertEquals(1, jobs.size());

        JobState state = ConcurrentTestJob.state;
        state.reset();
        scheduler.run(jobA);
        scheduler.run(jobA);
        Thread.sleep(3000);
        checkState(state, 2, true, jobA);
    }

    /**
     * Verifies that if two jobs are configured for the same class and both have the same name,
     * and the job it isn't stateful and doesn't have the {@link DisallowConcurrentExecution} annotation, it can run
     * concurrently.
     *
     * @throws Exception for any error
     */
    @Test
    public void testConcurrencyForSameJobSameName() throws Exception {
        Entity jobA1 = createJob("jobA", ConcurrentTestJob.class, user);
        Entity jobA2 = createJob("jobA", ConcurrentTestJob.class, user);
        List<Entity> jobs = scheduler.getJobs(JOB_TEST);
        assertEquals(2, jobs.size());

        JobState state = ConcurrentTestJob.state;
        state.reset();
        scheduler.run(jobA1);
        scheduler.run(jobA2);
        Thread.sleep(3000);
        checkState(state, 2, true, jobA1, jobA2);
    }

    /**
     * Verifies a job state matches that expected.
     *
     * @param state      the job state
     * @param runs       the expected no. of runs
     * @param concurrent if {@code true}, the jobs should have run concurrently, {@code false}, sequentially
     * @param configs    the expected configs
     */
    private void checkState(JobState state, int runs, boolean concurrent, Entity... configs) {
        assertEquals(runs, state.getRuns());
        assertEquals(concurrent, state.ranConcurrently());
        assertEquals(configs.length, state.getConfig().size());
        for (Entity config : configs) {
            assertTrue(state.getConfig().contains(config));
        }
    }

    /**
     * Creates a job.
     *
     * @param name     the job name
     * @param jobClass the job class
     * @param user     the user to run the job as
     * @return the job configuration
     */
    private Entity createJob(String name, Class jobClass, User user) {
        Entity job = (Entity) create(JOB_TEST);
        job.setName(name);
        IMObjectBean bean = getArchetypeService().getBean(job);
        bean.setValue("class", jobClass.getName());

        // schedule it to run yesterday, so it doesn't get automatically run while testing
        bean.setValue("dayOfMonth", LocalDate.now().minusDays(1).getDayOfMonth());

        bean.addTarget("runAs", user);
        save(job);
        return job;
    }

    /**
     * Schedules a job to run in the next 2 seconds.
     *
     * @param job the job
     */
    private void scheduleIn2Seconds(Entity job) {
        LocalDateTime now = LocalDateTime.now();
        int next = now.plusSeconds(2).getSecond();
        IMObjectBean bean = getArchetypeService().getBean(job);
        bean.setValue("dayOfMonth", LocalDate.now().getDayOfMonth());
        bean.setValue("seconds", next);
        save(job);
    }
}
