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

package org.openvpms.archetype.rules.math;

import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Tests the {@link Currency} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CurrencyTestCase extends TestCase {

    /**
     * Tests the {@link Currency#round(BigDecimal)} method.
     */
    @Test
    public void testRound() {
        Currency halfUp = new Currency(java.util.Currency.getInstance("AUD"),
                                       RoundingMode.HALF_UP,
                                       new BigDecimal("0.05"));
        Currency halfDown = new Currency(java.util.Currency.getInstance("AUD"),
                                         RoundingMode.HALF_DOWN,
                                         new BigDecimal("0.05"));
        Currency halfEven = new Currency(java.util.Currency.getInstance("AUD"),
                                         RoundingMode.HALF_EVEN,
                                         new BigDecimal("0.05"));
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
        Currency halfUp = new Currency(java.util.Currency.getInstance("AUD"),
                                       RoundingMode.HALF_UP,
                                       new BigDecimal("0.05"));
        checkRoundCash(halfUp, "1.925", "1.95");
        checkRoundCash(halfUp, "1.95", "1.95");
        checkRoundCash(halfUp, "1.96", "1.95");
        checkRoundCash(halfUp, "1.97", "1.95");
        checkRoundCash(halfUp, "1.975", "2.00");
        checkRoundCash(halfUp, "1.98", "2.00");

        Currency halfDown = new Currency(java.util.Currency.getInstance("AUD"),
                                         RoundingMode.HALF_DOWN,
                                         new BigDecimal("0.05"));
        checkRoundCash(halfDown, "1.925", "1.90");
        checkRoundCash(halfDown, "1.95", "1.95");
        checkRoundCash(halfDown, "1.96", "1.95");
        checkRoundCash(halfDown, "1.97", "1.95");
        checkRoundCash(halfDown, "1.975", "1.95");
        checkRoundCash(halfDown, "1.98", "2.00");

        Currency halfEven = new Currency(java.util.Currency.getInstance("AUD"),
                                         RoundingMode.HALF_EVEN,
                                         new BigDecimal("0.05"));
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
            boolean valid = mode == RoundingMode.HALF_UP
                            || mode == RoundingMode.HALF_DOWN
                            || mode == RoundingMode.HALF_EVEN;
            try {
                new Currency(java.util.Currency.getInstance("AUD"),
                             mode, new BigDecimal("0.05"));
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
     * Tests the {@link Currency#round(BigDecimal)} method.
     *
     * @param currency the currency
     * @param value    the value to round
     * @param expected the expected result
     */
    private void checkRound(Currency currency, String value, String expected) {
        BigDecimal e = new BigDecimal(expected);
        assertEquals(e, currency.round(new BigDecimal(value)));
    }

    /**
     * Tests the {@link Currency#roundCash(BigDecimal)} method.
     *
     * @param currency the currency
     * @param value    the value to round
     * @param expected the expected result
     */
    private void checkRoundCash(Currency currency, String value,
                                String expected) {
        BigDecimal e = new BigDecimal(expected);
        assertEquals(e, currency.roundCash(new BigDecimal(value)));
    }
}
