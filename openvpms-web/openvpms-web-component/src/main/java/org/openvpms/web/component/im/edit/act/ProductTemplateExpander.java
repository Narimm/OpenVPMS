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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.archetype.function.list.ListFunctions;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

/**
 * Expands product templates.
 *
 * @author Tim Anderson
 */
public class ProductTemplateExpander {

    /**
     * Determines if products should be restricted to those available at the location or stock location.
     */
    private final boolean useLocationProducts;

    /**
     * The practice location to restrict products to. May be {@code null}
     */
    private final Party location;

    /**
     * The stock location to restrict products to. May be {@code null}
     */
    private final Party stockLocation;

    /**
     * Stock rules.
     */
    private final StockRules stockRules;

    /**
     * Product rules.
     */
    private final ProductRules productRules;

    /**
     * Constructs a {@link ProductTemplateExpander}.
     */
    public ProductTemplateExpander() {
        this(false, null, null);
    }

    /**
     * Constructs a {@link ProductTemplateExpander}.
     *
     * @param useLocationProducts if {@code true}, products must be present at the location or stock location to be
     *                            included, unless the relationship is set to always include products
     * @param location            the practice location to restrict products to. Only relevant when
     *                            {@code useLocationProducts == true}. May be {@code null}
     * @param stockLocation       the stock location to restrict products to. Only relevant when
     *                            {@code useLocationProducts == true}. May be {@code null}
     */
    public ProductTemplateExpander(boolean useLocationProducts, Party location, Party stockLocation) {
        this.useLocationProducts = useLocationProducts;
        this.location = location;
        this.stockLocation = stockLocation;
        stockRules = ServiceHelper.getBean(StockRules.class);
        productRules = ServiceHelper.getBean(ProductRules.class);
    }

    /**
     * Expands a product template.
     *
     * @param template the template to expand
     * @param weight   the patient weight. If {@code 0}, indicates the weight is unknown
     * @param quantity the quantity
     * @param cache    the object cache  @return a map products to their corresponding quantities
     */
    public Collection<TemplateProduct> expand(Product template, Weight weight, BigDecimal quantity,
                                              IMObjectCache cache) {
        Map<TemplateProduct, TemplateProduct> includes = new LinkedHashMap<>();
        ArrayDeque<Product> parents = new ArrayDeque<>();
        if (!expand(template, template, weight, includes, quantity, quantity, false, parents, cache)) {
            includes.clear();
        } else if (includes.isEmpty()) {
            reportNoExpansion(template, weight);
        }
        return includes.values();
    }

    /**
     * Expands a product template.
     *
     * @param root         the root template
     * @param template     the template to expand
     * @param weight       the patient weight. If {@code 0}, indicates the weight is unknown
     * @param includes     the existing includes
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     * @param zeroPrice    if {@code true}, zero prices for all included products
     * @param parents      the parent templates
     * @param cache        the cache
     * @return {@code true} if the template expanded
     */
    protected boolean expand(Product root, Product template, Weight weight,
                             Map<TemplateProduct, TemplateProduct> includes, BigDecimal lowQuantity,
                             BigDecimal highQuantity, boolean zeroPrice, Deque<Product> parents, IMObjectCache cache) {
        boolean result = true;
        if (template.isActive()) {
            if (!parents.contains(template)) {
                parents.push(template);
                IMObjectBean bean = new IMObjectBean(template);
                List<EntityLink> links = bean.getValues("includes", EntityLink.class);
                Collections.sort(links, SequenceComparator.INSTANCE); // sort relationships on sequence
                for (EntityLink relationship : links) {
                    if (!include(relationship, root, template, weight, includes, lowQuantity, highQuantity, zeroPrice,
                                 parents, cache)) {
                        result = false;
                        break;
                    }
                }
                parents.pop();
            } else {
                reportRecursionError(root, template, parents);
                result = false;
            }
        }
        return result;
    }

    /**
     * Includes a product.
     *
     * @param relationship the <em>entityLink.productIncludes</em>
     * @param root         the root template
     * @param template     the template to expand
     * @param weight       the patient weight. If {@code 0}, indicates the weight is unknown
     * @param includes     the existing includes
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     * @param zeroPrice    if {@code true}, zero prices for all included products
     * @param parents      the parent templates
     * @param cache        the cache
     * @return {@code true} if the template expanded
     */
    protected boolean include(EntityLink relationship, Product root, Product template, Weight weight,
                              Map<TemplateProduct, TemplateProduct> includes, BigDecimal lowQuantity,
                              BigDecimal highQuantity, boolean zeroPrice, Deque<Product> parents,
                              IMObjectCache cache) {
        boolean result = true;
        Include include = new Include(relationship);
        if (include.requiresWeight() && weight.isZero()) {
            reportWeightError(template, relationship);
            result = false;
        } else if (include.isIncluded(weight)) {
            Product product = include.getProduct(cache);
            if (product != null && product.isActive()) {
                boolean skip = false;
                if (useLocationProducts) {
                    Party location = checkProductLocation(product);
                    if (location != null) {
                        if (!include.skipIfMissing()) {
                            reportLocationError(template, product, location);
                            result = false;
                        } else {
                            skip = true;
                        }
                    }
                }
                if (!skip) {
                    BigDecimal newLowQty = include.lowQuantity.multiply(lowQuantity);
                    BigDecimal newHighQty = include.highQuantity.multiply(highQuantity);
                    boolean zero = include.zeroPrice || zeroPrice;
                    if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
                        if (!expand(root, product, weight, includes, newLowQty, newHighQty, zero, parents, cache)) {
                            result = false;
                        }
                    } else {
                        TemplateProduct included = new TemplateProduct(product, newLowQty, newHighQty, zero,
                                                                       include.print);
                        TemplateProduct existing = includes.get(included);
                        if (existing == null) {
                            includes.put(included, included);
                        } else {
                            existing.add(newLowQty, newHighQty);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Invoked when a template includes a product on patient weight, but no weight has been supplied.
     *
     * @param template     the template
     * @param relationship the included relationship
     */
    protected void reportWeightError(Product template, EntityLink relationship) {
        String message = Messages.format("product.template.weightrequired", template.getName(),
                                         IMObjectHelper.getName(relationship.getTarget()));
        ErrorDialog.show(message);
    }

    /**
     * Invoked when a product template is included recursively.
     *
     * @param root     the root template
     * @param template the template included recursively
     * @param parents  the parent templates
     */
    protected void reportRecursionError(Product root, Product template, Deque<Product> parents) {
        ListFunctions functions = new ListFunctions(ServiceHelper.getArchetypeService(),
                                                    ServiceHelper.getLookupService());
        List<Object> products = new ArrayList<Object>(parents);
        Collections.reverse(products);
        products.add(template);
        String names = functions.names(products, Messages.get("product.template.includes"));
        String message = Messages.format("product.template.recursion", root.getName(), template.getName(), names);
        ErrorDialog.show(message);
    }

    /**
     * Invoked when a product template expansion results in no included products.
     *
     * @param root   the root template
     * @param weight the patient weight
     */
    protected void reportNoExpansion(Product root, Weight weight) {
        String message = Messages.format("product.template.noproducts", root.getName(), weight.getWeight());
        ErrorDialog.show(message);
    }

    /**
     * Invoked when an included product is not available at a location.
     *
     * @param template the template
     * @param product  the included product
     * @param location the location or stock location
     */
    protected void reportLocationError(Product template, Product product, Party location) {
        String message = Messages.format("product.template.notatlocation", template.getName(),
                                         product.getName(), location.getName());
        ErrorDialog.show(message);
    }

    /**
     * Verifies that a product is a available at a location.
     * <p/>
     * For medication and merchandise products, the product must have a relationship to the stock location, if
     * specified. <br/>
     * For service and template products, the product must have a relationship to the practice location, if specified.
     *
     * @param product the product
     * @return {@code true} if the product is available, or the location isn't specified, otherwise {@code false}
     */
    protected Party checkProductLocation(Product product) {
        Party result = null;
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE)) {
            if (stockLocation != null && !stockRules.hasStockRelationship(product, stockLocation)) {
                result = stockLocation;
            }
        } else if (TypeHelper.isA(product, ProductArchetypes.SERVICE, ProductArchetypes.TEMPLATE)) {
            if (location != null && !productRules.canUseProductAtLocation(product, location)) {
                result = location;
            }
        }
        return result;
    }

    /**
     * Represents a product included by a product template.
     * <p/>
     * Products may be included by patient weight range. To be included: <br/>
     * <pre> {@code minWeight <= patientWeight < maxWeight}</pre>
     */
    private static class Include {

        /**
         * The minimum weight.
         */
        private final BigDecimal minWeight;

        /**
         * The maximum weight.
         */
        private final BigDecimal maxWeight;

        /**
         * The weight units
         */
        private final WeightUnits units;

        /**
         * The low quantity.
         */
        private final BigDecimal lowQuantity;

        /**
         * The high quantity.
         */
        private final BigDecimal highQuantity;

        /**
         * The product reference.
         */
        private final IMObjectReference product;

        /**
         * Determines if prices should be zeroed.
         */
        private final boolean zeroPrice;

        /**
         * Determines if zero-price products should be printed.
         */
        private final boolean print;

        /**
         * Determines if products are skipped if they are not available at the location.
         */
        private final boolean skipIfMissing;

        /**
         * Constructs an {@link Include}.
         *
         * @param relationship the relationship
         */
        public Include(IMObjectRelationship relationship) {
            IMObjectBean bean = new IMObjectBean(relationship);
            units = WeightUnits.fromString(bean.getString("weightUnits"), WeightUnits.KILOGRAMS);
            minWeight = bean.getBigDecimal("minWeight", BigDecimal.ZERO);
            maxWeight = bean.getBigDecimal("maxWeight", BigDecimal.ZERO);
            lowQuantity = bean.getBigDecimal("lowQuantity", ZERO);
            highQuantity = bean.getBigDecimal("highQuantity", ZERO);
            zeroPrice = bean.getBoolean("zeroPrice");
            print = bean.getBoolean("print", true);
            product = relationship.getTarget();
            skipIfMissing = bean.getBoolean("skipIfMissing");
        }

        /**
         * Determines if the include is based on patient weight.
         *
         * @return {@code true} the include is based on patient weight
         */
        public boolean requiresWeight() {
            return minWeight.compareTo(ZERO) != 0 || maxWeight.compareTo(ZERO) != 0;
        }

        /**
         * Determines if the product is included, based on the patient weight.
         *
         * @param weight the patient weight
         * @return {@code true} if the product is included
         */
        public boolean isIncluded(Weight weight) {
            return !requiresWeight() || weight.between(minWeight, maxWeight, units);
        }

        /**
         * Returns the included product.
         *
         * @return the product, or {@code null} if it cannot be found
         */
        public Product getProduct(IMObjectCache cache) {
            return (Product) cache.get(product);
        }

        /**
         * Determines if the product should be skipped, if it isn't available at the location.
         *
         * @return {@code true} if the product should be skipped
         */
        public boolean skipIfMissing() {
            return skipIfMissing;
        }

    }
}
