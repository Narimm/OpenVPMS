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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link MedicalRecordRules} class.
 * Note: this requires the archetype service to be configured to trigger the
 * <em>archetypeService.remove.act.patientClinicalEvent.after.drl</em> rule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Verifies that deletion of an <em>act.patientClinicalEvent</em>
     * deletes all of the children.
     */
    public void testDeleteClinicalEvent() {
        Act event = createEvent();
        Act problem = createProblem();
        save(problem);

        ActBean bean = new ActBean(event);
        bean.addRelationship("actRelationship.patientClinicalEventItem",
                             problem);
        save(event);

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
        ActBean eventBean = new ActBean(event);
        eventBean.addRelationship("actRelationship.patientClinicalEventItem",
                                  problem);
        eventBean.addRelationship("actRelationship.patientClinicalEventItem",
                                  note);
        ActBean problemBean = new ActBean(problem);
        problemBean.addRelationship(
                "actRelationship.patientClinicalProblemItem",
                note);
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
     * Tests the {@link MedicalRecordRules#getEvent} method.
     */
    public void testGetEvent() {
        Date jan1 = getDate("2007-1-1");
        Date jan2 = getDate("2007-1-2");
        Date jan3 = getDate("2007-1-3 10:43:55");

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
        checkEvent(getDate("2007-1-3"), event2);
        // note that the time component is zero, but still picks up event2,
        // despite the event being created after 00:00:00. This is required
        // as the time component of startTime is not supplied consistently -
        // In some cases, it is present, in others it is 00:00:00.

        checkEvent(jan2, event1);
    }

    /**
     * Tests the {@link MedicalRecordRules#addToEvents} method.
     */
    public void testAddToEvents() {
        Date date = getDate("2007-04-05");
        Party patient2 = TestHelper.createPatient();
        Act med1 = createMedication(patient);
        Act med2 = createMedication(patient);
        Act med3 = createMedication(patient2);
        Act med4 = createMedication(patient2);

        save(med1);
        save(med2);
        save(med3);
        save(med4);

        List<Act> acts = Arrays.asList(med1, med2, med3, med4);

        Act event1 = createEvent(date);
        save(event1);
        rules.addToEvents(acts, date);

        event1 = (Act) get(event1);
        checkContains(event1, med1, med2);

        Act event2 = rules.getEvent(patient2, date);
        assertNotNull(event2);
        checkContains(event2, med3, med4);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        clinician = TestHelper.createClinician();
        patient = TestHelper.createPatient();
        rules = new MedicalRecordRules();
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @return a new act
     */
    protected Act createEvent() {
        Act act = createAct("act.patientClinicalEvent");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        bean.addParticipation("participation.clinician", clinician);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @param startTime the start time
     * @return a new act
     */
    protected Act createEvent(Date startTime) {
        Act act = createEvent();
        act.setActivityStartTime(startTime);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     *
     * @return a new act
     */
    protected Act createProblem() {
        Act act = createAct("act.patientClinicalProblem");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        bean.addParticipation("participation.clinician", clinician);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalNote</em>.
     *
     * @return a new act
     */
    protected Act createNote() {
        Act act = createAct("act.patientClinicalNote");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        return act;
    }

    /**
     * Helper to create an <em>act.patientMedication</em>.
     *
     * @param patient the patient
     * @return a new act
     */
    protected Act createMedication(Party patient) {
        Act act = createAct("act.patientMedication");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        Product product = TestHelper.createProduct();
        bean.addParticipation("participation.product", product);
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
    private void checkContains(Act event, Act ... acts) {
        ActBean bean = new ActBean(event);
        List<Act> items = bean.getActsForNode("items");
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
     * Returns a date for a date string.
     *
     * @param date the stringified date
     * @return the date
     */
    private Date getDate(String date) {
        if (date.contains(":")) {
            return Timestamp.valueOf(date);
        } else {
            return java.sql.Date.valueOf(date);
        }
    }

}
