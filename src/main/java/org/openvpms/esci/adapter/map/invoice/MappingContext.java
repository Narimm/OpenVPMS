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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper to contain the invoice mapping context.
 */
class MappingContext {

    /**
     * The invoice.
     */
    private final UBLInvoice invoice;

    /**
     * The supplier.
     */
    private final Party supplier;

    /**
     * The stock location.
     */
    private final Party stockLocation;

    /**
     * The document-level order.  May be <tt>null</tt>
     */
    private FinancialAct docOrder;

    /**
     * The orders associated with the invoice.
     */
    private Map<IMObjectReference, FinancialAct> orders = new HashMap<IMObjectReference, FinancialAct>();

    /**
     * The orders items keyed on their orders.
     */
    private Map<FinancialAct, List<FinancialAct>> items = new HashMap<FinancialAct, List<FinancialAct>>();

    /**
     * The invoice lines, and associated order items.
     */
    private List<InvoiceLineState> lines;


    /**
     * Constructs a <tt>MappingContext</tt>.
     *
     * @param invoice       the invoice
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @param docOrder      the document-level order. May be <tt>null</tt>
     */
    public MappingContext(UBLInvoice invoice, Party supplier, Party stockLocation, FinancialAct docOrder) {
        this.invoice = invoice;
        this.supplier = supplier;
        this.stockLocation = stockLocation;
        this.docOrder = docOrder;
        if (docOrder != null) {
            addOrder(docOrder);
        }
    }

    /**
     * Returns the invoice.
     *
     * @return the invoice
     */
    public UBLInvoice getInvoice() {
        return invoice;
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier
     */
    public Party getSupplier() {
        return supplier;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location
     */
    public Party getStockLocation() {
        return stockLocation;
    }

    /**
     * Returns the document-level order.
     *
     * @return the document level order, or <tt>null</tt> if none was specified
     */
    public FinancialAct getDocumentOrder() {
        return docOrder;
    }

    /**
     * Returns an order given its reference.
     * <p/>
     * The order must have been added previously via {@link #addOrder}, or be the document order.
     *
     * @param reference the order reference
     * @return the corresponding order, or <tt>null</tt> if it is not found
     */
    public FinancialAct getOrder(IMObjectReference reference) {
        return orders.get(reference);
    }

    /**
     * Returns all orders associated with the invoice.
     *
     * @return the orders
     */
    public List<FinancialAct> getOrders() {
        return new ArrayList<FinancialAct>(orders.values());
    }

    /**
     * Adds an order associated with the invoice.
     *
     * @param order the order
     */
    public void addOrder(FinancialAct order) {
        orders.put(order.getObjectReference(), order);
    }

    /**
     * Registers the order items for an order.
     *
     * @param order the order
     * @param items the order's items
     */
    public void setOrderItems(FinancialAct order, List<FinancialAct> items) {
        this.items.put(order, items);
    }

    /**
     * Returns the order items, keyed on their orders.
     *
     * @return the order items
     */
    public Map<FinancialAct, List<FinancialAct>> getOrderItems() {
        return items;
    }

    /**
     * Registers the invoice lines.
     *
     * @param lines the invoice lines
     */
    public void setInvoiceLines(List<InvoiceLineState> lines) {
        this.lines = lines;
    }

    /**
     * Returns the invoice lines.
     *
     * @return the invoice lines
     */
    public List<InvoiceLineState> getInvoiceLines() {
        return lines;
    }

}
