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
package org.openvpms.web.workspace.patient.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;


/**
 * An editor for <em>act.customerAccountInvoiceItem</em> acts, in the context of a patient visit.
 *
 * @author Tim Anderson
 */
public class VisitChargeItemEditor extends CustomerChargeActItemEditor {

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
        return new VisitChargeItemLayoutStrategy(fixedPrice);
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
         * The nodes to display.
         */
        private ArchetypeNodes nodes;


        /**
         * Constructs a {@link VisitChargeItemLayoutStrategy}.
         *
         * @param fixedPrice the fixed price editor
         */
        public VisitChargeItemLayoutStrategy(FixedPriceEditor fixedPrice) {
            super(fixedPrice);
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
            nodes = new ArchetypeNodes(super.getArchetypeNodes()).exclude("patient");
            return super.apply(object, properties, parent, context);
        }

        /**
         * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
         *
         * @return the archetype nodes
         */
        @Override
        protected ArchetypeNodes getArchetypeNodes() {
            return nodes;
        }
    }
}
