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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.edit.AbstractRemoveConfirmationHandler;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.RemoveConfirmationHandler;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.PriceActItemEditor;

import java.math.BigDecimal;

/**
 * Implementation if {@link RemoveConfirmationHandler} for {@link AbstractChargeItemRelationshipCollectionEditor}.
 * <p/>
 * If the charge item being removed has minimum quantities, this displays a confirmation. If not, it falls back
 * to the default remove confirmation.
 *
 * @author Tim Anderson
 */
public abstract class ChargeRemoveConfirmationHandler extends AbstractRemoveConfirmationHandler {

    /**
     * Confirms removal of an object from a collection.
     * <p/>
     * If approved, it performs the removal.
     *
     * @param object     the object to remove
     * @param collection the collection to remove the object from, if approved
     */
    @Override
    public void remove(final IMObject object, final IMObjectCollectionEditor collection) {
        AbstractChargeItemRelationshipCollectionEditor chargeCollection
                = (AbstractChargeItemRelationshipCollectionEditor) collection;
        PriceActEditContext editContext = chargeCollection.getEditContext();
        if (editContext.useMinimumQuantities()) {
            IMObjectEditor chargeEditor = chargeCollection.getEditor(object);
            if (chargeEditor instanceof PriceActItemEditor) {
                PriceActItemEditor editor = (PriceActItemEditor) chargeEditor;
                BigDecimal quantity = editor.getMinimumQuantity();
                if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                    removeMinimumQuantity(object, collection, editor, quantity,
                                          editContext.overrideMinimumQuantities());
                } else {
                    super.remove(object, collection);
                }
            } else {
                super.remove(object, collection);
            }
        } else {
            super.remove(object, collection);
        }
    }

    /**
     * Returns the display name for an object, used for display.
     *
     * @param object     the object
     * @param collection the collection the object is in
     * @return the display name
     */
    @Override
    protected String getDisplayName(IMObject object, IMObjectCollectionEditor collection) {
        String result;
        AbstractChargeItemRelationshipCollectionEditor chargeCollection
                = (AbstractChargeItemRelationshipCollectionEditor) collection;
        IMObjectEditor chargeEditor = chargeCollection.getEditor(object);
        if (chargeEditor instanceof PriceActItemEditor) {
            result = getDisplayName((PriceActItemEditor) chargeEditor);
        } else {
            result = super.getDisplayName(object, collection);
        }
        return result;
    }

    /**
     * Invoked when an object with a non-zero minimum quantity is being removed.
     * <p/>
     * This displays a confirmation dialog if there are no restrictions on removal, or the user can override
     * minimum quantities.
     * <p/>
     * If there are restrictions on removal, and the user can't override them, an error will be displayed.
     *
     * @param object     the object to remove
     * @param collection the collection to remove the object from, if confirmed
     * @param editor     the object editor
     * @param quantity   the minimum quantity
     * @param override   determines if the user can override minimum quantities
     */
    protected void removeMinimumQuantity(final IMObject object, final IMObjectCollectionEditor collection,
                                         PriceActItemEditor editor, BigDecimal quantity,
                                         boolean override) {
        String name = getDisplayName(editor);
        if (override) {
            ConfirmationDialog.show(
                    Messages.format("customer.charge.minquantity.delete.title", name),
                    Messages.format("customer.charge.minquantity.delete.message", name, quantity),
                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            collection.remove(object);
                        }
                    });
        } else {
            ErrorDialog.show(Messages.format("customer.charge.minquantity.deleteforbidden.title", name),
                             Messages.format("customer.charge.minquantity.deleteforbidden.message", name, quantity));
        }
    }

    /**
     * Returns the display name for an object.
     *
     * @param editor the object editor
     * @return a display name for the object
     */
    private String getDisplayName(PriceActItemEditor editor) {
        String result;
        Product product = editor.getProduct();
        if (product != null) {
            result = product.getName();
        } else {
            result = DescriptorHelper.getDisplayName(editor.getObject());
        }
        return result;
    }

}
