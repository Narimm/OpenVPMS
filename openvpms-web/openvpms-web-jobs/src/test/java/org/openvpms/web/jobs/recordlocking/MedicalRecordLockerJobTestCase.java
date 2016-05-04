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

package org.openvpms.web.jobs.recordlocking;

import org.hibernate.SessionFactory;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.patient.ProblemActStatus;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Tests the {@link MedicalRecordLockerJob} class.
 *
 * @author Tim Anderson
 */
public class MedicalRecordLockerJobTestCase extends ArchetypeServiceTest {

    /**
     * The session factory.
     */
    @Autowired
    private SessionFactory factory;

    /**
     * The job.
     */
    private MedicalRecordLockerJob job;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Entity configuration = (Entity) create(MedicalRecordLockingScheduler.JOB_SHORT_NAME);
        IArchetypeService service = getArchetypeService();
        MedicalRecordRules rules = new MedicalRecordRules(service);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.afterPropertiesSet();

        // lock records after 36 hours
        PracticeService practiceService = new PracticeService(service, applicationContext.getBean(PracticeRules.class),
                                                              executor) {
            @Override
            public Period getRecordLockPeriod() {
                return Period.hours(36);
            }
        };
        job = new MedicalRecordLockerJob(configuration, (IArchetypeRuleService) service, practiceService, factory,
                                         rules);
    }

    /**
     * Tests locking of medical records.
     * <p/>
     * Creates 2 sets of acts, one set dated 24 hours in the past, the other 48. Then runs the locker with a period
     * of 36 hours and verifies the oldest set have been locked.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLock() throws Exception {
        Date date1 = DateRules.getDate(new Date(), -1, DateUnits.DAYS);
        Date date2 = DateRules.getDate(new Date(), -2, DateUnits.DAYS);

        Party patient = TestHelper.createPatient();
        Entity investigationType = ProductTestHelper.createInvestigationType();
        Product product = TestHelper.createProduct();

        // NOTE: event and problem acts aren't locked
        Act event1 = PatientTestHelper.createEvent(date1, patient);
        Act problem1 = PatientTestHelper.createProblem(date1, patient);

        Act addendum1 = PatientTestHelper.createAddendum(date1, patient, null);
        Act attachment1 = PatientTestHelper.createDocumentAttachment(date1, patient);
        Act attachmentVersion1 = PatientTestHelper.createDocumentAttachmentVersion(date1);
        Act form1 = PatientTestHelper.createDocumentForm(date1, patient, null);
        Act image1 = PatientTestHelper.createDocumentImage(date1, patient);
        Act imageVersion1 = PatientTestHelper.createDocumentImageVersion(date1);
        Act investigation1 = PatientTestHelper.createInvestigation(date1, patient, null, null, investigationType);
        Act investigationVersion1 = PatientTestHelper.createInvestigationVersion(date1, investigationType);
        Act letter1 = PatientTestHelper.createDocumentLetter(date1, patient);
        Act letterVersion1 = PatientTestHelper.createDocumentLetterVersion(date1);
        Act medication1 = PatientTestHelper.createMedication(date1, patient, product);
        Act note1 = PatientTestHelper.createNote(date1, patient);
        Act weight1 = PatientTestHelper.createWeight(date1, patient);

        Act event2 = PatientTestHelper.createEvent(date2, patient);
        Act problem2 = PatientTestHelper.createProblem(date2, patient);

        Act addendum2 = PatientTestHelper.createAddendum(date2, patient, null);
        Act attachment2 = PatientTestHelper.createDocumentAttachment(date2, patient);
        Act attachmentVersion2 = PatientTestHelper.createDocumentAttachmentVersion(date2);
        Act form2 = PatientTestHelper.createDocumentForm(date2, patient, null);
        Act image2 = PatientTestHelper.createDocumentImage(date2, patient);
        Act imageVersion2 = PatientTestHelper.createDocumentImageVersion(date2);
        Act investigation2 = PatientTestHelper.createInvestigation(date2, patient, null, null, investigationType);
        Act investigationVersion2 = PatientTestHelper.createInvestigationVersion(date2, investigationType);
        Act letter2 = PatientTestHelper.createDocumentLetter(date2, patient);
        Act letterVersion2 = PatientTestHelper.createDocumentLetterVersion(date2);
        Act medication2 = PatientTestHelper.createMedication(date2, patient, product);
        Act note2 = PatientTestHelper.createNote(date2, patient);
        Act weight2 = PatientTestHelper.createWeight(date2, patient);

        // check initial statuses
        checkInProgress(event1);
        check(problem1, ProblemActStatus.UNRESOLVED);

        checkInProgress(addendum1, attachment1, attachmentVersion1, form1, image1, imageVersion1,
                        investigation1, investigationVersion1, letter1, letterVersion1, medication1, note1, weight1);
        checkInProgress(event2);
        check(problem2, ProblemActStatus.UNRESOLVED);
        checkInProgress(addendum2, attachment2, attachmentVersion2, form2, image2, imageVersion2,
                        investigation2, investigationVersion2, letter2, letterVersion2, medication2, note2, weight2);

        // run the locker
        job.execute(null);

        // acts dated on date1 should be unchanged
        checkInProgress(event1);
        check(problem1, ProblemActStatus.UNRESOLVED);

        checkInProgress(addendum1, attachment1, attachmentVersion1, form1, image1, imageVersion1,
                        investigation1, investigationVersion1, letter1, letterVersion1, medication1, note1, weight1);

        // acts dated on date1 should be POSTED, with the exception of the event and problem acts
        checkInProgress(event2);
        check(problem2, ProblemActStatus.UNRESOLVED);

        checkPosted(addendum2, attachment2, attachmentVersion2, form2, image2, imageVersion2,
                    investigation2, investigationVersion2, letter2, letterVersion2, medication2, note2, weight2);
    }

    /**
     * Verifies that if an investigation is cancelled, its status isn't changed by locking.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCancelledInvestigation() throws Exception {
        Date date = DateRules.getDate(new Date(), -2, DateUnits.DAYS);
        Party patient = TestHelper.createPatient();
        Entity investigationType = ProductTestHelper.createInvestigationType();

        Act investigation1 = PatientTestHelper.createInvestigation(date, patient, null, null, investigationType);
        Act investigation2 = PatientTestHelper.createInvestigation(date, patient, null, null, investigationType);
        Act investigation3 = PatientTestHelper.createInvestigation(date, patient, null, null, investigationType);

        investigation2.setStatus(ActStatus.CANCELLED);
        investigation3.setStatus(ActStatus.POSTED);
        save(investigation2, investigation3);

        job.execute(null);
        check(investigation1, ActStatus.POSTED, investigation1.getVersion() + 1); // act changed, version should inc
        check(investigation2, ActStatus.CANCELLED, investigation2.getVersion());
        check(investigation3, ActStatus.POSTED, investigation3.getVersion());
    }

    /**
     * Verifies that a set of acts are all IN_PROGRESS.
     *
     * @param acts the acts to check
     */
    private void checkInProgress(Act... acts) {
        for (Act act : acts) {
            check(act, ActStatus.IN_PROGRESS);
        }
    }

    /**
     * Verifies that a set of acts are all POSTED.
     *
     * @param acts the acts to check
     */
    private void checkPosted(Act... acts) {
        for (Act act : acts) {
            check(act, ActStatus.POSTED);
        }
    }

    /**
     * Verifies an act status matches that expected.
     *
     * @param act    the act
     * @param status the expected status
     */
    private void check(Act act, String status) {
        act = get(act);
        assertNotNull(act);
        assertEquals(status, act.getStatus());
    }

    /**
     * Verifies an act status and version matches that expected.
     *
     * @param act     the act
     * @param status  the expected status
     * @param version the expected version
     */
    private void check(Act act, String status, long version) {
        act = get(act);
        assertNotNull(act);
        assertEquals(status, act.getStatus());
        assertEquals(version, act.getVersion());
    }
}
