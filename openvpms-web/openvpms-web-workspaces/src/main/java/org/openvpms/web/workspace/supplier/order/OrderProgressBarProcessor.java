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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.supplier.order;

import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.workspace.supplier.SupplierHelper;

import java.util.Iterator;
import java.util.List;


/**
 * Order generation progress bar processor.
 *
 * @author Tim Anderson
 */
public class OrderProgressBarProcessor extends ProgressBarProcessor<OrderProgressBarProcessor.LocationSupplier> {

    /**
     * Stock location/Supplier pair.
     */
    public static class LocationSupplier {

        /**
         * The stock location.
         */
        private Party stockLocation;

        /**
         * The supplier.
         */
        private Party supplier;

        /**
         * Constructs a {@code LocationSupplier}.
         *
         * @param stockLocation the stock location
         * @param supplier      the supplier
         */
        public LocationSupplier(Party stockLocation, Party supplier) {
            this.stockLocation = stockLocation;
            this.supplier = supplier;
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
         * Returns the supplier.
         *
         * @return the supplier
         */
        public Party getSupplier() {
            return supplier;
        }
    }

    /**
     * The order rules.
     */
    private final OrderRules rules;

    /**
     * The no. of generated orders.
     */
    private int orders;


    /**
     * Constructs an {@code OrderProgressBarProcessor}.
     *
     * @param practice       the practice
     * @param stockLocations the stock locations to generate orders for
     * @param suppliers      the suppliers to generate orders for
     * @param title          the processor title
     */
    public OrderProgressBarProcessor(Party practice, List<IMObject> stockLocations, List<IMObject> suppliers,
                                     String title) {
        super(new LocationSuppliers(stockLocations, suppliers), stockLocations.size() * suppliers.size(), title);
        rules = SupplierHelper.createOrderRules(practice);
    }

    /**
     * Returns the no. of generated orders.
     *
     * @return the no. of generated orders
     */
    public int getOrders() {
        return orders;
    }

    /**
     * Processes an object.
     *
     * @param pair the object to process
     * @throws OpenVPMSException if the object cannot be processed
     */
    @Override
    protected void process(LocationSupplier pair) {
        List<FinancialAct> order = rules.createOrder(pair.getSupplier(), pair.getStockLocation());
        if (!order.isEmpty()) {
            if (!SaveHelper.save(order)) {
                cancel();
            } else {
                ++orders;
            }
        }
        if (!isSuspended()) {
            processCompleted(pair);
        }
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        super.notifyError(exception);
        notifyCompleted();
    }

    static class LocationSuppliers implements Iterable<LocationSupplier> {

        private final List<IMObject> stockLocations;
        private final List<IMObject> suppliers;

        public LocationSuppliers(List<IMObject> stockLocations, List<IMObject> suppliers) {
            this.stockLocations = stockLocations;
            this.suppliers = suppliers;
        }

        /**
         * Returns an iterator over the stock location/supplier pairs.
         *
         * @return an Iterator.
         */
        public Iterator<LocationSupplier> iterator() {
            return new Iterator<LocationSupplier>() {
                int stockIndex;
                int supplierIndex;

                public boolean hasNext() {
                    return stockIndex < stockLocations.size();
                }

                public LocationSupplier next() {
                    LocationSupplier result = new LocationSupplier((Party) stockLocations.get(stockIndex),
                                                                   (Party) suppliers.get(supplierIndex));
                    if (supplierIndex < suppliers.size() - 1) {
                        supplierIndex++;
                    } else {
                        supplierIndex = 0;
                        stockIndex++;
                    }
                    return result;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

}
