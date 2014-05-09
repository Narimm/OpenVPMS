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

package org.openvpms.web.workspace.patient.history;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_NOTE;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_PROBLEM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_WEIGHT;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link PatientHistoryIterator}.
 *
 * @author Tim Anderson
 */
public class PatientHistoryIteratorTestCase extends AbstractAppTest {

    /**
     * The short names to filter on.
     */
    private static final String[] SHORT_NAMES = new String[]{CLINICAL_PROBLEM, PATIENT_WEIGHT, CLINICAL_NOTE};

    /**
     * Verifies that when a note is linked to both an event and a child problem, it is returned after the problem.
     */
    @Test
    public void testIterator() {
        Party patient = TestHelper.createPatient(true);
        User clinician = TestHelper.createClinician();

        Act weight = PatientTestHelper.createWeight(getDatetime("2014-05-09 10:00:00"), patient, clinician);
        Act problemNote = PatientTestHelper.createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act problem = PatientTestHelper.createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician,
                                                      problemNote);

        Act event = PatientTestHelper.createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician,
                                                  weight, problemNote, problem);

        List<Act> acts = Arrays.asList(event);

        checkIterator(acts, new String[0], event);
        checkIterator(acts, new String[]{CLINICAL_PROBLEM, PATIENT_WEIGHT}, event, weight, problem);
        checkIterator(acts, SHORT_NAMES, event, weight, problem, problemNote);
    }


    /**
     * Verifies that {@link ActHierarchyIterator} returns the expected acts, in the correct order.
     *
     * @param events     the events
     * @param shortNames the child act short names
     * @param expected   the expected acts
     */
    private void checkIterator(List<Act> events, String[] shortNames, Act... expected) {
        int index = 0;
        for (Act act : new PatientHistoryIterator(events, shortNames, true)) {
            assertEquals(expected[index++], act);
        }
        assertEquals(expected.length, index);
    }
}
