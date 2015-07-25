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

package org.openvpms.hl7.impl;

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
abstract class OrderState {

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
     * Constructs an {@link OrderState}.
     *
     * @param patient  the patient. May be {@code null}
     * @param customer the customer. May be {@code null}
     * @param note     the note. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     */
    public OrderState(Party patient, Party customer, String note, IMObjectReference location,
                      IArchetypeService service) {
        this.patient = patient;
        this.customer = customer;
        this.note = note;
        this.location = location;
        this.service = service;
    }

    public Party getPatient() {
        return patient;
    }

    public ActBean getOrder() {
        if (orderBean == null) {
            orderBean = createOrder();
        }
        return orderBean;
    }

    public ActBean getReturn() {
        if (returnBean == null) {
            returnBean = createReturn();
        }
        return returnBean;
    }

    public abstract ActBean createOrderItem();

    public abstract ActBean createReturnItem();

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
        String notes = bean.getString("notes");
        if (!StringUtils.isEmpty(notes)) {
            notes += "\n" + value;
        } else {
            notes = value;
        }
        bean.setValue("notes", notes);
    }

    /**
     * Creates a new order.
     *
     * @return the order
     */
    protected abstract ActBean createOrder();

    /**
     * Creates a new return.
     *
     * @return the return
     */
    protected abstract ActBean createReturn();

    protected ActBean createParent(String shortName) {
        Act act = (Act) service.create(shortName);
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

    protected ActBean createItem(String shortName, ActBean parent) {
        Act act = (Act) service.create(shortName);
        ActBean bean = new ActBean(act, service);
        if (patient != null) {
            bean.addNodeParticipation("patient", patient);
        }
        parent.addNodeRelationship("items", act);
        acts.add(act);
        return bean;
    }

}
