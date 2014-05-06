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

package org.openvpms.web.workspace.patient;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link org.openvpms.web.workspace.patient.PatientMedicalRecordLinker} class.
 *
 * @author Tim Anderson
 */
public class PatientMedicalRecordLinkerTestCase extends AbstractAppTest {

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * Tests linking a note to an event.
     */
    @Test
    public void testLinkNote() {
        Act event = createEvent();
        Act note = createNote();

        PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, note);
        linker.run();

        event = get(event);
        note = get(note);

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, note));
    }

    /**
     * Tests linking a problem to an event.
     * <p/>
     * The problem has a note which should also be linked to the event
     */
    @Test
    public void testLinkProblem() {
        Act event = createEvent();
        Act problem = createProblem();
        Act note = createNote();

        ActBean bean = new ActBean(problem);
        bean.addNodeRelationship("items", note);
        save(problem, note);
        PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, problem);
        linker.run();

        event = get(event);
        problem = get(problem);
        note = get(note);

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, problem));
        assertTrue(eventBean.hasRelationship(PatientArchetypes.CLINICAL_EVENT_ITEM, note));
        ActBean problemBean = new ActBean(problem);
        assertTrue(problemBean.hasRelationship(PatientArchetypes.CLINICAL_PROBLEM_ITEM, note));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        patient = TestHelper.createPatient(true);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @return a new act
     */
    protected Act createEvent() {
        Act act = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.save();
        return act;
    }


    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     *
     * @return a new act
     */
    protected Act createProblem() {
        Act act = (Act) create(PatientArchetypes.CLINICAL_PROBLEM);
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        act.setReason(TestHelper.getLookup("lookup.diagnosis", "HEART_MURMUR").getCode());
        bean.save();
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalNote</em>.
     *
     * @return a new act
     */
    protected Act createNote() {
        Act act = (Act) create(PatientArchetypes.CLINICAL_NOTE);
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.save();
        return act;
    }

}
