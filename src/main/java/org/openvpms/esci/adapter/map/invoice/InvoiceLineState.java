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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.map.invoice;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;


/**
 * Helper to cache invoice line and associated order state.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class InvoiceLineState {

    /**
     * The invoice line.
     */
    private final UBLInvoiceLine line;

    /**
     * The product associated with the invoice line.
     */
    private final Product product;

    /**
     * The order associated with the invoice line.
     */
    private final FinancialAct order;

    /**
     * The order item associated with the invoice line.
     */
    private FinancialAct item;


    /**
     * Constructs an <tt>InvoiceLineState</tt>.
     *
     * @param line    the invoice line
     * @param product the invoice line product. May be <tt>null</tt>
     * @param order   the order. May be <tt>null</tt>
     * @param item    the order item. May be <tt>null</tt>
     */
    public InvoiceLineState(UBLInvoiceLine line, Product product, FinancialAct order, FinancialAct item) {
        this.line = line;
        this.product = product;
        this.order = order;
        this.item = item;
    }

    /**
     * Returns the invoice line.
     *
     * @return the invoice line
     */
    public UBLInvoiceLine getLine() {
        return line;
    }

    /**
     * Returns the product associated with the invoice line.
     *
     * @return the product. May be <tt>null</tt>
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Returns the order associated with the invoice line.
     *
     * @return the order. May be <tt>null</tt>
     */
    public FinancialAct getOrder() {
        return order;
    }

    /**
     * Returns the order item associated with the invoice line.
     *
     * @return the order item. May be <tt>null</tt>
     */
    public FinancialAct getOrderItem() {
        return item;
    }

    /**
     * Registers the order item.
     *
     * @param item the order item. May be <tt>null</tt>
     */
    public void setOrderItem(FinancialAct item) {
        this.item = item;
    }

    /**
     * Returns the invoiced quantity.
     *
     * @return the invoiced quantity
     */
    public BigDecimal getQuantity() {
        return line.getInvoicedQuantity();
    }

    /**
     * Determines if the invoice line isn't linked to an order item, but potentially could be.
     *
     * @return <tt>true</tt> if the line isn't linked, otherwise <tt>false</tt>
     */
    public boolean isUnlinked() {
        return item == null && order != null && product != null;
    }

}
