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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.problem;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_ADDENDUM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_NOTE;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_WEIGHT;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.addAddendum;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createAddendum;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createEvent;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createNote;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createProblem;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link ProblemHierarchyIterator}.
 *
 * @author Tim Anderson
 */
public class ProblemHierarchyIteratorTestCase extends ArchetypeServiceTest {

    /**
     * The short names to filter on.
     */
    private static final String[] SHORT_NAMES = new String[]{PATIENT_WEIGHT, CLINICAL_NOTE, CLINICAL_ADDENDUM};

    /**
     * Tests iteration.
     */
    @Test
    public void testIterator() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();

        Act note1 = createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act note2 = createNote(getDatetime("2014-05-09 10:10:00"), patient, clinician);
        Act problem = createProblem(getDatetime("2014-05-09 10:00:00"), patient, clinician, note1, note2);
        Act event = PatientTestHelper.createEvent(getDatetime("2014-05-09 09:00:00"), patient, clinician, problem,
                                                  note1, note2);

        List<Act> acts = Collections.singletonList(problem);
        check(acts, SHORT_NAMES, true, problem, event, note1, note2);
        check(acts, SHORT_NAMES, false, problem, event, note2, note1);
    }

    /**
     * Verifies that addendum records linked to a note appear in the correct order when also linked to a problem and
     * event.
     */
    @Test
    public void testAddendumLinkedToProblem() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();

        Act note = createNote(getDatetime("2016-03-20 10:00:05"), patient, clinician);

        Act addendum1 = createAddendum(getDatetime("2016-03-27 11:00:00"), patient, clinician);
        Act addendum2 = createAddendum(getDatetime("2016-03-27 12:00:00"), patient, clinician);
        addAddendum(note, addendum1);
        addAddendum(note, addendum2);

        Act problem = createProblem(getDatetime("2016-03-20 10:05:00"), patient, clinician, note, addendum1, addendum2);
        Act event = createEvent(getDatetime("2016-03-20 10:00:00"), patient, clinician, note, addendum1, addendum2,
                                problem);

        List<Act> acts = Collections.singletonList(problem);
        check(acts, SHORT_NAMES, true, problem, event, note, addendum1, addendum2);
        check(acts, SHORT_NAMES, false, problem, event, note, addendum1, addendum2);
    }

    /**
     * Verifies that {@link ProblemHierarchyIterator} returns the expected acts, in the correct order.
     *
     * @param problems      the events
     * @param shortNames    the child act short names
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     * @param expected      the expected acts
     */
    private void check(List<Act> problems, String[] shortNames, boolean sortAscending, Act... expected) {
        int index = 0;
        ProblemFilter filter = new ProblemFilter(shortNames, null, sortAscending);
        for (Act act : new ProblemHierarchyIterator(problems, filter)) {
            assertEquals(expected[index++], act);
        }
        assertEquals(expected.length, index);
    }

}
