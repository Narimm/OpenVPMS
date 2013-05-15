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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.workspace.BrowserCRUDWorkspace;
import org.openvpms.web.component.workspace.CRUDWindow;


/**
 * Stock management workspace.
 *
 * @author Tim Anderson
 */
public class StockWorkspace extends BrowserCRUDWorkspace<Party, Act> {

    /**
     * Constructs a {@code StockWorkspace}.
     *
     * @param context the context
     */
    public StockWorkspace(Context context) {
        super("product", "stock", context, false);
        setArchetypes(Party.class, StockArchetypes.STOCK_LOCATION);
        setChildArchetypes(Act.class, StockArchetypes.STOCK_TRANSFER,
                           StockArchetypes.STOCK_ADJUST);
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Act> createQuery() {
        return new StockQuery(getChildArchetypes().getShortNames(), getContext(), getHelpContext());
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new StockCRUDWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Determines if the parent object is optional (i.e may be {@code null},
     * when laying out the workspace.
     *
     * @return {@code true}
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }

}
