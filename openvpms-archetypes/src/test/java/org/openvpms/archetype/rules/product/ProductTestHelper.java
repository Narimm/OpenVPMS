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

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.Relationship;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.test.TestHelper.create;
import static org.openvpms.archetype.test.TestHelper.save;

/**
 * Product test helper methods.
 *
 * @author Tim Anderson
 */
public class ProductTestHelper {

    /**
     * Creates a medication product.
     *
     * @return a new product
     */
    public static Product createMedication() {
        return TestHelper.createProduct();
    }

    /**
     * Creates a medication product.
     *
     * @param restricted if {@code true}, assign the medication a restricted drug schedule, otherwise use an
     *                   unrestricted one
     * @return a new medication
     */
    public static Product createMedication(boolean restricted) {
        Product product = createMedication();
        IMObjectBean bean = new IMObjectBean(product);
        String code = (restricted) ? "S3" : "S4";
        Lookup schedule = TestHelper.getLookup(ProductArchetypes.DRUG_SCHEDULE, code, code, restricted);
        IMObjectBean scheduleBean = new IMObjectBean(schedule);
        scheduleBean.setValue("restricted", restricted);
        scheduleBean.save();
        bean.setValue("drugSchedule", code);
        bean.save();
        return product;
    }

    /**
     * Helper to create a medication product linked to a product type.
     *
     * @param productType the product type
     * @return a new product
     */
    public static Product createMedication(Entity productType) {
        Product product = TestHelper.createProduct();
        addProductType(product, productType);
        return product;
    }

    /**
     * Helper to create a merchandise product.
     *
     * @return a new product
     */
    public static Product createMerchandise() {
        return TestHelper.createProduct(ProductArchetypes.MERCHANDISE, null);
    }

    /**
     * Helper to create a service product.
     *
     * @return a new service product
     */
    public static Product createService() {
        return TestHelper.createProduct(ProductArchetypes.SERVICE, null);
    }

    /**
     * Helper to create a service with a fixed price and unit price.
     *
     * @param fixedPrice the fixed price
     * @param unitPrice  the unit price
     * @return a new service product
     */
    public static Product createService(BigDecimal fixedPrice, BigDecimal unitPrice) {
        Product service = createService();
        ProductPrice fixed = ProductPriceTestHelper.createFixedPrice(fixedPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                                                     BigDecimal.ZERO, (Date) null, null, true);
        ProductPrice unit = ProductPriceTestHelper.createUnitPrice(unitPrice, BigDecimal.ZERO, BigDecimal.ZERO,
                                                                   BigDecimal.ZERO, (Date) null, null);
        service.addProductPrice(fixed);
        service.addProductPrice(unit);
        save(service);
        return service;
    }

    /**
     * Helper to create a price template.
     *
     * @return a new product
     */
    public static Product createPriceTemplate() {
        return TestHelper.createProduct(ProductArchetypes.PRICE_TEMPLATE, null);
    }

    /**
     * Creates a medication product with a concentration.
     *
     * @param concentration the concentration
     * @return a new product
     */
    public static Product createProductWithConcentration(BigDecimal concentration) {
        Product product = TestHelper.createProduct();
        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("concentration", concentration);
        return product;
    }

    /**
     * Adds a product type to a product.
     *
     * @param product     the product
     * @param productType the product type
     */
    public static void addProductType(Product product, Entity productType) {
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("type", productType);
        bean.save();
    }

    /**
     * Creates an <em>entity.productDose</em> rounding to 2 decimal places.
     *
     * @param species   the species. May be {@code null}
     * @param minWeight the minimum weight, inclusive
     * @param maxWeight the maximum weight, exclusive
     * @param rate      the rate
     * @param quantity  the quantity
     * @return a new dose
     */
    public static Entity createDose(Lookup species, BigDecimal minWeight, BigDecimal maxWeight, BigDecimal rate,
                                    BigDecimal quantity) {
        return createDose(species, minWeight, maxWeight, rate, quantity, 2);
    }

    /**
     * Creates an <em>entity.productDose</em>.
     *
     * @param species   the species. May be {@code null}
     * @param minWeight the minimum weight, inclusive
     * @param maxWeight the maximum weight, exclusive
     * @param rate      the rate
     * @param quantity  the quantity
     * @param roundTo   the no. of decimal places to round to  @return a new dose
     */
    public static Entity createDose(Lookup species, BigDecimal minWeight, BigDecimal maxWeight, BigDecimal rate,
                                    BigDecimal quantity, int roundTo) {
        Entity dose = (Entity) TestHelper.create(ProductArchetypes.DOSE);
        IMObjectBean bean = new IMObjectBean(dose);
        if (species != null) {
            dose.addClassification(species);
        }
        bean.setValue("minWeight", minWeight);
        bean.setValue("maxWeight", maxWeight);
        bean.setValue("rate", rate);
        bean.setValue("quantity", quantity);
        bean.setValue("roundTo", roundTo);
        return dose;
    }

    /**
     * Adds a dose to a product.
     *
     * @param product the product
     * @param dose    the dose
     */
    public static void addDose(Product product, Entity dose) {
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("doses", dose);
        TestHelper.save(product, dose);
    }

    /**
     * Adds a pharmacy to a product.
     *
     * @param product  the product
     * @param pharmacy the pharmacy
     */
    public static void addPharmacy(Product product, Entity pharmacy) {
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("pharmacy", pharmacy);
        bean.save();
    }

    /**
     * Helper to create an <em>entity.investigationType</em>.
     *
     * @return a new investigation type
     */
    public static Entity createInvestigationType() {
        Entity investigation = (Entity) create(InvestigationArchetypes.INVESTIGATION_TYPE);
        investigation.setName("X-TestInvestigationType-" + investigation.hashCode());
        save(investigation);
        return investigation;
    }

    /**
     * Helper to create an <em>entity.investigationType</em> linked to a laboratory.
     *
     * @param laboratory         the laboratory
     * @param universalServiceId the universal service identifier
     * @return a new investigation type
     */
    public static Entity createInvestigationType(Entity laboratory, String universalServiceId) {
        Entity investigation = createInvestigationType();
        EntityBean bean = new EntityBean(investigation);
        bean.addNodeTarget("laboratory", laboratory);
        bean.setValue("universalServiceIdentifier", universalServiceId);
        bean.save();
        return investigation;
    }

    /**
     * Adds an investigation type to a product.
     *
     * @param product           the product
     * @param investigationType the investigation type
     */
    public static void addInvestigationType(Product product, Entity investigationType) {
        IMObjectBean bean = new IMObjectBean(product);
        bean.addNodeTarget("investigationTypes", investigationType);
        bean.save();
    }

    /**
     * Creates a template.
     *
     * @return a new template
     */
    public static Product createTemplate() {
        return TestHelper.createProduct(ProductArchetypes.TEMPLATE, null);
    }

    /**
     * Adds a location exclusion to a service or template product.
     *
     * @param product  the product
     * @param location the location
     */
    public static void addLocationExclusion(Product product, Party location) {
        IMObjectBean bean = new IMObjectBean(product);
        bean.addNodeTarget("locations", location);
        bean.save();
    }

    /**
     * Creates a template.
     *
     * @param name the template name
     * @return a new template
     */
    public static Product createTemplate(String name) {
        Product template = TestHelper.createProduct(ProductArchetypes.TEMPLATE, null, false);
        template.setName(name);
        TestHelper.save(template);
        return template;
    }

    /**
     * Creates a new product type.
     *
     * @return a new product type
     */
    public static Entity createProductType() {
        return createProductType("XPRODUCTTYPE_" + System.currentTimeMillis());
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
     * Creates a stock location linked to a practice location.
     *
     * @param location the practice location
     * @return a new stock location
     */
    public static Party createStockLocation(Party location) {
        Party stockLocation = createStockLocation();
        EntityBean locationBean = new EntityBean(location);
        locationBean.addRelationship("entityRelationship.locationStockLocation", stockLocation);
        save(location, stockLocation);
        return stockLocation;
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
     * @return the <em>entityLink.productStockLocation</em> relationship
     */
    public static IMObjectRelationship setStockQuantity(Product product, Party stockLocation, BigDecimal quantity) {
        EntityBean bean = new EntityBean(product);
        List<IMObjectRelationship> stockLocations = bean.getValues("stockLocations", IMObjectRelationship.class);
        IMObjectRelationship relationship;
        if (stockLocations.isEmpty()) {
            relationship = bean.addNodeTarget("stockLocations", stockLocation);
        } else {
            relationship = stockLocations.get(0);
        }
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("quantity", quantity);
        save(product);
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
        addInclude(template, include, lowQuantity, highQuantity, 0, 0, zeroPrice, false);
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
        addInclude(template, include, lowQuantity, highQuantity, minWeight, maxWeight, false, false);
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
        addInclude(template, include, quantity, quantity, 0, 0, zeroPrice, false);
    }

    /**
     * Adds an include to the template.
     *
     * @param template      the template
     * @param include       the product to include
     * @param lowQuantity   the include quantity
     * @param minWeight     the minimum weight
     * @param maxWeight     the maximum weight
     * @param zeroPrice     the zero price indicator
     * @param skipIfMissing if {@code true}, skip the product if it is not available at the location
     */
    public static void addInclude(Product template, Product include, int lowQuantity, int highQuantity, int minWeight,
                                  int maxWeight, boolean zeroPrice, boolean skipIfMissing) {
        EntityBean bean = new EntityBean(template);
        IMObjectRelationship relationship = bean.addNodeTarget("includes", include);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("lowQuantity", lowQuantity);
        relBean.setValue("highQuantity", highQuantity);
        relBean.setValue("minWeight", minWeight);
        relBean.setValue("maxWeight", maxWeight);
        relBean.setValue("zeroPrice", zeroPrice);
        relBean.setValue("skipIfMissing", skipIfMissing);
        relBean.setValue("sequence", bean.getValues("includes").size() - 1);
        bean.save();
    }

    /**
     * Adds a service ratio between a practice location and product type.
     *
     * @param location    the practice location
     * @param productType the product type
     * @param ratio       the service ratio
     */
    public static void addServiceRatio(Party location, Entity productType, BigDecimal ratio) {
        EntityBean bean = new EntityBean(location);
        IMObjectRelationship relationship = bean.addNodeTarget("serviceRatios", productType);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("ratio", ratio);
        bean.save();
    }

    /**
     * Creates a new batch.
     *
     * @param batchNumber    the batch number
     * @param product        the product
     * @param expiryDate     the expiry date. May be {@code null}
     * @param stockLocations the stock locations
     * @return a new batch
     */
    public static Entity createBatch(String batchNumber, Product product, Date expiryDate, Party... stockLocations) {
        Entity batch = (Entity) create(ProductArchetypes.PRODUCT_BATCH);
        IMObjectBean bean = new IMObjectBean(batch);
        bean.setValue("name", batchNumber);
        Relationship relationship = bean.addTarget("product", product);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("activeEndTime", expiryDate);
        for (Party stockLocation : stockLocations) {
            bean.addTarget("stockLocations", stockLocation);
        }
        save(batch, product);
        return batch;
    }
}
