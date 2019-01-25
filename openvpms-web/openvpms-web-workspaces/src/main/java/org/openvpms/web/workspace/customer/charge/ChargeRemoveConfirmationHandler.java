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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.AbstractRemoveConfirmationHandler;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.RemoveConfirmationHandler;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.table.DefaultDescriptorTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.PriceActItemEditor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.openvpms.web.echo.style.Styles.CELL_SPACING;
import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;

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
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@link ChargeRemoveConfirmationHandler}.
     *
     * @param context the context
     * @param help    the help context
     */
    public ChargeRemoveConfirmationHandler(Context context, HelpContext help) {
        this.context = context;
        this.help = help;
    }

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
        PriceActEditContext editContext = collection.getEditContext();
        List<IMObject> minQuantities = new ArrayList<>();
        if (editContext.useMinimumQuantities()) {
            for (IMObject object : objects) {
                BigDecimal quantity = getMinQuantity(object);
                if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                    minQuantities.add(object);
                }
            }
        }
        if (!minQuantities.isEmpty()) {
            removeMinimumQuantities(objects, minQuantities, collection, editContext.overrideMinimumQuantities());
        } else {
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
                    Messages.format("imobject.delete.title", name),
                    Messages.format("customer.charge.minquantity.delete.message", name, quantity),
                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            collection.remove(object);
                        }

                        @Override
                        public void onNo() {
                            cancelRemove(collection);
                        }
                    });
        } else {
            ErrorDialog.show(Messages.format("customer.charge.minquantity.deleteforbidden.title", name),
                             Messages.format("customer.charge.minquantity.deleteforbidden.message", name, quantity));
        }
    }

    /**
     * Invoked when multiple objects with non-zero minimum quantity are being removed.
     * <p/>
     * This displays a confirmation dialog if there are no restrictions on removal, or the user can override
     * minimum quantities.
     * <p/>
     * If there are restrictions on removal, and the user can't override them, an error will be displayed.
     *
     * @param objects       all objects to remove
     * @param minQuantities the objects with minimum quantities
     * @param collection    the collection to remove the object from, if confirmed
     * @param override      determines if the user can override minimum quantities
     */
    protected void removeMinimumQuantities(List<IMObject> objects, List<IMObject> minQuantities,
                                           AbstractChargeItemRelationshipCollectionEditor collection,
                                           boolean override) {
        String name = collection.getProperty().getDisplayName();
        if (override) {
            LayoutContext layout = new DefaultLayoutContext(context, help);
            BatchDeleteConfirmationDialog dialog = new BatchDeleteConfirmationDialog(name, minQuantities, layout);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    if (dialog.deleteAll()) {
                        removeAll(objects, collection);
                    } else {
                        List<IMObject> remaining = new ArrayList<>(objects);
                        remaining.removeAll(minQuantities);
                        if (!remaining.isEmpty()) {
                            removeAll(remaining, collection);
                        }
                        cancelRemove(collection); // unmark those not deleted
                    }
                }

                @Override
                public void onCancel() {
                    cancelRemove(collection);
                }
            });
            dialog.show();
        } else {
            // this shouldn't be possible, as multiple deletion is disabled if a user doesn't have permission
            ErrorDialog.show(Messages.format("customer.charge.minquantity.deleteforbidden.title", name),
                             Messages.format("customer.charge.minquantity.deleteforbidden.message", name,
                                             getMinQuantity(minQuantities.get(0))));
        }
    }

    private BigDecimal getMinQuantity(IMObject object) {
        IMObjectBean bean = new IMObjectBean(object);
        return bean.getBigDecimal(PriceActItemEditor.MINIMUM_QUANTITY, BigDecimal.ZERO);
    }

    /**
     * Removes a collection of items.
     *
     * @param objects    the objects to remove
     * @param collection the collection the objects belong to
     */
    private void removeAll(List<IMObject> objects, AbstractChargeItemRelationshipCollectionEditor collection) {
        for (IMObject object : objects) {
            apply(object, collection);
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

    private class BatchDeleteConfirmationDialog extends ConfirmationDialog {

        /**
         * Collection name.
         */
        private final String name;

        /**
         * The objects with minimum quantities.
         */
        private final List<IMObject> objects;

        /**
         * The layout context.
         */
        private final LayoutContext context;

        /**
         * Determines if all objects should be deleted.
         */
        private boolean deleteAll;


        /**
         * Constructs a {@link BatchDeleteConfirmationDialog}.
         *
         * @param name    the collection name
         * @param objects the objects with minimum quantities
         * @param context the layout context
         */
        BatchDeleteConfirmationDialog(String name, List<IMObject> objects, LayoutContext context) {
            super(Messages.format("imobject.collection.deletes.title", name),
                  Messages.format("customer.charge.minquantity.batchdelete.message", name), OK_CANCEL);
            setStyleName("MediumWidthHeightDialog");
            this.name = name;
            this.objects = objects;
            this.context = context;
        }

        /**
         * Determines if all objects should be deleted.
         *
         * @return {@code true} if all objects should be deleted
         */
        public boolean deleteAll() {
            return deleteAll;
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label content = LabelFactory.create(true, true);
            content.setText(getMessage());
            DefaultDescriptorTableModel<IMObject> model = new DefaultDescriptorTableModel<>(
                    objects.get(0).getArchetype(), context, "date", "patient", "product", "minQuantity");
            PagedIMTable<IMObject> table = new PagedIMTable<>(model);
            table.setResultSet(new ListResultSet<>(objects, 7));
            // TODO need a better dialog style. This is to support 1024x768

            ButtonGroup group = new ButtonGroup();
            RadioButton deselect = ButtonFactory.create(null, group, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    setDeleteAll(false);
                }
            });
            deselect.setText(Messages.format("customer.charge.minquantity.batchdelete.skip", name));

            RadioButton delete = ButtonFactory.create(null, group, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    setDeleteAll(true);
                }
            });
            delete.setText(Messages.format("customer.charge.minquantity.batchdelete.deleteall", name));
            Label treatment = LabelFactory.create("customer.charge.minquantity.batchdelete.treatment");
            Column buttons = ColumnFactory.create(CELL_SPACING, treatment, deselect, delete);

            Column preamble = ColumnFactory.create(CELL_SPACING, content, table.getComponent());
            Column column = ColumnFactory.create(WIDE_CELL_SPACING, preamble, buttons);
            getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
            delete.setSelected(true); // delete all is the default
            setDeleteAll(true);
        }

        private void setDeleteAll(boolean deleteAll) {
            this.deleteAll = deleteAll;
        }

    }

}
