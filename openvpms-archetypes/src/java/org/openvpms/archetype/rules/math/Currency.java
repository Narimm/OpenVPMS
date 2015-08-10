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
 * Represents a currency. Currencies are identified by their ISO 4217 currency codes.
 *
 * @author Tim Anderson
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
     * The minimum price.
     */
    private final BigDecimal minPrice;

    /**
     * Constants used in rounding.
     */
    private static final BigDecimal POS_HALF = new BigDecimal("0.5");
    private static final BigDecimal NEG_HALF = new BigDecimal("-0.5");
    private static final BigDecimal TWO = BigDecimal.valueOf(2);


    /**
     * Constructs a {@link Currency} from an <em>lookup.currency</em>.
     *
     * @throws CurrencyException if the currency code or rounding mode is invalid
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
        minDenomination = bean.getBigDecimal("minDenomination", BigDecimal.ZERO);
        minPrice = bean.getBigDecimal("minPrice", BigDecimal.ZERO);
    }

    /**
     * Constructs a {@link Currency}.
     * <p/>
     * Both the {@link #getMinimumDenomination()} and {@link #getMinimumPrice()} are set to the
     * {@link #getDefaultRoundingAmount()}.
     *
     * @param currency     the underlying currency
     * @param roundingMode the rounding mode
     */
    public Currency(java.util.Currency currency, RoundingMode roundingMode) {
        this(currency, roundingMode, getRoundingAmount(currency.getDefaultFractionDigits()));
    }

    /**
     * Constructs a {@link Currency}.
     * <p/>
     * The {@link #getMinimumPrice()} is set to the {@link #getDefaultRoundingAmount()}.
     *
     * @param currency        the underlying currency
     * @param roundingMode    the rounding mode
     * @param minDenomination the minimum cash denomination
     */
    public Currency(java.util.Currency currency, RoundingMode roundingMode, BigDecimal minDenomination) {
        this(currency, roundingMode, minDenomination, getRoundingAmount(currency.getDefaultFractionDigits()));
    }

    /**
     * Constructs a {@link Currency}.
     *
     * @param currency        the underlying currency
     * @param roundingMode    the rounding mode
     * @param minDenomination the minimum cash denomination
     * @param minPrice        the minimum price
     */
    public Currency(java.util.Currency currency, RoundingMode roundingMode, BigDecimal minDenomination,
                    BigDecimal minPrice) {
        EnumSet<RoundingMode> modes = EnumSet.of(RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN);
        if (!modes.contains(roundingMode)) {
            throw new CurrencyException(InvalidRoundingMode, roundingMode, currency.getCurrencyCode());
        }
        this.currency = currency;
        this.roundingMode = roundingMode;
        this.minDenomination = minDenomination;
        this.minPrice = minPrice;
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
     * In the case of pseudo-currencies, such as XAU Gold, -1 is returned.
     *
     * @return the default number of fraction digits used with this currency
     */
    public int getDefaultFractionDigits() {
        return currency.getDefaultFractionDigits();
    }

    /**
     * Returns the default rounding amount for a currency, based on the default number of fraction digits.
     * <p/>
     * For example, the default number of fraction digits for the Euro is 2, so the default rounding amount is 0.01,
     * whereas for the Yen it's 0, so the default rounding amount is 0.
     * <p/>
     * If a currency has {@code -1} for {@link #getDefaultFractionDigits()}, then {@code 0.01} is returned.
     */
    public BigDecimal getDefaultRoundingAmount() {
        return getRoundingAmount(getDefaultFractionDigits());
    }

    /**
     * Rounds an amount to the no. of digits specified by
     * {@link #getDefaultFractionDigits()}. If the digits are {@code -1}
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
     * Rounds an amount to the nearest minimum cash denomination, or {@code defaultFractionDigits} if the minimum
     * denomination is not specified.
     *
     * @param value the value to round
     * @return the rounded value
     */
    public BigDecimal roundCash(BigDecimal value) {
        return roundTo(value, minDenomination);
    }

    /**
     * Rounds a price to the nearest minimum price, or {@code defaultFractionDigits} if the minimum price is not
     * specified.
     *
     * @param price the value to round
     * @return the rounded value
     */
    public BigDecimal roundPrice(BigDecimal price) {
        return roundTo(price, minPrice);
    }

    /**
     * Returns the minimum denomination.
     *
     * @return the minimum denomination
     */
    public BigDecimal getMinimumDenomination() {
        return minDenomination;
    }

    /**
     * Returns the minimum price to round prices to.
     *
     * @return the minimum price to round prices to, or {@code 0} if prices should be rounded to the currency default
     */
    public BigDecimal getMinimumPrice() {
        return minPrice;
    }

    /**
     * Rounds an amount to the nearest minimum value or {@code defaultFractionDigits} if the minimum is not
     * specified.
     * <p/>
     * Uses the following algorithm:
     * <pre>
     * value = integer(value / minimum) * minimum
     * temp = value * minimum
     * intTemp = integer(temp + 0.5 + sign(value))
     * if temp - integer(temp) == 0.5 then
     *     if roundingMode == HALF_DOWN then
     *        intTemp = intTemp - sign(value)
     *     else if roundingMode == HALF_EVEN then
     *        if intTemp / 2 == integer(intTemp / 2) then
     *           intTemp = intTemp - sign(value)
     *        end
     *     end
     * end
     * roundTo = intTemp * minimum
     * </pre>
     *
     * @param value the value to round
     * @return the rounded value
     */
    protected BigDecimal roundTo(BigDecimal value, BigDecimal minimum) {
        if (minimum.compareTo(BigDecimal.ZERO) == 0) {
            return round(value);
        }
        BigDecimal temp = value.divide(minimum);
        BigInteger intPart = temp.toBigInteger();
        value = new BigDecimal(intPart).multiply(minimum);
        BigInteger intTemp = temp.add(getHalf(value)).toBigInteger();

        // handle rounding 0.5 according to the rounding mode
        if (temp.subtract(new BigDecimal(intPart)).compareTo(POS_HALF) == 0) {
            if (roundingMode == RoundingMode.HALF_DOWN) {
                intTemp = intTemp.subtract(BigInteger.valueOf(value.signum()));
            } else if (roundingMode == RoundingMode.HALF_EVEN) {
                BigDecimal div2 = new BigDecimal(intTemp).divide(TWO);
                if (div2.compareTo(new BigDecimal(div2.toBigInteger())) != 0) {
                    intTemp = intTemp.subtract(BigInteger.valueOf(value.signum()));
                }
            }
        }
        value = new BigDecimal(intTemp).multiply(minimum);
        return value;
    }

    /**
     * Helper to return +/-0.5 based on the sign of the specified value.
     *
     * @param value the value
     * @return {@code 0.5} if {@code value != -1}; otherwise {@code -0.5}
     */
    private BigDecimal getHalf(BigDecimal value) {
        return value.signum() != -1 ? POS_HALF : NEG_HALF;
    }

    /**
     * Returns a rounding amount.
     * <p/>
     * If the digits are < 0, returns 0.01
     *
     * @param digits the number of digits
     * @return the rounding amount
     */
    private static BigDecimal getRoundingAmount(int digits) {
        if (digits < 0) {
            digits = 2;
        }
        return BigDecimal.ONE.movePointLeft(digits);
    }

}
