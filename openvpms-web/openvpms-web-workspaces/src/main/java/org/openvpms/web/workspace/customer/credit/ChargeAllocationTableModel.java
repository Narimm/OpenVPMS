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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.credit;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Table model for displaying charges, including how much has been paid, and how much is left to pay.
 *
 * @author Tim Anderson
 */
public class ChargeAllocationTableModel extends DescriptorTableModel<FinancialAct> {

    /**
     * The 'to pay' column model index.
     */
    private int toPayIndex;

    /**
     * Constructs a {@link ChargeAllocationTableModel}.
     *
     * @param context the layout context
     */
    public ChargeAllocationTableModel(LayoutContext context) {
        this(CustomerAccountArchetypes.DEBITS, context);
    }

    /**
     * Constructs a {@link ChargeAllocationTableModel}.
     *
     * @param archetypes the archetype short names
     * @param context    the layout context
     */
    public ChargeAllocationTableModel(String[] archetypes, LayoutContext context) {
        super(archetypes, context);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(FinancialAct object, TableColumn column, int row) {
        if (column.getModelIndex() == toPayIndex) {
            BigDecimal amount = object.getTotal().subtract(object.getAllocatedAmount());
            String value = NumberFormatter.format(amount, NumberFormatter.getCurrencyFormat());
            return TableHelper.rightAlign(value);
        }
        return super.getValue(object, column, row);
    }

    /**
     * Returns an {@link ArchetypeNodes} that determines what nodes appear in the table.
     *
     * @return the nodes to include
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return ArchetypeNodes.onlySimple("id", "startTime", "amount", "allocatedAmount", "clinician", "notes")
                .hidden(true);
    }

    /**
     * Creates a column model.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(archetypes, context);
        DescriptorTableColumn paid = getColumn(model, "allocatedAmount");
        paid.setHeaderValue(Messages.get("customer.credit.paid"));
        toPayIndex = getNextModelIndex(model);
        TableColumn toPay = new TableColumn(toPayIndex);
        toPay.setHeaderValue(Messages.get("customer.credit.topay"));
        addColumnAfter(toPay, paid.getModelIndex(), model);
        return model;
    }
}
