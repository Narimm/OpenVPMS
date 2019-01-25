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

package org.openvpms.archetype.rules.finance.order;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.ActRelationship;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.product.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages state when processing customer orders and returns.
 *
 * @author Tim Anderson
 */
public abstract class CustomerOrder {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * A note to add to the order/return.
     */
    private final String note;

    /**
     * The practice location.
     */
    private final Reference location;

    /**
     * The order/return acts.
     */
    private final List<Act> acts = new ArrayList<>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The order bean.
     */
    private IMObjectBean orderBean;

    /**
     * The return bean.
     */
    private IMObjectBean returnBean;

    /**
     * The notes node name.
     */
    private static final String NOTES = "notes";

    /**
     * Constructs a {@link CustomerOrder}.
     *
     * @param patient  the patient. May be {@code null}
     * @param customer the customer. May be {@code null}
     * @param note     the note. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     */
    public CustomerOrder(Party patient, Party customer, String note, Reference location, IArchetypeService service) {
        this.patient = patient;
        this.customer = customer;
        this.note = note;
        this.location = location;
        this.service = service;
    }

    /**
     * Constructs a {@link CustomerOrder}.
     *
     * @param act     the order/order return act
     * @param isOrder if {@code true}, the act is an order, otherwise it is an order return
     * @param service the archetype service
     */
    public CustomerOrder(Act act, boolean isOrder, IArchetypeService service) {
        IMObjectBean bean = service.getBean(act);
        if (isOrder) {
            orderBean = bean;
        } else {
            returnBean = bean;
        }
        List<Act> items = bean.getTargets("items", Act.class);
        customer = bean.getTarget("customer", Party.class);
        note = bean.getString(NOTES);
        location = bean.getTargetRef("location");
        if (!items.isEmpty()) {
            IMObjectBean itemBean = service.getBean(items.get(0));
            patient = itemBean.getTarget("patient", Party.class);
        } else {
            patient = null;
        }
        this.service = service;
        acts.add(act);
        acts.addAll(items);
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Returns the order, creating it if required.
     *
     * @return the order
     */
    public IMObjectBean getOrder() {
        if (orderBean == null) {
            orderBean = createOrder();
        }
        return orderBean;
    }

    /**
     * Returns the order return, creating it if required.
     *
     * @return the order return
     */
    public IMObjectBean getReturn() {
        if (returnBean == null) {
            returnBean = createReturn();
        }
        return returnBean;
    }

    /**
     * Determines if an order is present.
     *
     * @return {@code true} if an order is present, {@code false} if an order return is present
     */
    public boolean hasOrder() {
        return orderBean != null;
    }

    /**
     * Creates a new order item.
     *
     * @return a new order item
     */
    public abstract IMObjectBean createOrderItem();

    /**
     * Creates a new order return item.
     *
     * @return a new order return item
     */
    public abstract IMObjectBean createReturnItem();

    /**
     * Returns the acts.
     *
     * @return the acts
     */
    public List<Act> getActs() {
        return acts;
    }

    /**
     * Saves all of the acts associated with the order and/or order return.
     */
    public void save() {
        service.save(acts);
    }

    /**
     * Removes the order and/or order return.
     */
    public void remove() {
        if (orderBean != null) {
            service.remove(orderBean.getObject());
        }
        if (returnBean != null) {
            service.remove(returnBean.getObject());
        }
    }

    /**
     * Adds a note to the notes node.
     *
     * @param bean  the act bean
     * @param value the note to add
     */
    public static void addNote(IMObjectBean bean, String value) {
        String notes = bean.getString(NOTES);
        if (!StringUtils.isEmpty(notes)) {
            notes += "\n" + value;
        } else {
            notes = value;
        }
        bean.setValue(NOTES, StringUtils.abbreviate(notes, bean.getMaxLength(NOTES)));
    }

    /**
     * Creates a new order.
     *
     * @return the order
     */
    protected abstract IMObjectBean createOrder();

    /**
     * Creates a new order return.
     *
     * @return the return
     */
    protected abstract IMObjectBean createReturn();

    /**
     * Creates the parent act.
     *
     * @param archetype the act archetype
     * @return a new act
     */
    protected IMObjectBean createParent(String archetype) {
        Act act = (Act) service.create(archetype);
        IMObjectBean bean = service.getBean(act);
        if (customer != null) {
            bean.setTarget("customer", customer);
        }
        if (location != null) {
            bean.setTarget("location", location);
        }
        if (note != null) {
            addNote(bean, note);
        }
        acts.add(act);
        return bean;
    }

    /**
     * Creates a new item.
     *
     * @param archetype the act archetype
     * @param parent    the parent act
     * @return a new act
     */
    protected IMObjectBean createItem(String archetype, IMObjectBean parent) {
        Act act = (Act) service.create(archetype);
        IMObjectBean bean = service.getBean(act);
        if (patient != null) {
            bean.setTarget("patient", patient);
        }
        ActRelationship relationship = (ActRelationship) parent.addTarget("items", act);
        act.addActRelationship(relationship);
        acts.add(act);
        return bean;
    }

    /**
     * Returns the first item for a given product.
     *
     * @param archetype the item archetype
     * @param product   the product. May be {@code null}
     * @return the item, or {@code null} if none is found
     */
    protected IMObjectBean getItem(String archetype, Product product) {
        Reference reference = (product != null) ? product.getObjectReference() : null;
        for (Act act : acts) {
            if (act.isA(archetype)) {
                IMObjectBean bean = service.getBean(act);
                if (ObjectUtils.equals(reference, bean.getTargetRef("product"))) {
                    return bean;
                }
            }
        }
        return null;
    }

}
