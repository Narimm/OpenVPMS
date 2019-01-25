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

package org.openvpms.web.jobs.pharmacy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.web.component.im.query.QueryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests the {@link PharmacyOrderDiscontinuationScheduler}.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderDiscontinuationSchedulerTestCase extends ArchetypeServiceTest {

    /**
     * The practice service.
     */
    private PracticeService practiceService;

    /**
     * The thread pool.
     */
    private ThreadPoolTaskExecutor threadPool;

    /**
     * The practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * The scheduler.
     */
    private PharmacyOrderDiscontinuationScheduler scheduler;

    /**
     * The practice.
     */
    private IMObjectBean practice;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        practice = getBean(TestHelper.getPractice());

        User user = TestHelper.createUser();
        practice.setTarget("serviceUser", user);
        practice.save();

        // disable the discontinuation job
        setDiscontinuationPeriod(0, DateUnits.MINUTES);

        IArchetypeService service = getArchetypeService();

        // remove any existing job
        List<IMObject> jobs = getJobs();
        for (IMObject job : jobs) {
            service.remove(job);
        }
        threadPool = new ThreadPoolTaskExecutor();
        threadPool.afterPropertiesSet();

        practiceService = new PracticeService(service, practiceRules, threadPool);
        scheduler = new PharmacyOrderDiscontinuationScheduler(service, practiceService, practiceRules);
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        scheduler.dispose();
        practiceService.dispose();
        threadPool.destroy();
    }

    /**
     * Verifies that the {@link PharmacyOrderDiscontinuationScheduler} creates a job if the
     * {@code pharmacyOrderDiscontinuePeriod} is set.
     * <p/>
     * The job should be scheduled twice as often as the discontinuation period, with a minimum of every 5 mins
     */
    @Test
    public void testSchedule() throws Exception {
        assertEquals(0, getJobs().size());

        setDiscontinuationPeriod(10, DateUnits.MINUTES);
        Thread.sleep(1000);                 // practice updates happen in a separate thread
        List<IMObject> jobs1 = getJobs();
        assertEquals(1, jobs1.size());
        checkSchedule(jobs1.get(0), "*/5", "*"); // every 5 minutes

        setDiscontinuationPeriod(90, DateUnits.MINUTES);
        Thread.sleep(1000);
        List<IMObject> jobs2 = getJobs();
        assertEquals(1, jobs2.size());
        checkSchedule(jobs2.get(0), "*/45", "*"); // every 45 minutes

        setDiscontinuationPeriod(2, DateUnits.HOURS);
        Thread.sleep(1000);
        List<IMObject> jobs3 = getJobs();
        assertEquals(1, jobs3.size());
        checkSchedule(jobs3.get(0), "0", "*/1"); // every hour

        // now set the discontinuation period to 0. This should disable the job.
        setDiscontinuationPeriod(0, DateUnits.MINUTES);
        Thread.sleep(1000);
        List<IMObject> jobs = getJobs();
        assertEquals(1, jobs.size());
        assertFalse(jobs.get(0).isActive());
    }

    /**
     * Verifies the job is scheduled correctly.
     *
     * @param job     the job
     * @param minutes the expected minutes
     * @param hours   the expected hours
     */
    private void checkSchedule(IMObject job, String minutes, String hours) {
        IMObjectBean bean = getBean(job);
        assertEquals(minutes, bean.getString("minutes"));
        assertEquals(hours, bean.getString("hours"));
    }

    /**
     * Sets the discontinuation period on the practice.
     *
     * @param period the period
     * @param units  the units
     */
    private void setDiscontinuationPeriod(int period, DateUnits units) {
        practice.setValue("pharmacyOrderDiscontinuePeriod", period);
        practice.setValue("pharmacyOrderDiscontinuePeriodUnits", units.toString());
        practice.save();
    }

    /**
     * Returns the configured jobs.
     *
     * @return the configured jobs
     */
    private List<IMObject> getJobs() {
        ArchetypeQuery query = new ArchetypeQuery(PharmacyOrderDiscontinuationJob.JOB_ARCHETYPE, false);
        return QueryHelper.query(query);
    }

}
