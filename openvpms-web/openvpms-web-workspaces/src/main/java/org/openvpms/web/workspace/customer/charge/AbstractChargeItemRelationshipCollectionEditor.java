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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.ProductTemplateExpander;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.workspace.customer.DoseManager;
import org.openvpms.web.workspace.customer.PriceActItemEditor;

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
        setRemoveConfirmationHandler(DefaultChargeRemoveConfirmationHandler.INSTANCE);
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
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        ChargeItemTableModel model = new ChargeItemTableModel(getCollectionPropertyEditor().getArchetypeRange(),
                                                              context);
        return (IMTableModel) model;
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

        final CheckBox template = CheckBoxFactory.create("customer.charge.show.template", showTemplate);
        final CheckBox productType = CheckBoxFactory.create("customer.charge.show.productType", showProductType);
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
     * Creates a new product template expander.
     * <p/>
     * This implementation will restrict products to those of the location and stock location,
     * if {@link PriceActEditContext#useLocationProducts} is {@code true}.
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
     * Returns the underlying table model.
     *
     * @return the model
     */
    private ChargeItemTableModel getModel() {
        IMTableModel model = getTable().getModel().getModel();
        return (ChargeItemTableModel) model;
    }
}
