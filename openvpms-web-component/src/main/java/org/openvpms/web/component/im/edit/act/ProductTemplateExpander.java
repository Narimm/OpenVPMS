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
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.cache.IMObjectCache;
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
     * @param cache    the object cache
     * @return a map products to their corresponding quantities
     */
    public Map<Product, BigDecimal> expand(Product template, IMObjectCache cache) {
        Map<Product, BigDecimal> includes = new LinkedHashMap<Product, BigDecimal>();
        if (!expand(template, template, includes, BigDecimal.ONE, new ArrayDeque<Product>(), cache)) {
            includes.clear();
        }
        return includes;
    }

    /**
     * Expands a product template.
     *
     * @param root     the root template
     * @param template the template to expeand
     * @param includes the existing includes
     * @param quantity the included quantity
     * @param parents  the parent templates
     * @param cache    the cache
     * @return {@code true} if the template expanded
     */
    protected boolean expand(Product root, Product template, Map<Product, BigDecimal> includes, BigDecimal quantity,
                             Deque<Product> parents, IMObjectCache cache) {
        boolean result = true;
        if (!parents.contains(template)) {
            parents.push(template);
            IMObjectBean bean = new IMObjectBean(template);
            for (EntityLink relationship : bean.getValues("includes", EntityLink.class)) {
                Product product = (Product) cache.get(relationship.getTarget());
                if (product != null) {
                    BigDecimal included = quantity.multiply(getQuantity(relationship));
                    if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
                        if (!expand(root, product, includes, included, parents, cache)) {
                            result = false;
                            break;
                        }
                    } else {
                        BigDecimal existing = includes.get(product);
                        if (existing == null) {
                            existing = BigDecimal.ZERO;
                        }
                        included = included.add(existing);
                        includes.put(product, included);
                    }
                }
            }
            parents.pop();
        } else {
            ListFunctions functions = new ListFunctions(ServiceHelper.getArchetypeService(),
                                                        ServiceHelper.getLookupService());
            List<Product> products = new ArrayList<Product>(parents);
            Collections.reverse(products);
            products.add(template);
            String names = functions.names(products, Messages.get("product.template.includes"));
            ErrorDialog.show(Messages.format("product.template.expanderror", root.getName(), template.getName(),
                                             names));
            result = false;
        }
        return result;
    }

    /**
     * Returns the include quantity.
     *
     * @param relationship the template product relationship.
     * @return the quantity
     */
    private BigDecimal getQuantity(IMObject relationship) {
        IMObjectBean bean = new IMObjectBean(relationship);
        return bean.getBigDecimal("includeQty", BigDecimal.ZERO);
    }

}
