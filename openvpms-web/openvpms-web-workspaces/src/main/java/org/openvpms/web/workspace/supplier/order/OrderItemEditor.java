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

package org.openvpms.web.workspace.supplier.order;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.supplier.SupplierStockItemEditor;

import java.math.BigDecimal;

import static org.openvpms.web.echo.style.Styles.CELL_SPACING;
import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;


/**
 * An editor for {@link Act}s which have an archetype of <em>act.supplierOrderItem</em>.
 *
 * @author Tim Anderson
 */
public class OrderItemEditor extends SupplierStockItemEditor {

    /**
     * Determines if the act was POSTED or ACCEPTED at construction. If so, only a limited set of properties may be
     * edited.
     */
    private final boolean postedOrAccepted;

    /**
     * The stock location.
     */
    private final IMObjectReference stockLocation;

    /**
     * The edit context.
     */
    private final OrderEditContext editContext;

    /**
     * The stock on-hand property.
     */
    private final SimpleProperty onHand = new SimpleProperty("onHand", null, BigDecimal.class,
                                                             Messages.get("product.stock.onhand"), true);

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
     * Constructs an {@link OrderItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public OrderItemEditor(FinancialAct act, Act parent, LayoutContext context) {
        this(act, parent, new OrderEditContext(), context);
    }

    /**
     * Constructs an {@link OrderItemEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent act
     * @param context       the edit context
     * @param layoutContext the layout context
     */
    public OrderItemEditor(FinancialAct act, Act parent, OrderEditContext context, LayoutContext layoutContext) {
        super(act, parent, layoutContext);
        if (!TypeHelper.isA(act, SupplierArchetypes.ORDER_ITEM)) {
            throw new IllegalArgumentException("Invalid act type: " + act.getArchetypeId().getShortName());
        }
        if (parent != null) {
            String status = parent.getStatus();
            postedOrAccepted = OrderStatus.POSTED.equals(status) || OrderStatus.ACCEPTED.equals(status);

            ActBean bean = new ActBean(parent);
            stockLocation = bean.getNodeParticipantRef("stockLocation");
        } else {
            postedOrAccepted = false;
            stockLocation = null;
        }
        editContext = context;
        updateStockOnHand();
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
     * Invoked when the product is changed, to update prices.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);
        updateStockOnHand();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
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
                String message = Messages.format("supplier.order.invalidCancelledQuantity", quantity, cancelled);
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
        return new LayoutStrategy();
    }

    /**
     * Updates the stock on hand.
     */
    private void updateStockOnHand() {
        Reference product = getProductRef();
        if (product == null || stockLocation == null) {
            onHand.setValue(null);
        } else {
            BigDecimal stock = editContext.getStock().getStock(product, stockLocation);
            onHand.setValue(stock);
        }
    }

    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            ComponentState quantity = createComponent(properties.get(QUANTITY), object, context);
            ComponentState stockQuantity = createStockOnHand(context);
            Row onHand = RowFactory.create(CELL_SPACING, RowFactory.rightAlign(), stockQuantity.getLabel(),
                                           stockQuantity.getComponent());
            Row row = RowFactory.create(WIDE_CELL_SPACING, quantity.getComponent(), onHand);
            addComponent(new ComponentState(row, quantity.getProperty()));
            return super.apply(object, properties, parent, context);
        }

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display {@code property}
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
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

        /**
         * Creates a component for the stock on hand.
         *
         * @param context the layout context
         * @return a new component for the stock on hand
         */
        private ComponentState createStockOnHand(LayoutContext context) {
            Component component = context.getComponentFactory().create(onHand);
            return new ComponentState(component, onHand);
        }
    }
}