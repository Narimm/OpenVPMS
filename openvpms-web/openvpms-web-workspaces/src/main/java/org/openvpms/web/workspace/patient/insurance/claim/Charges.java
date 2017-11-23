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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks charges across a claim.
 *
 * @author Tim Anderson
 */
class Charges {

    /**
     * The charges, keyed on reference.
     */
    private Map<IMObjectReference, Act> charges = new HashMap<>();


    /**
     * Constructs a {@link Charges}.
     */
    public Charges() {

    }

    /**
     * Adds a charge item.
     *
     * @param item the charge item
     */
    public void add(Act item) {
        charges.put(item.getObjectReference(), item);
    }

    /**
     * Removes a charge item.
     *
     * @param item the charge item
     */
    public void remove(Act item) {
        charges.remove(item.getObjectReference());
    }

    /**
     * Determines if a charge item exists.
     *
     * @param item the charge item
     * @return {@code true} if the charge item exists
     */
    public boolean contains(Act item) {
        return contains(item.getObjectReference());
    }

    /**
     * Determines if a charge item exists.
     *
     * @param reference the charge item reference
     * @return {@code true} if the charge item exists
     */
    public boolean contains(IMObjectReference reference) {
        return charges.containsKey(reference);
    }

    /**
     * Returns the references of each invoice associated with a charge.
     *
     * @return the invoice references
     */
    public Set<IMObjectReference> getInvoiceRefs() {
        Set<IMObjectReference> invoices = new HashSet<>();
        for (Act item : charges.values()) {
            ActBean bean = new ActBean(item);
            IMObjectReference invoiceRef = bean.getSourceRef("invoice");
            if (invoiceRef != null) {
                invoices.add(invoiceRef);
            }
        }
        return invoices;
    }
}
