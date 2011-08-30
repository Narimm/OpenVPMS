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

package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockDetailsSelector {

    /**
     * The supplier selector.
     */
    private IMObjectSelector<Party> supplier;

    /**
     * The stock location selector.
     */
    private IMObjectSelector<Party> location;

    /**
     * The component.
     */
    private Component component;

    /**
     * The focus group.
     */
    private FocusGroup group;


    /**
     * Creates a new <tt>StockDetailsSelector</tt>.
     *
     * @param context the context
     */
    public StockDetailsSelector(Context context) {
        supplier = new IMObjectSelector<Party>(
                Messages.get("supplier.type"), "party.supplier*");
        location = new StockLocationSelector(
                Messages.get("product.stockLocation"), context);

        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("supplier.type"));
        grid.add(supplier.getComponent());
        grid.add(LabelFactory.create("product.stockLocation"));
        grid.add(location.getComponent());

        component = grid;
        group = new FocusGroup("StockDetailsSelector");
        group.add(0, supplier.getFocusGroup());
        group.add(1, location.getFocusGroup());
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
     * Returns the supplier.
     *
     * @return the supplier, or <tt>null</tt> if none is selected
     */
    public Party getSupplier() {
        return supplier.getObject();
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location, or <tt>null</tt> if none is selected
     */
    public Party getStockLocation() {
        return location.getObject();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }

}
