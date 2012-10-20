/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.app.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;


/**
 * An editor for <em>act.customerAccountInvoiceItem</em> acts, in the context of a patient visit.
 *
 * @author Tim Anderson
 */
public class VisitChargeItemEditor extends CustomerChargeActItemEditor {

    /**
     * Filters patient node.
     */
    private static final NodeFilter patientFilter = new NamedNodeFilter("patient");

    /**
     * Constructs a {@code VisitChargeActItemEditor}.
     * <p/>
     * This recalculates the tax amount.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public VisitChargeItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        initParticipant("patient", context.getContext().getPatient());
    }

    /**
     * Creates the layout strategy.
     *
     * @param fixedPrice the fixed price editor
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(FixedPriceEditor fixedPrice) {
        return new CustomerChargeItemLayoutStrategy(fixedPrice) {
            @Override
            protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
                return FilterHelper.chain(patientFilter, context.getDefaultNodeFilter(), getFilter());
            }
        };
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        // the patient node is hidden, so need to update the product with the current patient to restrict
        // product searches by species
        ProductParticipationEditor product = getProductEditor();
        if (product != null) {
            product.setPatient(getPatient());
        }
    }
}
