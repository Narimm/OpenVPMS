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

import org.apache.commons.collections4.ComparatorUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSortConstraint;

import java.util.Comparator;
import java.util.Date;

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
     * Tests the {@link QueryHelper#getPage(ArchetypeQuery, int, long, Comparable, String, Comparator)} method.
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
        query1.add(new NodeSortConstraint("startTime"));
        query1.add(new NodeSortConstraint("id"));

        Comparator<Date> comparator = new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return DateRules.compareTo(o1, o2);
            }
        };

        checkPage(0, query1, act1, comparator);
        checkPage(0, query1, act2, comparator);
        checkPage(1, query1, act3, comparator);
        checkPage(1, query1, act4, comparator);
        checkPage(2, query1, act5, comparator);

        // check query on descending start time. Also need to reverse the comparator
        ArchetypeQuery query2 = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT);
        query2.add(new ParticipantConstraint("patient", PatientArchetypes.PATIENT_PARTICIPATION, patient));
        query2.add(new NodeSortConstraint("startTime", false));
        query2.add(new NodeSortConstraint("id"));

        Comparator<Date> reverse = ComparatorUtils.reversedComparator(comparator);

        checkPage(0, query2, act5, reverse);
        checkPage(0, query2, act4, reverse);
        checkPage(1, query2, act3, reverse);
        checkPage(1, query2, act2, reverse);
        checkPage(2, query2, act1, reverse);
    }

    /**
     * Verifies that an act appears on the expected page, when the page size is 2.
     *
     * @param expected   the expected page
     * @param query      the query
     * @param act        the act to find
     * @param comparator the comparator
     */
    private void checkPage(int expected, ArchetypeQuery query, Act act, Comparator<Date> comparator) {
        int page = QueryHelper.getPage(query, 2, act.getId(), act.getActivityStartTime(), "startTime", comparator);
        assertEquals(expected, page);
    }

}
