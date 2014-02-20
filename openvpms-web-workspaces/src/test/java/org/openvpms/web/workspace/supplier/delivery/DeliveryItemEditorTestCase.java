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

package org.openvpms.web.workspace.supplier.delivery;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DeliveryItemEditor} class.
 *
 * @author Tim Anderson
 */
public class DeliveryItemEditorTestCase extends AbstractAppTest {

    /**
     * Tests validation.
     */
    @Test
    public void testValidation() {
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        Lookup each = TestHelper.getLookup("lookup.uom", "EACH");

        Act delivery = (Act) create(SupplierArchetypes.DELIVERY);
        FinancialAct item = (FinancialAct) create(SupplierArchetypes.DELIVERY_ITEM);
        DeliveryItemEditor editor = new DeliveryItemEditor(item, delivery, context);
        assertFalse(editor.isValid());

        editor.setAuthor(TestHelper.createUser());
        assertTrue(editor.isValid());

        delivery.setStatus(ActStatus.POSTED);
        assertFalse(editor.isValid());

        Product product = TestHelper.createProduct();
        editor.setProduct(product);
        editor.setPackageSize(1);
        editor.setPackageUnits(each.getCode());
        assertTrue(editor.isValid());

        editor.getProperty("packageSize").setValue(null);
        editor.setPackageUnits(null);
        assertFalse(editor.isValid());
    }

    /**
     * Verifies that selecting a product for a manual delivery updates the prices.
     */
    @Test
    public void testProductUpdateForManualDelivery() {
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        Lookup each = TestHelper.getLookup("lookup.uom", "EACH");
        Lookup box = TestHelper.getLookup("lookup.uom", "BOX");
        Act delivery = (Act) create(SupplierArchetypes.DELIVERY);
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        ProductSupplier productSupplier = new ProductRules().createProductSupplier(product, supplier);
        productSupplier.setReorderCode("A1");
        productSupplier.setPackageSize(2);
        productSupplier.setPackageUnits(box.getCode());
        productSupplier.setNettPrice(BigDecimal.TEN);
        productSupplier.setListPrice(BigDecimal.TEN);
        save(product, supplier);

        FinancialAct item = (FinancialAct) create(SupplierArchetypes.DELIVERY_ITEM);
        ActBean bean = new ActBean(item);
        bean.setValue("reorderCode", "B1");
        bean.setValue("packageSize", 1);
        bean.setValue("packageUnits", each.getCode());
        bean.setValue("quantity", 10);
        bean.setValue("unitPrice", 12);
        bean.setValue("listPrice", 12);
        bean.setValue("tax", 12);
        bean.addNodeParticipation("author", TestHelper.createUser());

        DeliveryItemEditor editor = new DeliveryItemEditor(item, delivery, context);
        editor.setSupplier(supplier);
        editor.setStockLocation(SupplierTestHelper.createStockLocation());
        assertTrue(editor.isValid());

        editor.setProduct(product);
        assertTrue(editor.isValid());

        assertEquals("A1", editor.getReorderCode());
        assertEquals(2, editor.getPackageSize());
        assertEquals(box.getCode(), editor.getPackageUnits());
        checkEquals(BigDecimal.TEN, editor.getQuantity());
        checkEquals(BigDecimal.TEN, editor.getUnitPrice());
        checkEquals(BigDecimal.TEN, editor.getListPrice());
    }

    /**
     * Verifies that selecting a product for an ESCI delivery doesn't update the prices.
     */
    @Test
    public void testProductUpdateForESCIDelivery() {
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        Lookup each = TestHelper.getLookup("lookup.uom", "EACH");
        Lookup box = TestHelper.getLookup("lookup.uom", "BOX");
        Act delivery = (Act) create(SupplierArchetypes.DELIVERY);
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        ProductSupplier productSupplier = new ProductRules().createProductSupplier(product, supplier);
        productSupplier.setReorderCode("A1");
        productSupplier.setPackageSize(2);
        productSupplier.setPackageUnits(box.getCode());
        productSupplier.setNettPrice(BigDecimal.TEN);
        productSupplier.setListPrice(BigDecimal.TEN);
        save(product, supplier);

        FinancialAct item = (FinancialAct) create(SupplierArchetypes.DELIVERY_ITEM);
        ActBean bean = new ActBean(item);
        bean.setValue("supplierInvoiceLineId", "1");
        bean.setValue("reorderCode", "B1");
        bean.setValue("packageSize", 1);
        bean.setValue("packageUnits", each.getCode());
        bean.setValue("quantity", 10);
        bean.setValue("unitPrice", 12);
        bean.setValue("listPrice", 12);
        bean.setValue("tax", 12);
        bean.addNodeParticipation("author", TestHelper.createUser());

        DeliveryItemEditor editor = new DeliveryItemEditor(item, delivery, context);
        editor.setSupplier(supplier);
        editor.setStockLocation(SupplierTestHelper.createStockLocation());
        assertTrue(editor.isValid());

        editor.setProduct(product);
        assertTrue(editor.isValid());

        assertEquals("B1", editor.getReorderCode());
        assertEquals(1, editor.getPackageSize());
        assertEquals(each.getCode(), editor.getPackageUnits());
        checkEquals(BigDecimal.TEN, editor.getQuantity());
        checkEquals(new BigDecimal("12"), editor.getUnitPrice());
        checkEquals(new BigDecimal("12"), editor.getListPrice());
    }
}
