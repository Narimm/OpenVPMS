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

import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.List;

/**
 * An editor for {@link ProductPrice} collections.
 * <p/>
 * This enables prices to be filtered by pricing location
 *
 * @author Tim Anderson
 */
public class ProductPriceCollectionEditor extends IMObjectTableCollectionEditor {

    /**
     * The pricing location filter.
     */
    private PricingLocationFilter filter;

    /**
     * Constructs a {@link ProductPriceCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public ProductPriceCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(property, object, context);
        filter = new PricingLocationFilter(context);
    }

    /**
     * Creates the row of controls.
     *
     * @param focus the focus group
     * @return the row of controls
     */
    @Override
    protected Row createControls(FocusGroup focus) {
        Row row = super.createControls(focus);
        if (filter.needsFilter()) {
            row.add(filter.getComponent());
            filter.setListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    ProductPriceTableModel model = (ProductPriceTableModel) getTable().getModel().getModel();
                    model.setShowPricingLocations(filter.showAll());
                    populateTable();
                }
            });
        }
        return row;
    }

    /**
     * Creates a new price, subject to a short name being selected.
     * <p/>
     * If there is a pricing location selected, this will be added to the price.
     *
     * @return a new price, or {@code null} if the price can't be created
     */
    @Override
    public IMObject create() {
        IMObject object = super.create();
        if (object != null && filter.getPricingLocation() != null) {
            IMObjectBean bean = new IMObjectBean(object);
            bean.addValue("pricingLocations", filter.getPricingLocation());
        }
        return object;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = filter.getPrices(editor.getObjects());
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
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        return new ProductPriceTableModel(getProperty().getArchetypeRange(), context);
    }
}
