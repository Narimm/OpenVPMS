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

import nextapp.echo2.app.SelectField;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Pricing location helper methods.
 *
 * @author Tim Anderson
 */
public class PricingLocationHelper {

    /**
     * Creates a new {@code SelectField} that renders a list of pricing locations.
     *
     * @param selected the initial selection. May be {@code null}
     * @return a new field
     */
    public static SelectField createPricingLocationSelector(Lookup selected) {
        LookupQuery query = new ArchetypeLookupQuery("lookup.pricingLocation");
        final SelectField field = SelectFieldFactory.create(new LookupListModel(query, false, true));
        field.setCellRenderer(LookupListCellRenderer.INSTANCE);
        if (selected != null) {
            field.setSelectedItem(selected.getCode());
        }
        return field;
    }

    /**
     * Returns the pricing location for the context practice location.
     *
     * @param context the context
     * @return the pricing location, or {@code null} if none is found
     */
    public static Lookup getPricingLocation(Context context) {
        Lookup result = null;
        Party location = context.getLocation();
        if (location != null) {
            LocationRules rules = ServiceHelper.getBean(LocationRules.class);
            result = rules.getPricingLocation(location);
        }
        return result;
    }

    /**
     * Filters prices on pricing location.
     *
     * @param prices          the prices to filter
     * @param pricingLocation the pricing location to filter on. May be {@code null}, to indicate prices that have no
     *                        location
     * @return the filtered prices
     */
    public static List<IMObject> filterPrices(List<IMObject> prices, Lookup pricingLocation) {
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : prices) {
            IMObjectBean bean = new IMObjectBean(object);
            List<Lookup> locations = bean.getValues("pricingLocations", Lookup.class);

            if ((pricingLocation != null && locations.contains(pricingLocation))
                || (pricingLocation == null && locations.isEmpty())) {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * Determines if pricing locations have been configured.
     *
     * @return {@code true} if pricing locations have been configured
     */
    public static boolean pricingLocationsConfigured() {
        return !ServiceHelper.getLookupService().getLookups("lookup.pricingLocation").isEmpty();
    }

    /**
     * Determines if a price has pricing locations.
     *
     * @param price the price
     * @return {@code true} if the price has pricing locations
     */
    public static boolean hasPricingLocations(ProductPrice price) {
        ProductPriceRules rules = ServiceHelper.getBean(ProductPriceRules.class);
        return !rules.getPricingLocations(price).isEmpty();
    }
}
