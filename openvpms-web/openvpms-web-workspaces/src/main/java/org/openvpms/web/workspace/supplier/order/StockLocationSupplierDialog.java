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

package org.openvpms.web.workspace.supplier.order;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

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
     * If selected, generate orders for stock below ideal quantity; else generate orders for stock at or
     * below critical quantity.
     */
    private final RadioButton ideal;

    /**
     * Constructs a {@link StockLocationSupplierDialog}.
     *
     * @param title   the window title
     * @param context the context
     * @param help    the help context
     */
    public StockLocationSupplierDialog(String title, Context context, HelpContext help) {
        super(title, OK_CANCEL, help);
        setModal(true);

        selector = new StockLocationSupplierSelector(context);
        ButtonGroup buttonGroup = new ButtonGroup();
        ideal = new RadioButton(Messages.get("supplier.order.generate.ideal"));
        ideal.setSelected(true);
        RadioButton critical = new RadioButton(Messages.get("supplier.order.generate.critical"));
        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
            }
        };
        ideal.addActionListener(listener);
        critical.addActionListener(listener);
        critical.setGroup(buttonGroup);
        ideal.setGroup(buttonGroup);
        Column column = ColumnFactory.create(Styles.INSET_CELL_SPACING, selector.getComponent(),
                                             GroupBoxFactory.create("supplier.order.generate.for",
                                                                    ideal, critical));
        getLayout().add(column);
        FocusGroup group = getFocusGroup();
        group.add(selector.getFocusGroup());
        group.add(critical);
        group.add(ideal);
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

    /**
     * Determines if orders should be generated for stock below ideal quantity.
     *
     * @return {@code true} if orders should be generated for stock below ideal quantity; {@code false} if orders should
     *         be generated for stock at or below critical quantity
     */
    public boolean getBelowIdealQuantity() {
        return ideal.isSelected();
    }
}
