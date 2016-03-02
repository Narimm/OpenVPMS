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

package org.openvpms.web.workspace.patient.estimate;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.workspace.customer.charge.ChargeEditContext;
import org.openvpms.web.workspace.customer.estimate.EstimateItemEditor;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class VisitEstimateItemEditor extends EstimateItemEditor {

    /**
     * Constructs an {@link VisitEstimateItemEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent act
     * @param context       the edit context
     * @param layoutContext the layout context
     */
    public VisitEstimateItemEditor(Act act, Act parent, ChargeEditContext context, LayoutContext layoutContext) {
        super(act, parent, context, layoutContext);
        initParticipant("patient", layoutContext.getContext().getPatient());
    }

    /**
     * Creates the layout strategy.
     *
     * @param fixedPrice the fixed price editor
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(FixedPriceEditor fixedPrice) {
        return new VisitEstimateLayoutStrategy(fixedPrice);
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

    /**
     * A layout strategy that filters the patient node.
     */
    private class VisitEstimateLayoutStrategy extends EstimateItemLayoutStrategy {

        public VisitEstimateLayoutStrategy(FixedPriceEditor fixedPrice) {
            super(fixedPrice);
            getArchetypeNodes().exclude(PATIENT);
        }
    }

}
