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

package org.openvpms.archetype.function.product;

import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.math.BigDecimal;

/**
 * Product reporting functions.
 *
 * @author Tim Anderson
 */
public class ProductFunctions {

    /**
     * The price rules.
     */
    private final ProductPriceRules priceRules;

    /**
     * The practice rules.
     */
    private final PracticeRules practiceRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link ProductFunctions}.
     *
     * @param priceRules    the product price rules
     * @param practiceRules the practice rules
     * @param service       the archetype service
     */
    public ProductFunctions(ProductPriceRules priceRules, PracticeRules practiceRules, IArchetypeService service) {
        this.priceRules = priceRules;
        this.service = service;
        this.practiceRules = practiceRules;
    }

    /**
     * Returns a price for a product.
     * <p/>
     * This will either be tax inclusive or tax exclusive, depending on the practice preference.
     *
     * @param productId  the product identifier
     * @param taxExPrice the tax-exclusive price
     * @return the product price
     */
    public BigDecimal price(long productId, BigDecimal taxExPrice) {
        BigDecimal result = taxExPrice != null ? taxExPrice : BigDecimal.ZERO;
        Product product = getProduct(productId);
        if (product != null) {
            result = price(product, taxExPrice);
        }
        return result;
    }

    /**
     * Returns a price for a product.
     * <p/>
     * This will either be tax inclusive or tax exclusive, depending on the practice preference.
     *
     * @param product    the product
     * @param taxExPrice the tax-exclusive price
     * @return the product price
     */
    public BigDecimal price(Product product, BigDecimal taxExPrice) {
        BigDecimal result = taxExPrice != null ? taxExPrice : BigDecimal.ZERO;
        if (product != null) {
            Party practice = practiceRules.getPractice();
            if (practice != null) {
                IMObjectBean bean = new IMObjectBean(practice, service);
                Currency currency = practiceRules.getCurrency(practice);
                boolean taxInc = bean.getBoolean("showPricesTaxInclusive", true);
                if (taxInc) {
                    result = priceRules.getTaxIncPrice(taxExPrice, product, practice, currency);
                }
            }
        }
        return result;
    }

    /**
     * Returns a product given its identifier.
     *
     * @param productId the product identifier
     * @return the corresponding product, or {@code null} if none is found
     */
    private Product getProduct(long productId) {
        ArchetypeQuery query = new ArchetypeQuery("product.*", false);
        query.add(Constraints.eq("id", productId));
        query.setMaxResults(1);
        IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }
}
