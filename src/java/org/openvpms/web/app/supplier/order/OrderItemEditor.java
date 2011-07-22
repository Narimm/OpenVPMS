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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.order;

import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.supplier.SupplierStockItemEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.supplierOrderItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class OrderItemEditor extends SupplierStockItemEditor {

    /**
     * Determines if the act was POSTED or ACCEPTED at construction. If so, only a limited
     * set of properties may be edited.
     */
    private final boolean postedOrAccepted;


    /**
     * Constructs an <tt>OrderItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public OrderItemEditor(FinancialAct act, Act parent,
                           LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.supplierOrderItem")) {
            throw new IllegalArgumentException(
                    "Invalid act type: " + act.getArchetypeId().getShortName());
        }
        if (parent != null) {
            String status = parent.getStatus();
            postedOrAccepted = OrderStatus.POSTED.equals(status) || OrderStatus.ACCEPTED.equals(status);
        } else {
            postedOrAccepted = false;
        }
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid = super.validate(validator);
        if (valid) {
            BigDecimal quantity = getQuantity();
            BigDecimal received = getReceivedQuantity();
            BigDecimal cancelled = getCancelledQuantity();
            BigDecimal sum = received.add(cancelled);
            if (sum.compareTo(quantity) > 0) {
                valid = false;
                Property property = getProperty("quantity");
                String message = Messages.get("supplier.order.invalidQuantity",
                                              quantity, sum);
                ValidatorError error = new ValidatorError(property, message);
                validator.add(property, error);
            }
        }
        return valid;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractLayoutStrategy() {

            /**
             * Creates a component for a property.
             *
             * @param property the property
             * @param parent   the parent object
             * @param context  the layout context
             * @return a component to display <tt>property</tt>
             */
            @Override
            protected ComponentState createComponent(Property property,
                                                     IMObject parent,
                                                     LayoutContext context) {
                if (postedOrAccepted) {
                    String name = property.getName();
                    if (!name.equals("status") && !name.equals("cancelledQuantity")) {
                        property = new DelegatingProperty(property) {
                            @Override
                            public boolean isReadOnly() {
                                return true;
                            }
                        };
                    }
                }
                return super.createComponent(property, parent, context);
            }
        };
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    private BigDecimal getQuantity() {
        return (BigDecimal) getProperty("quantity").getValue();
    }

    /**
     * Returns the received quantity.
     *
     * @return the received quantity
     */
    private BigDecimal getReceivedQuantity() {
        return (BigDecimal) getProperty("receivedQuantity").getValue();
    }

    /**
     * Returns the cancelled quantity.
     *
     * @return the cancelled quantity
     */
    private BigDecimal getCancelledQuantity() {
        return (BigDecimal) getProperty("cancelledQuantity").getValue();
    }

}
