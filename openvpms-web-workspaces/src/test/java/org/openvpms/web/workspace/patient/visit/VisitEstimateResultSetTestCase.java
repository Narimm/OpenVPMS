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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import org.junit.Test;
import org.openvpms.archetype.rules.finance.estimate.EstimateTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSetIterator;

import java.math.BigDecimal;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.customer.CustomerArchetypes.CUSTOMER_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes.ESTIMATE;

/**
 * Tests the {@link VisitEstimateResultSet} class.
 *
 * @author Tim Anderson
 */
public class VisitEstimateResultSetTestCase extends ArchetypeServiceTest {

    /**
     * Verifies that estimates are only returned if they only contain items for the specified patient.
     */
    @Test
    public void testResultSet() {
        User author = TestHelper.createUser();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Party customer = TestHelper.createCustomer();
        ShortNameConstraint shortNames = new ShortNameConstraint(ESTIMATE);
        ParticipantConstraint customerConstraint = new ParticipantConstraint("customer", CUSTOMER_PARTICIPATION,
                                                                             customer);
        VisitEstimateResultSet set = new VisitEstimateResultSet(patient1, shortNames, customerConstraint, null, null,
                                                                new String[0], 20, null);
        Iterator<Act> iterator = new ResultSetIterator<Act>(set);
        assertFalse(iterator.hasNext());

        // create estimate1, with acts for patient1, and patient2
        Act item1 = EstimateTestHelper.createEstimateItem(patient1, product1, author, BigDecimal.ONE);
        Act item2 = EstimateTestHelper.createEstimateItem(patient2, product2, author, BigDecimal.ONE);
        Act estimate1 = EstimateTestHelper.createEstimate(customer, author, item1, item2);

        // create estimate2, with acts for patient1 only
        Act item3 = EstimateTestHelper.createEstimateItem(patient1, product1, author, BigDecimal.ONE);
        Act item4 = EstimateTestHelper.createEstimateItem(patient1, product2, author, BigDecimal.ONE);
        Act estimate2 = EstimateTestHelper.createEstimate(customer, author, item3, item4);

        // create estimate3, with acts for patient2 only
        Act item5 = EstimateTestHelper.createEstimateItem(patient2, product1, author, BigDecimal.ONE);
        Act item6 = EstimateTestHelper.createEstimateItem(patient2, product2, author, BigDecimal.ONE);
        Act estimate3 = EstimateTestHelper.createEstimate(customer, author, item5, item6);

        save(item1, item2, item3, item4, item4, item5, item6, estimate1, estimate2, estimate3);

        // verify the result set only returns estimate2
        set.reset();
        iterator = new ResultSetIterator<Act>(set);
        assertTrue(iterator.hasNext());
        assertEquals(estimate2, iterator.next());

        assertFalse(iterator.hasNext());
    }
}
