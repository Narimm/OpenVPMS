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

package org.openvpms.web.workspace.product.stock;

import org.openvpms.archetype.rules.stock.io.StockData;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.TableBrowser;

/**
 * Stock export browser.
 *
 * @author Tim Anderson
 */
public class StockExportBrowser extends TableBrowser<StockData> {

    /**
     * Constructs a {@link StockExportBrowser}.
     *
     * @param context the layout context
     */
    public StockExportBrowser(LayoutContext context) {
        super(new StockExportQuery(context.getContext().getStockLocation()), null, new StockDataTableModel(), context);
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    @Override
    public StockExportQuery getQuery() {
        return (StockExportQuery) super.getQuery();
    }

}
