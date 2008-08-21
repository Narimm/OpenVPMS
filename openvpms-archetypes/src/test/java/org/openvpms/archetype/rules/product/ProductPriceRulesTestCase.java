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
import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.UNIT_PRICE;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import static org.openvpms.archetype.test.TestHelper.getDate;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;


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
     * Tests the {@link ProductPriceRules#getProductPrice} method.
     */
    public void testGetProductPrice() {
        ProductPrice fixed1 = createPrice(FIXED_PRICE, "2008-1-1", "2008-1-31");
        ProductPrice fixed2 = createPrice(FIXED_PRICE, "2008-2-1",
                                          "2008-12-31");
        ProductPrice fixed3 = createPrice(FIXED_PRICE, "2008-2-1", null);

        ProductPrice unit1 = createPrice(UNIT_PRICE, "2008-1-1", "2008-1-10");
        ProductPrice unit2 = createPrice(UNIT_PRICE, "2008-2-1", null);

        assertNull(rules.getProductPrice(product, FIXED_PRICE, new Date()));
        assertNull(rules.getProductPrice(product, UNIT_PRICE, new Date()));

        fixed3.setFromDate(getDate("2009-1-1"));

        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        checkPrice(null, FIXED_PRICE, "2007-1-1");
        checkPrice(fixed1, FIXED_PRICE, "2008-1-1");
        checkPrice(fixed1, FIXED_PRICE, "2008-1-31");
        checkPrice(fixed2, FIXED_PRICE, "2008-2-1");
        checkPrice(fixed2, FIXED_PRICE, "2008-12-31");
        checkPrice(null, FIXED_PRICE, "2009-1-1");

        checkPrice(null, UNIT_PRICE, "2007-12-31");
        checkPrice(unit1, UNIT_PRICE, "2008-1-1");
        checkPrice(unit1, UNIT_PRICE, "2008-1-10");
        checkPrice(null, UNIT_PRICE, "2008-1-11");
        checkPrice(unit2, UNIT_PRICE, "2008-2-1");
        checkPrice(unit2, UNIT_PRICE, "2010-2-1"); // unbounded

        // verify that linked products are used if there are no matching prices
        // for the date
        Product priceTemplate = (Product) create(
                ProductArchetypes.PRICE_TEMPLATE);
        priceTemplate.addProductPrice(fixed3);
        priceTemplate.setName("XPriceTemplate");
        save(priceTemplate);

        EntityBean bean = new EntityBean(product);
        bean.addRelationship(ProductArchetypes.PRODUCT_LINK_RELATIONSHIP,
                             priceTemplate);
        bean.save();

        product = get(product);
        checkPrice(fixed3, FIXED_PRICE, "2009-1-1");
        checkPrice(fixed2, FIXED_PRICE, "2008-12-31");
    }

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
     * Verfies a price matches that expected.
     *
     * @param expected  the expected price
     * @param shortName the price short name
     * @param date      the date that the price applies to
     */
    private void checkPrice(ProductPrice expected, String shortName,
                            String date) {
        assertEquals(expected,
                     rules.getProductPrice(product, shortName, getDate(date)));
    }

    /**
     * Helper to create a new price.
     *
     * @param shortName the short name
     * @param from      the active from date. May be <tt>null</tt>
     * @param to        the active to date. May be <tt>null</tt>
     */
    private ProductPrice createPrice(String shortName, String from, String to) {
        ProductPrice result = (ProductPrice) create(shortName);
        result.setName("XPrice");
        Date fromDate = (from != null) ? getDate(from) : null;
        Date toDate = (to != null) ? getDate(to) : null;
        result.setPrice(BigDecimal.ONE);
        result.setFromDate(fromDate);
        result.setToDate(toDate);
        return result;
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
        taxBean.setValue("code", "XTAXTYPE" + Math.abs(new Random().nextInt()));
        taxBean.setValue("rate", new BigDecimal(10));
        taxBean.save();
        practice.addClassification(tax);
        save(practice);

        return practice;
    }
}
