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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceTestHelper;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.EditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkEditor;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link ProductEditor}.
 *
 * @author Tim Anderson
 */
public class ProductEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that uncommitted unit prices are calculated when autoPriceUpdate is set true.
     */
    @Test
    public void testCalculateUnitPrice() {
        TestHelper.getPractice(); // make sure the tax rates are removed
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null, false);
        Party supplier = TestHelper.createSupplier();

        assertTrue(product.getProductPrices().isEmpty());
        ProductEditor editor = createEditor(product);
        IMObjectTableCollectionEditor prices = (IMObjectTableCollectionEditor) editor.getPricesEditor();

        // add a new price. This won't be added to the product until the product-supplier relationship is populated
        ProductPriceEditor priceEditor = (ProductPriceEditor) prices.add(ProductArchetypes.UNIT_PRICE);
        assertNotNull(priceEditor);
        checkEquals(BigDecimal.ZERO, priceEditor.getCost());
        checkEquals(BigDecimal.valueOf(100), priceEditor.getMarkup());
        checkEquals(BigDecimal.ZERO, priceEditor.getPrice());
        assertFalse(priceEditor.isValid());
        ProductPrice unit = (ProductPrice) priceEditor.getObject();
        assertNull(unit.getPrice());  // underlying price should be null
        assertTrue(product.getProductPrices().isEmpty()); // not added yet

        // populate an entityLink.productSupplier
        EditableIMObjectCollectionEditor suppliers = editor.getSuppliersEditor();
        IMObject relationship = suppliers.create();
        assertNotNull(relationship);
        EntityLinkEditor relationshipEditor = (EntityLinkEditor) suppliers.getEditor(relationship);
        assertNotNull(relationshipEditor);
        relationshipEditor.getComponent();
        relationshipEditor.setTarget(supplier);
        relationshipEditor.getProperty("packageSize").setValue(BigDecimal.ONE);
        relationshipEditor.getProperty("listPrice").setValue(BigDecimal.ONE);
        relationshipEditor.getProperty("autoPriceUpdate").setValue(true);

        // price should be updated
        assertTrue(priceEditor.isValid());
        checkEquals(BigDecimal.ONE, priceEditor.getCost());
        checkEquals(BigDecimal.valueOf(100), priceEditor.getMarkup());
        checkEquals(BigDecimal.valueOf(2), priceEditor.getPrice());
        assertTrue(product.getProductPrices().contains(unit)); // now added

        assertTrue(SaveHelper.save(editor));
    }

    /**
     * Verifies that a product is invalid if its unit prices date range overlap.
     */
    @Test
    public void testDateRangeOverlap() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null, false);
        ProductPrice unit1 = ProductPriceTestHelper.createUnitPrice("2016-01-01", null);
        ProductPrice unit2 = ProductPriceTestHelper.createUnitPrice("2016-03-01", null);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        ProductEditor editor = createEditor(product);

        assertFalse(editor.isValid());
        unit1.setToDate(unit2.getFromDate());

        assertTrue(editor.isValid());
    }

    /**
     * Verifies that if two prices have date range overlaps, but the from and to dates are the same, the editor
     * automatically adjusts the times so they no longer overlap.
     */
    @Test
    public void testAdjustTimeOverlap() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null, false);
        ProductPrice unit1 = ProductPriceTestHelper.createUnitPrice("2016-01-01", null);
        Date fromDate = getDatetime("2016-03-25 10:00:00");
        ProductPrice unit2 = ProductPriceTestHelper.createUnitPrice(fromDate, null);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        ProductEditor editor = createEditor(product);
        assertFalse(editor.isValid());

        // now set the unit1 to-date so that it overlaps unit2 by 5 minutes
        unit1.setToDate(getDatetime("2016-03-25 10:05:00"));
        assertTrue(editor.isValid());

        // verify the editor has adjusted the dates to be the same
        assertEquals(fromDate, unit1.getToDate());
    }

    /**
     * Verifies the product is invalid if there are multiple preferred suppliers.
     */
    @Test
    public void testMultiplePreferredSuppliers() {
        ProductRules rules = ServiceHelper.getBean(ProductRules.class);

        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        ProductSupplier relationship1 = rules.createProductSupplier(product, supplier);
        ProductSupplier relationship2 = rules.createProductSupplier(product, supplier);
        relationship1.setPreferred(true);
        relationship2.setPreferred(true);
        save(product);

        ProductEditor editor1 = createEditor(product);
        assertFalse(editor1.isValid());

        relationship2.setPreferred(false);
        ProductEditor editor2 = createEditor(product);
        assertTrue(editor2.isValid());
    }

    /**
     * Creates a new product editor.
     *
     * @param product the product to edit
     * @return a new editor
     */
    protected ProductEditor createEditor(Product product) {
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        ProductEditor editor = new ProductEditor(product, null, context);
        editor.getComponent();
        return editor;
    }
}
