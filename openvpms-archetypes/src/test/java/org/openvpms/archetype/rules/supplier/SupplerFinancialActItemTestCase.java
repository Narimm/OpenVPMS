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

package org.openvpms.archetype.rules.supplier;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;


/**
 * Tests the <em>act.supplierAccountInvoiceItem</em>, <em>act.supplierAccountCreditItem<em>
 * <em>act.supplierOrderItem</em>, <em>act.supplierDeliveryItem</em> and <em>act.supplierReturnItem</em> archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SupplerFinancialActItemTestCase extends ArchetypeServiceTest {

    /**
     * Tests the derivation of the <em>act.supplierAccountInvoiceItem</em> total node.
     */
    @Test
    public void testCalculateInvoiceItemTotal() {
        checkCalculate(SupplierArchetypes.INVOICE_ITEM);
    }

    /**
     * Tests the derivation of the <em>act.supplierAccountCreditItem</em> total node.
     */
    @Test
    public void testCalculateCreditItemTotal() {
        checkCalculate(SupplierArchetypes.CREDIT_ITEM);
    }


    /**
     * Tests the derivation of the <em>act.supplierOrderItem</em> total node.
     */
    @Test
    public void testCalculateOrderItemTotal() {
        checkCalculate(SupplierArchetypes.ORDER_ITEM);
    }

    /**
     * Tests the derivation of the <em>act.supplierDeliveryItem</em> total node.
     */
    @Test
    public void testCalculateDeliveryItemTotal() {
        checkCalculate(SupplierArchetypes.DELIVERY_ITEM);
    }

    /**
     * Tests the derivation of the <em>act.supplierReturnItem</em> total node.
     */
    @Test
    public void testCalculateReturnItemTotal() {
        checkCalculate(SupplierArchetypes.RETURN_ITEM);
    }

    /**
     * Tests the derivation of a total node.
     *
     * @param itemShortName the account item short name
     */
    private void checkCalculate(String itemShortName) {
        Act item = (Act) create(itemShortName);

        ActBean bean = new ActBean(item);
        checkEquals(BigDecimal.ZERO, bean.getBigDecimal("total"));

        // populate the values
        bean.setValue("quantity", 10);
        bean.setValue("unitPrice", new BigDecimal("0.50"));
        bean.setValue("tax", new BigDecimal("0.50"));

        BigDecimal expected1 = new BigDecimal("5.50");
        checkEquals(expected1, bean.getBigDecimal("total"));

        // now ensure it is rounded to 2 decimal places
        bean.setValue("unitPrice", new BigDecimal("0.514"));
        BigDecimal expected2 = new BigDecimal("5.64");
        checkEquals(expected2, bean.getBigDecimal("total"));

        // check null handling
        bean.setValue("quantity", null);
        bean.setValue("unitPrice", null);
        bean.setValue("tax", null);
        checkEquals(BigDecimal.ZERO, bean.getBigDecimal("total"));
    }

}