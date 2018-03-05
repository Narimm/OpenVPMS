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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;

/**
 * Tests the {@link ProductPriceEditor}.
 *
 * @author Tim Anderson
 */
public class ProductPriceEditorTestCase extends AbstractAppTest {

    /**
     * The context.
     */
    private Context context;

    /**
     * The product.
     */
    private Product product;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // NOTE: need to create the practice prior to the application as it caches the practice in the context
        Party practice = TestHelper.getPractice();
        practice.addClassification(TestHelper.createTaxType(BigDecimal.TEN));
        save(practice);

        context = new LocalContext();
        context.setPractice(practice);

        product = TestHelper.createProduct();

        super.setUp();
    }

    /**
     * Verifies that setting the cost, markup and price updates the appropriate fields.
     */
    @Test
    public void testUpdates() {
        ProductPrice unitPrice = (ProductPrice) create(ProductArchetypes.UNIT_PRICE);
        checkUpdates(unitPrice);

        ProductPrice fixedPrice = (ProductPrice) create(ProductArchetypes.FIXED_PRICE);
        checkUpdates(fixedPrice);
    }

    /**
     * Verifies that setting the cost, markup and price updates the appropriate fields.
     *
     * @param price the price to check
     */
    protected void checkUpdates(ProductPrice price) {
        DefaultLayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        ProductPriceEditor editor = new ProductPriceEditor(price, product, layout);

        // check the defaults
        checkPrice(editor, "0.00", "100", "0.00", "0.00", "100");

        // set the cost and verify the price and max discount change
        editor.setCost(BigDecimal.ONE);
        checkPrice(editor, "1.00", "100", "2.00", "2.20", "50");

        // set the markup and verify the price and max discount change
        editor.setMarkup(BigDecimal.valueOf(50));
        checkPrice(editor, "1.00", "50", "1.50", "1.65", "33.3");

        // set the price and verify the markup and max discount changes
        editor.setPrice(BigDecimal.valueOf(2));
        checkPrice(editor, "1.00", "100", "2.00", "2.20", "50");
    }

    /**
     * Verifies that if the currency has a minPrice, calculated prices are rounded to it.
     */
    @Test
    public void testPriceRounding() {
        DefaultLayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Lookup currency = TestHelper.getCurrency("AUD");
        IMObjectBean bean = new IMObjectBean(currency);
        bean.setValue("minPrice", "0.00");
        bean.save();

        // first test without minPrice rounding
        ProductPrice price1 = (ProductPrice) create(ProductArchetypes.UNIT_PRICE);
        ProductPriceEditor editor1 = new ProductPriceEditor(price1, product, layout);

        editor1.setCost(BigDecimal.valueOf(1.63));
        checkPrice(editor1, "1.63", "100", "3.26", "3.59", "50");

        editor1.setMarkup(BigDecimal.valueOf(50));
        checkPrice(editor1, "1.63", "50", "2.445", "2.69", "33.30");

        editor1.setMarkup(BigDecimal.valueOf(75));
        checkPrice(editor1, "1.63", "75", "2.853", "3.14", "42.90");

        // now enable rounding to 0.20
        bean.setValue("minPrice", "0.20");
        bean.save();

        ProductPrice price2 = (ProductPrice) create(ProductArchetypes.UNIT_PRICE);
        ProductPriceEditor editor2 = new ProductPriceEditor(price2, product, layout);

        // note that the markup changes as it is now recalculated after the price is calculated.
        editor2.setCost(BigDecimal.valueOf(1.63));
        checkPrice(editor2, "1.63", "100.00", "3.26", "3.60", "50.00");

        editor2.setMarkup(BigDecimal.valueOf(50));
        checkPrice(editor2, "1.63", "50.00", "2.445", "2.60", "33.30");

        editor2.setMarkup(BigDecimal.valueOf(75));
        checkPrice(editor2, "1.63", "75", "2.853", "3.20", "42.90");
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param editor      the editor to check
     * @param cost        the expected cost
     * @param markup      the expected markup
     * @param price       the expected tax-exclusive price
     * @param taxIncPrice the expected tax-inclusive price
     * @param maxDiscount the expected max discount
     */
    private void checkPrice(ProductPriceEditor editor, String cost, String markup, String price, String taxIncPrice,
                            String maxDiscount) {
        checkEquals(new BigDecimal(cost), editor.getCost());
        checkEquals(new BigDecimal(markup), editor.getMarkup());
        checkEquals(new BigDecimal(price), editor.getPrice());
        checkEquals(new BigDecimal(taxIncPrice), editor.getTaxInclusivePrice());
        checkEquals(new BigDecimal(maxDiscount), editor.getMaxDiscount());
    }
}
