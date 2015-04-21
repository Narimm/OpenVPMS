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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
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
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;
import org.openvpms.hl7.util.HL7Archetypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Places orders with the {@link PharmacyOrderService}, if a product is dispensed via a pharmacy, or the
 * {@link LaboratoryOrderService}, if an investigation is processed by a laboratory.
 *
 * @author Tim Anderson
 */
public class OrderPlacer {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The location.
     */
    private final Party location;

    /**
     * The user responsible for the orders.
     */
    private final User user;

    /**
     * The cache.
     */
    private final IMObjectCache cache;

    /**
     * The orders, keyed on act reference.
     */
    private Map<IMObjectReference, Order> orders = new HashMap<IMObjectReference, Order>();

    /**
     * The order services.
     */
    private final OrderServices services;


    /**
     * Constructs an {@link OrderPlacer}.
     *
     * @param customer the customer
     * @param location the location
     * @param user     the user responsible for the orders
     * @param cache    the object cache
     * @param services the order services
     */
    public OrderPlacer(Party customer, Party location, User user, IMObjectCache cache, OrderServices services) {
        this.customer = customer;
        this.location = location;
        this.user = user;
        this.cache = cache;
        this.services = services;
    }

    /**
     * Initialises the order placer with an existing order.
     *
     * @param items the charge items and investigations
     */
    public void initialise(List<Act> items) {
        for (Act item : items) {
            initialise(item);
        }
    }

    /**
     * Initialises the order placer with an existing order.
     *
     * @param item the charge item or investigation
     */
    public void initialise(Act item) {
        Order order = getOrder(item);
        if (order != null) {
            orders.put(item.getObjectReference(), order);
        }
    }

    /**
     * Places any orders required by charge items.
     * <p/>
     * If items have been removed since initialisation, those items will be cancelled.
     *
     * @param items   the charge items
     * @param changes patient history changes, used to obtain patient events
     * @return the list of updated charge items
     */
    public List<Act> order(List<Act> items, PatientHistoryChanges changes) {
        List<IMObjectReference> ids = new ArrayList<IMObjectReference>(orders.keySet());
        List<Act> updated = new ArrayList<Act>();
        Set<Party> patients = new HashSet<Party>();
        for (Act act : items) {
            IMObjectReference id = act.getObjectReference();
            ids.remove(id);
            Order order = getOrder(act);
            Order existing = orders.get(id);
            if (order != null) {
                if (existing != null) {
                    if (existing.needsCancel(order)) {
                        // TODO - need to prevent this, as PlacerOrderNumbers should not be reused.
                        cancelOrder(existing, changes, patients);
                        if (createOrder(act, order, changes, patients)) {
                            updated.add(act);
                        }
                    } else if (existing.needsUpdate(order)) {
                        updateOrder(changes, order, patients);
                    }
                } else {
                    if (createOrder(act, order, changes, patients)) {
                        updated.add(act);
                    }
                }
                orders.put(id, order);
            } else if (existing != null) {
                // new product is not dispensed via a pharmacy.
                cancelOrder(existing, changes, patients);
            }
        }
        for (IMObjectReference id : ids) {
            Order existing = orders.remove(id);
            cancelOrder(existing, changes, patients);
        }
        return updated;
    }

    /**
     * Cancel orders.
     */
    public void cancel() {
        Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();
        for (Order order : orders.values()) {
            PatientContext context = getPatientContext(order, events);
            if (context != null) {
                order.cancel(context, services, user);
            }
        }
    }

    /**
     * Discontinue orders.
     */
    public void discontinue() {
        Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();
        for (Order order : orders.values()) {
            PatientContext context = getPatientContext(order, events);
            if (context != null) {
                order.discontinue(context, services, user);
            }
        }
    }

    private Order getOrder(Act act) {
        Order result = null;
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            result = getPharmacyOrder(act);
        } else if (TypeHelper.isA(act, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
            result = getInvestigationOrder(act);
        }
        return result;
    }

    private Order getPharmacyOrder(Act act) {
        Order order = null;
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
                    order = new PharmacyOrder(act, product, patient, quantity, clinician, pharmacy, event);
                }
            }
        }
        return order;
    }

    private Order getInvestigationOrder(Act act) {
        Order order = null;
        ActBean bean = new ActBean(act);
        Entity investigationType = (Entity) getObject(bean.getNodeParticipantRef("investigationType"));
        if (investigationType != null) {
            IMObjectBean typeBean = new IMObjectBean(investigationType);
            String serviceId = typeBean.getString("universalServiceIdentifier");
            Entity lab = getLaboratory(investigationType);
            if (lab != null && serviceId != null) {
                Party patient = (Party) getObject(bean.getNodeParticipantRef("patient"));
                if (patient != null) {
                    User clinician = (User) getObject(bean.getNodeParticipantRef("clinician"));
                    IMObjectReference event = bean.getNodeSourceObjectRef("event");
                    order = new LaboratoryOrder(act, serviceId, patient, clinician, lab, event);
                }
            }
        }
        return order;
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
            result = services.getFactory().createContext(order.getPatient(), customer, event, location,
                                                         order.getClinician());
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
            event = services.getRules().getEvent(order.getPatient(), order.getStartTime());
        }
        if (event != null) {
            events.put(order.getEvent(), event);
        }
        if (event != null) {
            PatientContextFactory factory = services.getFactory();
            result = factory.createContext(order.getPatient(), customer, event, location, order.getClinician());
        }
        return result;
    }

    /**
     * Creates an order.
     *
     * @param act      the invoice item
     * @param order    the order
     * @param changes  the changes
     * @param patients tracks patients that have had notifications sent
     * @return {@code true} if an order was created (and invoice updated)
     */
    private boolean createOrder(Act act, Order order, PatientHistoryChanges changes, Set<Party> patients) {
        boolean result = false;
        PatientContext context = getPatientContext(order, changes);
        if (context != null) {
            notifyPatientInformation(context, changes, patients);
            if (order.create(context, services, user)) {
                ActBean bean = new ActBean(act);
                bean.setValue("ordered", true);
                result = true;
            }
        }
        return result;
    }

    /**
     * Updates an order.
     *
     * @param order    the order
     * @param changes  the changes
     * @param patients tracks patients that have had notifications sent
     */
    private void updateOrder(PatientHistoryChanges changes, Order order, Set<Party> patients) {
        PatientContext context = getPatientContext(order, changes);
        if (context != null) {
            notifyPatientInformation(context, changes, patients);
            order.update(context, services, user);
        }
    }

    /**
     * Cancel an order.
     *
     * @param order    the order
     * @param changes  the changes
     * @param patients the patients, used to prevent duplicate patient update notifications being sent
     */
    private void cancelOrder(Order order, PatientHistoryChanges changes, Set<Party> patients) {
        PatientContext context = getPatientContext(order, changes);
        if (context != null) {
            notifyPatientInformation(context, changes, patients);
            order.cancel(context, services, user);
        }
    }

    /**
     * Notifies registered listeners of patient visit information, when placing orders outside of a current visit.
     * <p/>
     * This is required for listeners that remove patient information when a patient is discharged.
     * <p/>
     * The {@code patients} variable is used to track if patient information has already been sent for a given patient,
     * to avoid multiple notifications being sent. This isn't kept across calls to {@link #order}, so
     * redundant notifications may be sent.
     *
     * @param context  the context
     * @param changes  the patient history changes
     * @param patients tracks patients that have had notifications sent
     */
    private void notifyPatientInformation(PatientContext context, PatientHistoryChanges changes, Set<Party> patients) {
        Act visit = context.getVisit();
        if (!patients.contains(context.getPatient())) {
            if (changes.isNew(visit)
                || (visit.getActivityEndTime() != null
                    && DateRules.compareTo(visit.getActivityEndTime(), new Date()) < 0)) {
                services.getInformationService().updated(context, user);
                patients.add(context.getPatient());
            }
        }
    }

    /**
     * Returns the pharmacy for a product and location.
     *
     * @param product the product
     * @return the pharmacy, or {@code null} if none is present
     */
    private Entity getPharmacy(Product product) {
        IMObjectBean bean = new IMObjectBean(product);
        Entity pharmacy = (Entity) getObject(bean.getNodeTargetObjectRef("pharmacy"));
        if (pharmacy == null) {
            // use the pharmacy linked to the product type, if present
            Entity type = (Entity) getObject(bean.getNodeSourceObjectRef("type"));
            if (type != null) {
                IMObjectBean typeBean = new IMObjectBean(type);
                pharmacy = (Entity) getObject(typeBean.getNodeTargetObjectRef("pharmacy"));
            }
        }
        if (pharmacy != null && TypeHelper.isA(pharmacy, HL7Archetypes.PHARMACY_GROUP)) {
            pharmacy = services.getService(pharmacy, location);
        }
        return pharmacy;
    }

    /**
     * Returns the lab for an investigation and location.
     *
     * @param investigationType the investigation type
     * @return the pharmacy, or {@code null} if none is present
     */
    private Entity getLaboratory(Entity investigationType) {
        IMObjectBean bean = new IMObjectBean(investigationType);
        Entity laboratory = (Entity) getObject(bean.getNodeTargetObjectRef("laboratory"));
        if (laboratory != null && TypeHelper.isA(laboratory, HL7Archetypes.LABORATORY_GROUP)) {
            laboratory = services.getService(laboratory, location);
        }
        return laboratory;
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

    private static abstract class Order {

        private final IMObjectReference actId;
        private final Date startTime;
        private final Party patient;

        private final User clinician;

        private final IMObjectReference event;

        public Order(IMObjectReference actId, Date startTime, Party patient, User clinician, IMObjectReference event) {
            this.actId = actId;
            this.startTime = startTime;
            this.patient = patient;
            this.clinician = clinician;
            this.event = event;
        }

        public IMObjectReference getActId() {
            return actId;
        }

        public long getPlacerOrderNumber() {
            return getActId().getId();
        }

        public Date getStartTime() {
            return startTime;
        }

        public Party getPatient() {
            return patient;
        }

        public User getClinician() {
            return clinician;
        }

        public IMObjectReference getEvent() {
            return event;
        }

        public abstract boolean create(PatientContext context, OrderServices services, User user);

        public abstract void cancel(PatientContext context, OrderServices services, User user);

        public abstract void discontinue(PatientContext context, OrderServices services, User user);

        public abstract void update(PatientContext context, OrderServices services, User user);


        /**
         * Determines if the existing order needs cancelling.
         *
         * @param newOrder the new version of the order
         * @return {@code true} if the existing order needs cancelling
         */
        public abstract boolean needsCancel(Order newOrder);

        /**
         * Determines if the existing order needs updating.
         *
         * @param newOrder the new version of the order
         * @return {@code true} if the existing order needs updating
         */
        public abstract boolean needsUpdate(Order newOrder);
    }

    private static class PharmacyOrder extends Order {

        private Product product;

        private final BigDecimal quantity;

        private final Entity pharmacy;


        public PharmacyOrder(Act act, Product product, Party patient, BigDecimal quantity, User clinician,
                             Entity pharmacy, IMObjectReference event) {
            super(act.getObjectReference(), act.getActivityStartTime(), patient, clinician, event);
            this.product = product;
            this.quantity = quantity;
            this.pharmacy = pharmacy;
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

        @Override
        public boolean create(PatientContext context, OrderServices services, User user) {
            PharmacyOrderService service = services.getPharmacyService();
            return service.createOrder(context, product, quantity, getPlacerOrderNumber(), getStartTime(), pharmacy,
                                       user);
        }

        @Override
        public void update(PatientContext context, OrderServices services, User user) {
            PharmacyOrderService service = services.getPharmacyService();
            service.updateOrder(context, product, quantity, getPlacerOrderNumber(), getStartTime(), pharmacy, user);
        }

        @Override
        public void cancel(PatientContext context, OrderServices services, User user) {
            PharmacyOrderService service = services.getPharmacyService();
            service.cancelOrder(context, product, quantity, getPlacerOrderNumber(), getStartTime(), pharmacy, user);
        }

        @Override
        public void discontinue(PatientContext context, OrderServices services, User user) {
            PharmacyOrderService service = services.getPharmacyService();
            service.discontinueOrder(context, product, quantity, getPlacerOrderNumber(), getStartTime(), pharmacy,
                                     user);
        }

        /**
         * Determines if the existing order needs cancelling.
         *
         * @param newOrder the new version of the order
         * @return {@code true} if the existing order needs cancelling
         */
        @Override
        public boolean needsCancel(Order newOrder) {
            PharmacyOrder other = (PharmacyOrder) newOrder;
            return !ObjectUtils.equals(getPatient(), newOrder.getPatient())
                   || !ObjectUtils.equals(product, other.getProduct())
                   || !ObjectUtils.equals(pharmacy, other.getPharmacy());
        }

        public boolean needsUpdate(Order newOrder) {
            PharmacyOrder other = (PharmacyOrder) newOrder;
            return quantity.compareTo(other.getQuantity()) != 0
                   || !ObjectUtils.equals(getClinician(), other.getClinician());
        }
    }

    private static class LaboratoryOrder extends Order {

        private final String serviceId;

        private final Entity lab;

        public LaboratoryOrder(Act act, String serviceId, Party patient, User clinician, Entity lab,
                               IMObjectReference event) {
            super(act.getObjectReference(), act.getActivityStartTime(), patient, clinician, event);
            this.serviceId = serviceId;
            this.lab = lab;
        }

        @Override
        public boolean create(PatientContext context, OrderServices services, User user) {
            return services.getLaboratoryService().createOrder(context, getPlacerOrderNumber(), serviceId,
                                                               getStartTime(), lab, user);
        }

        @Override
        public void cancel(PatientContext context, OrderServices services, User user) {
        }

        @Override
        public void discontinue(PatientContext context, OrderServices services, User user) {
        }

        @Override
        public void update(PatientContext context, OrderServices services, User user) {
        }

        /**
         * Determines if the existing order needs cancelling.
         *
         * @param newOrder the new version of the order
         * @return {@code true} if the existing order needs cancelling
         */
        @Override
        public boolean needsCancel(Order newOrder) {
            return false;
        }

        /**
         * Determines if the existing order needs updating.
         *
         * @param newOrder the new version of the order
         * @return {@code true} if the existing order needs updating
         */
        @Override
        public boolean needsUpdate(Order newOrder) {
            return false;
        }
    }

}
