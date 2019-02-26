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
import org.openvpms.archetype.rules.finance.credit.AllocationBlock;
import org.openvpms.archetype.rules.finance.credit.CreditAllocation;
import org.openvpms.archetype.rules.finance.credit.GapClaimAllocationBlock;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Table model to display debits that have been blocked from automatic allocation.
 *
 * @author Tim Anderson
 */
public class BlockedDebitAllocationTableModel extends ChargeAllocationTableModel {

    /**
     * Debits blocked from automatic allocation.
     */
    private final Map<FinancialAct, AllocationBlock> blocked;

    /**
     * The allocation of debits to the credit.
     */
    private final Map<FinancialAct, BigDecimal> allocations;

    /**
     * The allocation order model index.
     */
    private int orderIndex;

    /**
     * The allocation column index.
     */
    private int allocationIndex;

    /**
     * The 'charge in claim' column model index.
     */
    private int claimIndex;

    /**
     * Constructs a {@link BlockedDebitAllocationTableModel}.
     *
     * @param allocation  the allocation
     * @param allocations the amounts allocated to each debit
     * @param context     the layout context
     */
    public BlockedDebitAllocationTableModel(CreditAllocation allocation, Map<FinancialAct, BigDecimal> allocations,
                                            LayoutContext context) {
        super(context);
        this.allocations = allocations;
        blocked = allocation.getBlocked();
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
        Object result;
        if (column.getModelIndex() == orderIndex) {
            String value = NumberFormatter.format(row + 1);
            result = TableHelper.rightAlign(value);
        } else if (column.getModelIndex() == allocationIndex) {
            BigDecimal amount = getAllocation(object);
            String value = NumberFormatter.format(amount, NumberFormatter.getCurrencyFormat());
            result = TableHelper.rightAlign(value);
        } else if (column.getModelIndex() == claimIndex) {
            AllocationBlock block = getAllocationBlock(object);
            CheckBox box = CheckBoxFactory.create(block instanceof GapClaimAllocationBlock);
            box.setEnabled(false);
            result = box;
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the allocation block for a debit.
     *
     * @param object the debit
     * @return the allocation block, or {@code null} if there is none
     */
    protected AllocationBlock getAllocationBlock(FinancialAct object) {
        return blocked.get(object);
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
        orderIndex = getNextModelIndex(model);
        allocationIndex = orderIndex + 1;
        claimIndex = allocationIndex + 1;
        TableColumn order = new TableColumn(orderIndex);
        TableColumn allocation = new TableColumn(allocationIndex);
        TableColumn plan = new TableColumn(claimIndex);

        // make the order column the first column
        model.addColumn(order);
        model.moveColumn(model.getColumnCount() - 1, 0);

        // add the allocation column after  the allocatedAmount column
        allocation.setHeaderValue(Messages.get("customer.credit.allocation"));
        model.addColumn(allocation);
        model.moveColumn(model.getColumnCount() - 1, getColumnOffset(model, "notes"));

        // make the 'charge in claim' indicator the last column
        plan.setHeaderValue(Messages.get("customer.credit.claim"));
        model.addColumn(plan);
        return model;
    }

    /**
     * Returns the credit allocation available to a charge at the specified row.
     *
     * @param debit the debit
     * @return the allocation available to the charge at the row
     */
    private BigDecimal getAllocation(FinancialAct debit) {
        return allocations.get(debit);
    }
}
