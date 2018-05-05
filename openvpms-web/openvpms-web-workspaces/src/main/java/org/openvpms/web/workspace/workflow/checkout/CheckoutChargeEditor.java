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

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;

/**
 * An editor for <em>act.customerAccountChargesInvoice</em> at checkout.
 * <p/>
 * This is responsible for updating the associated appointments on save to indicate that boarding charges have been
 * applied.
 *
 * @author Tim Anderson
 */
class CheckoutChargeEditor extends CustomerChargeActEditor {

    /**
     * The visits.
     */
    private final Visits visits;

    /**
     * Constructs a {@link CheckoutChargeEditor}.
     *
     * @param act     the act to edit
     * @param visits  the visits
     * @param context the layout context
     */
    public CheckoutChargeEditor(FinancialAct act, Visits visits, LayoutContext context) {
        super(act, null, context);
        this.visits = visits;
    }

    /**
     * Constructs a {@link CheckoutChargeEditor}.
     *
     * @param act            the act to edit
     * @param visits         the visits
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
     */
    public CheckoutChargeEditor(FinancialAct act, Visits visits, LayoutContext context, boolean addDefaultItem) {
        super(act, null, context, addDefaultItem);
        this.visits = visits;
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance
     * @throws OpenVPMSException if a new instance cannot be created
     */
    @Override
    public IMObjectEditor newInstance() {
        visits.reload();
        return new CheckoutChargeEditor(reload(getObject()), visits, getLayoutContext(), getAddDefaultIem());
    }

    /**
     * Save any edits.
     * <p/>
     * For invoices, this links items to their corresponding clinical events, creating events as required, and marks
     * matching reminders completed.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        super.doSave();
        visits.save();
    }

    /**
     * Returns the visits.
     *
     * @return the visits
     */
    public Visits getVisits() {
        return visits;
    }

}
