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
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ActHelper} class.
 *
 * @author Tim Anderson
 */
public class ActHelperTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ActHelper#getTargetActs(Collection)} method.
     */
    @Test
    public void testGetTargetActs() {
        Party patient = TestHelper.createPatient();
        Act event = createAct(PatientArchetypes.CLINICAL_EVENT, patient);
        Act note = createAct(PatientArchetypes.CLINICAL_NOTE, patient);
        Act problem = createProblem(patient);
        Act weight = createAct(PatientArchetypes.PATIENT_WEIGHT, patient);

        List<ActRelationship> relationships = new ArrayList<ActRelationship>();
        ActBean bean = new ActBean(event);
        relationships.add(bean.addNodeRelationship("items", note));
        relationships.add(bean.addNodeRelationship("items", problem));
        relationships.add(bean.addNodeRelationship("items", weight));
        save(event, note, problem, weight);

        List<Act> targetActs = ActHelper.getTargetActs(relationships);
        assertEquals(3, targetActs.size());
        assertTrue(targetActs.contains(note));
        assertTrue(targetActs.contains(problem));
        assertTrue(targetActs.contains(weight));
    }

    /**
     * Tests the {@link ActHelper#getActs(Collection) method}
     */
    @Test
    public void testGetActs() {
        Party patient = TestHelper.createPatient();
        Act note = createAct(PatientArchetypes.CLINICAL_NOTE, patient);
        Act problem = createProblem(patient);
        Act weight = createAct(PatientArchetypes.PATIENT_WEIGHT, patient);
        save(note, problem, weight);

        List<IMObjectReference> refs = new ArrayList<IMObjectReference>();
        refs.add(note.getObjectReference());
        refs.add(problem.getObjectReference());
        refs.add(weight.getObjectReference());

        List<Act> acts = ActHelper.getActs(refs);
        assertEquals(3, acts.size());
        assertTrue(acts.contains(note));
        assertTrue(acts.contains(problem));
        assertTrue(acts.contains(weight));
    }

    /**
     * Helper to create a new <em>act.patientClinicalProblem</em>.
     *
     * @param patient the patient
     * @return a new problem act
     */
    private Act createProblem(Party patient) {
        Act act = createAct(PatientArchetypes.CLINICAL_PROBLEM, patient);
        Lookup lookup = TestHelper.getLookup("lookup.diagnosis", "HEART_MURMUR");
        act.setReason(lookup.getCode());
        return act;
    }

    /**
     * Helper to create an act linked to a patient.
     *
     * @param shortName the archetype short name
     * @param patient   the patient
     * @return a new act
     */
    private Act createAct(String shortName, Party patient) {
        Act result = (Act) create(shortName);
        ActBean bean = new ActBean(result);
        bean.addNodeParticipation("patient", patient);
        return result;
    }
}
