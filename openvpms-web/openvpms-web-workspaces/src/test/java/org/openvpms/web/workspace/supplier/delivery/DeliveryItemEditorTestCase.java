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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.delivery;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.ProductRules;
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
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.supplier.AbstractSupplierStockItemEditorTest;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DeliveryItemEditor} class.
 *
 * @author Tim Anderson
 */
public class DeliveryItemEditorTestCase extends AbstractSupplierStockItemEditorTest {

    /**
     * The layout context.
     */
    private LayoutContext context;

    /**
     * 'Each' unit-of-measure.
     */
    private Lookup each;

    /**
     * Box unit-of-measure
     */
    private Lookup box;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        each = TestHelper.getLookup("lookup.uom", "EACH");
        box = TestHelper.getLookup("lookup.uom", "BOX");
    }

    /**
     * Tests validation.
     */
    @Test
    public void testValidation() {
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
        Act delivery = (Act) create(SupplierArchetypes.DELIVERY);
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        createProductSupplier(product, supplier, "A1", 2, box.getCode(), BigDecimal.TEN, BigDecimal.TEN);

        FinancialAct item = createDeliveryItem();

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
        Act delivery = (Act) create(SupplierArchetypes.DELIVERY);
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        createProductSupplier(product, supplier, "A1", 2, box.getCode(), BigDecimal.TEN, BigDecimal.TEN);

        FinancialAct item = (FinancialAct) create(SupplierArchetypes.DELIVERY_ITEM);
        ActBean bean = new ActBean(item);
        bean.setValue("supplierInvoiceLineId", "1");
        DeliveryItemEditor editor = new DeliveryItemEditor(item, delivery, context);
        editor.setAuthor(TestHelper.createUser());
        editor.setUnitPrice(new BigDecimal(12));
        populate(editor, product, supplier, BigDecimal.TEN, "B1", 1, each.getCode(), new BigDecimal(12));
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

    /**
     * Verifies that saving a delivery item doesn't create or update the product-supplier relationship.
     */
    @Test
    public void testSupplierRelationshipNotCreatedOrUpdatedForDelivery() {
        ProductRules productRules = ServiceHelper.getBean(ProductRules.class);
        Act delivery = (Act) create(SupplierArchetypes.DELIVERY);
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        Party stockLocation = SupplierTestHelper.createStockLocation();

        FinancialAct item1 = createDeliveryItem();

        DeliveryItemEditor editor1 = new DeliveryItemEditor(item1, delivery, context);
        editor1.setSupplier(supplier);
        editor1.setStockLocation(stockLocation);
        editor1.setProduct(product);
        assertTrue(SaveHelper.save(editor1));

        // verify no product-supplier relationship has been created
        product = get(product);
        supplier = get(supplier);
        assertTrue(productRules.getProductSuppliers(product, supplier).isEmpty());

        // now add a product-supplier relationship
        createProductSupplier(product, supplier, "A1", 2, box.getCode(), BigDecimal.TEN, BigDecimal.TEN);

        // create a new item, and verify it doesn't update the product-supplier relationship
        FinancialAct item2 = createDeliveryItem();
        DeliveryItemEditor editor2 = new DeliveryItemEditor(item2, delivery, context);
        editor2.setSupplier(supplier);
        editor2.setStockLocation(stockLocation);
        editor2.setProduct(product);
        editor2.setListPrice(new BigDecimal("20"));
        assertTrue(SaveHelper.save(editor2));

        // verify the product-supplier relationship hasn't changed
        checkProductSupplier(product, supplier, "A1", 2, box.getCode(), BigDecimal.TEN);
    }

    /**
     * Verifies that for new return items, a product-supplier relationship is created if none already exists.
     */
    @Test
    public void testCreateProductSupplierRelationship() {
        Act deliveryReturn = (Act) create(SupplierArchetypes.RETURN);
        FinancialAct item = (FinancialAct) create(SupplierArchetypes.RETURN_ITEM);
        DeliveryItemEditor editor = new DeliveryItemEditor(item, deliveryReturn, context);
        checkCreateProductSupplierRelationship(editor);
    }

    /**
     * Verifies that for new return items, the product-supplier relationship is updated if it is different.
     */
    @Test
    public void testUpdateProductSupplierRelationship() {
        Act deliveryReturn = (Act) create(SupplierArchetypes.RETURN);
        FinancialAct item = (FinancialAct) create(SupplierArchetypes.RETURN_ITEM);
        DeliveryItemEditor editor = new DeliveryItemEditor(item, deliveryReturn, context);
        checkUpdateProductSupplierRelationship(editor);
    }

    /**
     * Creates a pre-populated delivery item.
     *
     * @return a new delivery item
     */
    private FinancialAct createDeliveryItem() {
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
        return item;
    }

}
