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
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public Map<Product, Quantity> expand(Product template, BigDecimal weight, IMObjectCache cache) {
        Map<Product, Quantity> includes = new LinkedHashMap<Product, Quantity>();
        Quantity quantity = new Quantity(BigDecimal.ONE, BigDecimal.ONE);
        if (!expand(template, template, weight, includes, quantity, new ArrayDeque<Product>(), cache)) {
            includes.clear();
        } else if (includes.isEmpty()) {
            reportNoExpansion(template, weight);
        }
        return includes;
    }

    /**
     * Expands a product template.
     *
     * @param root     the root template
     * @param template the template to expand
     * @param weight   the patient weight, in kilograms. If {@code 0}, indicates the weight is unknown
     * @param includes the existing includes
     * @param quantity the included quantity
     * @param parents  the parent templates
     * @param cache    the cache
     * @return {@code true} if the template expanded
     */
    protected boolean expand(Product root, Product template, BigDecimal weight, Map<Product, Quantity> includes,
                             Quantity quantity, Deque<Product> parents, IMObjectCache cache) {
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
                        Quantity included = quantity.multiply(include.getQuantity());
                        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
                            if (!expand(root, product, weight, includes, included, parents, cache)) {
                                result = false;
                                break;
                            }
                        } else {
                            Quantity existing = includes.get(product);
                            if (existing == null) {
                                existing = new Quantity(ZERO, ZERO);
                            }
                            included = included.add(existing);
                            includes.put(product, included);
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
     * Reports an error where a template includes a product on patient weight, but no weight has been supplied.
     *
     * @param template     the template
     * @param relationship the included relationship
     */
    private void reportWeightError(Product template, EntityLink relationship) {
        String message = Messages.format("product.template.weightrequired", template.getName(),
                                         IMObjectHelper.getName(relationship.getTarget()));
        ErrorDialog.show(message);
    }

    /**
     * Reports an error where are product template is included recursively.
     *
     * @param root     the root template
     * @param template the template included recursively
     * @param parents  the parent templates
     */
    private void reportRecursionError(Product root, Product template, Deque<Product> parents) {
        ListFunctions functions = new ListFunctions(ServiceHelper.getArchetypeService(),
                                                    ServiceHelper.getLookupService());
        List<Product> products = new ArrayList<Product>(parents);
        Collections.reverse(products);
        products.add(template);
        String names = functions.names(products, Messages.get("product.template.includes"));
        String message = Messages.format("product.template.recursion", root.getName(), template.getName(), names);
        ErrorDialog.show(message);
    }

    private void reportNoExpansion(Product root, BigDecimal weight) {
        String message = Messages.format("product.template.noproducts", root.getName(), weight);
        ErrorDialog.show(message);
    }


    /**
     * Represents a product included by a product template.
     * <p/>
     * Products may be included by patient weight range. To be included: <br/>
     * <pre> {@code minWeight <= patientWeight < maxWeight}</pre>
     *
     * @author Tim Anderson
     */
    public static class Include {

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
         * Returns the include quantity.
         *
         * @return the quantity
         */
        public Quantity getQuantity() {
            return new Quantity(lowQuantity, highQuantity);
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
