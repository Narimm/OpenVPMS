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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
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
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null, false);
        Party supplier = TestHelper.createSupplier();

        assertTrue(product.getProductPrices().isEmpty());
        ProductEditor editor = new ProductEditor(product, null, new DefaultLayoutContext(new LocalContext(),
                                                                                         new HelpContext("foo", null)));
        editor.getComponent();
        IMObjectTableCollectionEditor prices = (IMObjectTableCollectionEditor) editor.getPricesEditor();

        // add a new price. This won't be added to the product until the product-supplier relationship is populaated
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
}
