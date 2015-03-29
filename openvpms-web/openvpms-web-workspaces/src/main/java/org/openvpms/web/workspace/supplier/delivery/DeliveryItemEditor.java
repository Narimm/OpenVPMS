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

package org.openvpms.web.workspace.supplier.delivery;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.supplier.SupplierStockItemEditor;


/**
 * An editor for <em>act.supplierDeliveryItem</em> and
 * <em>act.supplierReturnItem</em> acts.
 *
 * @author Tim Anderson
 */
public class DeliveryItemEditor extends SupplierStockItemEditor {

    /**
     * The order relationship editor.
     */
    private ActRelationshipCollectionEditor orderEditor;

    /**
     * The parent act status.
     */
    private String parentStatus;

    /**
     * Determines if product prices should be updated.
     */
    private final boolean updateProductPrices;


    /**
     * Constructs a {@link DeliveryItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act.
     * @param context the layout context
     */
    public DeliveryItemEditor(FinancialAct act, Act parent, LayoutContext context) {
        super(act, parent, context);
        CollectionProperty order = (CollectionProperty) getProperty("order");
        orderEditor = new ActRelationshipCollectionEditor(order, act, getLayoutContext());
        getEditors().add(orderEditor);
        parentStatus = (parent != null) ? parent.getStatus() : null;
        Property invoiceLineId = getProperty("supplierInvoiceLineId");
        updateProductPrices = !(invoiceLineId != null && !StringUtils.isEmpty(invoiceLineId.getString()));
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
     * This implementation caches the result of the validation. If valid, subsequent invocations will return this
     * result, otherwise {@link #doValidation(Validator)} will be invoked.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    public boolean validate(Validator validator) {
        String currentStatus = ((Act) getParent()).getStatus();
        if (!ObjectUtils.equals(parentStatus, currentStatus)) {
            parentStatus = currentStatus;
            resetValid(false); // triggers doValidation()
        }
        return super.validate(validator);
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the product, packageSize and packageUnits are set when the parent is
     * POSTED.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = super.doValidation(validator);
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
     * Invoked when the product is changed, to update prices.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        if (updateProductPrices) {
            super.productModified(product);
        }
    }

    /**
     * Validates that the product is set when the parent is POSTED.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid, otherwise {@code false}
     */
    private boolean validateProduct(Validator validator) {
        boolean result = true;
        Property property = getProperty("product");
        if (getProductRef() == null) {
            String message = Messages.format("property.error.required", property.getDisplayName());
            validator.add(property, new ValidatorError(property, message));
            result = false;
        }
        return result;
    }

    /**
     * Validates the packageSize when the parent is POSTED.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid, otherwise {@code false}
     */
    private boolean validatePackageSize(Validator validator) {
        boolean result = true;
        Property property = getProperty("packageSize");
        Number size = (Number) property.getValue();
        if (size.intValue() <= 0) {
            String message = Messages.format("property.error.required", property.getDisplayName());
            validator.add(property, new ValidatorError(property, message));
            result = false;
        }
        return result;
    }

    /**
     * Validates the packageUnits node when the parent is POSTED.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid, otherwise {@code false}
     */
    private boolean validatePackageUnits(Validator validator) {
        boolean result = true;
        Property property = getProperty("packageUnits");
        String units = (String) property.getValue();
        if (units == null) {
            String message = Messages.format("property.error.required", property.getDisplayName());
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
        return new DeliveryItemLayoutStrategy();
    }

}
