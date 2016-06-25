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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.customer.StockOnHand;
import org.openvpms.web.workspace.patient.mr.Prescriptions;

/**
 * Edit context for customer charges.
 *
 * @author Tim Anderson
 */
public class CustomerChargeEditContext extends ChargeEditContext {

    /**
     * The save context.
     */
    private final ChargeSaveContext saveContext;

    /**
     * The stock on hand.
     */
    private final StockOnHand stock;

    /**
     * The prescriptions. May be {@code null}.
     */
    private Prescriptions prescriptions;

    /**
     * Constructs a {@link CustomerChargeEditContext}.
     *
     * @param customer the customer
     * @param location the practice location. May be {@code null}
     * @param context  the layout context
     */
    public CustomerChargeEditContext(Party customer, Party location, LayoutContext context) {
        super(customer, location, context);
        saveContext = new ChargeSaveContext();
        stock = new StockOnHand(new StockRules(getCachingArchetypeService()));
    }

    /**
     * Returns the save context.
     *
     * @return the save context
     */
    public ChargeSaveContext getSaveContext() {
        return saveContext;
    }

    /**
     * Returns the stock on hand.
     *
     * @return the stock on hand
     */
    public StockOnHand getStock() {
        return stock;
    }

    /**
     * Sets the prescriptions.
     *
     * @param prescriptions the prescriptions. May be {@code null}
     */
    public void setPrescriptions(Prescriptions prescriptions) {
        this.prescriptions = prescriptions;
    }

    /**
     * Returns the prescriptions.
     *
     * @return the prescriptions. May be {@code null}
     */
    public Prescriptions getPrescriptions() {
        return prescriptions;
    }

}
