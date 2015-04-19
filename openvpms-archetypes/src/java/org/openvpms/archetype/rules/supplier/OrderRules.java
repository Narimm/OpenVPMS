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

package org.openvpms.archetype.rules.supplier;

import org.openvpms.archetype.rules.act.ActCopyHandler;
import org.openvpms.archetype.rules.act.DefaultActCopyHandler;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.rules.stock.StockArchetypes.STOCK_LOCATION_PARTICIPATION;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.CREDIT;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.CREDIT_ITEM;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.CREDIT_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.DELIVERY;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.DELIVERY_ITEM;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.DELIVERY_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.INVOICE;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.INVOICE_ITEM;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.INVOICE_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.ORDER;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.ORDER_ITEM;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.RETURN;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.RETURN_ITEM;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.RETURN_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.RETURN_ORDER_ITEM_RELATIONSHIP;


/**
 * Supplier Order rules.
 *
 * @author Tim Anderson
 */
public class OrderRules {

    /**
     * The tax rules.
     */
    private final TaxRules taxRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>OrderRules</tt>.
     *
     * @param service the archetype service
     */
    public OrderRules(TaxRules taxRules, IArchetypeService service) {
        this.taxRules = taxRules;
        this.service = service;
    }

    /**
     * Determines the delivery status of an order item.
     *
     * @param orderItem an <em>act.supplierOrderItem</em>
     * @return the delivery status
     */
    public DeliveryStatus getDeliveryStatus(FinancialAct orderItem) {
        return DeliveryProcessor.getDeliveryStatus(orderItem, service);
    }

    /**
     * Copies an order.
     * <p/>
     * The copied order will have an <em>IN_PROGRESS</em> status and <em>PENDING</em> delivery status.
     * <p/>
     * The copy is saved.
     *
     * @param order the order to copy
     * @param title a title to assign to the copy. May be {@code null}
     * @return the copy of the order
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct copyOrder(FinancialAct order, String title) {
        List<IMObject> objects = copy(order, ORDER, new DefaultActCopyHandler(), new Date(), false);
        FinancialAct copy = (FinancialAct) objects.get(0);
        IMObjectBean bean = new IMObjectBean(copy, service);
        bean.setValue("deliveryStatus", DeliveryStatus.PENDING);
        copy.setTitle(title);
        for (IMObject object : objects) {
            if (TypeHelper.isA(object, SupplierArchetypes.ORDER_ITEM)) {
                IMObjectBean itemBean = new IMObjectBean(object, service);
                itemBean.setValue("receivedQuantity", BigDecimal.ZERO);
                itemBean.setValue("cancelledQuantity", BigDecimal.ZERO);
            }
        }
        service.save(objects);
        return copy;
    }

    /**
     * Creates a new delivery item from an order item.
     * <p/>
     * The quantity on the delivery item will default to the order's:
     * <p/>
     * <tt>quantity - (receivedQuantity + cancelledQuantity)</tt>
     *
     * @param orderItem the order item
     * @return a new delivery item
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct createDeliveryItem(FinancialAct orderItem) {
        List<IMObject> objects = copy(orderItem, ORDER_ITEM,
                                      new DeliveryItemHandler(),
                                      orderItem.getActivityStartTime(), false);
        ActBean order = new ActBean(orderItem, service);
        BigDecimal quantity = orderItem.getQuantity();
        BigDecimal received = order.getBigDecimal("receivedQuantity");
        BigDecimal cancelled = order.getBigDecimal("cancelledQuantity");
        BigDecimal remaining = quantity.subtract(received.add(cancelled));
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }
        FinancialAct delivery = (FinancialAct) objects.get(0);
        delivery.setQuantity(remaining);
        return delivery;
    }

    /**
     * Creates a new return item from an order item.
     * <p/>
     * The quantity on the return item will default to the order's: <em>receivedQuantity</em>
     *
     * @param orderItem the order item
     * @return a new return item
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct createReturnItem(FinancialAct orderItem) {
        List<IMObject> objects = copy(orderItem, ORDER_ITEM, new ReturnItemHandler(), orderItem.getActivityStartTime(),
                                      false);
        ActBean order = new ActBean(orderItem, service);
        BigDecimal received = order.getBigDecimal("receivedQuantity");
        FinancialAct item = (FinancialAct) objects.get(0);
        item.setQuantity(received);
        return (FinancialAct) objects.get(0);
    }

    /**
     * Invoices a supplier from an <em>act.supplierDelivery</em> act.
     * <p/>
     * Both the invoice and delivery are saved.
     *
     * @param delivery the supplier delivery act
     * @return the invoice corresponding to the delivery
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IllegalStateException     if the delivery is already associated with a charge
     */
    public FinancialAct invoiceSupplier(Act delivery) {
        if (isInvoiced(delivery)) {
            throw new IllegalStateException("The delivery has already been invoiced");
        }
        ActBean bean = new ActBean(delivery, service);
        List<IMObject> objects = copy(delivery, DELIVERY, new DeliveryHandler(), new Date(), false);
        FinancialAct invoice = (FinancialAct) objects.get(0);
        bean.addNodeRelationship("invoice", invoice);
        objects.add(delivery);
        service.save(objects);
        return invoice;
    }

    /**
     * Determines if a delivery has been invoiced.
     *
     * @param delivery the delivery
     * @return {@code true} if the delivery has been invoiced
     */
    public boolean isInvoiced(Act delivery) {
        ActBean bean = new ActBean(delivery, service);
        return !bean.getNodeTargetObjectRefs("invoice").isEmpty();
    }

    /**
     * Credits a supplier from an <em>act.supplierReturn</em> act.
     * <p/>
     * The credit is saved.
     *
     * @param supplierReturn the supplier return act
     * @return the credit corresponding to the return
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IllegalStateException     if the return is already associated with a credit
     */
    public FinancialAct creditSupplier(Act supplierReturn) {
        if (isCredited(supplierReturn)) {
            throw new IllegalStateException("The return is already linked to a credit");
        }
        ActBean bean = new ActBean(supplierReturn, service);
        List<IMObject> objects = copy(supplierReturn, RETURN, new ReturnHandler(), new Date(), false);
        FinancialAct credit = (FinancialAct) objects.get(0);
        bean.addNodeRelationship("returnCredit", credit);
        objects.add(supplierReturn);
        service.save(objects);
        return (FinancialAct) objects.get(0);
    }

    /**
     * Determines if a return has been credited.
     *
     * @param supplierReturn the supplier return act
     * @return {@code true} if the return has been credited
     */
    public boolean isCredited(Act supplierReturn) {
        ActBean bean = new ActBean(supplierReturn, service);
        return !bean.getNodeTargetObjectRefs("returnCredit").isEmpty();
    }

    /**
     * Reverses a delivery.
     *
     * @param supplierDelivery the delivery to reverse
     * @return a new return
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act reverseDelivery(Act supplierDelivery) {
        List<IMObject> objects = copy(supplierDelivery,
                                      DELIVERY,
                                      new ReverseHandler(true), new Date(),
                                      true);
        return (Act) objects.get(0);
    }

    /**
     * Reverses a return.
     *
     * @param supplierReturn the return to reverse
     * @return a new delivery
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act reverseReturn(Act supplierReturn) {
        List<IMObject> objects = copy(supplierReturn, RETURN,
                                      new ReverseHandler(false), new Date(),
                                      true);
        return (Act) objects.get(0);
    }

    /**
     * Creates an order for all products supplied by the supplier for the specified stock location.
     *
     * @param supplier           the supplier
     * @param stockLocation      the stock location
     * @param belowIdealQuantity if {@code true}, return stock that is {@code <=} the ideal quantity, else return stock
     *                           that is {@code <=} the critical quantity
     * @return the order and its items, or an empty list if there are no products to order
     */
    public List<FinancialAct> createOrder(Party supplier, Party stockLocation, boolean belowIdealQuantity) {
        OrderGenerator generator = new OrderGenerator(taxRules, service);
        return generator.createOrder(supplier, stockLocation, belowIdealQuantity);
    }

    /**
     * Helper to copy an act.
     *
     * @param object    the object to copy
     * @param type      the expected type of the object
     * @param handler   the copy handler
     * @param startTime the start time of the copied object
     * @param save      if <tt>true</tt>, save the copied objects
     * @return the copied objects
     */
    private List<IMObject> copy(Act object, String type,
                                IMObjectCopyHandler handler, Date startTime,
                                boolean save) {
        if (!TypeHelper.isA(object, type)) {
            throw new IllegalArgumentException(
                    "Expected a " + type + " for argument 'object'"
                    + ", but got a"
                    + object.getArchetypeId().getShortName());
        }
        IMObjectCopier copier = new IMObjectCopier(handler, service);
        List<IMObject> objects = copier.apply(object);
        Act act = (Act) objects.get(0);
        act.setActivityStartTime(startTime);
        if (save) {
            service.save(objects);
        }
        return objects;
    }


    /**
     * Helper to create an <em>act.supplierDeliveryItem</em> from an
     * <em>act.supplierOrderItem</em>
     */
    private static class DeliveryItemHandler extends ActCopyHandler {

        private static final String[][] TYPE_MAP
                = {{ORDER_ITEM, DELIVERY_ITEM}};

        public DeliveryItemHandler() {
            super(TYPE_MAP);
            setCopy(Act.class, Participation.class);
            setExclude(ActRelationship.class);
        }
    }

    /**
     * Helper to create an <em>act.supplierReturnItem</em> from an
     * <em>act.supplierOrderItem</em>
     */
    private static class ReturnItemHandler extends ActCopyHandler {

        private static final String[][] TYPE_MAP
                = {{ORDER_ITEM, RETURN_ITEM}};

        public ReturnItemHandler() {
            super(TYPE_MAP);
            setCopy(Act.class, Participation.class);
            setExclude(ActRelationship.class);
        }
    }

    private static class DeliveryHandler extends ActCopyHandler {

        /**
         * Map of delivery types to their corresponding invoice types.
         */
        private static final String[][] TYPE_MAP = {
                {DELIVERY, INVOICE},
                {DELIVERY_ITEM, INVOICE_ITEM},
                {DELIVERY_ITEM_RELATIONSHIP, INVOICE_ITEM_RELATIONSHIP},
                {DELIVERY_ORDER_ITEM_RELATIONSHIP, null},
                {STOCK_LOCATION_PARTICIPATION, null}};

        public DeliveryHandler() {
            super(TYPE_MAP);
        }
    }

    private static class ReturnHandler extends ActCopyHandler {

        /**
         * Map of return types to their corresponding credit types.
         */
        private static final String[][] TYPE_MAP = {
                {RETURN, CREDIT},
                {RETURN_ITEM, CREDIT_ITEM},
                {RETURN_ITEM_RELATIONSHIP, CREDIT_ITEM_RELATIONSHIP},
                {STOCK_LOCATION_PARTICIPATION, null}};

        public ReturnHandler() {
            super(TYPE_MAP);
            setExclude("actRelationship.supplierReturnCredit");
        }
    }

    private static class ReverseHandler extends ActCopyHandler {

        private static final String[][] TYPE_MAP = {
                {DELIVERY, RETURN},
                {DELIVERY_ITEM, RETURN_ITEM},
                {DELIVERY_ITEM_RELATIONSHIP, RETURN_ITEM_RELATIONSHIP},
                {DELIVERY_ORDER_ITEM_RELATIONSHIP,
                 RETURN_ORDER_ITEM_RELATIONSHIP}};

        public ReverseHandler(boolean delivery) {
            super(TYPE_MAP);
            setReference(ORDER_ITEM);
            setReverse(!delivery);
            setExclude("actRelationship.supplierDeliveryInvoice");
        }
    }

}
