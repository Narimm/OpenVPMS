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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.table.DefaultDescriptorTableModel;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;
import java.util.List;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM;

/**
 * A warning dialog used to display invoice items that have been ordered but not dispensed, prior to the invoice's
 * posting.
 *
 * @author Tim Anderson
 */
public class UndispensedOrderDialog extends ConfirmationDialog {

    /**
     * The table of invoice items.
     */
    private PagedIMTable<Act> table;

    /**
     * Received quantity node.
     */
    private static final String RECEIVED_QUANTITY = "receivedQuantity";

    /**
     * The nodes to display.
     */
    private static final String[] NODES = new String[]{"id", "product", "quantity", RECEIVED_QUANTITY};

    /**
     * Constructs an {@link UndispensedOrderDialog}.
     */
    public UndispensedOrderDialog(List<Act> items, HelpContext help) {
        super(Messages.get("customer.order.nondispensed.title"),
              Messages.get("customer.order.nondispensed.message"));

        LayoutContext layout = new DefaultLayoutContext(new LocalContext(), help);
        DefaultDescriptorTableModel<Act> model = new DefaultDescriptorTableModel<>(INVOICE_ITEM, layout, NODES);
        DescriptorTableColumn column = model.getColumn(RECEIVED_QUANTITY);
        if (column != null) {
            column.setDefaultValue(BigDecimal.ZERO);
        }
        table = new PagedIMTable<>(model);
        table.setResultSet(new IMObjectListResultSet<>(items, 5));
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create(true, true);
        message.setText(getMessage());
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, message, table.getComponent());
        Row row = RowFactory.create(Styles.LARGE_INSET, column);
        getLayout().add(row);
    }

}
