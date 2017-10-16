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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.util.List;

/**
 * Base class for claim editors.
 *
 * @author Tim Anderson
 */
public abstract class AbstractClaimEditor extends AbstractActEditor {

    /**
     * The total node to sum.
     */
    private final String total;

    /**
     * Constructs an {@link AbstractClaimEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param total   the total node to sum
     * @param context the layout context
     */
    public AbstractClaimEditor(Act act, IMObject parent, String total, LayoutContext context) {
        super(act, parent, context);
        this.total = total;
    }

    /**
     * Returns the item acts to sum.
     *
     * @return the acts
     */
    protected abstract List<Act> getItemActs();

    /**
     * Updates the amount and tax when an act item changes.
     */
    protected void onItemsChanged() {
        calculateAmount();
        calculateTax();
    }

    /**
     * Calculates the amount by summing the child act totals.
     */
    protected void calculateAmount() {
        Property amount = getProperty("amount");
        BigDecimal value = ActHelper.sum(getObject(), getItemActs(), total);
        amount.setValue(value);
    }

    /**
     * Calculates the tax by summing the child act totals.
     */
    protected void calculateTax() {
        Property taxAmount = getProperty("tax");
        BigDecimal tax = ActHelper.sum(getObject(), getItemActs(), "tax");
        taxAmount.setValue(tax);
    }

}
