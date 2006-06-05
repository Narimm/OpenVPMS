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
 * 
 * NOTE: This will evolve once we get requirements
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Money extends BigDecimal {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * default constructor
     */
    public Money() {
        super(0.0);
    }
    
    /**
     * Construct from a {@link BigDecimal}
     * 
     * @param value
     */
    public Money(BigDecimal value) {
        super(value.toString());
    }
    
    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public Money(char[] arg0, int arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public Money(char[] arg0, int arg1, int arg2, MathContext arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    /**
     * @param arg0
     */
    public Money(char[] arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public Money(char[] arg0, MathContext arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public Money(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public Money(String arg0, MathContext arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public Money(double arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public Money(double arg0, MathContext arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public Money(BigInteger arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public Money(BigInteger arg0, MathContext arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public Money(BigInteger arg0, int arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public Money(BigInteger arg0, int arg1, MathContext arg2) {
        super(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     */
    public Money(int arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public Money(int arg0, MathContext arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public Money(long arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public Money(long arg0, MathContext arg1) {
        super(arg0, arg1);
    }

}
