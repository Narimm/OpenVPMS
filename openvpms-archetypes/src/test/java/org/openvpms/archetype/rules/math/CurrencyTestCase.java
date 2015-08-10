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

package org.openvpms.archetype.rules.math;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.test.TestHelper.checkEquals;


/**
 * Tests the {@link Currency} class.
 *
 * @author Tim Anderson
 */
public class CurrencyTestCase {

    /**
     * Tests the {@link Currency#round(BigDecimal)} method.
     */
    @Test
    public void testRound() {
        Currency halfUp = new Currency(getCurrency("AUD"), RoundingMode.HALF_UP, new BigDecimal("0.05"));
        Currency halfDown = new Currency(getCurrency("AUD"), RoundingMode.HALF_DOWN, new BigDecimal("0.05"));
        Currency halfEven = new Currency(getCurrency("AUD"), RoundingMode.HALF_EVEN, new BigDecimal("0.05"));
        checkRound(halfUp, "1.95", "1.95");
        checkRound(halfUp, "1.951", "1.95");
        checkRound(halfUp, "1.955", "1.96");
        checkRound(halfUp, "1.959", "1.96");

        checkRound(halfDown, "1.95", "1.95");
        checkRound(halfDown, "1.951", "1.95");
        checkRound(halfDown, "1.955", "1.95");
        checkRound(halfDown, "1.959", "1.96");

        checkRound(halfEven, "1.95", "1.95");
        checkRound(halfEven, "1.951", "1.95");
        checkRound(halfEven, "1.955", "1.96");
        checkRound(halfEven, "1.959", "1.96");
        checkRound(halfEven, "1.965", "1.96"); // round down to nearest even
    }

    /**
     * Tests the {@link Currency#roundCash(BigDecimal)} method.
     */
    @Test
    public void testRoundCash() {
        Currency halfUp = new Currency(getCurrency("AUD"), RoundingMode.HALF_UP, new BigDecimal("0.05"));
        checkRoundCash(halfUp, "1.925", "1.95");
        checkRoundCash(halfUp, "1.95", "1.95");
        checkRoundCash(halfUp, "1.96", "1.95");
        checkRoundCash(halfUp, "1.97", "1.95");
        checkRoundCash(halfUp, "1.975", "2.00");
        checkRoundCash(halfUp, "1.98", "2.00");

        Currency halfDown = new Currency(getCurrency("AUD"), RoundingMode.HALF_DOWN, new BigDecimal("0.05"));
        checkRoundCash(halfDown, "1.925", "1.90");
        checkRoundCash(halfDown, "1.95", "1.95");
        checkRoundCash(halfDown, "1.96", "1.95");
        checkRoundCash(halfDown, "1.97", "1.95");
        checkRoundCash(halfDown, "1.975", "1.95");
        checkRoundCash(halfDown, "1.98", "2.00");

        Currency halfEven = new Currency(getCurrency("AUD"), RoundingMode.HALF_EVEN, new BigDecimal("0.05"));
        checkRoundCash(halfEven, "1.925", "1.90"); // round down to nearest even
        checkRoundCash(halfEven, "1.95", "1.95");
        checkRoundCash(halfEven, "1.96", "1.95");
        checkRoundCash(halfEven, "1.97", "1.95");
        checkRoundCash(halfEven, "1.975", "2.00");
        checkRoundCash(halfEven, "1.98", "2.00");
    }

    /**
     * Verifies that the {@link Currency} constructor only supports
     * {@link RoundingMode#HALF_UP}, {@link RoundingMode#HALF_DOWN} and {@link RoundingMode#HALF_EVEN}.
     */
    @Test
    public void testValidRoundingModes() {
        for (RoundingMode mode : RoundingMode.values()) {
            boolean valid = mode == RoundingMode.HALF_UP || mode == RoundingMode.HALF_DOWN
                            || mode == RoundingMode.HALF_EVEN;
            try {
                new Currency(getCurrency("AUD"), mode, new BigDecimal("0.05"));
                if (!valid) {
                    fail("Expected " + mode + " to throw an exception");
                }
            } catch (CurrencyException exception) {
                if (valid) {
                    fail("Expected " + mode + " to not throw an exception");
                }
            }
        }
    }

    /**
     * Tests the {@link Currency#roundPrice(BigDecimal)} method.
     */
    @Test
    public void testRoundPrice() {
        BigDecimal minDenomination = new BigDecimal("0.01");
        BigDecimal minPrice = new BigDecimal("0.05"); // round all prices to 0.05
        java.util.Currency AUD = getCurrency("AUD");
        Currency halfUp = new Currency(AUD, RoundingMode.HALF_UP, minDenomination, minPrice);
        checkRoundPrice(halfUp, "1.925", "1.95");
        checkRoundPrice(halfUp, "1.95", "1.95");
        checkRoundPrice(halfUp, "1.96", "1.95");
        checkRoundPrice(halfUp, "1.97", "1.95");
        checkRoundPrice(halfUp, "1.975", "2.00");
        checkRoundPrice(halfUp, "1.98", "2.00");

        Currency halfDown = new Currency(AUD, RoundingMode.HALF_DOWN, minDenomination, minPrice);
        checkRoundPrice(halfDown, "1.925", "1.90");
        checkRoundPrice(halfDown, "1.95", "1.95");
        checkRoundPrice(halfDown, "1.96", "1.95");
        checkRoundPrice(halfDown, "1.97", "1.95");
        checkRoundPrice(halfDown, "1.975", "1.95");
        checkRoundPrice(halfDown, "1.98", "2.00");

        Currency halfEven = new Currency(AUD, RoundingMode.HALF_EVEN, minDenomination, minPrice);
        checkRoundPrice(halfEven, "1.925", "1.90"); // round down to nearest even
        checkRoundPrice(halfEven, "1.95", "1.95");
        checkRoundPrice(halfEven, "1.96", "1.95");
        checkRoundPrice(halfEven, "1.97", "1.95");
        checkRoundPrice(halfEven, "1.975", "2.00");
        checkRoundPrice(halfEven, "1.98", "2.00");
    }

    /**
     * Tests the {@link Currency#getMinimumDenomination()} method.
     */
    @Test
    public void getMinimumDenomination() {
        // if not specified, the minimum denomination should be derived from the getDefaultRoundingAmount().
        Currency aud = new Currency(getCurrency("AUD"), RoundingMode.HALF_UP);
        checkEquals(new BigDecimal("0.01"), aud.getMinimumDenomination());

        Currency jpy = new Currency(getCurrency("JPY"), RoundingMode.HALF_UP);
        checkEquals(BigDecimal.ONE, jpy.getMinimumDenomination());

        Currency xau = new Currency(getCurrency("XAU"), RoundingMode.HALF_UP); // gold
        checkEquals(new BigDecimal("0.01"), xau.getMinimumDenomination());

        // override the default
        Currency nzd = new Currency(getCurrency("NZD"), RoundingMode.HALF_UP, new BigDecimal("0.05"));
        checkEquals(new BigDecimal("0.05"), nzd.getMinimumDenomination());
    }

    /**
     * Tests the {@link Currency#getMinimumPrice()} method.
     */
    @Test
    public void getMinimumPrice() {
        // if not specified, the minimum price should be derived from the getDefaultRoundingAmount().
        Currency aud = new Currency(getCurrency("AUD"), RoundingMode.HALF_UP);
        checkEquals(new BigDecimal("0.01"), aud.getMinimumPrice());

        Currency jpy = new Currency(getCurrency("JPY"), RoundingMode.HALF_UP);
        checkEquals(BigDecimal.ONE, jpy.getMinimumPrice());

        Currency xau = new Currency(getCurrency("XAU"), RoundingMode.HALF_UP); // gold
        checkEquals(new BigDecimal("0.01"), xau.getMinimumPrice());

        // override the default denomination
        Currency nzd = new Currency(getCurrency("NZD"), RoundingMode.HALF_UP, new BigDecimal("0.05"));
        checkEquals(new BigDecimal("0.01"), nzd.getMinimumPrice());

        // override the default denomination and price
        Currency gbp = new Currency(getCurrency("GBP"), RoundingMode.HALF_UP, new BigDecimal("0.05"),
                                    new BigDecimal("0.20"));
        checkEquals(new BigDecimal("0.20"), gbp.getMinimumPrice());
    }

    /**
     * Tests the {@link Currency#getDefaultRoundingAmount()} method.
     */
    @Test
    public void getDefaultRoundingAmount() {
        Currency aud = new Currency(getCurrency("AUD"), RoundingMode.HALF_UP);
        checkEquals(new BigDecimal("0.01"), aud.getDefaultRoundingAmount());

        Currency jpy = new Currency(getCurrency("JPY"), RoundingMode.HALF_UP);
        checkEquals(BigDecimal.ONE, jpy.getDefaultRoundingAmount());

        Currency xau = new Currency(getCurrency("XAU"), RoundingMode.HALF_UP); // gold
        checkEquals(new BigDecimal("0.01"), xau.getDefaultRoundingAmount());
    }

    /**
     * Tests the {@link Currency#round(BigDecimal)} and {@link Currency#roundTo(BigDecimal, BigDecimal)} methods.
     * <p/>
     * The former uses {@code BigDecimal.setScale(} supplying the default fraction digits for the currency.
     * The latter uses the {@link Currency#getDefaultRoundingAmount()}. Both should yield the same results although
     * the former should be faster.
     *
     * @param currency the currency
     * @param value    the value to round
     * @param expected the expected result
     */
    private void checkRound(Currency currency, String value, String expected) {
        BigDecimal e = new BigDecimal(expected);
        assertEquals(e, currency.round(new BigDecimal(value)));

        assertEquals(e, currency.roundTo(new BigDecimal(value), currency.getDefaultRoundingAmount()));
    }

    /**
     * Tests the {@link Currency#roundCash(BigDecimal)} method.
     *
     * @param currency the currency
     * @param value    the value to round
     * @param expected the expected result
     */
    private void checkRoundCash(Currency currency, String value, String expected) {
        BigDecimal e = new BigDecimal(expected);
        assertEquals(e, currency.roundCash(new BigDecimal(value)));
    }

    /**
     * Tests the {@link Currency#roundPrice(BigDecimal)} method.
     *
     * @param currency the currency
     * @param value    the value to round
     * @param expected the expected result
     */
    private void checkRoundPrice(Currency currency, String value, String expected) {
        BigDecimal e = new BigDecimal(expected);
        assertEquals(e, currency.roundPrice(new BigDecimal(value)));
    }

    /**
     * Helper to get a java currency instance.
     *
     * @param code the currency code
     * @return the corresponding currency
     */
    private java.util.Currency getCurrency(String code) {
        return java.util.Currency.getInstance(code);
    }

}
