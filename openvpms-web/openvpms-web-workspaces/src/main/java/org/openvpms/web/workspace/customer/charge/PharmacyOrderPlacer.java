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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.PatientContext;
import org.openvpms.hl7.PatientContextFactory;
import org.openvpms.hl7.PharmacyOrderService;
import org.openvpms.web.component.im.util.IMObjectCache;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Places orders with the {@link PharmacyOrderService}, if a product is dispensed via a pharmacy.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderPlacer {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The location.
     */
    private final Party location;

    /**
     * The cache.
     */
    private final IMObjectCache cache;

    private Map<Long, Order> orders = new HashMap<Long, Order>();

    private final PharmacyOrderService service;

    private final PatientContextFactory factory;


    /**
     * Constructs an {@link PharmacyOrderPlacer}.
     *
     * @param customer the customer
     * @param location the location
     * @param cache    the object cache
     */
    public PharmacyOrderPlacer(Party customer, Party location, IMObjectCache cache, PharmacyOrderService service) {
        this.customer = customer;
        this.location = location;
        this.cache = cache;
        this.service = service;
        factory = ServiceHelper.getBean(PatientContextFactory.class);
    }

    /**
     * Initialises the order placer with any existing orders.
     *
     * @param items the charge items
     */
    public void initialise(List<Act> items) {
        for (Act act : items) {
            Order order = getOrder(act);
            if (order != null) {
                orders.put(act.getId(), order);
            }
        }
    }

    /**
     * Places any orders required by charge items.
     *
     * @param items   the charge items
     * @param changes patient history changes, used to obtain patient events
     */
    public void order(List<Act> items, PatientHistoryChanges changes) {
        for (Act act : items) {
            Order order = getOrder(act);
            Order existing = orders.get(act.getId());
            if (order != null) {
                if (existing != null) {
                    if (needsCancel(existing, order)) {
                        PatientContext context = getPatientContext(order, changes, act);
                        service.cancelOrder(context, existing.getProduct(), existing.getQuantity(),
                                            existing.getId(), act.getActivityStartTime(), existing.getPharmacy());
                        createOrder(order, changes, act);
                    } else if (needsUpdate(existing, order)) {
                        PatientContext context = getPatientContext(order, changes, act);
                        service.updateOrder(context, order.getProduct(), order.getQuantity(),
                                            order.getId(), act.getActivityStartTime(), order.getPharmacy());
                    }
                } else {
                    createOrder(order, changes, act);
                }
            } else if (existing != null) {
                // new product is not dispensed via a pharmacy
                PatientContext context = getPatientContext(existing, changes, act);
                service.cancelOrder(context, existing.getProduct(), existing.getQuantity(),
                                    existing.getId(), act.getActivityStartTime(), existing.getPharmacy());
            }
        }
    }

    public void cancel(List<Act> items, PatientHistoryChanges changes) {
        for (Act act : items) {
            Order order = getOrder(act);
            if (order != null) {
                PatientContext context = getPatientContext(order, changes, act);
                service.cancelOrder(context, order.getProduct(), order.getQuantity(),
                                    order.getId(), act.getActivityStartTime(), order.getPharmacy());
            }
        }
    }

    private Order getOrder(Act act) {
        Order result = null;
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            ActBean bean = new ActBean(act);
            Product product = (Product) getObject(bean.getNodeParticipantRef("product"));
            if (product != null && TypeHelper.isA(product, ProductArchetypes.MEDICATION,
                                                  ProductArchetypes.MERCHANDISE)) {
                Entity pharmacy = getPharmacy(product);
                if (pharmacy != null) {
                    Party patient = (Party) getObject(bean.getNodeParticipantRef("patient"));
                    if (patient != null) {
                        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
                        User clinician = (User) getObject(bean.getNodeParticipantRef("clinician"));
                        result = new Order(act.getId(), product, patient, quantity, clinician, pharmacy);
                    }
                }
            }
        }
        return result;
    }


    private void createOrder(Order order, PatientHistoryChanges changes, Act item) {
        PatientContext patientContext = getPatientContext(order, changes, item);
        if (patientContext != null) {
            service.createOrder(patientContext, order.getProduct(), order.getQuantity(), order.getId(),
                                item.getActivityStartTime(),
                                order.getPharmacy());
        }
    }

    private PatientContext getPatientContext(Order order, PatientHistoryChanges changes, Act item) {
        PatientContext result = null;
        Act event = changes.getLinkedEvent(item);
        if (event != null) {
            result = factory.createContext(order.getPatient(), customer, event, location, order.getClinician());
        }
        return result;
    }

    /**
     * Determines if an existing order needs updating.
     *
     * @param existing the existing order
     * @param order    the new order
     * @return {@code true} if the existing order needs updating
     */
    private boolean needsUpdate(Order existing, Order order) {
        return !ObjectUtils.equals(existing.getPatient(), order.getPatient())
               || !ObjectUtils.equals(existing.getProduct(), order.getProduct())
               || existing.getQuantity().compareTo(order.getQuantity()) != 0
               || !ObjectUtils.equals(existing.getClinician(), order.getClinician());
    }

    /**
     * Determines if an existing order needs cancelling.
     *
     * @param existing the existing order
     * @param order    the new order
     * @return {@code true} if the existing order needs cancelling
     */
    private boolean needsCancel(Order existing, Order order) {
        return !ObjectUtils.equals(existing.getPharmacy(), order.getPharmacy());
    }

    /**
     * Returns the pharmacy for a product.
     *
     * @param product the product
     * @return the pharmacy, or {@code null} if none is present
     */
    private Entity getPharmacy(Product product) {
        EntityBean bean = new EntityBean(product);
        IMObjectReference ref = bean.getNodeTargetObjectRef("pharmacy");
        return (Entity) getObject(ref);
    }

    /**
     * Returns an object given its reference.
     *
     * @param reference the reference. May be {@code null}
     * @return the object, or {@code null} if none is found
     */
    private IMObject getObject(IMObjectReference reference) {
        return (reference != null) ? cache.get(reference) : null;
    }

    private static class Order {

        private final long id;

        private Product product;

        private final Party patient;

        private final BigDecimal quantity;

        private final Entity pharmacy;

        private final User clinician;

        public Order(long id, Product product, Party patient, BigDecimal quantity, User clinician, Entity pharmacy) {
            this.pharmacy = pharmacy;
            this.id = id;
            this.product = product;
            this.patient = patient;
            this.quantity = quantity;
            this.clinician = clinician;
        }

        public Party getPatient() {
            return patient;
        }

        public Entity getPharmacy() {
            return pharmacy;
        }

        public Product getProduct() {
            return product;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public long getId() {
            return id;
        }

        public User getClinician() {
            return clinician;
        }
    }
}
