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

package org.openvpms.web.workspace.supplier.order;

import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.supplier.SupplierStockItemEditor;

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
     * Quantity node name.
     */
    private static final String QUANTITY = "quantity";

    /**
     * Received quantity node name.
     */
    private static final String RECEIVED_QUANTITY = "receivedQuantity";

    /**
     * Cancelled quantity node name.
     */
    private static final String CANCELLED_QUANTITY = "cancelledQuantity";


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
     * Returns the received quantity.
     *
     * @return the received quantity
     */
    public BigDecimal getReceivedQuantity() {
        return (BigDecimal) getProperty(RECEIVED_QUANTITY).getValue();
    }

    /**
     * Sets the received quantity.
     *
     * @param quantity the received quantity
     */
    public void setReceivedQuantity(BigDecimal quantity) {
        getProperty(RECEIVED_QUANTITY).setValue(quantity);
    }

    /**
     * Returns the cancelled quantity.
     *
     * @return the cancelled quantity
     */
    public BigDecimal getCancelledQuantity() {
        return (BigDecimal) getProperty(CANCELLED_QUANTITY).getValue();
    }

    /**
     * Sets the cancelled quantity.
     *
     * @param quantity the cancelled quantity
     */
    public void setCancelledQuantity(BigDecimal quantity) {
        getProperty(CANCELLED_QUANTITY).setValue(quantity);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = super.doValidation(validator);
        if (valid) {
            BigDecimal quantity = getQuantity();
            BigDecimal cancelled = getCancelledQuantity();
            if (cancelled.compareTo(quantity) > 0) {
                valid = false;
                Property property = getProperty(QUANTITY);
                String message = Messages.get("supplier.order.invalidCancelledQuantity", quantity, cancelled);
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
                    if (!name.equals("status") && !name.equals(CANCELLED_QUANTITY)) {
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

}
