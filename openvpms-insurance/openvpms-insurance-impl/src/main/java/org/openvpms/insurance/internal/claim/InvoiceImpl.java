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

package org.openvpms.insurance.internal.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.insurance.claim.Invoice;
import org.openvpms.insurance.claim.Item;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of the {@link Invoice} interface.
 *
 * @author Tim Anderson
 */
public class InvoiceImpl implements Invoice {

    /**
     * The invoice identifier.
     */
    private final long id;

    /**
     * The invoice finalisation date.
     */
    private final Date date;

    /**
     * The invoice items being claimed.
     */
    private final List<Item> items;

    /**
     * Constructs an {@link InvoiceImpl}.
     *
     * @param invoice the invoice
     * @param items   the items being claimed
     */
    public InvoiceImpl(Act invoice, List<Item> items) {
        this.id = invoice.getId();
        this.date = invoice.getActivityStartTime();
        this.items = items;
    }

    /**
     * Returns the invoice identifier.
     *
     * @return the invoice identifier
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Returns the date when the invoice was finalised.
     *
     * @return the date
     */
    @Override
    public Date getDate() {
        return date;
    }

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    @Override
    public BigDecimal getDiscount() {
        BigDecimal result = BigDecimal.ZERO;
        for (Item item : items) {
            result = result.add(item.getDiscount());
        }
        return result;
    }

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    @Override
    public BigDecimal getDiscountTax() {
        BigDecimal result = BigDecimal.ZERO;
        for (Item item : items) {
            result = result.add(item.getDiscountTax());
        }
        return result;
    }

    /**
     * Returns the total amount, including tax.
     *
     * @return the total amount
     */
    @Override
    public BigDecimal getTotal() {
        BigDecimal result = BigDecimal.ZERO;
        for (Item item : items) {
            result = result.add(item.getTotal());
        }
        return result;
    }

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    @Override
    public BigDecimal getTotalTax() {
        BigDecimal result = BigDecimal.ZERO;
        for (Item item : items) {
            result = result.add(item.getTotalTax());
        }
        return result;
    }

    /**
     * Returns the items being claimed.
     *
     * @return the items being claimed
     */
    @Override
    public List<Item> getItems() {
        return items;
    }
}
