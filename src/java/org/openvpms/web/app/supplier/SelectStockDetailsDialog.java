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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.focus.FocusGroup;


/**
 * A dialog that prompts for the supplier and stock location when creating
 * orders, deliveries or returns.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SelectStockDetailsDialog extends PopupDialog {

    /**
     * The stock details selector.
     */
    private StockDetailsSelector selector;

    /**
     * Creates a new <tt>SelectOrderDetailsDialog</tt>.
     *
     * @param title   the window title
     * @param context the context
     */
    public SelectStockDetailsDialog(String title, Context context) {
        super(title, OK_CANCEL);
        setModal(true);

        selector = new StockDetailsSelector(context);
        getLayout().add(selector.getComponent());
        FocusGroup group = getFocusGroup();
        group.add(selector.getFocusGroup());
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier, or <tt>null</tt> if none is selected
     */
    public Party getSupplier() {
        return selector.getSupplier();
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location, or <tt>null</tt> if none is selected
     */
    public Party getStockLocation() {
        return selector.getStockLocation();
    }

    /**
     * Invoked when the 'OK' button is pressed.
     */
    @Override
    protected void onOK() {
        if (getSupplier() != null && getStockLocation() != null) {
            super.onOK();
        }
    }
}
