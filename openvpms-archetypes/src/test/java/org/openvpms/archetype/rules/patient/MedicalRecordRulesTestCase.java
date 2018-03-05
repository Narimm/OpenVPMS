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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createDocumentAttachment;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link MedicalRecordRules} class.
 *
 * @author Tim Anderson
 */
public class MedicalRecordRulesTestCase extends ArchetypeServiceTest {

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The rules.
     */
    private MedicalRecordRules rules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        clinician = TestHelper.createClinician();
        patient = TestHelper.createPatient();
        rules = new MedicalRecordRules(getArchetypeService());
    }

    /**
     * Verifies that deletion of an <em>act.patientClinicalProblem</em>
     * doesn't affect its children.
     */
    @Test
    public void testDeleteClinicalProblem() {
        Act event = createEvent();
        Act problem = createProblem();
        Act note = createNote();
        ActBean eventBean = new ActBean(event);
        eventBean.addNodeRelationship("items", problem);
        eventBean.addNodeRelationship("items", note);
        ActBean problemBean = new ActBean(problem);
        problemBean.addNodeRelationship("items", note);
        save(event, problem, note);

        // make sure each of the objects can be retrieved
        assertNotNull(get(event.getObjectReference()));
        assertNotNull(get(problem.getObjectReference()));

        remove(problem);  // remove shouldn't cascade to delete note

        // make sure the all but the problem can be retrieved
        assertNotNull(get(event));
        assertNull(get(problem));
        assertNotNull(get(note));
    }

    /**
     * Tests the {@link MedicalRecordRules#getEvent(IMObjectReference)}
     * method.
     */
    @Test
    public void testGetEvent() {
        Act event1 = createEvent(getDate("2007-01-01"));
        event1.setStatus(IN_PROGRESS);
        save(event1);
        checkEvent(event1);

        Act event2 = createEvent(getDate("2007-01-02"));
        event2.setStatus(COMPLETED);
        save(event2);
        checkEvent(event2);

        Act event3 = createEvent(getDate("2008-01-01"));
        event3.setStatus(IN_PROGRESS);
        save(event3);
        checkEvent(event3);

        // ensure that where there are 2 events with the same timestamp, the one with the higher id is returned
        Act event4 = createEvent(getDate("2008-01-01"));
        event4.setStatus(IN_PROGRESS);
        save(event4);
        checkEvent(event4);
    }

    /**
     * Tests the {@link MedicalRecordRules#getEvent} method.
     */
    @Test
    public void testGetEventByDate() {
        Date jan1 = getDate("2007-01-01");
        Date jan2 = getDate("2007-01-02");
        Date jan3 = getDatetime("2007-01-03 10:43:55");

        checkEvent(jan2, null);

        Act event1 = createEvent(jan2);
        save(event1);

        checkEvent(jan2, event1);
        checkEvent(jan1, null);
        checkEvent(jan3, event1);

        event1.setActivityEndTime(jan2);
        save(event1);
        checkEvent(jan1, null);
        checkEvent(jan3, null);

        Act event2 = createEvent(jan3);
        save(event2);
        checkEvent(jan3, event2);
        checkEvent(getDate("2007-01-03"), event2);
        // note that the time component is zero, but still picks up event2,
        // despite the event being created after 00:00:00. This is required
        // as the time component of startTime is not supplied consistently -
        // In some cases, it is present, in others it is 00:00:00.

        checkEvent(jan2, event1);

        // make sure that when an event has a duplicate timestamp, the earliest (by id) is returned
        Act event2dup = createEvent(jan3);
        save(event2dup);
        checkEvent(jan3, event2);
    }

    /**
     * Tests the {@link MedicalRecordRules#createNote(Date, Party, String, User, User)} method.
     */
    @Test
    public void testCreateNote() {
        Date startTime = getDate("2012-07-17");
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();
        String text = "Test note";
        Act note = rules.createNote(startTime, patient, text, clinician, author);

        ActBean bean = new ActBean(note);
        assertEquals(startTime, note.getActivityStartTime());
        assertEquals(text, bean.getString("note"));
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(clinician, bean.getNodeParticipant("clinician"));
        assertEquals(author, bean.getNodeParticipant("author"));
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvent} method where no event
     * exists for the patient. A new one will be created with IN_PROGRESS status,
     * and the specified startTime.
     */
    @Test
    public void testAddToEventForNonExistentEvent() {
        Date date = getDate("2007-04-05");
        Act medication = PatientTestHelper.createMedication(patient);

        rules.addToEvent(medication, date);
        Act event = rules.getEvent(patient);
        assertTrue(IN_PROGRESS.equals(event.getStatus()));
        assertEquals(date, event.getActivityStartTime());
        checkContains(event, medication);
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvent} method where there is
     * an existing IN_PROGRESS event that has a startTime < 7 days prior to
     * that specified. The medication should be added to it.
     */
    @Test
    public void testAddToEventForExistingInProgressEvent() {
        Date date = getDate("2007-04-05");
        Act medication = PatientTestHelper.createMedication(patient);

        Act expected = createEvent(date);
        save(expected);

        rules.addToEvent(medication, date);
        Act event = rules.getEvent(patient);
        checkContains(event, medication);
        assertEquals(expected, event);
        assertTrue(IN_PROGRESS.equals(event.getStatus()));
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvent} method where
     * there is an IN_PROGRESS event that has a startTime > 7 days prior to
     * the specified startTime. A new IN_PROGRESS event should be created.
     */
    @Test
    public void testAddToEventForExistingOldInProgressEvent() {
        Date date = getDate("2007-04-05");
        Act medication = PatientTestHelper.createMedication(patient);

        Date old = DateRules.getDate(date, -8, DateUnits.DAYS);
        Act oldEvent = createEvent(old);
        save(oldEvent);

        rules.addToEvent(medication, date);
        Act event = rules.getEvent(patient);
        checkContains(event, medication);
        assertFalse(oldEvent.equals(event));
        assertEquals(date, event.getActivityStartTime());
        assertTrue(IN_PROGRESS.equals(event.getStatus()));
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvent} method where
     * there is a COMPLETED event that has a startTime > 7 days prior to
     * the specified startTime. A new COMPLETED event should be created.
     */
    public void testAddToEventForExistingOldCompletedEvent() {
        Date date = getDate("2007-04-05");
        Act medication = PatientTestHelper.createMedication(patient);

        Date old = DateRules.getDate(date, -8, DateUnits.DAYS);
        Act oldEvent = createEvent(old);
        oldEvent.setStatus(COMPLETED);
        save(oldEvent);

        rules.addToEvent(medication, date);
        Act event = rules.getEvent(patient);
        checkContains(event, medication);
        assertFalse(oldEvent.equals(event));
        assertEquals(date, event.getActivityStartTime());
        assertTrue(COMPLETED.equals(event.getStatus()));
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvent} method where
     * there is a COMPLETTED event that has a startTime and endTime that
     * overlaps the specified start time. The medication should be added to it.
     */
    @Test
    public void testAddToEventForExistingCompletedEvent() {
        Date date = getDate("2007-04-05");
        Act medication = PatientTestHelper.createMedication(patient);

        Act completed = createEvent(getDate("2007-04-03"));
        completed.setActivityEndTime(getDate("2007-04-06"));
        completed.setStatus(COMPLETED);
        save(completed);

        rules.addToEvent(medication, date);
        Act event = rules.getEvent(patient);
        checkContains(event, medication);
        assertEquals(completed, event);
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvent} method where
     * there is a COMPLETTED event that has a startTime and endTime that
     * DOESN'T overlap the specified start time. The medication should be added
     * to a new IN_PROGRESS event whose startTime equals that specified.
     */
    @Test
    public void testAddToEventForExistingNonOverlappingCompletedEvent() {
        Date date = getDate("2007-04-05");
        Act medication = PatientTestHelper.createMedication(patient);

        Act completed = createEvent(getDate("2007-04-03"));
        completed.setActivityEndTime(getDate("2007-04-04"));
        completed.setStatus(COMPLETED);
        save(completed);

        rules.addToEvent(medication, date);
        Act event = rules.getEvent(patient);
        checkContains(event, medication);
        assertFalse(completed.equals(event));
        assertEquals(date, event.getActivityStartTime());
        assertTrue(IN_PROGRESS.equals(event.getStatus()));
    }

    /**
     * Tests the {@link org.openvpms.archetype.rules.patient.MedicalRecordRules#linkMedicalRecords} method.
     */
    @Test
    public void testLinkMedicalRecords() {
        Act event = createEvent();
        Act problem = createProblem();
        Act note = createNote();
        Act addendum = createAddendum();
        rules.linkMedicalRecords(event, problem, note, addendum);

        event = get(event);
        problem = get(problem);
        note = get(note);
        addendum = get(addendum);

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasNodeTarget("items", problem));
        assertTrue(eventBean.hasNodeTarget("items", note));
        assertTrue(eventBean.hasNodeTarget("items", addendum));

        ActBean problemBean = new ActBean(problem);
        assertTrue(problemBean.hasNodeTarget("items", note));
        assertTrue(problemBean.hasNodeTarget("items", addendum));

        // verify that it can be called again with no ill effect
        rules.linkMedicalRecords(event, problem, note, addendum);
    }

    /**
     * Tests the {@link MedicalRecordRules#linkMedicalRecords(Act, Act)} method
     * passing an <em>act.patientClinicalNote</em>.
     */
    @Test
    public void testLinkMedicalRecordsWithItem() {
        Act event = createEvent();
        Act note = createNote();

        rules.linkMedicalRecords(event, note);

        event = get(event);
        note = get(note);

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, note));
        assertEquals(1, event.getActRelationships().size());

        // verify that it can be called again with no ill effect
        rules.linkMedicalRecords(event, note);
        assertEquals(1, event.getActRelationships().size());
    }

    /**
     * Tests the {@link MedicalRecordRules#linkMedicalRecords(Act, Act)} method passing
     * an <em>act.customerAccountInvoiceItem</em>.
     */
    @Test
    public void testLinkMedicalRecordsWithInvoiceItem() {
        Act event = createEvent();
        Act invoiceItem = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM,
                                                               patient, TestHelper.createProduct(), BigDecimal.ONE);
        save(invoiceItem);
        rules.linkMedicalRecords(event, invoiceItem);

        event = get(event);
        invoiceItem = get(invoiceItem);

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM, invoiceItem));
        assertEquals(1, event.getActRelationships().size());

        // verify that it can be called again with no ill effect
        rules.linkMedicalRecords(event, invoiceItem);
        assertEquals(1, event.getActRelationships().size());
    }

    /**
     * Tests the {@link MedicalRecordRules#linkMedicalRecords(Act, Act)} method,
     * passing an <em>act.patientClinicalProblem</em>.
     */
    @Test
    public void testLinkMedicalRecordsWithProblem() {
        Act event = createEvent();
        Act problem = createProblem();
        Act note = createNote();

        ActBean problemBean = new ActBean(problem);
        problemBean.addNodeRelationship("items", note);
        save(problem, note);

        rules.linkMedicalRecords(event, problem);

        event = get(event);
        problem = get(problem);
        note = get(note);

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, note));
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, problem));
        assertEquals(2, event.getActRelationships().size());
        assertEquals(2, problem.getActRelationships().size());

        // verify that it can be called again with no ill effect
        rules.linkMedicalRecords(event, problem);
        assertEquals(2, event.getActRelationships().size());
        assertEquals(2, problem.getActRelationships().size());
    }

    /**
     * Verifies the {@link MedicalRecordRules#linkMedicalRecords(Act, Act, Act, Act)} method,
     * links all of a problem's items to the parent event if they aren't already present.
     */
    @Test
    public void testLinkMedicalRecordsForMissingLinks() {
        Act event = createEvent();
        Act problem = createProblem();
        Act note1 = createNote();
        Act note2 = createNote();
        Act medication = PatientTestHelper.createMedication(patient);
        ActBean problemBean = new ActBean(problem);
        problemBean.addNodeRelationship("items", note1);
        problemBean.addNodeRelationship("items", medication);
        save(problem, note1, medication);

        // now link the records to the event
        rules.linkMedicalRecords(event, problem, note2, null);

        event = get(event);
        problem = get(problem);
        note1 = get(note1);
        note2 = get(note2);
        medication = get(medication);

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, problem));
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, note1));
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, note2));
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, medication));

        problemBean = new ActBean(problem);
        assertTrue(problemBean.hasRelationship(PatientArchetypes.CLINICAL_PROBLEM_ITEM, note1));
        assertTrue(problemBean.hasRelationship(PatientArchetypes.CLINICAL_PROBLEM_ITEM, note2));
        assertTrue(problemBean.hasRelationship(PatientArchetypes.CLINICAL_PROBLEM_ITEM, medication));
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvents} method.
     */
    @Test
    public void testAddToEvents() {
        Date date = getDate("2007-04-05");
        Party patient2 = TestHelper.createPatient();
        Act med1 = PatientTestHelper.createMedication(patient);
        Act med2 = PatientTestHelper.createMedication(patient);
        Act med3 = PatientTestHelper.createMedication(patient2);
        Act med4 = PatientTestHelper.createMedication(patient2);

        List<Act> acts = Arrays.asList(med1, med2, med3, med4);

        Act event1 = createEvent(date);
        save(event1);
        rules.addToEvents(acts, date);

        event1 = rules.getEvent(patient, date);
        checkContains(event1, med1, med2);

        Act event2 = rules.getEvent(patient2, date);
        assertNotNull(event2);
        checkContains(event2, med3, med4);
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvents} method where an event relationship already exists, but
     * to a different patient.
     */
    @Test
    public void testAddToEventsForDifferentPatient() {
        Date date = getDate("2014-03-22");
        Party patient2 = TestHelper.createPatient();
        Party patient3 = TestHelper.createPatient();
        Act med1 = PatientTestHelper.createMedication(patient);
        Act med2 = PatientTestHelper.createMedication(patient);
        Act med3 = PatientTestHelper.createMedication(patient2);
        Act med4 = PatientTestHelper.createMedication(patient2);

        List<Act> acts = Arrays.asList(med1, med2, med3, med4);

        Act event1 = createEvent(date);
        save(event1);
        rules.addToEvents(acts, date);

        event1 = rules.getEvent(patient, date);
        checkContains(event1, med1, med2);

        Act event2 = rules.getEvent(patient2, date);
        assertNotNull(event2);
        checkContains(event2, med3, med4);

        // now change the patient for med2 and med4 to patient3
        setPatient(med2, patient3);
        setPatient(med4, patient3);
        rules.addToEvents(acts, date);

        event1 = rules.getEvent(patient, date);
        assertNotNull(event1);
        checkContains(event1, med1);

        event2 = rules.getEvent(patient2, date);
        assertNotNull(event2);
        checkContains(event2, med3);

        Act event3 = rules.getEvent(patient3, date);
        assertNotNull(event3);
        checkContains(event3, med2, med4);
    }

    /**
     * Tests the {@link MedicalRecordRules#addToHistoricalEvents} method.
     */
    @Test
    public void testAddToHistoricalEvents() {
        Date eventDate1 = getDate("2007-04-05");
        Date eventDate2 = getDate("2007-07-01");
        Date eventDate3 = getDate("2007-08-01");
        Act med1 = PatientTestHelper.createMedication(patient);
        Act med2 = PatientTestHelper.createMedication(patient);
        Act med3 = PatientTestHelper.createMedication(patient);

        Date medDate1 = getDate("2007-04-04"); // eventDate1-1
        med1.setActivityStartTime(medDate1);
        save(med1);

        med2.setActivityStartTime(eventDate2);
        save(med2);

        med3.setActivityStartTime(eventDate3);
        save(med3);

        Act event1 = createEvent(eventDate1);
        save(event1);

        Act event2 = createEvent(eventDate2);
        save(event2);

        Act event3 = createEvent(eventDate3);
        save(event3);

        rules.addToHistoricalEvents(Collections.singletonList(med1), eventDate1);
        rules.addToHistoricalEvents(Collections.singletonList(med2), eventDate2);
        rules.addToHistoricalEvents(Collections.singletonList(med3), eventDate3);

        event1 = rules.getEvent(patient, eventDate1);
        checkContains(event1, med1);

        event2 = rules.getEvent(patient, eventDate2);
        assertNotNull(event2);
        checkContains(event2, med2);

        event3 = rules.getEvent(patient, eventDate3);
        assertNotNull(event3);
        checkContains(event3, med3);
    }

    /**
     * Tests the {@link MedicalRecordRules#getEventForAddition(Party, Date, Entity)} method.
     */
    @Test
    public void testGetEventForAdditionForWithCompletedEventOnSameDay() {
        Act event1 = createEvent(getDatetime("2013-11-21 10:00:00"), getDatetime("2013-11-21 11:00:00"), COMPLETED);
        Act event2 = createEvent(getDatetime("2013-11-21 12:00:05"), null, IN_PROGRESS);
        save(event1, event2);

        // no events, so a new event should be created
        checkGetEventForAddition(null, getDatetime("2013-11-20 00:00:00"));

        // timestamps closest to event1 should return event1
        checkGetEventForAddition(event1, getDatetime("2013-11-21 00:00:00"));
        checkGetEventForAddition(event1, getDatetime("2013-11-21 09:00:00"));
        checkGetEventForAddition(event1, getDatetime("2013-11-21 10:00:00"));
        checkGetEventForAddition(event1, getDatetime("2013-11-21 11:00:00"));

        // timestamps closest to event2 should return event2
        checkGetEventForAddition(event2, getDatetime("2013-11-21 12:00:00"));
        checkGetEventForAddition(event2, getDatetime("2013-11-21 13:00:00"));
        checkGetEventForAddition(event2, getDatetime("2013-11-22 00:00:00"));
        checkGetEventForAddition(event2, getDatetime("2013-11-28 00:00:00"));

        // over a week after event2, a new event should be created
        checkGetEventForAddition(null, getDatetime("2013-11-29 00:00:00"));

        // now make event2 a boarding event. It should now be returned
        Party customer = TestHelper.createCustomer();
        Entity cageType = ScheduleTestHelper.createCageType("Z Cage Type");
        Entity schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation(), cageType);
        Act appointment = ScheduleTestHelper.createAppointment(getDatetime("2013-11-21 11:50:00"),
                                                               getDatetime("2013-11-30 17:00:00"),
                                                               schedule, customer, patient);
        ActBean bean = new ActBean(appointment);
        bean.addNodeRelationship("event", event2);
        save(appointment, event2);
        checkGetEventForAddition(event2, getDatetime("2013-11-29 00:00:00"));
    }

    /**
     * Tests the {@link MedicalRecordRules#getEventForAddition} method.
     */
    @Test
    public void testGetEventWithCompletedEventWithNoEndDate() {
        Act event1 = createEvent(getDatetime("2013-11-15 10:00:00"), null, COMPLETED);
        Act event2 = createEvent(getDatetime("2013-11-21 12:00:05"), null, IN_PROGRESS);
        save(event1, event2);

        // no events before the 15th, so a new event should be created
        checkGetEventForAddition(null, getDatetime("2013-11-14 00:00:00"));

        // event2 should be returned for all timestamps on the 15th
        checkGetEventForAddition(event1, getDatetime("2013-11-15 00:00:00"));
        checkGetEventForAddition(event1, getDatetime("2013-11-15 23:59:59"));

        // a new event should be returned from 16-20th
        checkGetEventForAddition(null, getDatetime("2013-11-16 00:00:00"));
        checkGetEventForAddition(null, getDatetime("2013-11-20 23:59:59"));

        // event2 should be returned for all timestamps on or after the 21st
        checkGetEventForAddition(event2, getDatetime("2013-11-21 00:00:00"));
        checkGetEventForAddition(event2, getDatetime("2013-11-21 12:00:00"));
        checkGetEventForAddition(event2, getDatetime("2013-11-21 12:00:05"));
        checkGetEventForAddition(event2, getDatetime("2013-11-22 00:00:00"));
        checkGetEventForAddition(event2, getDatetime("2013-11-28 00:00:00"));

        // over a week after event2, a new event should be created
        checkGetEventForAddition(null, getDatetime("2013-11-29 13:00:00"));

        // now make event2 a boarding event. It should now be returned
        Party customer = TestHelper.createCustomer();
        Entity cageType = ScheduleTestHelper.createCageType("Z Cage Type");
        Entity schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation(), cageType);
        Act appointment = ScheduleTestHelper.createAppointment(getDatetime("2013-11-21 11:50:00"),
                                                               getDatetime("2013-11-30 17:00:00"),
                                                               schedule, customer, patient);
        ActBean bean = new ActBean(appointment);
        bean.addNodeRelationship("event", event2);
        save(appointment, event2);

        checkGetEventForAddition(event2, getDatetime("2013-11-29 13:00:00"));
    }

    /**
     * Tests the {@link MedicalRecordRules#getLockableRecords()} method.
     */
    @Test
    public void testGetLockableRecords() {
        List<String> shortNames = Arrays.asList(rules.getLockableRecords());
        assertEquals(13, shortNames.size());
        assertTrue(shortNames.contains(PatientArchetypes.CLINICAL_ADDENDUM));
        assertTrue(shortNames.contains(PatientArchetypes.CLINICAL_NOTE));
        assertTrue(shortNames.contains(PatientArchetypes.DOCUMENT_ATTACHMENT));
        assertTrue(shortNames.contains(PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION));
        assertTrue(shortNames.contains(PatientArchetypes.DOCUMENT_FORM));
        assertTrue(shortNames.contains(PatientArchetypes.DOCUMENT_IMAGE));
        assertTrue(shortNames.contains(PatientArchetypes.DOCUMENT_IMAGE_VERSION));
        assertTrue(shortNames.contains(PatientArchetypes.DOCUMENT_LETTER));
        assertTrue(shortNames.contains(PatientArchetypes.DOCUMENT_LETTER_VERSION));
        assertTrue(shortNames.contains(InvestigationArchetypes.PATIENT_INVESTIGATION));
        assertTrue(shortNames.contains(InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION));
        assertTrue(shortNames.contains(PatientArchetypes.PATIENT_MEDICATION));
        assertTrue(shortNames.contains(PatientArchetypes.PATIENT_WEIGHT));
    }

    /**
     * Verifies that each of the lockable medical records have a startTime and status node.
     * If the status is hidden, it must be read-only.
     * <br/>
     * Note that in the original version of medical record locking, the startTime node was also read only.
     * However this prevented the 1.8 behaviour of allowing practices to forward/back-date records when locking is not
     * enabled.
     */
    @Test
    public void testLockableRecordStartTimeAndStatusNodes() {
        IArchetypeService service = getArchetypeService();
        for (String shortName : rules.getLockableRecords()) {
            NodeDescriptor startTime = DescriptorHelper.getNode(shortName, "startTime", service);
            assertNotNull(shortName, startTime);

            NodeDescriptor status = DescriptorHelper.getNode(shortName, "status", service);
            assertNotNull(shortName, status);
            if (status.isHidden()) {
                assertTrue(shortName, status.isReadOnly());
            }
        }
    }

    /**
     * Tests the {@link MedicalRecordRules#getAttachment(String, Act)}.
     */
    @Test
    public void testGetAttachment() {
        Act event = createEvent();
        ActBean bean = new ActBean(event);

        assertNull(rules.getAttachment("notes.pdf", event));
        DocumentAct act1 = createDocumentAttachment(getDatetime("2017-04-22 10:00:00"), patient, "notes.pdf");
        DocumentAct act2 = createDocumentAttachment(getDatetime("2017-04-22 11:00:00"), patient, "billing.pdf");
        bean.addNodeRelationship("items", act1);
        bean.addNodeRelationship("items", act2);
        bean.save();

        assertEquals(act1, rules.getAttachment("notes.pdf", event));
        assertEquals(act2, rules.getAttachment("billing.pdf", event));

        DocumentAct act3 = createDocumentAttachment(getDatetime("2017-04-22 12:00:00"), patient, "notes.pdf");
        bean.addNodeRelationship("items", act3);
        bean.save();

        assertEquals(act3, rules.getAttachment("notes.pdf", event));
    }

    /**
     * Tests the {@link MedicalRecordRules#getAttachment(String, Act, String, String)} method.
     */
    @Test
    public void testGetAttachmentWithIdentity() {
        Act event = createEvent();
        ActBean bean = new ActBean(event);

        // Smart Flow Sheet supplies the same surgery UID for both anaesthetic and anaesthetic records reports.
        String archetype = "actIdentity.smartflowsheet";
        String surgeryUid = UUID.randomUUID().toString();
        ActIdentity identity1a = createIdentity(archetype, surgeryUid);
        ActIdentity identity1b = createIdentity(archetype, surgeryUid);
        ActIdentity identity2 = createIdentity(archetype, UUID.randomUUID().toString());
        assertNull(rules.getAttachment("anaesthetic.pdf", event, archetype, identity1a.getIdentity()));

        DocumentAct act1a = createDocumentAttachment(getDatetime("2017-04-22 10:00:00"), patient, "anaesthetic.pdf",
                                                     identity1a);
        DocumentAct act1b = createDocumentAttachment(getDatetime("2017-04-22 10:00:00"), patient,
                                                     "anaesthetic records.pdf", identity1b);
        DocumentAct act2 = createDocumentAttachment(getDatetime("2017-04-22 11:00:00"), patient, "anaesthetic.pdf",
                                                    identity2);

        bean.addNodeRelationship("items", act1a);
        bean.addNodeRelationship("items", act1b);
        bean.addNodeRelationship("items", act2);
        bean.save();

        assertEquals(act1a, rules.getAttachment("anaesthetic.pdf", event, archetype, surgeryUid));
        assertEquals(act1b, rules.getAttachment("anaesthetic records.pdf", event, archetype, surgeryUid));
        assertEquals(act2, rules.getAttachment("anaesthetic.pdf", event, archetype, identity2.getIdentity()));
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @return a new act
     */
    protected Act createEvent() {
        return PatientTestHelper.createEvent(patient, clinician);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @param startTime the start time
     * @return a new act
     */
    protected Act createEvent(Date startTime) {
        return createEvent(startTime, null);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @param startTime the start time. May be {@code null}
     * @param endTime   the end time. May be {@code null}
     * @param status    the event status
     * @return a new act
     */
    protected Act createEvent(Date startTime, Date endTime, String status) {
        Act act = createEvent(startTime, endTime);
        act.setStatus(status);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @param startTime the start time. May be {@code null}
     * @param endTime   the end time. May be {@code null}
     * @return a new act
     */
    protected Act createEvent(Date startTime, Date endTime) {
        return PatientTestHelper.createEvent(startTime, endTime, patient, clinician);
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     *
     * @return a new act
     */
    protected Act createProblem() {
        Act act = createAct(PatientArchetypes.CLINICAL_PROBLEM);
        Lookup diagnosis = TestHelper.getLookup("lookup.diagnosis", "HEART_MURMUR");
        act.setReason(diagnosis.getCode());
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);
        bean.addNodeParticipation("clinician", clinician);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalNote</em>.
     *
     * @return a new act
     */
    protected Act createNote() {
        Act act = createAct(PatientArchetypes.CLINICAL_NOTE);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalAddendum</em>.
     *
     * @return a new act
     */
    protected Act createAddendum() {
        Act act = createAct(PatientArchetypes.CLINICAL_ADDENDUM);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);
        return act;
    }

    /**
     * Creates an act identity.
     *
     * @param archetype the identity archetype
     * @param identity  the identity
     * @return a new identity
     */
    private ActIdentity createIdentity(String archetype, String identity) {
        ActIdentity result = (ActIdentity) create(archetype);
        result.setIdentity(identity);
        return result;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected Act createAct(String shortName) {
        Act act = (Act) create(shortName);
        assertNotNull(act);
        return act;
    }

    /**
     * Verifies that the correct event is returned.
     *
     * @param expected the expected event. May be <tt>null</tt>
     */
    private void checkEvent(Act expected) {
        Act event = rules.getEvent(patient);
        if (expected == null) {
            assertNull(event);
        } else {
            assertEquals(expected, event);
        }
    }

    /**
     * Verifies that the correct event is returned for a particular date.
     *
     * @param date     the date
     * @param expected the expected event. May be <tt>null</tt>
     */
    private void checkEvent(Date date, Act expected) {
        Act event = rules.getEvent(patient, date);
        if (expected == null) {
            assertNull(event);
        } else {
            assertEquals(expected, event);
        }
    }

    /**
     * Verifies that an event contains a set of acts.
     *
     * @param event the event
     * @param acts  the expected acts
     */
    private void checkContains(Act event, Act... acts) {
        List<Act> items = getActs(event);
        assertEquals(acts.length, items.size());
        for (Act act : acts) {
            boolean found = false;
            for (Act item : items) {
                if (item.equals(act)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    /**
     * Returns the items linked to an event.
     *
     * @param event the event
     * @return the items
     */
    private List<Act> getActs(Act event) {
        ActBean bean = new ActBean(event);
        return bean.getNodeActs("items");
    }

    /**
     * Verifies that the expected event is returned for a give date by
     * {@link MedicalRecordRules#getEventForAddition(Party, Date, Entity)}.
     *
     * @param expected the expected event. If {@code null}, a new event is expected
     * @param date     the date
     */
    private void checkGetEventForAddition(Act expected, Date date) {
        Act actual = rules.getEventForAddition(patient, date, null);
        if (expected != null) {
            assertEquals(expected, actual);
        } else {
            assertTrue(actual.isNew());
            assertEquals(date, actual.getActivityStartTime());
            assertEquals(IN_PROGRESS, actual.getStatus());
        }
    }

    /**
     * Helper to change the patient for an act.
     *
     * @param act     the act
     * @param patient the new patient
     */
    private void setPatient(Act act, Party patient) {
        ActBean itemBean = new ActBean(act);
        Participation participation = itemBean.getParticipation(PatientArchetypes.PATIENT_PARTICIPATION);
        participation.setEntity(patient.getObjectReference());
        itemBean.save();
    }

}
