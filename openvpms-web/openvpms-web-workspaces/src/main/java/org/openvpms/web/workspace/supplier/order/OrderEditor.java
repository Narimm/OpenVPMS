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

package org.openvpms.web.workspace.supplier.order;

import nextapp.echo2.app.text.TextComponent;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
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
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
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
        initialise();
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the total matches that of the sum of the item totals.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return checkRestricted(validator) && super.doValidation(validator);
    }

    /**
     * Ensures that if when the order is being POSTED, and contains restricted products, and finalisation is restricted
     * to clinicians,
     *
     * @param validator the validator
     * @return {@code true} if the order is valid
     */
    protected boolean checkRestricted(Validator validator) {
        boolean valid = true;
        if (!posted && OrderStatus.POSTED.equals(getStatus())) {
            Context context = getLayoutContext().getContext();
            Party practice = context.getPractice();
            User user = context.getUser();
            if (practice != null) {
                ProductRules productRules = ServiceHelper.getBean(ProductRules.class);
                PracticeRules practiceRules = ServiceHelper.getBean(PracticeRules.class);
                UserRules userRules = ServiceHelper.getBean(UserRules.class);
                if (!userRules.isClinician(user) && practiceRules.isOrderingRestricted(practice)) {
                    for (IMObjectEditor editor : getItems().getEditors()) {
                        OrderItemEditor itemEditor = (OrderItemEditor) editor;
                        Product product = itemEditor.getProduct();
                        if (product != null && productRules.isRestricted(product)) {
                            validator.add(this, new ValidatorError(Messages.get("supplier.order.restricted.message")));
                            valid = false;
                            break;
                        }
                    }
                }
            }
        }
        return valid;
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
     * Creates a collection editor for the items collection.
     *
     * @param act   the act
     * @param items the items collection
     * @return a new collection editor
     */
    @Override
    protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
        return super.createItemsEditor(act, items);
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
            deliveryStatusField.setText(LookupNameHelper.getName(getObject(), deliveryStatus.getName()));
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
