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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;


/**
 * Tests the {@link MedicalRecordRules} class when invoked by the
 * <em>archetypeService.remove.act.patientClinicalEvent.after.drl</em> '
 * rule. In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MedicalRecordRulesTestCase extends ArchetypeServiceTest {

    /**
     * The patient.
     */
    private Party _patient;

    /**
     * The clinician.
     */
    private User _clinician;


    /**
     * Verifies that deletion of an <em>act.patientClinicalEvent</em>
     * deletes all of the children.
     */
    public void testDeleteClinicalEvent() {
        Act event = createEvent();
        Act problem = createProblem();
        addActRelationship(event, problem,
                           "actRelationship.patientClinicalEventItem");
        save(event);
        save(problem);

        // make sure each of the objects can be retrieved
        assertNotNull(get(event.getObjectReference()));
        assertNotNull(get(problem.getObjectReference()));

        remove(event);  // remove should cascade to delete problem

        // make sure the event, but none of the child acts can be retrieved
        assertNull(get(event.getObjectReference()));
        assertNull(get(problem.getObjectReference()));
    }

    /**
     * Verifies that deletion of an <em>act.patientClinicalProblem</em>
     * doesn't affect its children.
     */
    public void testDeleteClinicalProblem() {
        Act event = createEvent();
        Act problem = createProblem();
        Act note = createNote();
        addActRelationship(event, problem,
                           "actRelationship.patientClinicalEventItem");
        addActRelationship(event, note,
                           "actRelationship.patientClinicalEventItem");
        addActRelationship(problem, note,
                           "actRelationship.patientClinicalProblemItem");
        save(event);
        save(problem);
        save(note);

        // make sure each of the objects can be retrieved
        assertNotNull(get(event.getObjectReference()));
        assertNotNull(get(problem.getObjectReference()));

        remove(problem);  // remove shouldn't cascade to delete note

        // make sure the all but the problem can be retrieved
        assertNotNull(get(event.getObjectReference()));
        assertNull(get(problem.getObjectReference()));
        assertNotNull(get(note.getObjectReference()));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        _clinician = TestHelper.createClinician(false);
        _patient = createPatient();
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @return a new act
     */
    protected Act createEvent() {
        Act act = createAct("act.patientClinicalEvent");
        addParticipation(act, _patient, "participation.patient");
        addParticipation(act, _clinician, "participation.clinician");
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     *
     * @return a new act
     */
    protected Act createProblem() {
        Act act = createAct("act.patientClinicalProblem");
        addParticipation(act, _patient, "participation.patient");
        addParticipation(act, _clinician, "participation.clinician");
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalNote</em>.
     *
     * @return a new act
     */
    protected Act createNote() {
        Act act = createAct("act.patientClinicalNote");
        addParticipation(act, _patient, "participation.patient");
        return act;
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
     * Helper to add a relationship between two acts.
     *
     * @param source    the source act
     * @param target    the target act
     * @param shortName the act relationship short name
     */
    protected void addActRelationship(Act source, Act target,
                                      String shortName) {
        ActRelationship relationship = (ActRelationship) create(shortName);
        assertNotNull(relationship);
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addActRelationship(relationship);
        target.addActRelationship(relationship);
    }

    /**
     * Adds a participation.
     *
     * @param act           the act to add to
     * @param entity        the participation entity             `
     * @param participation the participation short name
     */
    protected void addParticipation(Act act, Entity entity,
                                    String participation) {
        Participation p = (Participation) create(participation);
        assertNotNull(p);
        p.setAct(act.getObjectReference());
        p.setEntity(entity.getObjectReference());
        act.addParticipation(p);
    }

    /**
     * Creates a new patient.
     *
     * @return a new patient
     */
    protected Party createPatient() {
        Party patient = (Party) create("party.patientpet");
        assertNotNull(patient);
        return patient;
    }

}
