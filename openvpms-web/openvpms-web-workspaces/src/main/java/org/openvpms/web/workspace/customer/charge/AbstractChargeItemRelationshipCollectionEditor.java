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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.UserPreferences;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.system.ServiceHelper;

/**
 * Editor for collections of {@link ActRelationship}s belonging to charges and estimates.
 *
 * @author Tim Anderson
 */
public abstract class AbstractChargeItemRelationshipCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * Constructs an {@link AbstractChargeItemRelationshipCollectionEditor}
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public AbstractChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Constructs an {@link AbstractChargeItemRelationshipCollectionEditor}
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     * @param factory  the result set factory
     */
    public AbstractChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context,
                                                          CollectionResultSetFactory factory) {
        super(property, act, context, factory);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        return new ChargeItemTableModel<>(getCollectionPropertyEditor().getArchetypeRange(), context);
    }

    /**
     * Creates the row of controls.
     * <p/>
     * This provides checkboxes to show/hide the template and product type columns.
     *
     * @param focus the focus group
     * @return the row of controls
     */
    @Override
    protected Row createControls(FocusGroup focus) {
        Row controls = super.createControls(focus);
        final UserPreferences preferences = ServiceHelper.getPreferences();
        boolean showTemplate = preferences.getShowTemplateDuringCharging();
        boolean showProductType = preferences.getShowProductTypeDuringCharging();
        final CheckBox template = CheckBoxFactory.create("customer.charge.show.template", showTemplate);
        final CheckBox productType = CheckBoxFactory.create("customer.charge.show.productType", showProductType);
        template.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                preferences.setShowTemplateDuringCharging(template.isSelected());
                IMTableModel<IMObject> model = getTable().getModel().getModel();
                if (model instanceof ChargeItemTableModel) {
                    ((ChargeItemTableModel) model).setShowTemplate(preferences.getShowTemplateDuringCharging());
                }
            }
        });
        productType.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                preferences.setShowProductTypeDuringCharging(productType.isSelected());
                IMTableModel<IMObject> model = getTable().getModel().getModel();
                if (model instanceof ChargeItemTableModel) {
                    ((ChargeItemTableModel) model).setShowProductType(preferences.getShowProductTypeDuringCharging());
                }
            }
        });
        controls.add(template);
        controls.add(productType);
        return controls;
    }

}
