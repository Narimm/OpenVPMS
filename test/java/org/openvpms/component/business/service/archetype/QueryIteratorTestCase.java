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

package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.NodeSetQueryIterator;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.QueryIterator;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;


/**
 * Tests the {@link QueryIterator} classes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-04 06:59:40Z $
 */
public class QueryIteratorTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The no. of acts.
     */
    private final int actCount = 10;

    /**
     * The name assigned to each act in a test run.
     */
    private String name;


    /**
     * Tests the {@link IMObjectQueryIterator}.
     */
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
     * Tests the {@link NodeSetQueryIterator}.
     */
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

    /*
    * (non-Javadoc)
    *
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
    */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /**
     * Subclasses can override this method in place of the
     * <code>setUp()</code> method, which is final in this class.
     * This implementation does nothing.
     *
     * @throws Exception simply let any exception propagate
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        if (name == null) {
            name = "QueryIteratorTestCase" + System.currentTimeMillis();
            for (int i = 0; i < actCount; ++i) {
                Act act = (Act) service.create("act.simple");
                assertNotNull(act);
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
    private ArchetypeQuery createQuery() {
        ShortNameConstraint constraint = new ShortNameConstraint("act",
                                                                 "act.simple");
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
    private <T> void checkIterator(Iterator<T> iterator, Check<T> check) {
        int count = 0;
        while (iterator.hasNext()) {
            T object = iterator.next();
            assertNotNull(object);
            check.check(object);
            ++count;
        }
        assertEquals(actCount, count);
    }


    interface Check<T> {
        void check(T object);
    }

}
