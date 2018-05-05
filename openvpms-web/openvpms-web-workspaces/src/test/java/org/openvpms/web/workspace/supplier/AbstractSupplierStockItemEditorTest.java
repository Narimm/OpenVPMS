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

package org.openvpms.web.workspace.supplier;

import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Base class for {@link SupplierStockItemEditor} test cases
 *
 * @author Tim Anderson
 */
public class AbstractSupplierStockItemEditorTest extends AbstractAppTest {

    /**
     * Verifies that for new stock items, a product-supplier relationship is created if none already exists.
     *
     * @param editor the stock item editor
     */
    protected void checkCreateProductSupplierRelationship(SupplierStockItemEditor editor) {
        Lookup box = TestHelper.getLookup("lookup.uom", "BOX");
        editor.setAuthor(TestHelper.createUser());
        Product product = TestHelper.createProduct();
        Party supplier = TestHelper.createSupplier();

        populate(editor, product, supplier, BigDecimal.ONE, "abc", 10, box.getCode(), BigDecimal.ONE);
        assertTrue(SaveHelper.save(editor));

        // verify a product-supplier relationship has been created
        checkProductSupplier(product, supplier, "abc", 10, box.getCode(), BigDecimal.ONE);

        // verify the product-supplier relationship doesn't update on subsequent save
        editor.setListPrice(BigDecimal.TEN);
        assertTrue(SaveHelper.save(editor));
        checkProductSupplier(product, supplier, "abc", 10, box.getCode(), BigDecimal.ONE);
    }

    /**
     * Verifies that for new stock items, the product-supplier relationship is updated if it is different.
     */
    protected void checkUpdateProductSupplierRelationship(SupplierStockItemEditor editor) {
        Lookup box = TestHelper.getLookup("lookup.uom", "BOX");
        Product product = TestHelper.createProduct();
        Party supplier = TestHelper.createSupplier();
        createProductSupplier(product, supplier, "abc", 5, box.getCode(), BigDecimal.TEN, BigDecimal.TEN);

        editor.setAuthor(TestHelper.createUser());

        populate(editor, product, supplier, BigDecimal.ONE, "abc", 10, box.getCode(), BigDecimal.ONE);
        assertTrue(SaveHelper.save(editor));

        // verify the product-supplier relationship has been updated
        checkProductSupplier(product, supplier, "abc", 10, box.getCode(), BigDecimal.ONE);
    }

    /**
     * Helper to populate an editor.
     *
     * @param editor       the editor
     * @param product      the product
     * @param supplier     the supplier
     * @param quantity     the quantity
     * @param reorderCode  the reorder code
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @param listPrice    the list price
     */

    protected void populate(SupplierStockItemEditor editor, Product product, Party supplier, BigDecimal quantity,
                            String reorderCode, int packageSize, String packageUnits, BigDecimal listPrice) {
        editor.setProduct(product);
        editor.setQuantity(quantity);
        editor.setSupplier(supplier);
        editor.setReorderCode(reorderCode);
        editor.setPackageSize(packageSize);
        editor.setPackageUnits(packageUnits);
        editor.setListPrice(listPrice);
    }

    /**
     * Creates a product supplier relationship.
     *
     * @param product      the product
     * @param supplier     the supplier
     * @param reorderCode  the reorder code
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @param listPrice    the list price
     * @param nettPrice    the nett price
     */
    protected void createProductSupplier(Product product, Party supplier, String reorderCode, int packageSize,
                                         String packageUnits, BigDecimal listPrice, BigDecimal nettPrice) {
        ProductRules productRules = ServiceHelper.getBean(ProductRules.class);
        ProductSupplier productSupplier = productRules.createProductSupplier(product, supplier);
        productSupplier.setReorderCode(reorderCode);
        productSupplier.setPackageSize(packageSize);
        productSupplier.setPackageUnits(packageUnits);
        productSupplier.setListPrice(listPrice);
        productSupplier.setNettPrice(nettPrice);
        save(product, supplier);
    }

    /**
     * Verifies that the there is only one product-supplier relationship, and it matches the expected values.
     *
     * @param product      the product
     * @param supplier     the supplier
     * @param reorderCode  the expected reorder code
     * @param packageSize  the expected package size
     * @param packageUnits the expected package units
     * @param listPrice    the expected list price
     */
    protected void checkProductSupplier(Product product, Party supplier, String reorderCode, int packageSize,
                                        String packageUnits, BigDecimal listPrice) {
        ProductRules productRules = ServiceHelper.getBean(ProductRules.class);
        product = get(product);
        supplier = get(supplier);
        List<ProductSupplier> productSuppliers = productRules.getProductSuppliers(product, supplier);
        assertEquals(1, productSuppliers.size());
        ProductSupplier ps = productSuppliers.get(0);
        assertEquals(reorderCode, ps.getReorderCode());
        assertEquals(packageSize, ps.getPackageSize());
        assertEquals(packageUnits, ps.getPackageUnits());
        checkEquals(listPrice, ps.getListPrice());
    }

}
