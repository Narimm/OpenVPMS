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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product.io;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.PriceNotFound;

/**
 * Product I/O helper methods.
 *
 * @author Tim Anderson
 */
class ProductIOHelper {

    /**
     * Returns the price with an identifier matching that in the data.
     *
     * @param data   the price data
     * @param prices the available prices
     * @return the corresponding price
     * @throws ProductIOException if the price is not found
     */
    public static ProductPrice getPrice(PriceData data, List<ProductPrice> prices) {
        long id = data.getId();
        for (ProductPrice price : prices) {
            if (price.getId() == id) {
                return price;
            }
        }
        throw new ProductIOException(PriceNotFound, data.getLine(), id);
    }

    /**
     * Determines if a price is the default price.
     *
     * @param bean the price bean
     * @return {@code true} if the price is the default, otherwise {@code false}
     */
    public static boolean isDefault(IMObjectBean bean) {
        return bean.hasNode("default") && bean.getBoolean("default");
    }

    /**
     * Returns the pricing group codes.
     *
     * @param price the product price
     * @return the pricing group codes, sorted alphabetically
     */
    public static String[] getPricingGroupCodes(ProductPrice price, IArchetypeService service) {
        List<Lookup> groups = getPricingGroupList(price, service);
        String[] codes = new String[groups.size()];
        for (int i = 0; i < codes.length; ++i) {
            codes[i] = groups.get(i).getCode();
        }
        Arrays.sort(codes);
        return codes;
    }

    /**
     * Returns the pricing groups for a price.
     *
     * @param price the product price
     * @return the pricing groups
     */
    public static Set<Lookup> getPricingGroups(ProductPrice price, IArchetypeService service) {
        return new HashSet<Lookup>(getPricingGroupList(price, service));
    }

    private static List<Lookup> getPricingGroupList(ProductPrice price, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(price, service);
        return bean.getValues("pricingGroups", Lookup.class);
    }

    public static boolean intersects(PriceData price1, PriceData price2) {
        return DateRules.intersects(price1.getFrom(), price1.getTo(), price2.getFrom(), price2.getTo())
               && groupsIntersect(price1, price2);
    }

    public static boolean intersects(PriceData price1, ProductPrice price2, IArchetypeService service) {
        return DateRules.intersects(price2.getFromDate(), price2.getToDate(), price1.getFrom(), price1.getTo())
               && groupsIntersect(price1, price2, service);
    }

    public static boolean groupsIntersect(PriceData price1, ProductPrice price2, IArchetypeService service) {
        return intersects(price1.getPricingGroups(), getPricingGroups(price2, service), false);
    }

    public static boolean groupsIntersect(PriceData price1, PriceData price2) {
        return intersects(price1.getPricingGroups(), price2.getPricingGroups(), true);
    }

    private static boolean intersects(Set<Lookup> group1, Set<Lookup> group2, boolean copy2) {
        if (group1.equals(group2)) {
            return true;
        }
        if (copy2) {
            group2 = new HashSet<Lookup>(group2);
        }
        group2.retainAll(group1);
        return !group2.isEmpty();
    }
}
