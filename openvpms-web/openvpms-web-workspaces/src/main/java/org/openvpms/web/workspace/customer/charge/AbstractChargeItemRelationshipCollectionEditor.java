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

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.edit.DefaultCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.DefaultRemoveConfirmationHandler;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.ProductTemplateExpander;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.ListMarkModel;
import org.openvpms.web.component.im.table.MarkablePagedIMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.workspace.customer.DoseManager;
import org.openvpms.web.workspace.customer.PriceActItemEditor;

import java.util.List;

/**
 * Editor for collections of {@link ActRelationship}s belonging to charges and estimates.
 * <p/>
 * This provides an {@link DoseManager} to {@link PriceActItemEditor} instances.
 *
 * @author Tim Anderson
 */
public abstract class AbstractChargeItemRelationshipCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * The edit context.
     */
    private final PriceActEditContext editContext;

    /**
     * Constructs an {@link AbstractChargeItemRelationshipCollectionEditor}
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public AbstractChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context,
                                                          PriceActEditContext editContext) {
        this(property, act, context, DefaultCollectionResultSetFactory.INSTANCE, editContext);
    }

    /**
     * Constructs an {@link AbstractChargeItemRelationshipCollectionEditor}
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     * @param factory  the result set factory
     */
    public AbstractChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act,
                                                          LayoutContext context, CollectionResultSetFactory factory,
                                                          PriceActEditContext editContext) {
        super(property, act, context, factory);
        this.editContext = editContext;
        setRemoveConfirmationHandler(new DefaultChargeRemoveConfirmationHandler(context.getContext(),
                                                                                context.getHelpContext()));
    }

    /**
     * Unmarks all charge items.
     */
    public void unmarkAll() {
        MarkablePagedIMObjectTableModel<IMObject> model
                = (MarkablePagedIMObjectTableModel<IMObject>) getTable().getModel();
        model.unmarkAll();
    }

    /**
     * Returns the edit context.
     *
     * @return the edit context
     */
    protected PriceActEditContext getEditContext() {
        return editContext;
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<IMObject> createTable(IMTableModel<IMObject> model) {
        return new PagedIMTable<>(new MarkablePagedIMObjectTableModel<>((IMObjectTableModel<IMObject>) model));
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        TableComponentFactory factory = new TableComponentFactory(context);
        factory.setTruncateLongText(true);
        context.setComponentFactory(factory);
        ChargeItemTableModel model = createChargeItemTableModel(context);
        model.getRowMarkModel().addListener(new ListMarkModel.Listener() {
            @Override
            public void changed(int index, boolean marked) {
                enableDelete();
            }

            @Override
            public void cleared() {
                enableDelete();
            }
        });
        return (IMTableModel) model;
    }

    /**
     * Creates a new {@link ChargeItemTableModel}.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected ChargeItemTableModel createChargeItemTableModel(LayoutContext context) {
        PriceActEditContext editContext = getEditContext();
        return new ChargeItemTableModel(getCollectionPropertyEditor().getArchetypeRange(), null,
                                        editContext.useMinimumQuantities(), editContext.overrideMinimumQuantities(),
                                        context);
    }

    /**
     * Creates the row of controls.
     * <p/>
     * This provides checkboxes to show/hide the template, product type and batch columns.
     *
     * @param focus the focus group
     * @return the row of controls
     */
    @Override
    protected ButtonRow createControls(FocusGroup focus) {
        ButtonRow controls = super.createControls(focus);
        final Preferences preferences = getContext().getPreferences();
        boolean showBatch = preferences.getBoolean(PreferenceArchetypes.CHARGE, "showBatch", false);
        boolean showTemplate = preferences.getBoolean(PreferenceArchetypes.CHARGE, "showTemplate", false);
        boolean showProductType = preferences.getBoolean(PreferenceArchetypes.CHARGE, "showProductType", false);

        ChargeItemTableModel model = getModel();
        if (model.hasBatch()) {
            final CheckBox batch = CheckBoxFactory.create("customer.charge.show.batch", showBatch);
            batch.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    boolean selected = batch.isSelected();
                    preferences.setPreference(PreferenceArchetypes.CHARGE, "showBatch", selected);
                    getModel().setShowBatch(preferences.getBoolean(PreferenceArchetypes.CHARGE, "showBatch", false));
                }
            });
            controls.add(batch);
        }

        CheckBox template = CheckBoxFactory.create("customer.charge.show.template", showTemplate);
        CheckBox productType = CheckBoxFactory.create("customer.charge.show.productType", showProductType);
        template.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                boolean selected = template.isSelected();
                preferences.setPreference(PreferenceArchetypes.CHARGE, "showTemplate", selected);
                getModel().setShowTemplate(selected);
            }
        });
        productType.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                boolean selected = productType.isSelected();
                preferences.setPreference(PreferenceArchetypes.CHARGE, "showProductType", selected);
                getModel().setShowProductType(selected);
            }
        });
        controls.add(template);
        controls.add(productType);
        return controls;
    }

    /**
     * Determines if the delete button should be enabled.
     *
     * @return {@code true} if the delete button should be enabled, {@code false} if it should be disabled
     */
    @Override
    protected boolean getEnableDelete() {
        return super.getEnableDelete() || !getModel().getRowMarkModel().isEmpty();
    }

    /**
     * Creates a new product template expander.
     * <p/>
     * This implementation will restrict products to those of the location and stock location,
     * if {@link PriceActEditContext#useLocationProducts()} is {@code true}.
     *
     * @return a new product template expander
     */
    @Override
    protected ProductTemplateExpander getProductTemplateExpander() {
        PriceActEditContext context = getEditContext();
        return new ProductTemplateExpander(context.useLocationProducts(), context.getLocation(),
                                           context.getStockLocation());
    }

    /**
     * Invoked when the 'delete' button is pressed.
     * If the selected object has been saved, delegates to the registered
     * {@link #getRemoveConfirmationHandler() RemoveConfirmationHandler},
     * or uses {@link DefaultRemoveConfirmationHandler} if none is registered.
     */
    protected void onDelete() {
        MarkablePagedIMObjectTableModel<IMObject> model
                = (MarkablePagedIMObjectTableModel<IMObject>) getTable().getModel();
        List<IMObject> items = model.getMarked(getCurrentObjects());
        if (!items.isEmpty()) {
            ChargeRemoveConfirmationHandler handler = (ChargeRemoveConfirmationHandler) getRemoveConfirmationHandler();
            handler.remove(items, this);
        } else {
            super.onDelete();
        }
    }

    /**
     * Returns the underlying table model.
     *
     * @return the model
     */
    @SuppressWarnings("unchecked")
    private ChargeItemTableModel<Act> getModel() {
        IMTableModel model = getTable().getModel().getModel();
        return (ChargeItemTableModel<Act>) model;
    }
}
