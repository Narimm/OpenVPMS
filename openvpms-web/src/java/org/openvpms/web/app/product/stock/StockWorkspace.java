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
 *
 *  $Id$
 */

package org.openvpms.web.app.product.stock;

import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.component.subsystem.CRUDWindow;


/**
 * Stock management workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-05-07 06:50:24Z $
 */
public class StockWorkspace extends BrowserCRUDWorkspace<Party, Act> {

    /**
     * Constructs a new <tt>StockWorkspace</tt>.
     */
    public StockWorkspace() {
        super("product", "stock", false);
        setArchetypes(Party.class, StockArchetypes.STOCK_LOCATION);
        setChildArchetypes(Act.class, StockArchetypes.STOCK_TRANSFER,
                           StockArchetypes.STOCK_ADJUST);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new StockCRUDWindow(getChildArchetypes());
    }

    /**
     * Determines if the parent object is optional (i.e may be <tt>null</tt>,
     * when laying out the workspace.
     *
     * @return <tt>true</tt>
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }

}
