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

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Layout strategy for {@link ProductPrice} instances.
 * <p/>
 * This suppresses the pricingGroups node if it is not required.
 *
 * @author Tim Anderson
 */
public class ProductPriceLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @param object  the object to display
     * @param context the layout context
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes(IMObject object, LayoutContext context) {
        ArchetypeNodes nodes;
        if (ProductHelper.hasPricingGroups((ProductPrice) object)) {
            nodes = super.getArchetypeNodes(object, context);
        } else if (!context.isEdit() || !ProductHelper.pricingGroupsConfigured()) {
            nodes = new ArchetypeNodes().exclude("pricingGroups");
        } else {
            nodes = super.getArchetypeNodes(object, context);
        }
        return nodes;
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                  LayoutContext context) {
        if (parent instanceof Product) {
            BigDecimal price = getTaxIncPrice((ProductPrice) object, (Product) parent, context);
            Property taxIncPrice = new SimpleProperty("taxIncPrice", price, BigDecimal.class,
                                                      Messages.get("product.price.taxinc"));
            taxIncPrice.setValue(price);
            ArchetypeNodes.insert(properties, "price", taxIncPrice);
        }
        super.doSimpleLayout(object, parent, properties, container, context);
    }

    /**
     * Returns the tax-inclusive price.
     *
     * @param object  the price
     * @param product the parent product
     * @param context the layout context
     * @return the tax-inclusive price
     */
    private BigDecimal getTaxIncPrice(ProductPrice object, Product product, LayoutContext context) {
        BigDecimal price = object.getPrice();
        BigDecimal result = price;
        PracticeService service = ServiceHelper.getBean(PracticeService.class);
        Currency currency = service.getCurrency();
        Party practice = context.getContext().getPractice();
        if (price != null && currency != null && practice != null) {
            ProductPriceRules rules = ServiceHelper.getBean(ProductPriceRules.class);
            result = rules.getTaxIncPrice(price, product, practice, currency);
        }
        return result;
    }

}
