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
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

    /**
     * The orders, keyed on invoice item id.
     */
    private Map<Long, Order> orders = new HashMap<Long, Order>();

    /**
     * The pharmacy order service.
     */
    private final PharmacyOrderService service;

    /**
     * The patient context factory.
     */
    private final PatientContextFactory factory;

    /**
     * Medical record rules, used to retrieve events.
     */
    private final MedicalRecordRules rules;


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
        rules = ServiceHelper.getBean(MedicalRecordRules.class);
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
     * <p/>
     * If items have been removed since initialisation, those items will be cancelled.
     *
     * @param items   the charge items
     * @param changes patient history changes, used to obtain patient events
     */
    public void order(List<Act> items, PatientHistoryChanges changes) {
        List<Long> ids = new ArrayList<Long>(orders.keySet());
        for (Act act : items) {
            long id = act.getId();
            ids.remove(id);
            Order order = getOrder(act);
            Order existing = orders.get(id);
            if (order != null) {
                if (existing != null) {
                    if (needsCancel(existing, order)) {
                        cancelOrder(existing, changes);
                        createOrder(order, changes);
                    } else if (needsUpdate(existing, order)) {
                        updateOrder(changes, order);
                    }
                } else {
                    createOrder(order, changes);
                }
                orders.put(id, order);
            } else if (existing != null) {
                // new product is not dispensed via a pharmacy
                cancelOrder(existing, changes);
            }
        }
        for (long id : ids) {
            Order existing = orders.remove(id);
            cancelOrder(existing, changes);
        }
    }

    /**
     * Cancel orders.
     */
    public void cancel() {
        Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();
        for (Order order : orders.values()) {
            PatientContext context = getPatientContext(order, events);
            if (context != null) {
                service.cancelOrder(context, order.getProduct(), order.getQuantity(),
                                    order.getId(), order.getStartTime(), order.getPharmacy());
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
                        IMObjectReference event = bean.getNodeSourceObjectRef("event");
                        result = new Order(act.getId(), act.getActivityStartTime(), product, patient, quantity,
                                           clinician, pharmacy, event);
                    }
                }
            }
        }
        return result;
    }

    private PatientContext getPatientContext(Order order, PatientHistoryChanges changes) {
        PatientContext result = null;
        List<Act> events = changes.getEvents(order.getPatient().getObjectReference());
        Act event;
        if (events == null || events.isEmpty()) {
            event = changes.getEvent(order.getEvent());
        } else {
            event = Collections.max(events, new Comparator<Act>() {
                @Override
                public int compare(Act o1, Act o2) {
                    return DateRules.compareDateTime(o1.getActivityStartTime(), o2.getActivityStartTime(), true);
                }
            });
        }
        if (event != null) {
            result = factory.createContext(order.getPatient(), customer, event, location, order.getClinician());
        }
        return result;
    }

    private PatientContext getPatientContext(Order order, Map<IMObjectReference, Act> events) {
        PatientContext result = null;
        Act event = events.get(order.getEvent());
        if (event == null) {
            event = (Act) getObject(order.getEvent());
        }
        if (event == null) {
            event = rules.getEvent(order.getPatient(), order.getStartTime());
        }
        if (event != null) {
            events.put(order.getEvent(), event);
        }
        if (event != null) {
            result = factory.createContext(order.getPatient(), customer, event, location, order.getClinician());
        }
        return result;
    }

    /**
     * Creates an order.
     *
     * @param order   the order
     * @param changes the changes
     */
    private void createOrder(Order order, PatientHistoryChanges changes) {
        PatientContext patientContext = getPatientContext(order, changes);
        if (patientContext != null) {
            service.createOrder(patientContext, order.getProduct(), order.getQuantity(), order.getId(),
                                order.getStartTime(), order.getPharmacy());
        }
    }

    /**
     * Updates an order.
     *
     * @param order   the order
     * @param changes the changes
     */
    private void updateOrder(PatientHistoryChanges changes, Order order) {
        PatientContext context = getPatientContext(order, changes);
        service.updateOrder(context, order.getProduct(), order.getQuantity(), order.getId(), order.getStartTime(),
                            order.getPharmacy());
    }

    /**
     * Cancel an order.
     *
     * @param order   the order
     * @param changes the changes
     */
    private void cancelOrder(Order order, PatientHistoryChanges changes) {
        PatientContext context = getPatientContext(order, changes);
        service.cancelOrder(context, order.getProduct(), order.getQuantity(), order.getId(), order.getStartTime(),
                            order.getPharmacy());
    }

    /**
     * Determines if an existing order needs updating.
     *
     * @param existing the existing order
     * @param order    the new order
     * @return {@code true} if the existing order needs updating
     */
    private boolean needsUpdate(Order existing, Order order) {
        return existing.getQuantity().compareTo(order.getQuantity()) != 0
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
        return !ObjectUtils.equals(existing.getPatient(), order.getPatient())
               || !ObjectUtils.equals(existing.getProduct(), order.getProduct())
               || !ObjectUtils.equals(existing.getPharmacy(), order.getPharmacy());
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

        private final Date startTime;

        private Product product;

        private final Party patient;

        private final BigDecimal quantity;

        private final Entity pharmacy;

        private final User clinician;

        private final IMObjectReference event;

        public Order(long id, Date startTime, Product product, Party patient, BigDecimal quantity, User clinician,
                     Entity pharmacy, IMObjectReference event) {
            this.id = id;
            this.startTime = startTime;
            this.product = product;
            this.patient = patient;
            this.quantity = quantity;
            this.pharmacy = pharmacy;
            this.clinician = clinician;
            this.event = event;
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

        public IMObjectReference getEvent() {
            return event;
        }

        public Date getStartTime() {
            return startTime;
        }
    }

}
