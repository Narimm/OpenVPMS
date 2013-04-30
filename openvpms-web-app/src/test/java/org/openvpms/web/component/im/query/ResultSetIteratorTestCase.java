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

package org.openvpms.web.component.im.query;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link ResultSetIterator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ResultSetIteratorTestCase extends AbstractResultSetTest {

    /**
     * The customer.
     */
    private Party customer;


    /**
     * Tests the behaviour of iterating an empty result set.
     */
    @Test
    public void testEmpty() {
        ActResultSet<Act> set = createResultSet();
        ResultSetIterator<Act> iterator = new ResultSetIterator<Act>(set);
        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException expected) {
            // expected behaviour
        }
    }

    /**
     * Tests iteration.
     */
    @Test
    public void testIteration() {
        Party patient = TestHelper.createPatient(true);
        Product product = TestHelper.createProduct();

        final int count = 10;
        for (int i = 0; i < count; ++i) {
            createEstimation(customer, patient, product);
        }

        ActResultSet<Act> set = createResultSet();
        ResultSetIterator<Act> iterator = new ResultSetIterator<Act>(set);
        int acts = 0;
        while (iterator.hasNext()) {
            acts++;
            Act act = iterator.next();
            assertNotNull(act);
            assertTrue(TypeHelper.isA(act, "act.customerEstimation"));
        }
        assertEquals(count, acts);
    }

    private ActResultSet<Act> createResultSet() {
        ShortNameConstraint archetypes = new ShortNameConstraint(
            "act.customerEstimation", true, true);
        Date from = null;       // query all dates
        Date to = null;
        String[] statuses = {}; // query all statuses
        SortConstraint[] sort = null;
        ParticipantConstraint participant = new ParticipantConstraint(
            "customer", "participation.customer",
            customer.getObjectReference());
        return new ActResultSet<Act>(archetypes, participant,
                                     from, to, statuses,
                                     5, sort);
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer(true);
    }
}
