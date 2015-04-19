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

package org.openvpms.component.system.common.query;

import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertNotNull;


/**
 * Tests the {@link QueryIterator} classes.
 *
 * @author Tim Anderson
 */
public class QueryIteratorTestCase extends AbstractQueryTest {

    /**
     * Tests the {@link IMObjectQueryIterator}.
     */
    @Test
    public void testIMObjectQueryIterator() {
        ArchetypeQuery query = createQuery();

        Check<Act> check = new Check<Act>() {
            public void check(Act object) {
                // no-op
            }
        };
        query.setMaxResults(1);
        QueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        checkIterator(iterator, check);

        query.setFirstResult(0); // reset
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        iterator = new IMObjectQueryIterator<Act>(query);
        checkIterator(iterator, check);
    }

    /**
     * Verifies that the archetype service is only accessed as many times as is necessary.
     */
    @Test
    public void testArchetypeServiceCalls() {
        checkArchetypeServiceCalls(IArchetypeQuery.ALL_RESULTS, 1);
        checkArchetypeServiceCalls(ACT_COUNT, 2); // first 1 page is full, last page is empty
        checkArchetypeServiceCalls(ACT_COUNT / 2, 3); // first 2 pages are full, last page is empty
        checkArchetypeServiceCalls(ACT_COUNT + 1, 1); // first page not completely full, so should invoke 1 call
    }

    /**
     * Tests the {@link NodeSetQueryIterator}.
     */
    @Test
    public void testNodeSetQueryIterator() {
        ArchetypeQuery query = createQuery();
        Check<NodeSet> check = new Check<NodeSet>() {
            public void check(NodeSet set) {
                Date startTime = (Date) set.get("startTime");
                assertNotNull(startTime);
            }
        };
        QueryIterator<NodeSet> iterator
                = new NodeSetQueryIterator(query, Arrays.asList("startTime"));
        checkIterator(iterator, check);
    }

    /**
     * Tests the {@link ObjectSetQueryIterator}.
     */
    @Test
    public void testObjectSetQueryIterator() {
        ArchetypeQuery query = createQuery();
        query.add(new NodeSelectConstraint("act.startTime"));

        Check<ObjectSet> check = new Check<ObjectSet>() {
            public void check(ObjectSet set) {
                Date startTime = (Date) set.get("act.startTime");
                assertNotNull(startTime);
            }
        };
        QueryIterator<ObjectSet> iterator = new ObjectSetQueryIterator(query);
        checkIterator(iterator, check);
    }

    /**
     * Verifies that the archetype service is only accessed as many times as is necessary.
     *
     * @param maxResults    the maximum no. of results to query
     * @param expectedCalls the expected no. of archetype service calls
     */
    private void checkArchetypeServiceCalls(int maxResults, int expectedCalls) {
        IArchetypeService service = Mockito.spy(getArchetypeService());
        ArchetypeQuery query = createQuery();
        query.setMaxResults(maxResults);
        QueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(service, query);
        Check<Act> check = new Check<Act>() {
            public void check(Act object) {
                // no-op
            }
        };
        checkIterator(iterator, check);

        Mockito.verify(service, Mockito.times(expectedCalls)).get(query);
    }

}
