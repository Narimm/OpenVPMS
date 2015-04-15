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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.domain.im.datatypes.quantity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;


/**
 * This is a marker class to denote a Money class.
 * <p/>
 * NOTE: This will evolve once we get requirements
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Money extends BigDecimal {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value 0, with a scale of 0.
     */
    public static final Money ZERO = new Money(0);

    /**
     * The value 1, with a scale of 0.
     */
    public static final Money ONE = new Money(1);

    /**
     * The value 10, with a scale of 0.
     */
    public static final Money TEN = new Money(10);


    /**
     * Default constructor.
     */
    public Money() {
        super(0);
    }

    /**
     * Construct from a {@link BigDecimal}.
     *
     * @param value the value
     */
    public Money(BigDecimal value) {
        super(value.toString());
    }

    /**
     * Translates a character array representation of a
     * <tt>Money</tt> into a <tt>Money</tt>, accepting the
     * same sequence of characters as the {@link #Money(String)}
     * constructor, while allowing a sub-array to be specified.
     *
     * @param in     <tt>char</tt> array that is the source of characters.
     * @param offset first character in the array to inspect.
     * @param len    number of characters to consider.
     * @throws NumberFormatException if <tt>in</tt> is not a valid
     *                               representation of a <tt>Money</tt> or
     *                               the defined subarray is not wholly within
     *                               <tt>in</tt>.
     */
    public Money(char[] in, int offset, int len) {
        super(in, offset, len);
    }

    /**
     * Translates a character array representation of a
     * <tt>Money</tt> into a <tt>Money</tt>, accepting the
     * same sequence of characters as the {@link #Money(String)}
     * constructor, while allowing a sub-array to be specified and
     * with rounding according to the context settings.
     *
     * @param in     <tt>char</tt> array that is the source of characters.
     * @param offset first character in the array to inspect.
     * @param len    number of characters to consider..
     * @param mc     the context to use.
     * @throws ArithmeticException   if the result is inexact but the
     *                               rounding mode is <tt>UNNECESSARY</tt>.
     * @throws NumberFormatException if <tt>in</tt> is not a valid
     *                               representation of a <tt>Money</tt> or the
     *                               defined subarray is not wholly within
     *                               <tt>in</tt>.
     */
    public Money(char[] in, int offset, int len, MathContext mc) {
        super(in, offset, len, mc);
    }

    /**
     * Translates a character array representation of a
     * <tt>Money</tt> into a <tt>Money</tt>, accepting the
     * same sequence of characters as the {@link #Money(String)}
     * constructor.
     *
     * @param in <tt>char</tt> array that is the source of characters.
     * @throws NumberFormatException if <tt>in</tt> is not a valid
     *                               representation of a <tt>Money</tt>.
     */
    public Money(char[] in) {
        super(in);
    }

    /**
     * Translates a character array representation of a
     * <tt>Money</tt> into a <tt>Money</tt>, accepting the
     * same sequence of characters as the {@link #Money(String)}
     * constructor and with rounding according to the context
     * settings.
     *
     * @param in <tt>char</tt> array that is the source of characters.
     * @param mc the context to use.
     * @throws ArithmeticException   if the result is inexact but the
     *                               rounding mode is <tt>UNNECESSARY</tt>.
     * @throws NumberFormatException if <tt>in</tt> is not a valid
     *                               representation of a <tt>Money</tt>.
     */
    public Money(char[] in, MathContext mc) {
        super(in, mc);
    }

    /**
     * Translates the string representation of a <tt>Money</tt>
     * into a <tt>Money</tt>.  The string representation consists
     * of an optional sign, <tt>'+'</tt> (<tt>'&#92;u002B'</tt>) or
     * <tt>'-'</tt> (<tt>'&#92;u002D'</tt>), followed by a sequence of
     * zero or more decimal digits ("the integer"), optionally
     * followed by a fraction, optionally followed by an exponent.
     *
     * @param value String representation of <tt>Money</tt>.
     * @throws NumberFormatException if <tt>val</tt> is not a valid
     *                               representation of a <tt>Money</tt>.
     * @see BigDecimal#BigDecimal(String)
     */
    public Money(String value) {
        super(value);
    }

    /**
     * Translates the string representation of a <tt>Money</tt>
     * into a <tt>Money</tt>, accepting the same strings as the
     * {@link #Money(String)} constructor, with rounding
     * according to the context settings.
     *
     * @param value string representation of a <tt>Money</tt>.
     * @param mc    the context to use.
     * @throws ArithmeticException   if the result is inexact but the
     *                               rounding mode is <tt>UNNECESSARY</tt>.
     * @throws NumberFormatException if <tt>val</tt> is not a valid
     *                               representation of a Money.
     */
    public Money(String value, MathContext mc) {
        super(value, mc);
    }

    /**
     * Translates a <tt>double</tt> into a <tt>Money</tt> which
     * is the exact decimal representation of the <tt>double</tt>'s
     * binary floating-point value.  The scale of the returned
     * <tt>Money</tt> is the smallest value such that
     * <tt>(10<sup>scale</sup> &times; val)</tt> is an integer.
     *
     * @param value <tt>double</tt> value to be converted to <tt>Money</tt>.
     * @throws NumberFormatException if <tt>value</tt> is infinite or NaN.
     */
    public Money(double value) {
        super(value);
    }

    /**
     * Translates a <tt>double</tt> into a <tt>Money</tt>, with
     * rounding according to the context settings.  The scale of the
     * <tt>Money</tt> is the smallest value such that
     * <tt>(10<sup>scale</sup> &times; val)</tt> is an integer.
     *
     * @param val <tt>double</tt> value to be converted to <tt>Money</tt>.
     * @param mc  the context to use.
     * @throws ArithmeticException   if the result is inexact but the
     *                               RoundingMode is UNNECESSARY.
     * @throws NumberFormatException if <tt>val</tt> is infinite or NaN.
     */
    public Money(double val, MathContext mc) {
        super(val, mc);
    }

    /**
     * Translates a <tt>BigInteger</tt> into a <tt>Money</tt>.
     * The scale of the <tt>Money</tt> is zero.
     *
     * @param value <tt>BigInteger</tt> value to be converted to <tt>Money</tt>.
     */
    public Money(BigInteger value) {
        super(value);
    }

    /**
     * Translates a <tt>BigInteger</tt> into a <tt>Money</tt>
     * rounding according to the context settings.  The scale of the
     * <tt>Money</tt> is zero.
     *
     * @param value <tt>BigInteger</tt> value to be converted to <tt>Money</tt>.
     * @param mc    the context to use
     * @throws ArithmeticException if the result is inexact but the
     *                             rounding mode is <tt>UNNECESSARY</tt>.
     */
    public Money(BigInteger value, MathContext mc) {
        super(value, mc);
    }

    /**
     * Translates a <tt>BigInteger</tt> unscaled value and an
     * <tt>int</tt> scale into a <tt>Money</tt>.  The value of the
     * <tt>Money</tt> is
     * <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
     *
     * @param unscaledVal unscaled value of the <tt>Money</tt>.
     * @param scale       scale of the <tt>Money</tt>.
     */
    public Money(BigInteger unscaledVal, int scale) {
        super(unscaledVal, scale);
    }

    /**
     * Translates a <tt>BigInteger</tt> unscaled value and an
     * <tt>int</tt> scale into a <tt>Money</tt>, with rounding
     * according to the context settings.  The value of the
     * <tt>Money</tt> is <tt>(unscaledVal &times;
     * 10<sup>-scale</sup>)</tt>, rounded according to the
     * <tt>precision</tt> and rounding mode settings.
     *
     * @param unscaledVal unscaled value of the <tt>Money</tt>.
     * @param scale       scale of the <tt>Money</tt>.
     * @param mc          the context to use
     * @throws ArithmeticException if the result is inexact but the
     *                             rounding mode is <tt>UNNECESSARY</tt>.
     */
    public Money(BigInteger unscaledVal, int scale, MathContext mc) {
        super(unscaledVal, scale, mc);
    }

    /**
     * Translates an <tt>int</tt> into a <tt>Money</tt>.  The
     * scale of the <tt>Money</tt> is zero.
     *
     * @param value <tt>int</tt> value to be converted to <tt>Money</tt>.
     */
    public Money(int value) {
        super(value);
    }

    /**
     * Translates an <tt>int</tt> into a <tt>Money</tt>, with
     * rounding according to the context settings.  The scale of the
     * <tt>Money</tt>, before any rounding, is zero.
     *
     * @param value <tt>int</tt> value to be converted to <tt>Money</tt>.
     * @param mc    the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *                             rounding mode is <tt>UNNECESSARY</tt>.
     */
    public Money(int value, MathContext mc) {
        super(value, mc);
    }

    /**
     * Translates a <tt>long</tt> into a <tt>Money</tt>.  The
     * scale of the <tt>Money</tt> is zero.
     *
     * @param value <tt>long</tt> value to be converted to <tt>Money</tt>.
     */
    public Money(long value) {
        super(value);
    }

    /**
     * Translates <tt>long</tt> into a <tt>Money</tt>, with rounding according
     * to the context settings.  The scale of the
     * <tt>Money</tt>, before any rounding, is zero.
     *
     * @param value <tt>long</tt> value to be converted to <tt>Money</tt>.
     * @param mc    the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *                             rounding mode is <tt>UNNECESSARY</tt>.
     */
    public Money(long value, MathContext mc) {
        super(value, mc);
    }

}
