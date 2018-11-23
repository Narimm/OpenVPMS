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

package org.openvpms.web.workspace.customer.charge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.List;

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ChargeRemoveConfirmationHandler.class);

    /**
     * Confirms removal of an object from a collection.
     * <p/>
     * If approved, it performs the removal.
     *
     * @param object     the object to remove
     * @param collection the collection to remove the object from, if approved
     */
    @Override
    public void remove(IMObject object, IMObjectCollectionEditor collection) {
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
     * Removes a collection of items.
     *
     * @param objects    the objects to remove
     * @param collection the collection the objects belong to
     */
    public void remove(List<IMObject> objects, AbstractChargeItemRelationshipCollectionEditor collection) {
        if (objects.size() == 1) {
            remove(objects.get(0), collection);
        } else if (objects.size() > 1) {
            confirmRemove(objects, collection);
        }
    }

    /**
     * Displays a confirmation dialog to confirm removal of multiple objects from a collection.
     * <p>
     * If approved, it performs the removal.
     *
     * @param objects    the objects to remove
     * @param collection the collection to remove the objects from, if approved
     */
    protected void confirmRemove(List<IMObject> objects, AbstractChargeItemRelationshipCollectionEditor collection) {
        String displayName = collection.getProperty().getDisplayName();
        String title = Messages.format("imobject.collection.deletes.title", displayName);
        String message = Messages.format("imobject.collection.deletes.message", objects.size(), displayName);
        ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onYes() {
                removeAll(objects, collection);
            }

            @Override
            public void onNo() {
                cancelRemove(collection);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when removal is cancelled.
     * <p/>
     * This unmarks all marked objects,
     *
     * @param collection the collection
     */
    @Override
    protected void cancelRemove(IMObjectCollectionEditor collection) {
        ((AbstractChargeItemRelationshipCollectionEditor) collection).unmarkAll();
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
    protected void removeMinimumQuantity(IMObject object, IMObjectCollectionEditor collection,
                                         PriceActItemEditor editor, BigDecimal quantity, boolean override) {
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
     * Removes a collection of items.
     *
     * @param objects    the objects to remove
     * @param collection the collection the objects belong to
     */
    private void removeAll(List<IMObject> objects, AbstractChargeItemRelationshipCollectionEditor collection) {
        PriceActEditContext editContext = collection.getEditContext();
        boolean useMinimumQuantities = editContext.useMinimumQuantities();
        boolean override = editContext.overrideMinimumQuantities();
        for (IMObject object : objects) {
            boolean remove = true;
            if (useMinimumQuantities && !override) {
                // Sanity check to make sure the user can delete objects. Objects shouldn't be selectable for deletion
                // in this case.
                IMObjectEditor chargeEditor = collection.getEditor(object);
                if (chargeEditor instanceof PriceActItemEditor) {
                    PriceActItemEditor editor = (PriceActItemEditor) chargeEditor;
                    BigDecimal quantity = editor.getMinimumQuantity();
                    if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                        log.error("Ignoring attempt to remove object=" + object.getId() + " with minimum quantity="
                                  + quantity);
                        remove = false;
                    }
                }
            }
            if (remove) {
                apply(object, collection);
            }
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
