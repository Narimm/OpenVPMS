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

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product test helper methods.
 *
 * @author Tim Anderson
 */
public class ProductTestHelper {

    /**
     * Helper to create a product linked to a product type.
     *
     * @param productType the product type
     * @return a new product
     */
    public static Product createProduct(Entity productType) {
        Product product = TestHelper.createProduct();
        EntityBean bean = new EntityBean(productType);
        bean.addRelationship("entityRelationship.productTypeProduct", product);
        TestHelper.save(productType, product);
        return product;
    }

    /**
     * Creates a template.
     *
     * @param name the template name
     * @return a new template
     */
    public static Product createTemplate(String name) {
        Product template = (Product) TestHelper.create(ProductArchetypes.TEMPLATE);
        template.setName(name);
        TestHelper.save(template);
        return template;
    }

    /**
     * Creates a new product type.
     *
     * @param name the product type name
     * @return a new product type
     */
    public static Entity createProductType(String name) {
        Entity result = (Entity) TestHelper.create(ProductArchetypes.PRODUCT_TYPE);
        result.setName(name);
        TestHelper.save(result);
        return result;
    }

    /**
     * Creates a stock location.
     *
     * @return a new stock location
     */
    public static Party createStockLocation() {
        Party result = (Party) TestHelper.create(StockArchetypes.STOCK_LOCATION);
        result.setName("STOCK-LOCATION-" + result.hashCode());
        TestHelper.save(result);
        return result;
    }

    /**
     * Adds a demographic update to a product, and saves it.
     *
     * @param product    the product
     * @param node       the node name. May be {@code null}
     * @param expression the expression
     */
    public static void addDemographicUpdate(Product product, String node, String expression) {
        Lookup lookup = (Lookup) TestHelper.create("lookup.demographicUpdate");
        lookup.setCode("XDEMOGRAPHICUPDATE_" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("nodeName", node);
        bean.setValue("expression", expression);
        bean.save();
        product.addClassification(lookup);
        TestHelper.save(product);
    }

    /**
     * Initialises the quantity for the product and stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param quantity      the quantity
     * @return the <em>entityRelationship.productStockLocation</em> relationship
     */
    public static EntityRelationship setStockQuantity(Product product, Party stockLocation, BigDecimal quantity) {
        EntityBean bean = new EntityBean(product);
        List<EntityRelationship> stockLocations = bean.getNodeRelationships("stockLocations");
        EntityRelationship relationship;
        if (stockLocations.isEmpty()) {
            relationship = bean.addNodeRelationship("stockLocations", stockLocation);
        } else {
            relationship = stockLocations.get(0);
        }
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("quantity", quantity);
        TestHelper.save(product, stockLocation);
        return relationship;
    }

    /**
     * Adds an include to the template with no weight restrictions.
     *
     * @param template     the template
     * @param include      the product to include
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     */
    public static void addInclude(Product template, Product include, int lowQuantity, int highQuantity) {
        addInclude(template, include, lowQuantity, highQuantity, false);
    }

    /**
     * Adds an include to the template with no weight restrictions.
     *
     * @param template     the template
     * @param include      the product to include
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     * @param zeroPrice    the zero price indicator
     */
    public static void addInclude(Product template, Product include, int lowQuantity, int highQuantity,
                                  boolean zeroPrice) {
        addInclude(template, include, lowQuantity, highQuantity, 0, 0, zeroPrice);
    }

    /**
     * Adds an include to the template.
     *
     * @param template    the template
     * @param include     the product to include
     * @param lowQuantity the include quantity
     * @param minWeight   the minimum weight
     * @param maxWeight   the maximum weight
     */
    public static void addInclude(Product template, Product include, int lowQuantity, int highQuantity, int minWeight,
                                  int maxWeight) {
        addInclude(template, include, lowQuantity, highQuantity, minWeight, maxWeight, false);
    }

    /**
     * Adds an include to the template with no weight restrictions, and the same low and high quantities.
     *
     * @param template  the template
     * @param include   the product to include
     * @param quantity  the low and high quantity
     * @param zeroPrice the zero price indicator
     */
    public static void addInclude(Product template, Product include, int quantity, boolean zeroPrice) {
        addInclude(template, include, quantity, quantity, 0, 0, zeroPrice);
    }

    /**
     * Adds an include to the template.
     *
     * @param template    the template
     * @param include     the product to include
     * @param lowQuantity the include quantity
     * @param minWeight   the minimum weight
     * @param maxWeight   the maximum weight
     * @param zeroPrice   the zero price indicator
     */
    public static void addInclude(Product template, Product include, int lowQuantity, int highQuantity, int minWeight,
                                  int maxWeight, boolean zeroPrice) {
        EntityBean bean = new EntityBean(template);
        IMObjectRelationship relationship = bean.addNodeTarget("includes", include);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("lowQuantity", lowQuantity);
        relBean.setValue("highQuantity", highQuantity);
        relBean.setValue("minWeight", minWeight);
        relBean.setValue("maxWeight", maxWeight);
        relBean.setValue("zeroPrice", zeroPrice);
        bean.save();
    }

}
