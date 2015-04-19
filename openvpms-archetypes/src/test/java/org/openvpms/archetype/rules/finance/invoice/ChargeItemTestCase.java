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

package org.openvpms.archetype.rules.finance.invoice;

import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;


/**
 * Tests the <em>act.customerAccountInvoiceItem</em>, <em>act.customerAccountCreditItem</em> and
 * <em>act.customerAccountCounterItem</em> archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ChargeItemTestCase extends ArchetypeServiceTest {

    /**
     * Tests the derivation of the invoice item total.
     */
    @Test
    public void testCalculateInvoiceItemTotal() {
        checkCalculate(CustomerAccountArchetypes.INVOICE_ITEM);
    }

    /**
     * Tests the derivation of the counter item total.
     */
    @Test
    public void testCalculateCounterItemTotal() {
        checkCalculate(CustomerAccountArchetypes.COUNTER_ITEM);
    }

    /**
     * Tests the derivation of the counter item total.
     */
    @Test
    public void testCalculateCreditItemTotal() {
        checkCalculate(CustomerAccountArchetypes.CREDIT_ITEM);
    }

    /**
     * Tests the derivation of a total node.
     *
     * @param itemShortName the charge item short name
     */
    private void checkCalculate(String itemShortName) {
        Act item = (Act) create(itemShortName);

        ActBean bean = new ActBean(item);
        checkEquals(BigDecimal.ZERO, bean.getBigDecimal("total"));

        // populate the values
        bean.setValue("fixedPrice", 10);
        bean.setValue("discount", 5);
        bean.setValue("unitPrice", new BigDecimal("0.50"));
        bean.setValue("quantity", 2);

        BigDecimal expected1 = new BigDecimal(6);
        checkEquals(expected1, bean.getBigDecimal("total"));

        // now ensure it is rounded to 2 decimal places
        bean.setValue("unitPrice", new BigDecimal("0.514"));
        BigDecimal expected2 = new BigDecimal("6.03");
        checkEquals(expected2, bean.getBigDecimal("total"));

        // check null handling
        bean.setValue("fixedPrice", null);
        bean.setValue("discount", null);
        bean.setValue("unitPrice", null);
        bean.setValue("quantity", null);
        checkEquals(BigDecimal.ZERO, bean.getBigDecimal("total"));
    }

}