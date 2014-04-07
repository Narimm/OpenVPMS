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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Pricing group helper methods.
 *
 * @author Tim Anderson
 */
public class PricingGroupHelper {

    /**
     * Returns the pricing group for the context practice location.
     *
     * @param context the context
     * @return the pricing group, or {@code null} if none is found
     */
    public static PricingGroup getPricingGroup(Context context) {
        Lookup result = null;
        Party location = context.getLocation();
        if (location != null) {
            LocationRules rules = ServiceHelper.getBean(LocationRules.class);
            result = rules.getPricingGroup(location);
        }
        return new PricingGroup(result);
    }

    /**
     * Filters prices on pricing group.
     *
     * @param prices       the prices to filter
     * @param pricingGroup the pricing group to filter on
     * @return the filtered prices
     */
    public static List<IMObject> filterPrices(List<IMObject> prices, PricingGroup pricingGroup) {
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : prices) {
            IMObjectBean bean = new IMObjectBean(object);
            List<Lookup> groups = bean.getValues("pricingGroups", Lookup.class);
            if (pricingGroup.matches(groups)) {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * Determines if pricing groups have been configured.
     *
     * @return {@code true} if pricing groups have been configured
     */
    public static boolean pricingGroupsConfigured() {
        return !getPricingGroups().isEmpty();
    }

    /**
     * Returns the pricing groups.
     *
     * @return the pricing groups
     */
    public static Collection<Lookup> getPricingGroups() {
        return ServiceHelper.getLookupService().getLookups("lookup.pricingGroup");
    }

    /**
     * Determines if a price has pricing groups.
     *
     * @param price the price
     * @return {@code true} if the price has pricing groups
     */
    public static boolean hasPricingGroups(ProductPrice price) {
        ProductPriceRules rules = ServiceHelper.getBean(ProductPriceRules.class);
        return !rules.getPricingGroups(price).isEmpty();
    }
}
