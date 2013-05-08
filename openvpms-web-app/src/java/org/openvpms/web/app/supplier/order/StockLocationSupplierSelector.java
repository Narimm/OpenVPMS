/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.AbstractArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import java.util.List;

/**
 * Stock Location and supplier selector.
 *
 * @author Tim Anderson
 */
public class StockLocationSupplierSelector {

    /**
     * The available stock locations.
     */
    private List<IMObject> stockLocations;

    /**
     * The available suppliers.
     */
    private final List<IMObject> suppliers;

    /**
     * The stock locations.
     */
    private SelectField stockLocation;

    /**
     * The suppliers.
     */
    private SelectField supplier;

    /**
     * The component.
     */
    private Component component;

    /**
     * The focus group.
     */
    private FocusGroup group;


    /**
     * Creates a new {@code StockLocationSupplierSelector}.
     *
     * @param context the context
     */
    public StockLocationSupplierSelector(Context context) {
        stockLocations = query(StockArchetypes.STOCK_LOCATION);
        stockLocation = SelectFieldFactory.create(new IMObjectListModel(stockLocations, true, false),
                                                  context.getStockLocation());
        stockLocation.setCellRenderer(IMObjectListCellRenderer.NAME);

        suppliers = query("party.supplier*");
        supplier = SelectFieldFactory.create(new IMObjectListModel(suppliers, true, false));
        supplier.setCellRenderer(IMObjectListCellRenderer.NAME);

        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("product.stockLocation"));
        grid.add(stockLocation);
        grid.add(LabelFactory.create("supplier.type"));
        grid.add(supplier);
        component = ColumnFactory.create("Inset", grid);
        group = new FocusGroup("StockLocationSupplierSelector");
        group.add(0, stockLocation);
        group.add(1, supplier);
        group.setFocus();
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Returns the available stock locations.
     *
     * @return the available stock locations
     */
    public List<IMObject> getStockLocations() {
        return stockLocations;
    }

    /**
     * Returns the available suppliers.
     *
     * @return the available suppliers
     */
    public List<IMObject> getSuppliers() {
        return suppliers;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location, or {@code null} if none is selected
     */
    public Party getStockLocation() {
        return (Party) stockLocation.getSelectedItem();
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier, or {@code null} if none is selected
     */
    public Party getSupplier() {
        return (Party) supplier.getSelectedItem();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }

    /**
     * Queries the specified archetype short name.
     *
     * @param shortName the archetype short name
     * @return the objects matching the query
     */
    private List<IMObject> query(String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName)
            .add(Constraints.sort("name"))
            .add(Constraints.sort("id"))
            .setMaxResults(AbstractArchetypeQuery.ALL_RESULTS);
        return QueryHelper.query(query);
    }

}
