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

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.EnumSet;

import static org.openvpms.archetype.rules.math.CurrencyException.ErrorCode.InvalidCurrencyCode;
import static org.openvpms.archetype.rules.math.CurrencyException.ErrorCode.InvalidRoundingMode;


/**
 * Represents a currency. Currencies are identified by their ISO 4217 currency
 * codes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Currency {

    /**
     * The underlying currency.
     */
    private final java.util.Currency currency;

    /**
     * The rounding mode.
     */
    private final RoundingMode roundingMode;

    /**
     * The minimum cash denomination.
     */
    private final BigDecimal minDenomination;

    /**
     * Constants used in rounding.
     */
    private static final BigDecimal POS_HALF = new BigDecimal("0.5");
    private static final BigDecimal NEG_HALF = new BigDecimal("-0.5");
    private static final BigDecimal TWO = BigDecimal.valueOf(2);


    /**
     * Creates a new <tt>Currency</tt> from an <em>lookup.currency</em>.
     *
     * @throws CurrencyException if the currency code or rounding mode is
     *                           invalid
     */
    public Currency(Lookup lookup, IArchetypeService service) {
        String code = lookup.getCode();
        currency = java.util.Currency.getInstance(code);
        if (currency == null) {
            throw new CurrencyException(InvalidCurrencyCode, code);
        }
        IMObjectBean bean = new IMObjectBean(lookup, service);
        String mode = bean.getString("roundingMode");
        roundingMode = RoundingMode.valueOf(mode);
        if (roundingMode == null) {
            throw new CurrencyException(InvalidRoundingMode, mode, code);
        }
        minDenomination = bean.getBigDecimal("minDenomination");
    }

    /**
     * Creates a new <tt>Currency</tt>.
     *
     * @param currency        the underlying currency
     * @param roundingMode    the rounding mode
     * @param minDenomination the minimum cash denonmination
     */
    public Currency(java.util.Currency currency,
                    RoundingMode roundingMode,
                    BigDecimal minDenomination) {
        EnumSet<RoundingMode> modes = EnumSet.of(RoundingMode.HALF_UP,
                                                 RoundingMode.HALF_DOWN,
                                                 RoundingMode.HALF_EVEN);
        if (!modes.contains(roundingMode)) {
            throw new CurrencyException(InvalidRoundingMode, roundingMode,
                                        currency.getCurrencyCode());
        }
        this.currency = currency;
        this.roundingMode = roundingMode;
        this.minDenomination = minDenomination;
    }

    /**
     * Returns the ISO 4217 currency code of this currency.
     *
     * @return the ISO 4217 currency code of this currency
     */
    public String getCode() {
        return currency.getCurrencyCode();
    }

    /**
     * Gets the default number of fraction digits used with this currency.
     * For example, the default number of fraction digits for the Euro is 2,
     * while for the Japanese Yen it's 0.
     * In the case of pseudo-currencies, such as IMF Special Drawing Rights,
     * -1 is returned.
     *
     * @return the default number of fraction digits used with this currency
     */
    public int getDefaultFractionDigits() {
        return currency.getDefaultFractionDigits();
    }

    /**
     * Rounds an amount to the no. of digits specified by
     * {@link #getDefaultFractionDigits()}. If the digits are <tt>-1</tt>
     * returns the value unchanged.
     *
     * @param value the value to round
     * @return the rounded value
     */
    public BigDecimal round(BigDecimal value) {
        int digits = getDefaultFractionDigits();
        return (digits != -1) ? value.setScale(digits, roundingMode) : value;
    }

    /**
     * Rounds an amount to the nearest minimum denomination,
     * or <tt>defaultFractionDigits</tt> if the minimum denomination is not
     * specified.
     * Uses the following algorithm:
     * <tt>
     * roundCash = integer(value / minDenomination) * minDenomination
     * temp = value * minDenomination
     * intTemp = integer(temp + 0.5 + sign(value))
     * if temp - integer(temp) == 0.5 then
     * if roundingMode == HALF_DOWN then
     * intTemp = intTemp - sign(value)
     * else if roundingMode == HALF_EVEN then
     * if intTemp / 2 == integer(intTemp / 2) then
     * intTemp = intTemp - sign(value)
     * end
     * end
     * end
     * roundCash = intTemp * minDenomination
     * </tt>
     *
     * @param value the value to round
     * @return the rounded value
     */
    public BigDecimal roundCash(BigDecimal value) {
        if (minDenomination.compareTo(BigDecimal.ZERO) == 0) {
            return round(value);
        }
        BigDecimal temp = value.divide(minDenomination);
        BigInteger intPart = temp.toBigInteger();
        value = new BigDecimal(intPart).multiply(minDenomination);
        BigInteger intTemp = temp.add(getHalf(value)).toBigInteger();

        // handle rounding 0.5 according to the rounding mode
        if (temp.subtract(new BigDecimal(intPart)).compareTo(POS_HALF) == 0) {
            if (roundingMode == RoundingMode.HALF_UP) {
                // no-op
            } else if (roundingMode == RoundingMode.HALF_DOWN) {
                intTemp = intTemp.subtract(BigInteger.valueOf(value.signum()));
            } else if (roundingMode == RoundingMode.HALF_EVEN) {
                BigDecimal div2 = new BigDecimal(intTemp).divide(TWO);
                if (div2.compareTo(new BigDecimal(div2.toBigInteger())) != 0) {
                    intTemp = intTemp.subtract(
                            BigInteger.valueOf(value.signum()));
                }
            }
        }
        value = new BigDecimal(intTemp).multiply(minDenomination);
        return value;
    }

    /**
     * Helper to return +/-0.5 based on the sign of the specified value.
     *
     * @param value the value
     * @return <tt>0.5</tt> if <tt>value != -1</tt>; otherwise <tt>-0.5</tt>
     */
    private BigDecimal getHalf(BigDecimal value) {
        return value.signum() != -1 ? POS_HALF : NEG_HALF;
    }
}
