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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.claim;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Represents an invoice being claimed in an insurance claim.
 *
 * @author Tim Anderson
 */
public interface Invoice {

    /**
     * Returns the invoice identifier.
     *
     * @return the invoice identifier
     */
    long getId();

    /**
     * Returns the date when the invoice was finalised.
     *
     * @return the date
     */
    Date getDate();

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    BigDecimal getDiscount();

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    BigDecimal getDiscountTax();

    /**
     * Returns the total amount, including tax.
     *
     * @return the total amount
     */
    BigDecimal getTotal();

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    BigDecimal getTotalTax();

    /**
     * Returns the items being claimed.
     *
     * @return the items being claimed
     */
    List<Item> getItems();
}
