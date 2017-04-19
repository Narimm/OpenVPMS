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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.order;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

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
    private final IMObjectReference location;

    /**
     * The order bean.
     */
    private ActBean orderBean;

    /**
     * The return bean.
     */
    private ActBean returnBean;

    /**
     * The order/return acts.
     */
    private final List<Act> acts = new ArrayList<>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The notes node name.
     */
    private static final String NOTES = "notes";

    /**
     * Constructs an {@link CustomerOrder}.
     *
     * @param patient  the patient. May be {@code null}
     * @param customer the customer. May be {@code null}
     * @param note     the note. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     */
    public CustomerOrder(Party patient, Party customer, String note, IMObjectReference location,
                         IArchetypeService service) {
        this.patient = patient;
        this.customer = customer;
        this.note = note;
        this.location = location;
        this.service = service;
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
    public ActBean getOrder() {
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
    public ActBean getReturn() {
        if (returnBean == null) {
            returnBean = createReturn();
        }
        return returnBean;
    }

    /**
     * Creates a new order item.
     *
     * @return a new order item
     */
    public abstract ActBean createOrderItem();

    /**
     * Creates a new order return item.
     *
     * @return a new order return item
     */
    public abstract ActBean createReturnItem();

    /**
     * Returns the acts.
     *
     * @return the acts
     */
    public List<Act> getActs() {
        return acts;
    }

    /**
     * Adds a note to the notes node.
     *
     * @param bean  the act bean
     * @param value the note to add
     */
    public static void addNote(ActBean bean, String value) {
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
    protected abstract ActBean createOrder();

    /**
     * Creates a new order return.
     *
     * @return the return
     */
    protected abstract ActBean createReturn();

    /**
     * Creates the parent act.
     *
     * @param archetype the act archetype
     * @return a new act
     */
    protected ActBean createParent(String archetype) {
        Act act = (Act) service.create(archetype);
        ActBean bean = new ActBean(act, service);
        if (customer != null) {
            bean.addNodeParticipation("customer", customer);
        }
        if (location != null) {
            bean.addNodeParticipation("location", location);
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
    protected ActBean createItem(String archetype, ActBean parent) {
        Act act = (Act) service.create(archetype);
        ActBean bean = new ActBean(act, service);
        if (patient != null) {
            bean.addNodeParticipation("patient", patient);
        }
        parent.addNodeRelationship("items", act);
        acts.add(act);
        return bean;
    }

}
