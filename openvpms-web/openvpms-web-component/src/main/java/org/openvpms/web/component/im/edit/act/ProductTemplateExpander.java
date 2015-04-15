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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.archetype.function.list.ListFunctions;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.product.Product;
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

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.openvpms.archetype.rules.math.WeightUnits.KILOGRAMS;

/**
 * Expands product templates.
 *
 * @author Tim Anderson
 */
class ProductTemplateExpander {

    /**
     * Expands a product template.
     *
     * @param template the template to expand
     * @param weight   the patient weight, in kilograms. If {@code 0}, indicates the weight is unknown
     * @param cache    the object cache
     * @return a map products to their corresponding quantities
     */
    public Collection<TemplateProduct> expand(Product template, BigDecimal weight, IMObjectCache cache) {
        Map<TemplateProduct, TemplateProduct> includes = new LinkedHashMap<TemplateProduct, TemplateProduct>();
        if (!expand(template, template, weight, includes, ONE, ONE, false, new ArrayDeque<Product>(), cache)) {
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
     * @param weight       the patient weight, in kilograms. If {@code 0}, indicates the weight is unknown
     * @param includes     the existing includes
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     * @param zeroPrice    if {@code true}, zero prices for all included products
     * @param parents      the parent templates
     * @param cache        the cache
     * @return {@code true} if the template expanded
     */
    protected boolean expand(Product root, Product template, BigDecimal weight,
                             Map<TemplateProduct, TemplateProduct> includes, BigDecimal lowQuantity,
                             BigDecimal highQuantity, boolean zeroPrice, Deque<Product> parents,
                             IMObjectCache cache) {
        boolean result = true;
        if (!parents.contains(template)) {
            parents.push(template);
            IMObjectBean bean = new IMObjectBean(template);
            for (EntityLink relationship : bean.getValues("includes", EntityLink.class)) {
                Include include = new Include(relationship);
                if (include.requiresWeight() && (weight == null || weight.compareTo(ZERO) == 0)) {
                    reportWeightError(template, relationship);
                    result = false;
                    break;
                } else if (include.isIncluded(weight)) {
                    Product product = include.getProduct(cache);
                    if (product != null) {
                        BigDecimal newLowQty = include.lowQuantity.multiply(lowQuantity);
                        BigDecimal newHighQty = include.highQuantity.multiply(highQuantity);
                        boolean zero = include.zeroPrice || zeroPrice;
                        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
                            if (!expand(root, product, weight, includes, newLowQty, newHighQty, zero, parents, cache)) {
                                result = false;
                                break;
                            }
                        } else {
                            TemplateProduct included = new TemplateProduct(product, newLowQty, newHighQty, zero);
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
            parents.pop();
        } else {
            reportRecursionError(root, template, parents);
            result = false;
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
        List<Product> products = new ArrayList<Product>(parents);
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
    protected void reportNoExpansion(Product root, BigDecimal weight) {
        String message = Messages.format("product.template.noproducts", root.getName(), weight);
        ErrorDialog.show(message);
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
         * Constructs an {@link Include}.
         *
         * @param relationship the relationship
         */
        public Include(IMObjectRelationship relationship) {
            IMObjectBean bean = new IMObjectBean(relationship);
            WeightUnits units = getUnits(bean);
            minWeight = getWeight("minWeight", bean, units);
            maxWeight = getWeight("maxWeight", bean, units);
            lowQuantity = bean.getBigDecimal("lowQuantity", ZERO);
            highQuantity = bean.getBigDecimal("highQuantity", ZERO);
            zeroPrice = bean.getBoolean("zeroPrice");
            product = relationship.getTarget();
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
        public boolean isIncluded(BigDecimal weight) {
            return !requiresWeight() || weight.compareTo(minWeight) >= 0 && weight.compareTo(maxWeight) < 0;
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
         * Returns a weight node, in kilograms.
         *
         * @param name  the node name
         * @param bean  the bean
         * @param units the units
         * @return the weight, in kilograms
         */
        private BigDecimal getWeight(String name, IMObjectBean bean, WeightUnits units) {
            BigDecimal weight = bean.getBigDecimal(name, ZERO);
            return (units == KILOGRAMS) ? weight : MathRules.convert(weight, units, KILOGRAMS);
        }

        /**
         * Returns the weight units.
         *
         * @param bean the bean
         * @return the weight units
         */
        private WeightUnits getUnits(IMObjectBean bean) {
            return WeightUnits.valueOf(bean.getString("weightUnits", KILOGRAMS.toString()));
        }

    }
}
