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

package org.openvpms.web.workspace.patient.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeEditContext;


/**
 * An editor for <em>act.customerAccountInvoiceItem</em> acts, in the context of a patient visit.
 *
 * @author Tim Anderson
 */
public class VisitChargeItemEditor extends CustomerChargeActItemEditor {

    /**
     * Constructs a {@link VisitChargeItemEditor}.
     * <p>
     * This recalculates the tax amount.
     *
     * @param act           the act to edit
     * @param parent        the parent act
     * @param context       the edit context
     * @param layoutContext the layout context
     */
    public VisitChargeItemEditor(FinancialAct act, Act parent, CustomerChargeEditContext context,
                                 LayoutContext layoutContext) {
        super(act, parent, context, layoutContext);
        initParticipant("patient", layoutContext.getContext().getPatient());
    }

    /**
     * Creates the layout strategy.
     *
     * @param fixedPrice         the fixed price editor
     * @param serviceRatioEditor the service ratio editor
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(FixedPriceEditor fixedPrice,
                                                          ServiceRatioEditor serviceRatioEditor) {
        return new VisitChargeItemLayoutStrategy(fixedPrice, serviceRatioEditor);
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
    private class VisitChargeItemLayoutStrategy extends CustomerChargeItemLayoutStrategy {

        /**
         * Constructs a {@link VisitChargeItemLayoutStrategy}.
         *
         * @param fixedPrice         the fixed price editor
         * @param serviceRatioEditor the service ratio editor
         */
        public VisitChargeItemLayoutStrategy(FixedPriceEditor fixedPrice, ServiceRatioEditor serviceRatioEditor) {
            super(fixedPrice, serviceRatioEditor);
        }

        /**
         * Apply the layout strategy.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            ArchetypeNodes nodes = new ArchetypeNodes(super.getArchetypeNodes()).exclude("patient");
            setArchetypeNodes(nodes);
            return super.apply(object, properties, parent, context);
        }

    }
}
