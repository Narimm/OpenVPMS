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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.otc;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.payment.AbstractCustomerPaymentEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.util.List;

/**
 * Editor for over-the-counter payments.
 * <p/>
 * This ensures that the payment total is the same as the charge.
 * <p/>
 * It also suppresses changing of the act status, in case the workflow is cancelled and the act needs to be deleted.
 *
 * @author Tim Anderson
 */
class OTCPaymentEditor extends AbstractCustomerPaymentEditor {

    /**
     * Constructs an {@link OTCPaymentEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     * @param charge  the charge amount
     */
    public OTCPaymentEditor(Act act, IMObject parent, LayoutContext context, BigDecimal charge) {
        super(act, parent, context);
        setInvoiceAmount(charge);
        setExpectedAmount(charge);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    private class LayoutStrategy extends ActLayoutStrategy {

        /**
         * Constructs a {@link LayoutStrategy}.
         */
        public LayoutStrategy() {
            super(getItems());
        }

        /**
         * Lays out child components in a grid.
         *
         * @param object     the object to lay out
         * @param parent     the parent object. May be {@code null}
         * @param properties the properties
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                      Component container, LayoutContext context) {
            ComponentSet set = createComponentSet(object, properties, context);
            ComponentGrid grid = new ComponentGrid();
            grid.add(createComponent(getInvoiceAmountProperty(), object, context));
            grid.add(set);
            doGridLayout(grid, container);
        }

        /**
         * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
         *
         * @return the archetype nodes
         */
        protected ArchetypeNodes getArchetypeNodes() {
            // suppresses the status node
            return OTCChargeEditor.NODES;
        }
    }
}
