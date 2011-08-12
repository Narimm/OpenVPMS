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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.estimation;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;


/**
 * Tests the <em>act.customerEstimationItem</em> archetype.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EstimationItemTestCase extends ArchetypeServiceTest {

    /**
     * Tests the derivation of the <em>lowTotal</em>.
     */
    @Test
    public void testCalculateLowTotal() {
        checkCalculate("lowUnitPrice", "lowQty", "lowTotal");
    }

    /**
     * Tests the derivation of the <em>highTotal</em>.
     */
    @Test
    public void testCalculateHighTotal() {
        checkCalculate("highUnitPrice", "highQty", "highTotal");
    }

    /**
     * Tests the derivation of a total node.
     *
     * @param unitPriceNode the unit price node name
     * @param quantityNode  the quantity node name
     * @param totalNode     the total node name
     */
    private void checkCalculate(String unitPriceNode, String quantityNode, String totalNode) {
        Act item = (Act) create(EstimationArchetypes.ESTIMATION_ITEM);

        ActBean bean = new ActBean(item);
        checkEquals(BigDecimal.ZERO, bean.getBigDecimal(totalNode));

        // populate the values
        bean.setValue("fixedPrice", 10);
        bean.setValue("discount", 5);
        bean.setValue(unitPriceNode, new BigDecimal("0.50"));
        bean.setValue(quantityNode, 2);

        BigDecimal expected1 = new BigDecimal(6);
        checkEquals(expected1, bean.getBigDecimal(totalNode));

        // now ensure it is rounded to 2 decimal places
        bean.setValue(unitPriceNode, new BigDecimal("0.514"));
        BigDecimal expected2 = new BigDecimal("6.03");
        checkEquals(expected2, bean.getBigDecimal(totalNode));

        // check null handling
        bean.setValue("fixedPrice", null);
        bean.setValue("discount", null);
        bean.setValue(unitPriceNode, null);
        bean.setValue(quantityNode, null);
        checkEquals(BigDecimal.ZERO, bean.getBigDecimal(totalNode));
    }

}