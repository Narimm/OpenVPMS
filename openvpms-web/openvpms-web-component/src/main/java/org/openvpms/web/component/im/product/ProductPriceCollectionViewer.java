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

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.IMObjectTableCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.event.ActionListener;

import java.util.List;

/**
 * Product price collection viewer.
 *
 * @author Tim Anderson
 */
public class ProductPriceCollectionViewer extends IMObjectTableCollectionViewer {

    /**
     * The pricing location filter.
     */
    private PricingGroupFilter filter;

    /**
     * Constructs a {@link ProductPriceCollectionViewer}.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context
     */
    public ProductPriceCollectionViewer(CollectionProperty property, IMObject parent, LayoutContext layout) {
        super(property, parent, layout);
        filter = new PricingGroupFilter(layout);
    }

    /**
     * Lays out the component.
     *
     * @return a new component
     */
    @Override
    protected Component doLayout() {
        Component component = super.doLayout();
        if (filter.needsFilter()) {
            component.add(filter.getComponent(), 0);
            filter.setListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    ProductPriceTableModel model = (ProductPriceTableModel) getTable().getModel().getModel();
                    model.setShowPricingGroups(filter.showAll());
                    populateTable();
                }
            });
        }

        return component;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        List<IMObject> objects = filter.getPrices(getObjects());
        return new IMObjectListResultSet<IMObject>(objects, ROWS);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        return new ProductPriceTableModel(getProperty().getArchetypeRange(), context);
    }
}
