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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.UserPreferences;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DelegatingIMTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.act.ActRelationshipCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;


/**
 * Viewer for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sorts the items on descending start time.
 *
 * @author Tim Anderson
 */
public class ChargeItemRelationshipCollectionViewer extends ActRelationshipCollectionViewer {

    /**
     * Constructs a {@link ChargeItemRelationshipCollectionViewer}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionViewer(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Lays out the component.
     *
     * @return a new component
     */
    @Override
    protected Component doLayout() {
        Component component = super.doLayout();
        component.add(createControls(), 0);
        return component;
    }

    /**
     * Create controls to show/hide columns.
     *
     * @return a row of controls
     */
    protected Row createControls() {
        Row row = RowFactory.create(Styles.CELL_SPACING);

        // TODO - this largely duplicates code from AbstractChargeItemRelationshipCollectionEditor
        final UserPreferences preferences = ServiceHelper.getPreferences();
        boolean showBatch = preferences.getShowBatchDuringCharging();
        boolean showTemplate = preferences.getShowTemplateDuringCharging();
        boolean showProductType = preferences.getShowProductTypeDuringCharging();

        ChargeItemTableModel model = getModel();
        if (model != null) {
            if (model.hasBatch()) {
                final CheckBox batch = CheckBoxFactory.create("customer.charge.show.batch", showBatch);
                batch.addActionListener(new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        boolean selected = batch.isSelected();
                        preferences.setShowBatchDuringCharging(selected);
                        getModel().setShowBatch(preferences.getShowBatchDuringCharging());
                    }
                });
                row.add(batch);
            }

            final CheckBox template = CheckBoxFactory.create("customer.charge.show.template", showTemplate);
            final CheckBox productType = CheckBoxFactory.create("customer.charge.show.productType", showProductType);
            template.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    boolean selected = template.isSelected();
                    preferences.setShowTemplateDuringCharging(selected);
                    getModel().setShowTemplate(selected);
                }
            });
            productType.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    boolean selected = productType.isSelected();
                    preferences.setShowProductTypeDuringCharging(selected);
                    getModel().setShowProductType(selected);
                }
            });
            row.add(template);
            row.add(productType);
        }
        return row;
    }

    /**
     * Returns the underlying table model.
     *
     * @return the underlying table model, or {@code null} if it is not of the expected type
     */
    private ChargeItemTableModel getModel() {
        IMTableModel result = null;
        IMTableModel relationshipModel = getTable().getModel().getModel();
        if (relationshipModel instanceof DelegatingIMTableModel) {
            result = ((DelegatingIMTableModel) relationshipModel).getModel();
        }
        return (result instanceof ChargeItemTableModel) ? (ChargeItemTableModel) result : null;
    }

}
