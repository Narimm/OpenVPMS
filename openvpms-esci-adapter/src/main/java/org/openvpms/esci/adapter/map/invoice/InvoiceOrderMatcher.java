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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.AbstractUBLMapper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Helper to match invoice lines to their corresponding order items.
 * Where an invoice line doesn't explicitly refer to an order line, an corresponding
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class InvoiceOrderMatcher extends AbstractUBLMapper {

    /**
     * The bean factory.
     */
    private final IMObjectBeanFactory factory;

    /**
     * Constructs an <tt>InvoiceOrderMatcher</tt>.
     *
     * @param service the archetype service
     * @param factory the bean factory
     */
    public InvoiceOrderMatcher(IArchetypeService service, IMObjectBeanFactory factory) {
        setArchetypeService(service);
        this.factory = factory;
    }

    /**
     * Matches invoice lines to their corresponding order lines.
     *
     * @param invoice       the invoice
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @return the mapping context containing the invoice line mapping and associated order items
     */
    public MappingContext match(UBLInvoice invoice, Party supplier, Party stockLocation) {
        FinancialAct order = invoice.getOrder();
        MappingContext context = new MappingContext(invoice, supplier, stockLocation, order);
        if (order != null) {
            checkOrder(order, context);
            addOrderItems(order, context);
        }

        List<InvoiceLineState> lines = new ArrayList<InvoiceLineState>();
        List<InvoiceLineState> unlinked = new ArrayList<InvoiceLineState>();

        // first pass - create state for each invoice line, and determine the list of unlinked lines
        for (UBLInvoiceLine line : invoice.getInvoiceLines()) {
            InvoiceLineState state = getState(line, context);
            lines.add(state);
            if (state.isUnlinked()) {
                unlinked.add(state);
            }
        }

        if (!unlinked.isEmpty()) {
            // second pass - link invoice lines to unlinked order items, where an exact match exists
            UnlinkedTracker tracker = new UnlinkedTracker(context);
            for (InvoiceLineState line : unlinked) {
                exactMatch(line, tracker);
            }

            // third pass - link invoice lines to unlinked order items, where a partial match exists
            for (InvoiceLineState line : tracker.getPartialMatches()) {
                partialMatch(line, tracker);
            }
        }
        context.setInvoiceLines(lines);
        return context;
    }

    /**
     * Returns the invoice line state for an invoice line.
     * <p/>
     * This ensures that any referenced order and product is valid.
     * <p/>
     * If there is:
     * <ul>
     * <li>a document-level order reference, then all order lines must reference this order
     * <li>no document level order reference, order references must be fully qualified i.e must specify both
     * the order line and order
     * <li>a document-level order reference, but no order line reference, the first order item matching the invoice item
     * will be returned
     * <ul>
     *
     * @param line    the invoice line
     * @param context the mapping context
     * @return the invoice line state
     * @throws ESCIAdapterException if the order reference or product was inccrrectly specified
     */
    protected InvoiceLineState getState(UBLInvoiceLine line, MappingContext context) {
        FinancialAct docOrder = context.getDocumentOrder();
        FinancialAct order;
        FinancialAct item = null;
        IMObjectReference orderRef = line.getOrderReference();
        IMObjectReference orderItemRef = line.getOrderItemReference();
        Product product = line.getProduct(context.getSupplier());

        if (orderItemRef != null) {
            // invoice line is referring to an order line
            if (orderRef != null) {
                // referencing an order. Make sure it can be retrieved
                order = getReferencedOrder(orderRef, line, context);
            } else {
                // no order reference specified, so must be working with the document level order
                if (docOrder == null) {
                    // no order was specified in the invoice line, and no document level order specified
                    // Expected 0 cardinality
                    throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                            "OrderLineReference", "InvoiceLine", line.getID(), "0", 1));
                }
                order = docOrder;
            }
            item = line.getOrderItem();
            if (item != null) {
                // make sure there is a relationship between the order and the order item
                ActBean bean = factory.createActBean(order);
                if (!bean.hasRelationship(SupplierArchetypes.ORDER_ITEM_RELATIONSHIP, item)) {
                    throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidOrderItem(
                            line.getID(), Long.toString(item.getId())));
                }
            }
        } else if (orderRef != null) {
            // referencing an order but no order item specified.
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                    "OrderLineReference/OrderReference", "InvoiceLine", line.getID(), "0", 1));
        } else {
            order = docOrder;
        }
        return new InvoiceLineState(line, product, order, item);
    }

    /**
     * Verifies that an order has a relationship to the expected supplier and stock location and is not
     * already associated with the invoice.
     *
     * @param order   the order
     * @param context the mapping context
     * @throws ESCIAdapterException      if the order wasn't submitted by the supplier or the invoice is a duplicate
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void checkOrder(FinancialAct order, MappingContext context) {
        checkOrder(order, context.getSupplier(), context.getStockLocation(), context.getInvoice());
        String invoiceId = context.getInvoice().getID();
        ActBean orderBean = factory.createActBean(order);
        List<FinancialAct> deliveries = orderBean.getNodeActs("deliveries", FinancialAct.class);
        for (FinancialAct delivery : deliveries) {
            ActBean deliveryBean = factory.createActBean(delivery);
            String supplierInvoiceId = deliveryBean.getString("supplierInvoiceId");
            if (ObjectUtils.equals(invoiceId, supplierInvoiceId)) {
                throw new ESCIAdapterException(ESCIAdapterMessages.duplicateInvoiceForOrder(invoiceId, order.getId()));
            }
        }
    }

    /**
     * Retrieves an order referenced by an invoice line.
     *
     * @param orderRef the order reference
     * @param line     the invoice line
     * @param context  the mapping context
     * @return the corresponding order
     */
    private FinancialAct getReferencedOrder(IMObjectReference orderRef, UBLInvoiceLine line, MappingContext context) {
        FinancialAct childOrder = context.getOrder(orderRef);
        if (childOrder == null) {
            // get the order and ensure it was submitted to the supplier from the same stock location
            childOrder = line.getOrder();
            checkOrder(childOrder, context);
            context.addOrder(childOrder);
            addOrderItems(childOrder, context);
        }
        FinancialAct docOrder = context.getDocumentOrder();
        if (docOrder != null && !ObjectUtils.equals(docOrder.getObjectReference(), childOrder.getObjectReference())) {
            // top-level order specified, but the child order is different.
            UBLInvoice invoice = context.getInvoice();
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidOrder(invoice.getType(), invoice.getID(),
                                                                            Long.toString(childOrder.getId())));
        }
        return childOrder;
    }

    /**
     * Attempts to match an invoice line to an unreferenced order item.
     * <p/>
     * This performs an exact match on product and quantity. If there is only a match on product, the line is
     * registered as partial match with the item.
     *
     * @param line     the invoice line
     * @param unlinked the set of unreferenced order items
     */
    private void exactMatch(InvoiceLineState line, UnlinkedTracker unlinked) {
        FinancialAct result = null;
        IMObjectReference invoiceRef = line.getProduct().getObjectReference();
        for (FinancialAct item : unlinked.getItems(line.getOrder())) {
            ActBean bean = factory.createActBean(item);
            IMObjectReference orderRef = bean.getNodeParticipantRef("product");
            if (ObjectUtils.equals(invoiceRef, orderRef)) {
                BigDecimal orderQuantity = item.getQuantity();
                if (line.getQuantity().compareTo(orderQuantity) == 0) {
                    result = item;
                    break;
                } else {
                    unlinked.addPartialMatch(line, item);
                }
            }
        }
        if (result != null) {
            line.setOrderItem(result);
            unlinked.remove(line);
        }
    }

    /**
     * Attempts to match an invoice line to an unreferenced order item.
     * <p/>
     * This performs an exact match on product and quantity. If there is only a match on product, the line is
     * registered as partial match with the item.
     *
     * @param line     the invoice line
     * @param unlinked the set of unreferenced order items
     */
    private void partialMatch(InvoiceLineState line, UnlinkedTracker unlinked) {
        List<FinancialAct> matches = unlinked.getPartialMatches(line);
        if (!matches.isEmpty()) {
            if (matches.size() > 1) {
                // sort items on ascending quantity.
                Collections.sort(matches, new Comparator<FinancialAct>() {
                    public int compare(FinancialAct o1, FinancialAct o2) {
                        return o1.getQuantity().compareTo(o2.getQuantity());
                    }
                });
            }
            line.setOrderItem(matches.get(0));
            unlinked.remove(line);
        }
    }


    /**
     * Adds order items associated with an order to the mapping context.
     *
     * @param order   the order
     * @param context the mapping context
     */
    private void addOrderItems(FinancialAct order, MappingContext context) {
        ActBean bean = factory.createActBean(order);
        context.setOrderItems(order, bean.getNodeActs("items", FinancialAct.class));
    }

    /**
     * Tracks unlinked invoice lines and order items.
     */
    private static class UnlinkedTracker {

        /**
         * The unlinked order items, keyed on order.
         */
        private Map<FinancialAct, List<FinancialAct>> unlinked;

        /**
         * The order items that partially match an invoice lines.
         */
        private Map<InvoiceLineState, List<FinancialAct>> partial = new HashMap<InvoiceLineState, List<FinancialAct>>();


        /**
         * Constructs an <tt>UnlinkedTracker</tt>.
         *
         * @param context the mapping context
         */
        public UnlinkedTracker(MappingContext context) {
            unlinked = new HashMap<FinancialAct, List<FinancialAct>>(context.getOrderItems());
        }

        /**
         * Returns the unlinked items for an order.
         *
         * @param order the order
         * @return the unlinked items for the order
         */
        public List<FinancialAct> getItems(FinancialAct order) {
            List<FinancialAct> result = unlinked.get(order);
            return (result != null) ? result : Collections.<FinancialAct>emptyList();
        }

        /**
         * Removes unlinked information for a line, and it associated order item.
         *
         * @param line the line
         */
        public void remove(InvoiceLineState line) {
            List<FinancialAct> items = unlinked.get(line.getOrder());
            FinancialAct item = line.getOrderItem();
            if (items != null) {
                items.remove(item);
            }
            partial.remove(line);

            for (InvoiceLineState l : getPartialMatches()) {
                List<FinancialAct> matches = getPartialMatches(l);
                if (!matches.isEmpty()) {
                    matches.remove(item);
                }
            }
        }

        /**
         * Add a partial match between a line and an order item.
         *
         * @param line the invoice line
         * @param item the order item
         */
        public void addPartialMatch(InvoiceLineState line, FinancialAct item) {
            List<FinancialAct> items = partial.get(line);
            if (items == null) {
                items = new ArrayList<FinancialAct>();
                partial.put(line, items);
            }
            items.add(item);
        }

        /**
         * Returns the invoice lines where there is a partial match.
         *
         * @return the invoice lines
         */
        public Set<InvoiceLineState> getPartialMatches() {
            return partial.keySet();
        }

        /**
         * Returns order items that partially match an invoice line.
         *
         * @param line the invoice line
         * @return the order items
         */
        public List<FinancialAct> getPartialMatches(InvoiceLineState line) {
            List<FinancialAct> result = partial.get(line);
            return (result != null) ? result : Collections.<FinancialAct>emptyList();
        }

    }

}
