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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.invoice.InvoiceItemStatus;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;
import org.openvpms.hl7.util.HL7Archetypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
     * The order services.
     */
    private final OrderServices services;

    /**
     * The pharmacy products helper.
     */
    private final PharmacyProducts pharmacies;

    private final boolean discontinueOnFinalisation;

    /**
     * The orders, keyed on act reference.
     */
    private Map<Reference, Order> orders = new HashMap<>();


    /**
     * Constructs an {@link OrderPlacer}.
     *
     * @param customer the customer
     * @param location the location
     * @param user     the user responsible for the orders
     * @param practice the practice
     * @param cache    the object cache
     * @param services the order services
     */
    public OrderPlacer(Party customer, Party location, User user, Party practice, IMObjectCache cache,
                       OrderServices services) {
        this.customer = customer;
        this.location = location;
        this.user = user;
        this.cache = cache;
        this.services = services;
        PracticeRules rules = services.getPracticeRules();
        Period period = rules.getPharmacyOrderDiscontinuePeriod(practice);
        discontinueOnFinalisation = period == null || period.toStandardDuration().compareTo(Duration.ZERO) < 0;
        this.pharmacies = new PharmacyProducts(services.getPharmacies(), location, cache);
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
     * Places any orders required by invoice items and investigations.
     * <p/>
     * If items have been removed since initialisation, those items will be cancelled.
     *
     * @param items   the invoice items and investigations
     * @param changes patient history changes, used to obtain patient events
     * @return the set of updated charge and investigation items
     */
    public Set<Act> order(List<Act> items, PatientHistoryChanges changes) {
        Map<Reference, Act> invoiceItems = getInvoiceItems(items);
        List<Reference> ids = new ArrayList<>(orders.keySet());
        Set<Act> updated = new HashSet<>();
        Set<Party> patients = new HashSet<>();
        for (Act item : items) {
            Act invoiceItem = getInvoiceItem(item, invoiceItems);
            Reference id = item.getObjectReference();
            ids.remove(id);
            Order order = getOrder(item);
            Order existing = orders.get(id);
            if (order != null) {
                if (existing != null) {
                    if (existing.needsCancel(order)) {
                        // TODO - need to prevent this, as PlacerOrderNumbers should not be reused.
                        cancelOrder(existing, changes, patients, updated);
                        createOrder(invoiceItem, order, changes, patients, updated);
                    } else if (existing.needsUpdate(order)) {
                        updateOrder(changes, order, patients);
                    }
                } else {
                    createOrder(invoiceItem, order, changes, patients, updated);
                }
                orders.put(id, order);
            } else if (existing != null) {
                // new product is not ordered
                cancelOrder(existing, changes, patients, updated);
            }
        }
        for (Reference id : ids) {
            Order existing = orders.remove(id);
            cancelOrder(existing, changes, patients, updated);
        }
        return updated;
    }

    /**
     * Cancel orders.
     */
    public void cancel() {
        Map<Reference, Act> events = new HashMap<>();
        for (Order order : orders.values()) {
            PatientContext context = getPatientContext(order, events);
            if (context != null) {
                order.cancel(context, services, user, new HashSet<>());
            }
        }
    }

    /**
     * Cancels any orders where the associated invoice item has been deleted.
     *
     * @param current the set of current invoice items and investigations
     * @param changes patient history changes, used to obtain patient events
     * @return the set of updated charge and investigation items
     */
    public Set<Act> cancelDeleted(List<Act> current, PatientHistoryChanges changes) {
        Set<Act> updated = new HashSet<>();
        Map<Reference, Order> copy = new HashMap<>(orders);
        for (Act act : current) {
            copy.remove(act.getObjectReference());
        }
        if (!copy.isEmpty()) {
            Set<Party> patients = new HashSet<>();
            for (Map.Entry<Reference, Order> removed : copy.entrySet()) {
                cancelOrder(removed.getValue(), changes, patients, updated);
                orders.remove(removed.getKey());
            }
        }
        return updated;
    }

    /**
     * Discontinue orders.
     * <p/>
     * For pharmacy orders, this sends an HL7 message indicating that dispensing should no longer occur.<br/>
     * For laboratory orders, it is a no-op.<br/>
     * In both cases, the associated invoice items will have their statuses updated to
     * {@link InvoiceItemStatus#DISCONTINUED}, despite the fact that investigations will continue to be processed.
     * <p/>
     * This change in status for invoice items associated with investigations is required to ensure that the
     * {@code PharmacyOrderDiscontinuationJob} doesn't continually process the same POSTED invoices.
     *
     * @param items the invoice items and investigations. These will have their status updated if it was previously
     *              {@link InvoiceItemStatus#ORDERED}
     * @return the updated items
     */
    public Set<Act> discontinue(List<Act> items) {
        Set<Act> updated = new HashSet<>();
        Map<Reference, Act> invoiceItems = getInvoiceItems(items);
        Map<Reference, Act> ordersToInvoiceItems = getOrdersToInvoices(items, invoiceItems);
        Map<Reference, Act> events = new HashMap<>();
        for (Map.Entry<Reference, Order> entry : orders.entrySet()) {
            Order order = entry.getValue();
            if (order instanceof PharmacyOrder) {
                PatientContext context = getPatientContext(order, events);
                if (context != null) {
                    order.discontinue(context, services, user);
                }
            }
            Reference id = entry.getKey();
            Act invoiceItem = ordersToInvoiceItems.get(id);
            if (invoiceItem != null && InvoiceItemStatus.ORDERED.equals(invoiceItem.getStatus())) {
                invoiceItem.setStatus(InvoiceItemStatus.DISCONTINUED);
                updated.add(invoiceItem);
            }
        }
        return updated;
    }

    /**
     * Determines if a product is dispensed via a pharmacy.
     *
     * @param product the product. May be {@code null}
     * @return {@code true} if the product is dispensed via a pharmacy
     */
    public boolean isPharmacyProduct(Product product) {
        return product != null && getPharmacy(product) != null;
    }

    /**
     * Determines if pharmacy orders should be discontinued on finalisation of the invoice.
     *
     * @return {@code true} if pharmacy orders should be discontinued on finalisation of the invoice, or {@code false}
     * if they should be discontinued after a delay
     */
    public boolean discontinueOnFinalisation() {
        return discontinueOnFinalisation;
    }

    /**
     * Returns the references to the ordered acts with their corresponding invoice items.
     *
     * @param items        the invoice items and investigations
     * @param invoiceItems the invoice items, keyed on reference
     * @return references to the ordered acts with their corresponding invoice items
     */
    private Map<Reference, Act> getOrdersToInvoices(List<Act> items, Map<Reference, Act> invoiceItems) {
        Map<Reference, Act> result = new HashMap<>();
        for (Act item : items) {
            if (item.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
                result.put(item.getObjectReference(), item);
            } else {
                result.put(item.getObjectReference(), getInvoiceItem(item, invoiceItems));
            }
        }
        return result;
    }

    /**
     * Returns the invoice item associated with an act.
     *
     * @param act          the act. If it is an invoice item, it will be returned
     * @param invoiceItems invoice items, keyed on reference
     * @return the invoice item
     * @throws IllegalStateException if the invoice item is not found
     */
    private Act getInvoiceItem(Act act, Map<Reference, Act> invoiceItems) {
        Act result;
        if (act.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
            result = act;
        } else {
            IMObjectBean bean = new IMObjectBean(act);
            Reference reference = bean.getSourceRef("invoiceItem");
            result = (reference != null) ? invoiceItems.get(reference) : null;
            if (result == null) {
                throw new IllegalStateException("No invoice item found for " + act);
            }
        }
        return result;
    }

    /**
     * Returns the invoice items from a list of invoice items and investigations.
     *
     * @param items the invoice items and investigation acts
     * @return the invoice items, keyed on reference
     */
    private Map<Reference, Act> getInvoiceItems(List<Act> items) {
        Map<Reference, Act> result = new HashMap<>();
        for (Act item : items) {
            if (item.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
                result.put(item.getObjectReference(), item);
            }
        }
        return result;
    }

    /**
     * Returns the order for an act.
     *
     * @param act the invoice item or investigation
     * @return the order. May be {@code null}
     */
    private Order getOrder(Act act) {
        Order result = null;
        if (act.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
            result = getPharmacyOrder(act);
        } else if (act.isA(InvestigationArchetypes.PATIENT_INVESTIGATION)) {
            result = getLaboratory(act);
        }
        return result;
    }

    /**
     * Returns a pharmacy order.
     *
     * @param act the invoice item
     * @return a pharmacy order, or {@code null} if the product isn't ordered via a pharmacy
     */
    private Order getPharmacyOrder(Act act) {
        Order order = null;
        IMObjectBean bean = new IMObjectBean(act);
        Product product = (Product) getObject(bean.getTargetRef("product"));
        if (product != null && product.isA(ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE)) {
            Entity pharmacy = getPharmacy(product);
            if (pharmacy != null) {
                Party patient = (Party) getObject(bean.getTargetRef("patient"));
                if (patient != null) {
                    BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
                    User clinician = (User) getObject(bean.getTargetRef("clinician"));
                    Reference event = bean.getSourceRef("event");
                    order = new PharmacyOrder(act, product, patient, quantity, clinician, pharmacy, event);
                }
            }
        }
        return order;
    }

    /**
     * Returns a laboratory order.
     *
     * @param act the investigation act
     * @return a laboratory order, or {@code null} if the investigation isn't ordered via a laboratory
     */
    private Order getLaboratory(Act act) {
        Order order = null;
        IMObjectBean bean = new IMObjectBean(act);
        Entity investigationType = (Entity) getObject(bean.getTargetRef("investigationType"));
        if (investigationType != null) {
            IMObjectBean typeBean = new IMObjectBean(investigationType);
            String serviceId = typeBean.getString("universalServiceIdentifier");
            Entity lab = getLaboratory(investigationType);
            if (lab != null && serviceId != null) {
                Party patient = (Party) getObject(bean.getTargetRef("patient"));
                if (patient != null) {
                    User clinician = (User) getObject(bean.getTargetRef("clinician"));
                    Reference event = bean.getSourceRef("event");
                    order = new LaboratoryOrder(act, serviceId, patient, clinician, lab, event);
                }
            }
        }
        return order;
    }

    /**
     * Returns the patient context associated with an order.
     *
     * @param order   the order
     * @param changes the changes
     * @return the patient context, or {@code null} if there is no corresponding patient event
     */
    private PatientContext getPatientContext(Order order, PatientHistoryChanges changes) {
        PatientContext result = null;
        Reference patient = order.getPatient().getObjectReference();
        List<Act> events = changes.getEvents(patient);
        Act event;
        if (events == null || events.isEmpty()) {
            event = changes.getEvent(order.getEvent());
        } else {
            event = Collections.max(events, (o1, o2) ->
                    DateRules.compareDateTime(o1.getActivityStartTime(), o2.getActivityStartTime(), true));
        }
        if (event != null) {
            result = services.getFactory().createContext(order.getPatient(), customer, event, location,
                                                         order.getClinician());
        }
        return result;
    }

    /**
     * Returns the patient context associated with an order.
     *
     * @param order  the order
     * @param events the cache of clinical events
     * @return the patient context, or {@code null} if there is no corresponding patient event
     */
    private PatientContext getPatientContext(Order order, Map<Reference, Act> events) {
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
     * @param updated  the set of changed acts
     * @return {@code true} if an order was created (and invoice updated)
     */
    private boolean createOrder(Act act, Order order, PatientHistoryChanges changes, Set<Party> patients,
                                Set<Act> updated) {
        boolean result = false;
        PatientContext context = getPatientContext(order, changes);
        if (context != null) {
            notifyPatientInformation(context, changes, patients);
            if (order.create(context, services, user, updated)) {
                act.setStatus(InvoiceItemStatus.ORDERED);
                result = true;
                updated.add(act);
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
     * <p/>
     * Note that if the charge was the result of an order being automatically charged and the charge is then removed,
     * there will be nothing to cancel.
     *
     * @param order    the order
     * @param changes  the changes
     * @param patients the patients, used to prevent duplicate patient update notifications being sent
     * @param updated  the set of changed acts
     */
    private void cancelOrder(Order order, PatientHistoryChanges changes, Set<Party> patients, Set<Act> updated) {
        if (!order.getActId().isNew()) {
            PatientContext context = getPatientContext(order, changes);
            if (context != null) {
                notifyPatientInformation(context, changes, patients);
                order.cancel(context, services, user, updated);
            }
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
     * @param product the product. May be {@code null}
     * @return the pharmacy, or {@code null} if none is present
     */
    private Entity getPharmacy(Product product) {
        return pharmacies.getPharmacy(product);
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
        if (laboratory != null && laboratory.isA(HL7Archetypes.LABORATORY_GROUP)) {
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
    private IMObject getObject(Reference reference) {
        return (reference != null) ? cache.get(reference) : null;
    }

    private static abstract class Order {

        private final Reference actId;

        private final Date startTime;

        private final Party patient;

        private final User clinician;

        private final Reference event;

        Order(Reference actId, Date startTime, Party patient, User clinician, Reference event) {
            this.actId = actId;
            this.startTime = startTime;
            this.patient = patient;
            this.clinician = clinician;
            this.event = event;
        }

        public Reference getActId() {
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

        public Reference getEvent() {
            return event;
        }

        public abstract boolean create(PatientContext context, OrderServices services, User user, Set<Act> updated);

        public abstract void cancel(PatientContext context, OrderServices services, User user, Set<Act> updated);

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

        private final BigDecimal quantity;

        private final Entity pharmacy;

        private Product product;


        PharmacyOrder(Act act, Product product, Party patient, BigDecimal quantity, User clinician, Entity pharmacy,
                      Reference event) {
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
        public boolean create(PatientContext context, OrderServices services, User user, Set<Act> updated) {
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
        public void cancel(PatientContext context, OrderServices services, User user, Set<Act> updated) {
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

        private final Act investigation;

        private final String serviceId;

        private final Entity lab;

        LaboratoryOrder(Act act, String serviceId, Party patient, User clinician, Entity lab, Reference event) {
            super(act.getObjectReference(), act.getActivityStartTime(), patient, clinician, event);
            this.investigation = act;
            this.serviceId = serviceId;
            this.lab = lab;
        }

        @Override
        public boolean create(PatientContext context, OrderServices services, User user, Set<Act> updated) {
            LaboratoryOrderService service = services.getLaboratoryService();
            boolean created = service.createOrder(context, getPlacerOrderNumber(), serviceId, getStartTime(), lab,
                                                  user);
            if (created) {
                investigation.setStatus2(InvestigationActStatus.SENT);
                updated.add(investigation);
            }
            return created;
        }

        @Override
        public void cancel(PatientContext context, OrderServices services, User user, Set<Act> updated) {
            LaboratoryOrderService service = services.getLaboratoryService();
            boolean sent = service.cancelOrder(context, getPlacerOrderNumber(), serviceId, getStartTime(), lab, user);
            if (sent) {
                investigation.setStatus(ActStatus.CANCELLED);
                updated.add(investigation);
            }
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
            LaboratoryOrder other = (LaboratoryOrder) newOrder;
            return !ObjectUtils.equals(getPatient(), newOrder.getPatient())
                   || !ObjectUtils.equals(serviceId, other.serviceId)
                   || !ObjectUtils.equals(lab, other.lab);
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
