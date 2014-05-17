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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.order;

import nextapp.echo2.app.text.TextComponent;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.EditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.act.FinancialActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupFilter;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.workspace.supplier.SupplierHelper;

import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of <em>act.supplierOrder</em>.
 *
 * @author Tim Anderson
 */
public class OrderEditor extends FinancialActEditor {

    /**
     * Determines if the act was POSTED at construction. If so, only a limited
     * set of properties may be edited.
     */
    private final boolean posted;

    /**
     * Determines if the act was ACCEPTED at construction. If so, only a limited
     * set of properties may be edited.
     */
    private final boolean accepted;

    /**
     * Order business rules.
     */
    private final OrderRules rules;

    /**
     * Delivery status field.
     */
    private TextComponent deliveryStatusField;


    /**
     * Constructs an {@code OrderEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public OrderEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.supplierOrder")) {
            throw new IllegalArgumentException(
                    "Invalid act type: " + act.getArchetypeId().getShortName());
        }
        posted = OrderStatus.POSTED.equals(act.getStatus());
        accepted = OrderStatus.ACCEPTED.equals(act.getStatus());
        rules = SupplierHelper.createOrderRules(context.getContext().getPractice());
    }

    /**
     * Updates the amount, tax and delivery status when an act item changes
     */
    protected void onItemsChanged() {
        super.onItemsChanged();
        List<Act> acts = getItems().getCurrentActs();
        checkDeliveryStatus(acts);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy(getItems());
    }

    /**
     * Checks if the delivery status needs to be updated.
     *
     * @param acts the current order item acts
     */
    private void checkDeliveryStatus(List<Act> acts) {
        Property deliveryStatus = getProperty("deliveryStatus");
        DeliveryStatus current = DeliveryStatus.valueOf((String) deliveryStatus.getValue());
        DeliveryStatus newStatus = null;
        for (Act act : acts) {
            FinancialAct item = (FinancialAct) act;
            DeliveryStatus status = rules.getDeliveryStatus(item);
            if (newStatus == null) {
                newStatus = status;
            } else if (status == DeliveryStatus.PART) {
                newStatus = status;
            } else if (status == DeliveryStatus.PENDING && newStatus != DeliveryStatus.PART) {
                newStatus = status;
            }
        }
        if (newStatus != null && newStatus != current) {
            deliveryStatus.setValue(newStatus.toString());
            NodeDescriptor descriptor = deliveryStatus.getDescriptor();
            if (descriptor != null) {
                deliveryStatusField.setText(LookupNameHelper.getLookupName(
                        descriptor, getObject()));
            }
        }
    }

    private class LayoutStrategy extends ActLayoutStrategy {

        /**
         * Creates a new {@code NonPostedLayoutStrategy}.
         *
         * @param editor the act items editor
         */
        public LayoutStrategy(EditableIMObjectCollectionEditor editor) {
            super(editor);
            if (posted || accepted) {
                editor.setCardinalityReadOnly(true);
            }
        }

        @Override
        protected ComponentState createComponent(Property property,
                                                 IMObject parent,
                                                 LayoutContext context) {
            ComponentState state;
            if (property.getName().equals("deliveryStatus")) {
                property = createReadOnly(property);
                state = super.createComponent(property, parent, context);
                deliveryStatusField = (TextComponent) state.getComponent();
            } else if (posted || accepted) {
                if (property.getName().equals("status")) {
                    LookupQuery query = new NodeLookupQuery(parent, property);
                    if (posted) {
                        query = new LookupFilter(query, true, OrderStatus.POSTED, OrderStatus.CANCELLED);
                    } else {
                        query = new LookupFilter(query, true, OrderStatus.ACCEPTED, OrderStatus.CANCELLED);
                    }
                    LookupField field = LookupFieldFactory.create(property, query);
                    state = new ComponentState(field, property);
                } else {
                    // all other properties are read-only
                    property = createReadOnly(property);
                    state = super.createComponent(property, parent, context);
                }
            } else {
                state = super.createComponent(property, parent, context);
            }
            return state;
        }

    }

}
