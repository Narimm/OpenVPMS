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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.delivery;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.supplier.SupplierStockItemEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.util.Messages;


/**
 * An editor for <em>act.supplierDeliveryItem</em> and
 * <em>act.supplierReturnItem</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DeliveryItemEditor extends SupplierStockItemEditor {

    /**
     * The order relationship editor.
     */
    private ActRelationshipCollectionEditor orderEditor;


    /**
     * Construct a new <tt>DeliveryItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act.
     * @param context the layout context. May be <tt>null</tt>
     */
    public DeliveryItemEditor(FinancialAct act, Act parent,
                              LayoutContext context) {
        super(act, parent, context);
        CollectionProperty order = (CollectionProperty) getProperty("order");
        orderEditor = new ActRelationshipCollectionEditor(order, act,
                                                          getLayoutContext());
        getEditors().add(orderEditor);
    }

    /**
     * Associates an order item with this.
     *
     * @param order the order item
     */
    public void setOrderItem(FinancialAct order) {
        orderEditor.add(order);
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the product, packageSize and packageUnits are set when the parent is
     * POSTED.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean result = super.validate(validator);
        if (result) {
            Act parent = (Act) getParent();
            if (parent != null && ActStatus.POSTED.equals(parent.getStatus())) {
                result = validateProduct(validator);
                if (result) {
                    result = validatePackageSize(validator);
                    if (result) {
                        result = validatePackageUnits(validator);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Validates that the product is set when the parent is POSTED.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the property is valid, otherwise <tt>false</tt>
     */
    private boolean validateProduct(Validator validator) {
        boolean result = true;
        Property property = getProperty("product");
        if (getProductRef() == null) {
            String message = Messages.get("property.error.required", property.getDisplayName());
            validator.add(property, new ValidatorError(property, message));
            result = false;
        }
        return result;
    }

    /**
     * Validates the packageSize when the parent is POSTED.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the property is valid, otherwise <tt>false</tt>
     */
    private boolean validatePackageSize(Validator validator) {
        boolean result = true;
        Property property = getProperty("packageSize");
        Number size = (Number) property.getValue();
        if (size.intValue() <= 0) {
            String message = Messages.get("property.error.required", property.getDisplayName());
            validator.add(property, new ValidatorError(property, message));
            result = false;
        }
        return result;
    }

    /**
     * Validates the packageUnits node when the parent is POSTED.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the property is valid, otherwise <tt>false</tt>
     */
    private boolean validatePackageUnits(Validator validator) {
        boolean result = true;
        Property property = getProperty("packageUnits");
        String units = (String) property.getValue();
        if (units == null) {
            String message = Messages.get("property.error.required", property.getDisplayName());
            validator.add(property, new ValidatorError(property, message));
            result = false;
        }
        return result;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new ActLayoutStrategy("order", false);
    }
}
