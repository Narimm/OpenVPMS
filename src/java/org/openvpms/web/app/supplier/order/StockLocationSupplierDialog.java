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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.focus.FocusGroup;

import java.util.List;


/**
 * A dialog that prompts for the stock location and supplier when generating orders.
 *
 * @author Tim Anderson
 */
public class StockLocationSupplierDialog extends PopupDialog {

    /**
     * The stock location/supplier selector.
     */
    private StockLocationSupplierSelector selector;

    /**
     * Creates a new {@code SelectOrderDetailsDialog}.
     *
     * @param title   the window title
     * @param context the context
     */
    public StockLocationSupplierDialog(String title, Context context) {
        super(title, OK_CANCEL);
        setModal(true);

        selector = new StockLocationSupplierSelector(context);
        getLayout().add(selector.getComponent());
        FocusGroup group = getFocusGroup();
        group.add(selector.getFocusGroup());
    }

    /**
     * Returns the available stock locations.
     *
     * @return the available stock locations
     */
    public List<IMObject> getStockLocations() {
        return selector.getStockLocations();
    }

    /**
     * Returns the available suppliers.
     *
     * @return the available suppliers
     */
    public List<IMObject> getSuppliers() {
        return selector.getSuppliers();
    }

    /**
     * Returns the selected stock location.
     *
     * @return the stock location, or {@code null} if none is selected
     */
    public Party getStockLocation() {
        return selector.getStockLocation();
    }

    /**
     * Returns the selected supplier.
     *
     * @return the supplier, or {@code null} if none is selected
     */
    public Party getSupplier() {
        return selector.getSupplier();
    }

}
