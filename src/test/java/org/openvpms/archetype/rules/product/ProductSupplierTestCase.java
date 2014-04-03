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

package org.openvpms.archetype.rules.product;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Tests the {@link ProductSupplier} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductSupplierTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ProductSupplier} accessors.
     */
    @Test
    public void test() {
        ProductRules rules = new ProductRules(getArchetypeService());
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        ProductSupplier ps = rules.createProductSupplier(product, supplier);

        assertEquals(product, ps.getProduct());
        assertEquals(product.getObjectReference(), ps.getProductRef());
        assertEquals(supplier, ps.getSupplier());
        assertEquals(supplier.getObjectReference(), ps.getSupplierRef());

        Date startTime = new Date();
        ps.setActiveStartTime(startTime);
        assertEquals(startTime, ps.getActiveStartTime());

        Date endTime = new Date();
        ps.setActiveEndTime(endTime);
        assertEquals(endTime, ps.getActiveEndTime());

        String reorderCode = "REORDERCODE";
        ps.setReorderCode(reorderCode);
        assertEquals(reorderCode, ps.getReorderCode());

        String reorderDesc = "REORDERDESC";
        ps.setReorderDescription(reorderDesc);
        assertEquals(reorderDesc, ps.getReorderDescription());

        String barCode = "BARCODE";
        ps.setBarCode(barCode);
        assertEquals(barCode, ps.getBarCode());

        int packageSize = 9;
        ps.setPackageSize(packageSize);
        assertEquals(packageSize, ps.getPackageSize());

        String packageUnits = "BOX";
        ps.setPackageUnits(packageUnits);
        assertEquals(packageUnits, ps.getPackageUnits());

        int minQuantity = 10;
        ps.setMinimumOrderQuantity(minQuantity);
        assertEquals(minQuantity, ps.getMinimumOrderQuantity());

        int increment = 11;
        ps.setOrderQuantityIncrement(increment);
        assertEquals(increment, ps.getOrderQuantityIncrement());

        int leadTime = 12;
        ps.setLeadTime(leadTime);
        assertEquals(leadTime, ps.getLeadTime());

        String leadTimeUnits = "MONTHS";
        ps.setLeadTimeUnits(leadTimeUnits);
        assertEquals(leadTimeUnits, ps.getLeadTimeUnits());

        BigDecimal listPrice = new BigDecimal("1");
        ps.setListPrice(listPrice);
        checkEquals(listPrice, ps.getListPrice());

        BigDecimal nettPrice = new BigDecimal("2");
        ps.setNettPrice(nettPrice);
        checkEquals(nettPrice, ps.getNettPrice());

        ps.setPreferred(false);
        assertFalse(ps.isPreferred());
    }
}
