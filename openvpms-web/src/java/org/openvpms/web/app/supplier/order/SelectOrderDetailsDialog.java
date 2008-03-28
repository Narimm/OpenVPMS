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

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Grid;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * A dialog that prompts for the supplier and stock location when placing
 * orders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SelectOrderDetailsDialog extends PopupDialog {

    /**
     * The supplier selector.
     */
    private IMObjectSelector<Party> supplier;

    /**
     * The stock location selector.
     */
    private IMObjectSelector<Party> location;


    /**
     * Creates a new <tt>SelectOrderDetailsDialog</tt>.
     *
     * @param title   the window title
     * @param context the context
     */
    public SelectOrderDetailsDialog(String title, Context context) {
        super(title, OK_CANCEL);
        setModal(true);

        supplier = new IMObjectSelector<Party>(
                Messages.get("supplier.order.type"),
                "party.supplier*");
        location = new StockLocationSelector(
                Messages.get("supplier.order.location"), context);

        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("supplier.order.type"));
        grid.add(supplier.getComponent());
        grid.add(LabelFactory.create("supplier.order.location"));
        grid.add(location.getComponent());
        getLayout().add(grid);

        FocusGroup group = getFocusGroup();
        group.add(0, supplier.getFocusGroup());
        group.add(1, location.getFocusGroup());
        group.setFocus();
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
     * Invoked when the 'OK' button is pressed.
     */
    @Override
    protected void onOK() {
        if (supplier.getObject() != null && location.getObject() != null) {
            super.onOK();
        }
    }
}
