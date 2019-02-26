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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.math.BigDecimal;

/**
 * Claim gap payment editor.
 *
 * @author Tim Anderson
 */
public class GapPaymentEditor extends CustomerPaymentEditor {

    /**
     * Constructs a {@link CustomerPaymentEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     * @param invoice the invoice amount
     */
    GapPaymentEditor(Act act, IMObject parent, LayoutContext context, BigDecimal invoice) {
        super(act, parent, context, invoice);
        act.setStatus(ActStatus.POSTED);
        setExpectedAmount(invoice);
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance
     * @throws OpenVPMSException if a new instance cannot be created
     */
    @Override
    public IMObjectEditor newInstance() {
        return new GapPaymentEditor(reloadPayment(), getParent(), getLayoutContext(), getInvoiceAmount());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PaymentLayoutStrategy(getItems());
    }

    private class PaymentLayoutStrategy extends LayoutStrategy {
        /**
         * Creates a new {@code LayoutStrategy}.
         *
         * @param editor the act items editor
         */
        PaymentLayoutStrategy(IMObjectCollectionEditor editor) {
            super(editor);
            // don't allow editing of the status
            getArchetypeNodes().exclude("status");
        }
    }
}
