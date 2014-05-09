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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.act;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_EVENT;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_NOTE;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_PROBLEM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_WEIGHT;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link ActHierarchyIterator} class.
 *
 * @author Tim Anderson
 */
public class ActHierarchyIteratorTestCase extends AbstractAppTest {

    /**
     * The short names to filter on.
     */
    private static final String[] SHORT_NAMES = new String[]{CLINICAL_PROBLEM, PATIENT_WEIGHT, CLINICAL_NOTE};

    /**
     * Tests the {@link ActHierarchyIterator} class.
     */
    @Test
    public void testEvents() {
        Party patient = TestHelper.createPatient(true);

        // create some events for a patient. Each event has a problem, note and weight. Each problem has a note.
        createEvent(patient, getDatetime("2007-02-01 10:30:00"));
        createEvent(patient, getDatetime("2007-02-10 11:45:00"));
        createEvent(patient, getDatetime("2007-01-10 11:45:00"));

        // query the events
        PatientHistoryQuery query = new PatientHistoryQuery(patient);
        query.getComponent();

        // check iteration to unlimited depth
        checkIterator(query, -1, 3); // should only return events
        checkIterator(query, -1, 6, CLINICAL_PROBLEM);

        // Now return all problems and notes
        checkIterator(query, -1, 12, CLINICAL_PROBLEM, CLINICAL_NOTE);

        // Now return all problems, notes and weights.
        checkIterator(query, -1, 15, CLINICAL_NOTE, CLINICAL_PROBLEM, PATIENT_WEIGHT);

        // check iteration to limited depth
        checkIterator(query, 1, 3);

        // excludes all children of the event
        checkIterator(query, 1, 3, CLINICAL_NOTE, CLINICAL_PROBLEM, PATIENT_WEIGHT);

        // excludes all children of the problems
        checkIterator(query, 2, 12, CLINICAL_NOTE, CLINICAL_PROBLEM, PATIENT_WEIGHT);
    }

    /**
     * Verifies that when a note is linked to both an event and a child problem, it is returned after the problem.
     */
    @Test
    public void testProblem() {
        Party patient = TestHelper.createPatient(true);
        User clinician = TestHelper.createClinician();

        Act weight = PatientTestHelper.createWeight(getDatetime("2014-05-09 10:00:00"), patient, clinician);
        Act problemNote = PatientTestHelper.createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act problem = PatientTestHelper.createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician,
                                                      problemNote);

        Act event = PatientTestHelper.createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician,
                                                  weight, problemNote, problem);

        // query the events
        PatientHistoryQuery query = new PatientHistoryQuery(patient);
        query.getComponent();

        // check iteration to unlimited depth
        checkIterator(query, -1, new String[0], event); // should only return events
        checkIterator(query, -1, new String[]{CLINICAL_PROBLEM, PATIENT_WEIGHT}, event, weight, problem);
        checkIterator(query, -1, SHORT_NAMES, event, weight, problem, problemNote);

        // check iteration at specific depths
        checkIterator(query, 1, SHORT_NAMES, event);
        checkIterator(query, 2, SHORT_NAMES, event, weight, problemNote, problem);
        checkIterator(query, 3, SHORT_NAMES, event, weight, problem, problemNote);
        // problemNote moves as its a child of problem
    }

    /**
     * Verifies that {@link ActHierarchyIterator} returns the expected acts, in the correct order.
     *
     * @param query      the query
     * @param maxDepth   the maximum depth to iterate to, or <tt>-1</tt> to not limit depth
     * @param shortNames the child act short names
     * @param acts       the expected acts
     */
    private void checkIterator(PatientHistoryQuery query, int maxDepth, String[] shortNames, Act... acts) {
        int index = 0;
        for (Act act : new ActHierarchyIterator<Act>(query, shortNames, maxDepth)) {
            assertEquals(acts[index++], act);
        }
        assertEquals(index, acts.length);
    }

    /**
     * Verifies that {@link ActHierarchyIterator} returns the expected acts, in the correct order.
     *
     * @param query      the query
     * @param maxDepth   the maximum depth to iterate to, or <tt>-1</tt> to not limit depth
     * @param expected   the expected no. of acts
     * @param shortNames the child act short names
     */
    private void checkIterator(PatientHistoryQuery query, int maxDepth, int expected, String... shortNames) {
        Iterable<Act> summary = new ActHierarchyIterator<Act>(query, shortNames, maxDepth);
        int acts = 0;
        Act event = null;
        for (Act act : summary) {
            if (event == null) {
                assertTrue(TypeHelper.isA(act, CLINICAL_EVENT));
            }
            if (TypeHelper.isA(act, CLINICAL_EVENT)) {
                event = act;
            } else {
                assertNotNull(event); // must have an event prior to an item
                assertTrue(TypeHelper.isA(act, shortNames));
            }
            ++acts;
        }
        assertEquals(expected, acts);
    }

    /**
     * Creates a new <em>act.patientClinicalEvent</em> for the supplied patient,
     * and adds child <em>act.patientClinicalProblem</em>,
     * <em>act.patientClinicalNote</em> and <em>act.patientWeight<em> acts.
     * The <em>act.patientClinicalProblem</em> also has an <em>act.patientClinicalProblem</em>.
     *
     * @param patient   the patient
     * @param startTime the act start time seed
     * @return a new event
     */
    private Act createEvent(Party patient, Date startTime) {
        Act eventNote = PatientTestHelper.createNote(startTime, patient);
        Act problemNote = PatientTestHelper.createNote(startTime, patient);
        Act problem = PatientTestHelper.createProblem(startTime, patient, problemNote);
        Act weight = PatientTestHelper.createWeight(startTime, patient, null);
        return PatientTestHelper.createEvent(startTime, patient, eventNote, problem, weight);
    }

}
