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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event.impl;

import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.order.CustomerPharmacyOrder;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.hl7.impl.HL7MessageHelper;
import org.openvpms.smartflow.model.Medic;
import org.openvpms.smartflow.model.Treatment;
import org.openvpms.smartflow.model.Treatments;
import org.openvpms.smartflow.model.event.TreatmentEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.openvpms.archetype.rules.finance.order.CustomerOrder.addNote;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;

/**
 * Processes {@link TreatmentEvent}s.
 *
 * @author Tim Anderson
 */
public class TreatmentEventProcessor extends EventProcessor<TreatmentEvent> {

    /**
     * The location.
     */
    private final Party location;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * Supported product archetypes.
     */
    private static final String[] PRODUCT_ARCHETYPES = {ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE,
                                                        ProductArchetypes.SERVICE};

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(TreatmentEventProcessor.class);

    /**
     * Constructs a {@link TreatmentEventProcessor}.
     *
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     * @param lookups  the lookup service
     * @param rules    the patient rules
     */
    public TreatmentEventProcessor(Party location, IArchetypeService service, ILookupService lookups,
                                   PatientRules rules) {
        super(service);
        this.location = location;
        this.lookups = lookups;
        this.rules = rules;
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(TreatmentEvent event) {
        Treatments list = event.getObject();
        if (list != null && list.getTreatments() != null) {
            List<Treatment> treatments = new ArrayList<>(list.getTreatments());
            // sort treatments to process removals first, as a workaround for OVPMS-2028
            Collections.sort(treatments, (o1, o2) -> {
                Comparator<Object> comparator = ComparatorUtils.nullLowComparator(null);
                int result = comparator.compare(o1.getTreatmentGuid(), o2.getTreatmentGuid());
                if (result == 0) {
                    Status status1 = Status.fromString(o1.getStatus());
                    Status status2 = Status.fromString(o2.getStatus());
                    result = status1.compareTo(status2);
                }
                return result;
            });
            for (Treatment treatment : treatments) {
                treated(treatment);
            }
        }
    }

    /**
     * Invoked when a patient is treated.
     * <p>
     * If a treatment is billed, a customer order/return will be created.
     *
     * @param treatment the treatment
     */
    protected void treated(Treatment treatment) {
        if (log.isDebugEnabled()) {
            log.debug("treatment=" + treatment.getTreatmentGuid() + ", inventoryId=" + treatment.getInventoryId()
                      + ", name=" + treatment.getName() + ", quantity=" + treatment.getQty()
                      + ", status=" + treatment.getStatus() + ", billed=" + treatment.getBilled());
        }
        Act visit = getVisit(treatment.getHospitalizationId());
        Party patient = null;
        Party customer = null;
        if (visit != null) {
            patient = getPatient(visit);
            if (patient != null) {
                customer = rules.getOwner(patient);
            }
        }
        Product product = getProduct(treatment);
        IMObjectReference clinician = getClinician(treatment);

        if (Treatment.ADDED_STATUS.equals(treatment.getStatus())) {
            if (treatment.getBilled()) {
                treatmentAdded(treatment, visit, patient, customer, product, null, clinician);
            }
        } else if (Treatment.CHANGED_STATUS.equals(treatment.getStatus())) {
            treatmentChanged(treatment, visit, patient, customer, product, clinician);
        } else if (Treatment.REMOVED_STATUS.equals(treatment.getStatus())) {
            treatmentRemoved(treatment, visit, patient, customer, product, clinician);
        }
    }

    /**
     * Returns the orders associated with a treatment.
     *
     * @param treatment the treatment
     * @return the orders
     */
    protected List<CustomerPharmacyOrder> getOrders(Treatment treatment) {
        return getOrders(treatment.getTreatmentGuid(), getService());
    }

    /**
     * Returns the orders associated with a treatment
     *
     * @param treatmentGuid the treatment identifier
     * @param service       the archetype service
     * @return the orders, ordered on increasing time
     */
    protected static List<CustomerPharmacyOrder> getOrders(String treatmentGuid, IArchetypeService service) {
        List<CustomerPharmacyOrder> result = new ArrayList<>();
        String[] archetypes = {OrderArchetypes.PHARMACY_ORDER, OrderArchetypes.PHARMACY_RETURN};
        ArchetypeQuery query = new ArchetypeQuery(archetypes, false, false);
        query.add(join("identities", shortName(SFS_IDENTITY)).add(eq("identity", treatmentGuid)));
        query.add(Constraints.sort("startTime"));
        query.add(Constraints.sort("id"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            result.add(new CustomerPharmacyOrder(iterator.next(), service));
        }
        return result;
    }

    /**
     * Invoked when a treatment is added. Creates a new order.
     *
     * @param treatment the treatment
     * @param visit     the patient visit. May be {@code null}
     * @param patient   the patient. May be {@code null}
     * @param customer  the customer. May be {@code null}
     * @param product   the product. May be {@code null}
     * @param note      a note to add to the order. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     */
    private void treatmentAdded(Treatment treatment, Act visit, Party patient, Party customer, Product product,
                                String note, IMObjectReference clinician) {
        BigDecimal quantity = getQuantity(treatment);
        if (!MathRules.isZero(quantity)) {
            createOrder(treatment, visit, patient, customer, product, quantity, note, clinician);
        }
    }

    /**
     * Invoked when a treatment is added. Amends the existing order, or creates a new order/order return with the
     * difference if the order(s) have been POSTED.
     *
     * @param treatment the treatment
     * @param visit     the patient visit. May be {@code null}
     * @param patient   the patient. May be {@code null}
     * @param customer  the customer. May be {@code null}
     * @param product   the product. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     */
    private void treatmentChanged(Treatment treatment, Act visit, Party patient, Party customer, Product product,
                                  IMObjectReference clinician) {
        List<CustomerPharmacyOrder> orders = getOrders(treatment);
        if (orders.isEmpty()) {
            if (treatment.getBilled()) {
                treatmentAdded(treatment, visit, patient, customer, product,
                               "NOTE: Treatment changed, but original order not found.", clinician);
            }
        } else {
            if (treatment.getBilled()) {
                BigDecimal quantity = getQuantity(treatment);
                update(orders, treatment, visit, patient, customer, product, quantity, clinician);
            } else {
                update(orders, treatment, visit, patient, customer, product, BigDecimal.ZERO, clinician);
            }
        }
    }

    /**
     * Returns the product from a set of order/order returns.
     * <p>
     * The most recent order/order return is used.
     *
     * @param orders the order/order returns
     * @return the product, or {@code null} if none is found
     */
    private Product getProduct(List<CustomerPharmacyOrder> orders) {
        Product result = null;
        CustomerPharmacyOrder order = orders.get(orders.size() - 1);
        for (Act act : order.getActs()) {
            IMObjectBean bean = getService().getBean(act);
            if (bean.hasNode("product")) {
                Product product = bean.getTarget("product", Product.class);
                if (product != null) {
                    result = product;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Invoked when a treatment is removed.
     *
     * @param treatment the treatment
     * @param visit     the patient visit. May be {@code null}
     * @param patient   the patient. May be {@code null}
     * @param customer  the customer. May be {@code null}
     * @param product   the product. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     */
    private void treatmentRemoved(Treatment treatment, Act visit, Party patient, Party customer, Product product,
                                  IMObjectReference clinician) {
        List<CustomerPharmacyOrder> orders = getOrders(treatment);
        if (orders.isEmpty()) {
            if (treatment.getBilled()) {
                // the treatment was billed, but no existing order could be found. May have been deleted by a user
                log.warn("Treatment=" + treatment.getTreatmentGuid() + " removed, but no order found");
            }
        } else {
            update(orders, treatment, visit, patient, customer, product, BigDecimal.ZERO, clinician);
        }
    }

    /**
     * Updates the orders associated with a treatment.
     * <p>
     * This takes into account existing IN_PROGRESS and POSTED orders and order returns.
     * <p>
     * If the total POSTED quantity is:
     * <ul>
     * <li>the same as the new treatment quantity, any IN_PROGRESS order or return is removed</li>
     * <li>less than the new treatment quantity, an order will be created to make up the difference</li>
     * <li>greater than the new treatment quantity, an order return will be created to reverse the difference</li>
     * </ul>
     *
     * @param orders      the existing orders
     * @param treatment   the treatment
     * @param visit       the patient visit. May be {@code null}
     * @param patient     the patient. May be {@code null}
     * @param customer    the customer. May be {@code null}
     * @param product     the product. May be {@code null}
     * @param newQuantity the new treatment quantity
     * @param clinician   the clinician. May be {@code null}
     */
    private void update(List<CustomerPharmacyOrder> orders, Treatment treatment, Act visit, Party patient,
                        Party customer, Product product, BigDecimal newQuantity, IMObjectReference clinician) {
        if (product == null) {
            // get the product from the last order/order return
            product = getProduct(orders);
        }

        ChargedState state = new ChargedState(orders, product);
        BigDecimal postedQuantity = state.getPostedQuantity();
        CustomerPharmacyOrder inProgress = state.getInProgress();
        if (postedQuantity.compareTo(newQuantity) == 0) {
            if (inProgress != null) {
                inProgress.remove();
            }
        } else if (postedQuantity.compareTo(newQuantity) < 0) {
            BigDecimal diff = newQuantity.subtract(postedQuantity);
            if (inProgress == null) {
                createOrder(treatment, visit, patient, customer, product, diff, null, clinician);
            } else if (inProgress.hasOrder()) {
                ActBean item = inProgress.getItem(product);
                if (item == null) {
                    item = inProgress.createOrderItem();
                }
                populateItem(inProgress.getOrder(), item, treatment, product, diff, clinician);
                inProgress.save();
            } else {
                // remove the existing return, and add a new order
                inProgress.remove();
                createOrder(treatment, visit, patient, customer, product, diff, null, clinician);
            }
        } else {
            // the ordered quantity is greater than the treatment quantity
            BigDecimal diff = postedQuantity.subtract(newQuantity);
            if (inProgress == null) {
                createReturn(treatment, visit, patient, customer, product, diff, null, clinician);
            } else if (inProgress.hasOrder()) {
                // remove the existing order, and add a new return
                inProgress.remove();
                createReturn(treatment, visit, patient, customer, product, diff, null, clinician);
            } else {
                ActBean item = inProgress.getItem(product);
                if (item == null) {
                    item = inProgress.createReturnItem();
                }
                populateItem(inProgress.getReturn(), item, treatment, product, diff, clinician);
                inProgress.save();
            }
        }
    }

    /**
     * Creates a pharmacy order for a treatment.
     *
     * @param treatment the treatment
     * @param visit     the patient visit. May be {@code null}
     * @param patient   the patient. May be {@code null}
     * @param customer  the customer. May be {@code null}
     * @param product   the product. May be {@code null}
     * @param quantity  the quantity
     * @param note      a note. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     */
    private void createOrder(Treatment treatment, Act visit, Party patient, Party customer, Product product,
                             BigDecimal quantity, String note, IMObjectReference clinician) {
        IArchetypeService service = getService();
        CustomerPharmacyOrder order = new CustomerPharmacyOrder(
                patient, customer, null, location != null ? location.getObjectReference() : null, service);
        populate(order.getOrder(), treatment, visit, patient, customer, note, clinician);
        populateItem(order.getOrder(), order.createOrderItem(), treatment, product, quantity, clinician);
        service.save(order.getActs());
    }

    /**
     * Creates a pharmacy order return for a treatment.
     *
     * @param treatment the treatment
     * @param visit     the patient visit. May be {@code null}
     * @param patient   the patient. May be {@code null}
     * @param customer  the customer. May be {@code null}
     * @param product   the product. May be {@code null}
     * @param quantity  the quantity
     * @param note      a note. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     */
    private void createReturn(Treatment treatment, Act visit, Party patient, Party customer, Product product,
                              BigDecimal quantity, String note, IMObjectReference clinician) {
        IArchetypeService service = getService();
        CustomerPharmacyOrder orderReturn = new CustomerPharmacyOrder(
                patient, customer, null, location != null ? location.getObjectReference() : null, service);
        populate(orderReturn.getReturn(), treatment, visit, patient, customer, note, clinician);
        populateItem(orderReturn.getReturn(), orderReturn.createReturnItem(), treatment, product, quantity, clinician);
        service.save(orderReturn.getActs());
    }

    /**
     * Populates an order/order return.
     *
     * @param act       the order/order return act
     * @param treatment the treatment
     * @param visit     the patient visit. May be {@code null}
     * @param patient   the patient. May be {@code null}
     * @param customer  the customer. May be {@code null}
     * @param note      a note. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     */
    private void populate(ActBean act, Treatment treatment, Act visit, Party patient, Party customer, String note,
                          IMObjectReference clinician) {
        ActIdentity identity = createIdentity(treatment.getTreatmentGuid());
        act.getAct().addIdentity(identity);

        if (!StringUtils.isEmpty(note)) {
            addNote(act, note);
        }
        if (!StringUtils.isEmpty(treatment.getValue())) {
            addNote(act, treatment.getValue());
        }
        if (visit == null) {
            addNote(act, "Unknown visit, Id='" + treatment.getHospitalizationId()
                         + "'. The customer and patient cannot be determined");
        } else if (patient == null) {
            addNote(act, "Cannot determine patient for visit");
        } else if (customer == null) {
            addNote(act, "Cannot determine customer for patient");
        }

        if (clinician != null) {
            act.setNodeParticipant("clinician", clinician);
        }
    }

    /**
     * Populates an order/order return item.
     *
     * @param order     the order/order return
     * @param item      the order/order return item
     * @param treatment the treatment
     * @param product   the product. May be {@code null}
     * @param quantity  the quantity
     * @param clinician the clinician. May be {@code null}
     */
    private void populateItem(ActBean order, ActBean item, Treatment treatment, Product product, BigDecimal quantity,
                              IMObjectReference clinician) {
        item.setValue("quantity", quantity);
        if (product != null) {
            item.setNodeParticipant("product", product);

            String units = treatment.getUnits();
            IMObjectBean productBean = getService().getBean(product);
            String sellingUnits = productBean.getString("sellingUnits");
            if (!StringUtils.isEmpty(units) && !StringUtils.isEmpty(sellingUnits)) {
                boolean match = true;
                if (!units.equalsIgnoreCase(sellingUnits)) {
                    Lookup uom = lookups.getLookup("lookup.uom", sellingUnits);
                    if (uom != null && !StringUtils.isEmpty(uom.getName())) {
                        if (!units.equalsIgnoreCase(uom.getName())) {
                            sellingUnits = uom.getName();
                            match = false;
                        }
                    } else {
                        match = false;
                    }
                }
                if (!match) {
                    addNote(order, "Dispensing units ('" + units + "')" + " do not match selling units ('"
                                   + sellingUnits + "')");
                }
            }
        } else {
            addNote(order, "Unknown Treatment, Id='" + treatment.getInventoryId()
                           + "', name='" + treatment.getName() + "'");
        }
        if (clinician != null) {
            item.setNodeParticipant("clinician", clinician);
        }
    }

    /**
     * Returns the product associated with a treatment.
     *
     * @param treatment the treatment
     * @return the product, or {@code null} if none is found
     */
    private Product getProduct(Treatment treatment) {
        Product result = null;
        long id = SmartFlowSheetHelper.getId(treatment.getInventoryId());
        if (id != -1) {
            ArchetypeQuery query = new ArchetypeQuery(PRODUCT_ARCHETYPES, true, true);
            query.add(eq("id", id));
            IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<>(getService(), query);
            result = (iterator.hasNext()) ? iterator.next() : null;
        }
        return result;
    }

    /**
     * Returns the treatment quantity.
     *
     * @param treatment the treatment
     * @return the quantity, rounded to 3 decimal places
     */
    private BigDecimal getQuantity(Treatment treatment) {
        BigDecimal qty = treatment.getQty();
        if (qty == null) {
            qty = BigDecimal.ZERO;
        } else {
            qty = MathRules.round(qty, 3);
        }
        return qty;
    }

    /**
     * Returns the clinician associated with a treatment.
     *
     * @param treatment the treatment
     * @return the clinician reference, or {@code null} if none is found
     */
    private IMObjectReference getClinician(Treatment treatment) {
        IMObjectReference result = null;
        Medic doctor = treatment.getDoctor();
        if (doctor != null) {
            long id = HL7MessageHelper.getId(doctor.getMedicId());
            if (id != -1) {
                ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER);
                query.getArchetypeConstraint().setAlias("user");
                query.add(new ObjectRefSelectConstraint("user"));
                query.add(join("classifications").add(eq("code", UserArchetypes.CLINICIAN_USER_TYPE)));
                query.add(eq("id", id));
                ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(getService(), query);
                if (iterator.hasNext()) {
                    ObjectSet set = iterator.next();
                    result = set.getReference("user.reference");
                }
                if (result == null) {
                    log.warn("No clinician found for id=" + doctor.getMedicId() + ", name=" + doctor.getName());
                }
            }
        }
        return result;
    }

    /**
     * Helper to determine the POSTED quantity associated with a treatment.
     */
    private class ChargedState {

        /**
         * The POSTED quantity.
         */
        private BigDecimal quantity = BigDecimal.ZERO;

        /**
         * The IN_PROGRESS order.
         */
        private CustomerPharmacyOrder inProgress;

        /**
         * Constructs a {@link ChargedState}.
         *
         * @param orders  the orders
         * @param product the treatment product. May be {@code null}
         */
        public ChargedState(List<CustomerPharmacyOrder> orders, Product product) {
            for (CustomerPharmacyOrder order : orders) {
                boolean isOrder = order.hasOrder();
                ActBean bean = isOrder ? order.getOrder() : order.getReturn();
                String status = bean.getStatus();
                ActBean item = order.getItem(product);
                BigDecimal itemQty = BigDecimal.ZERO;
                if (item != null) {
                    itemQty = item.getBigDecimal("quantity", BigDecimal.ZERO);
                    if (!isOrder) {
                        itemQty = itemQty.negate();
                    }
                }
                if (ActStatus.IN_PROGRESS.equals(status)) {
                    inProgress = order;
                } else {
                    quantity = quantity.add(itemQty);
                }
            }
        }

        /**
         * Returns the quantity that has already been POSTED.
         *
         * @return the POSTED quantity. May be negative due to returns
         */
        public BigDecimal getPostedQuantity() {
            return quantity;
        }

        /**
         * Returns the current IN_PROGRESS order or return.
         *
         * @return the order or return, or {@code null} if none is found
         */
        public CustomerPharmacyOrder getInProgress() {
            return inProgress;
        }
    }

    /**
     * Treatment statuses, ordered such that removals are processed first.
     */
    private enum Status {
        removed, added, changed, not_changed, unknown;

        /**
         * Returns the status for a status name.
         * @param status the status name
         * @return the corresponding status code, or {@code unknown} if it is {@code null} or invalid
         */
        public static Status fromString(String status) {
            Status result;
            if (status != null) {
                try {
                    result = Status.valueOf(status);
                } catch (IllegalArgumentException ignore) {
                    result = Status.unknown;
                }
            } else {
                result = Status.unknown;
            }
            return result;
        }
    }

}
