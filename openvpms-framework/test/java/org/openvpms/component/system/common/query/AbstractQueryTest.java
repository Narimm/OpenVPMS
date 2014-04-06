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

import org.junit.Before;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Abstract base class for query tests.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml")
public class AbstractQueryTest extends AbstractArchetypeServiceTest {

    /**
     * The no. of acts.
     */
    protected static final int ACT_COUNT = 10;

    /**
     * The name assigned to each act in a test run.
     */
    private String name;


    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception {
        if (name == null) {
            name = "QueryTest" + System.currentTimeMillis();
            for (int i = 0; i < ACT_COUNT; ++i) {
                Act act = (Act) create("act.simple");
                ActBean bean = new ActBean(act);
                bean.setValue("startTime", new Date());
                bean.setValue("name", name);
                bean.save();
            }
        }
    }

    /**
     * Creates a new query for all 'act.simple' acts with a particular name.
     *
     * @return a new query
     */
    protected ArchetypeQuery createQuery() {
        ShortNameConstraint constraint = new ShortNameConstraint("act", "act.simple");
        ArchetypeQuery query = new ArchetypeQuery(constraint);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        query.add(new NodeConstraint("name", name));
        return query;
    }

    /**
     * Verfies that an iterator has the expected no. of elements and that
     * each element passes a user supplied check.
     *
     * @param iterator the iterator
     * @param check    the check
     */
    protected <T> void checkIterator(Iterator<T> iterator, Check<T> check) {
        int count = 0;
        while (iterator.hasNext()) {
            T object = iterator.next();
            assertNotNull(object);
            check.check(object);
            ++count;
        }
        assertEquals(ACT_COUNT, count);
    }

    interface Check<T> {

        void check(T object);
    }
}
