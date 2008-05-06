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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;


/**
 * Tests the {@link OrderRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private OrderRules rules;


    /**
     * Tests the {@link OrderRules#getDeliveryStatus(FinancialAct)} method.
     */
    public void testGetDeliveryStatus() {
        BigDecimal two = new BigDecimal("2.0");
        BigDecimal three = new BigDecimal("3.0");

        FinancialAct act = (FinancialAct) create("act.supplierOrderItem");
        assertEquals(DeliveryStatus.PENDING, rules.getDeliveryStatus(act));

        checkDeliveryStatus(act, three, ZERO, ZERO, DeliveryStatus.PENDING);
        checkDeliveryStatus(act, three, three, ZERO, DeliveryStatus.FULL);
        checkDeliveryStatus(act, three, two, ZERO, DeliveryStatus.PART);
        checkDeliveryStatus(act, three, two, ONE, DeliveryStatus.FULL);
        checkDeliveryStatus(act, three, ZERO, three, DeliveryStatus.FULL);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new OrderRules(getArchetypeService());
    }

    /**
     * Verifies the delivery status matches that expected for the supplied
     * values.
     *
     * @param quantity  the quantity
     * @param received  the received quantity
     * @param cancelled the cancelled quantity
     * @param expected  the expected delivery status
     */
    private void checkDeliveryStatus(FinancialAct act,
                                     BigDecimal quantity, BigDecimal received,
                                     BigDecimal cancelled,
                                     DeliveryStatus expected) {
        ActBean bean = new ActBean(act);
        bean.setValue("quantity", quantity);
        bean.setValue("receivedQuantity", received);
        bean.setValue("cancelledQuantity", cancelled);
        assertEquals(expected, rules.getDeliveryStatus(act));
    }

}
