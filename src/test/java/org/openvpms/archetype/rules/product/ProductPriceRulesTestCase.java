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

import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;


/**
 * Tests the {@link ProductPriceRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductPriceRulesTestCase extends ArchetypeServiceTest {

    /**
     * The product.
     */
    private Product product;

    /**
     * The <em>party.organisationPractice</em>, for taxes.
     */
    private Party practice;

    /**
     * The practice location currency.
     */
    private Currency currency;

    /**
     * The rules.
     */
    private ProductPriceRules rules;


    /**
     * Tests the {@link ProductPriceRules#getPrice} method.
     */
    public void testGetPrice() {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal markup = BigDecimal.valueOf(100); // 100% markup
        BigDecimal price = rules.getPrice(product, cost, markup, practice,
                                          currency);
        assertEquals(new BigDecimal("2.20"), price);
    }

    /**
     * Tests the {@link ProductPriceRules#getMarkup} method.
     */
    public void testGetMarkup() {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal price = new BigDecimal("2.20");
        BigDecimal markup = rules.getMarkup(product, cost, price, practice);
        assertEquals(BigDecimal.valueOf(100), markup);
    }

    /**
     * Sets up the test case.
     * <p/>
     * This sets up the practice to have a 10% tax on all products.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        product = TestHelper.createProduct();
        practice = createPractice();
        rules = new ProductPriceRules();
        IMObjectBean bean = new IMObjectBean(practice);
        Currencies currencies = new Currencies();
        currency = currencies.getCurrency(bean.getString("currency"));
    }

    /**
     * Helper to create an <em>party.organisationPractice</em> with a 10%
     * tax rate.
     *
     * @return the practice
     */
    private Party createPractice() {
        Party practice = TestHelper.getPractice();

        // add a 10% tax rate
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean taxBean = new IMObjectBean(tax);
        taxBean.setValue("code", "XTAXTYPE-" + tax.hashCode());
        taxBean.setValue("rate", new BigDecimal(10));
        taxBean.save();
        practice.addClassification(tax);
        save(practice);

        return practice;
    }
}
