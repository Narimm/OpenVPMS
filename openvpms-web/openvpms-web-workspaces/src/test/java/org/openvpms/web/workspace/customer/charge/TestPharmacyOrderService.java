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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Test implementation of the {@link PharmacyOrderService}.
 *
 * @author Tim Anderson
 */
class TestPharmacyOrderService implements PharmacyOrderService {

    public static class Order {

        enum Type {
            CREATE,
            UPDATE,
            CANCEL
        }

        private final Type type;

        private final Party patient;

        private final Product product;

        private final BigDecimal quantity;

        private final long placerOrderNumber;

        private final Date date;

        private final User clinician;

        private final Entity pharmacy;

        public Order(Type type, Party patient, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date, User clinician, Entity pharmacy) {
            this.type = type;
            this.patient = patient;
            this.product = product;
            this.quantity = quantity;
            this.placerOrderNumber = placerOrderNumber;
            this.date = date;
            this.clinician = clinician;
            this.pharmacy = pharmacy;
        }


        public Type getType() {
            return type;
        }

        public Party getPatient() {
            return patient;
        }

        public Product getProduct() {
            return product;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public long getPlacerOrderNumber() {
            return placerOrderNumber;
        }

        public Date getDate() {
            return date;
        }

        public User getClinician() {
            return clinician;
        }

        public Entity getPharmacy() {
            return pharmacy;
        }
    }

    private List<Order> orders = new ArrayList<Order>();

    /**
     * Creates an order, placing it with the specified pharmacy.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. An <em>entity.HL7ServicePharmacy</em>
     */
    @Override
    public boolean createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                               Date date, Entity pharmacy) {
        orders.add(new Order(Order.Type.CREATE, context.getPatient(), product, quantity, placerOrderNumber, date,
                             context.getClinician(), pharmacy));
        return true;
    }

    /**
     * Updates an order.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. An <em>entity.HL7ServicePharmacy</em>
     */
    @Override
    public void updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date, Entity pharmacy) {
        orders.add(new Order(Order.Type.UPDATE, context.getPatient(), product, quantity, placerOrderNumber, date,
                             context.getClinician(), pharmacy));
    }

    /**
     * Cancels an order.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. An <em>entity.HL7ServicePharmacy</em>
     */
    @Override
    public void cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date, Entity pharmacy) {
        orders.add(new Order(Order.Type.CANCEL, context.getPatient(), product, quantity, placerOrderNumber, date,
                             context.getClinician(), pharmacy));
    }

    /**
     * Returns the orders.
     *
     * @return the orders
     */
    public List<Order> getOrders() {
        return getOrders(false);
    }

    /**
     * Returns orders.
     *
     * @param sort if {@code true}, sort on increasing product id
     * @return the orders
     */
    public List<Order> getOrders(boolean sort) {
        List<Order> result = new ArrayList<Order>(orders);
        if (sort) {
            Collections.sort(result, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    long id1 = o1.getProduct().getId();
                    long id2 = o2.getProduct().getId();
                    return id1 < id2 ? -1 : (id1 == id2) ? 0 : 1;
                }
            });
        }
        return result;
    }

    public void clear() {
        orders.clear();
    }

}
