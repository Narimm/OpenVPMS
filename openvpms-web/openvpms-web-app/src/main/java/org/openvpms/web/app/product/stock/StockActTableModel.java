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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.product.stock;

import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Iterator;


/**
 * Table model for <em>act.stockAdjust</em> and <em>act.stockTransfer</em> acts.
 *
 * @author Tim Anderson
 */
public class StockActTableModel extends AbstractActTableModel {

    /**
     * The nodes to display.
     */
    private static final String[] NODE_NAMES = {"startTime", "stockLocation", "to", "status"};

    /**
     * The archetype short names.
     */
    private static final String[] SHORT_NAMES = {StockArchetypes.STOCK_ADJUST, StockArchetypes.STOCK_TRANSFER};

    /**
     * Creates a new {@code StockActTableModel}.
     *
     * @param context the layout context
     */
    public StockActTableModel(LayoutContext context) {
        super(SHORT_NAMES, context);
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return NODE_NAMES;
    }

    /**
     * Returns the index to insert the archetype column.
     *
     * @return the index to insert the archetype column
     */
    @Override
    protected int getArchetypeColumnIndex() {
        return 1;
    }

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
        TableColumnModel model = super.createColumnModel(shortNames, context);
        Iterator iter = model.getColumns();
        while (iter.hasNext()) {
            TableColumn column = (TableColumn) iter.next();
            if (column instanceof DescriptorTableColumn) {
                DescriptorTableColumn col = (DescriptorTableColumn) column;
                if (col.getName().equals("stockLocation")) {
                    col.setHeaderValue(
                        Messages.get("stockacttable.stockLocation"));
                } else if (col.getName().equals("to")) {
                    col.setHeaderValue(
                        Messages.get("stockacttable.toStockLocation"));
                }
            }
        }
        return model;
    }

}
