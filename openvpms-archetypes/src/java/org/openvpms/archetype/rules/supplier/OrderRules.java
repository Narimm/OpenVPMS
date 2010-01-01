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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import org.openvpms.archetype.rules.act.ActCopyHandler;
import org.openvpms.archetype.rules.act.DefaultActCopyHandler;
import static org.openvpms.archetype.rules.stock.StockArchetypes.STOCK_LOCATION_PARTICIPATION;
import static org.openvpms.archetype.rules.supplier.SupplierArchetypes.*;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Supplier Order rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>OrderRules</tt>.
     */
    public OrderRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>OrderRules</tt>.
     *
     * @param service the archetype service
     */
    public OrderRules(IArchetypeService service) {
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
     * The copied order will have an <em>IN_PROGRESS</em> status.
     * The copy is saved.
     *
     * @param order the order to copy
     * @return the copy of the order
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct copyOrder(FinancialAct order) {
        List<IMObject> objects = copy(order, ORDER,
                                      new DefaultActCopyHandler(), new Date(),
                                      true);
        return (FinancialAct) objects.get(0);
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
     * The invoice is saved.
     *
     * @param supplierDelivery the supplier delivery act
     * @return the invoice corresponding to the delivery
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct invoiceSupplier(Act supplierDelivery) {
        List<IMObject> objects = copy(supplierDelivery,
                                      DELIVERY,
                                      new DeliveryHandler(), new Date(), true);
        return (FinancialAct) objects.get(0);
    }

    /**
     * Credits a supplier from an <em>act.supplierReturn</em> act.
     * <p/>
     * The credit is saved.
     *
     * @param supplierReturn the supplier return act
     * @return the credit corresponding to the return
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct creditSupplier(Act supplierReturn) {
        List<IMObject> objects = copy(supplierReturn, RETURN,
                                      new ReturnHandler(), new Date(), true);
        return (FinancialAct) objects.get(0);
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
        }
    }

}
