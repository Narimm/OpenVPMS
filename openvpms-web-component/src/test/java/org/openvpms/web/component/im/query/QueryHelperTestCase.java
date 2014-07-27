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

package org.openvpms.web.component.im.query;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSortConstraint;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createEvent;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Tests the {@link QueryHelper} class.
 *
 * @author Tim Anderson
 */
public class QueryHelperTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link QueryHelper#getPage(IMObject, ArchetypeQuery, int, String, boolean, Comparator)} method.
     */
    @Test
    public void testGetPage() {
        Party patient = TestHelper.createPatient();

        Act act1 = createEvent(getDate("2014-07-01"), patient);
        Act act2 = createEvent(getDate("2014-07-02"), patient);
        Act act3 = createEvent(getDate("2014-07-03"), patient);
        Act act4 = createEvent(getDate("2014-07-04"), patient);
        Act act5 = createEvent(getDate("2014-07-05"), patient);

        // check query on ascending start time
        ArchetypeQuery query1 = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT);
        query1.add(new ParticipantConstraint("patient", PatientArchetypes.PATIENT_PARTICIPATION, patient));

        checkPage(0, query1, act1, true);
        checkPage(0, query1, act2, true);
        checkPage(1, query1, act3, true);
        checkPage(1, query1, act4, true);
        checkPage(2, query1, act5, true);

        // check query on descending start time. Also need to reverse the comparator
        ArchetypeQuery query2 = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT);
        query2.add(new ParticipantConstraint("patient", PatientArchetypes.PATIENT_PARTICIPATION, patient));
        query2.add(new NodeSortConstraint("startTime", false));
        query2.add(new NodeSortConstraint("id"));

        checkPage(0, query2, act5, false);
        checkPage(0, query2, act4, false);
        checkPage(1, query2, act3, false);
        checkPage(1, query2, act2, false);
        checkPage(2, query2, act1, false);
    }

    /**
     * Verifies that an act appears on the expected page, when the page size is 2.
     *
     * @param expected  the expected page
     * @param query     the query
     * @param act       the act to find
     * @param ascending if {@code true} sort on {@code node} in ascending order, else use descending order
     */
    private void checkPage(int expected, ArchetypeQuery query, Act act, boolean ascending) {
        int page = QueryHelper.getPage(act, query, 2, "startTime", ascending, PageLocator.DATE_COMPARATOR);
        assertEquals(expected, page);
    }

}
