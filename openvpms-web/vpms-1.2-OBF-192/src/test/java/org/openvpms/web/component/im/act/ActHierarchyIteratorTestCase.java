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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.patient.mr.PatientSummaryQuery;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.test.TestHelper;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;


/**
 * Tests the {@link ActHierarchyIterator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActHierarchyIteratorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link ActHierarchyIterator} class.
     */
    public void test() {
        Party patient = TestHelper.createPatient(true);

        // create some events for a patient. Each event has a problem, note
        // and weight
        createEvent(patient, Timestamp.valueOf("2007-02-01 10:30:00"));
        createEvent(patient, Timestamp.valueOf("2007-02-10 11:45:00"));
        createEvent(patient, Timestamp.valueOf("2007-01-10 11:45:00"));

        // query the events
        PatientSummaryQuery query = new PatientSummaryQuery(patient);
        query.getComponent();

        checkIterator(query, 3); // should only return events
        checkIterator(query, 6, "act.patientClinicalProblem");
        checkIterator(query, 9, "act.patientClinicalProblem",
                      "act.patientClinicalNote");
        checkIterator(query, 12, "act.patientClinicalNote",
                      "act.patientClinicalProblem", "act.patientWeight");

    }

    /**
     * Verifies that {@link ActHierarchyIterator} returns the expected acts,
     * in the correct order.
     *
     * @param query      the query
     * @param expected   the expected no. of acts
     * @param shortNames the child act short names
     */
    private void checkIterator(PatientSummaryQuery query, int expected,
                               String ... shortNames) {
        Iterable<Act> summary = new ActHierarchyIterator<Act>(query,
                                                              shortNames);
        int acts = 0;
        Date lastItemTime = null;
        Act event = null;
        for (Act act : summary) {
            if (event == null) {
                assertTrue(TypeHelper.isA(act, "act.patientClinicalEvent"));
            }
            if (TypeHelper.isA(act, "act.patientClinicalEvent")) {
                event = act;
                lastItemTime = null;
            } else {
                assertNotNull(event); // must have an event prior to an item
                assertTrue(TypeHelper.isA(act, shortNames));
                if (lastItemTime != null) {
                    assertTrue(lastItemTime.compareTo(
                            act.getActivityStartTime()) <= 0);
                }
                lastItemTime = act.getActivityStartTime();
            }
            ++acts;
        }
        assertEquals(expected, acts);
    }

    /**
     * Creates a new <em>act.patientClinicalEvent</em> for the supplied patient,
     * and adds child <em>act.patientClinicalProblem</em>,
     * <em>act.patientClinicalNote</em> and <em>act.patientWeight<em> acts.
     *
     * @param patient   the patient
     * @param startTime the act start time seed
     * @return a new event
     */
    private Act createEvent(Party patient, Date startTime) {
        Act event = createAct("act.patientClinicalEvent", patient, startTime);
        Act note = createAct("act.patientClinicalNote", patient, startTime);
        Act problem = createAct("act.patientClinicalProblem", patient,
                                startTime);
        Act weight = createAct("act.patientWeight", patient, startTime);
        ActBean bean = new ActBean(event);
        bean.addRelationship("actRelationship.patientClinicalEventItem", note);
        bean.addRelationship("actRelationship.patientClinicalEventItem",
                             problem);
        bean.addRelationship("actRelationship.patientClinicalEventItem",
                             weight);
        TestHelper.save(event, note, problem, weight);
        bean.save();
        return event;
    }

    /**
     * Creates a new act.
     *
     * @param shortName the act short name
     * @param patient   the patient to associate
     * @param startTime the start time seed
     * @return a new act
     */
    private Act createAct(String shortName, Party patient, Date startTime) {
        Act act = (Act) TestHelper.create(shortName);
        act.setActivityStartTime(createRandTime(startTime));
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        return act;
    }

    /**
     * Randomises the seconds of a time.
     *
     * @param startTime the start time
     * @return the start time with randomised seconds component
     */
    private Date createRandTime(Date startTime) {
        int millis = new Random().nextInt(60) * 1000;
        return new Date(startTime.getTime() + millis);
    }

}
