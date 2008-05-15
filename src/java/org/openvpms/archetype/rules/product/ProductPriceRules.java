/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.math.BigDecimal;


/**
 * Product price rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z` $
 */
public class ProductPriceRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>ProductPriceRules</tt>.
     *
     * @param service the archetype service
     */
    public ProductPriceRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Calculates a product price using the following formula:
     * <p/>
     * <tt>price = (cost * (1 + markup/100) ) * (1 + tax/100)</tt>
     *
     * @param product  the product
     * @param cost     the product cost
     * @param markup   the markup percentage
     * @param practice the <em>party.organisationPractice</em> used to determine
     *                 product taxes
     * @param currency the currency, for rounding conventions
     * @return the price
     */
    public BigDecimal getPrice(Product product, BigDecimal cost,
                               BigDecimal markup, Party practice,
                               Currency currency) {
        BigDecimal price = BigDecimal.ZERO;
        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal markupDec = getRate(markup);
            BigDecimal taxRate = getTaxRate(product, practice);
            price = cost.multiply(
                    BigDecimal.ONE.add(markupDec)).multiply(
                    BigDecimal.ONE.add(taxRate));
            price = currency.round(price);
        }
        return price;
    }

    /**
     * Returns the tax rate of a product.
     *
     * @param product the product
     * @return the product tax rate
     * @throws ArchetypeServiceException for any archetype service error
     */
    private BigDecimal getTaxRate(Product product, Party practice) {
        TaxRules rules = new TaxRules(practice, service);
        return getRate(rules.getTaxRate(product));
    }

    /**
     * Returns a percentage / 100.
     *
     * @param percent the percent
     * @return <tt>percent / 100 </tt>
     */
    private BigDecimal getRate(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.ZERO) != 0) {
            return percent.divide(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

}
