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

package org.openvpms.insurance.claim;

import org.openvpms.component.model.lookup.Lookup;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Represents a condition being claimed in an insurance claim.
 *
 * @author Tim Anderson
 */
public interface Condition {

    enum Status {
        RESOLVED,     // condition is resolved
        UNRESOLVED,   // condition is ongoing
        DIED,         // animal died from the condition
        EUTHANASED    // animal was euthanased because of the condition
    }

    /**
     * The date when treatment for the condition was started.
     *
     * @return date when treatment for the condition was started
     */
    Date getTreatedFrom();

    /**
     * The date when treatment for the condition was ended.
     *
     * @return date when treatment for the condition was ended
     */
    Date getTreatedTo();

    /**
     * Returns the diagnosis.
     * <p>
     * If no diagnosis is provided, a {@link #getDescription() description} is required.
     *
     * @return the diagnosis. May be {@code null}
     */
    Lookup getDiagnosis();

    /**
     * Returns the condition description.
     * <p>
     * This can provide a short summary of the condition.
     *
     * @return the condition description. May be {@code null}
     */
    String getDescription();

    /**
     * Returns the status of the animal as a result of this condition.
     *
     * @return the status of the animal
     */
    Status getStatus();

    /**
     * Returns the reason for euthanasing the animal, if {@link #getStatus()} is {@code EUTHANASED}.
     *
     * @return the reason for euthanasing the animal
     */
    String getEuthanasiaReason();

    /**
     * Returns the consultation notes.
     *
     * @return the consultation notes
     */
    List<Note> getConsultationNotes();

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    BigDecimal getDiscount();

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    BigDecimal getDiscountTax();

    /**
     * Returns the total amount, including tax.
     *
     * @return the total amount
     */
    BigDecimal getTotal();

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    BigDecimal getTotalTax();

    /**
     * Returns the invoices being claimed.
     *
     * @return the invoices being claimed
     */
    List<Invoice> getInvoices();

}
