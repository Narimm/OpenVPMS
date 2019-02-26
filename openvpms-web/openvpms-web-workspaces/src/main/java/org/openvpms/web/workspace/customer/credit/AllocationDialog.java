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

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.finance.account.BalanceCalculator;
import org.openvpms.archetype.rules.finance.credit.AllocationBlock;
import org.openvpms.archetype.rules.finance.credit.CreditAllocation;
import org.openvpms.archetype.rules.finance.credit.GapClaimAllocationBlock;
import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.relationship.SequencedTable;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.util.StyleSheetHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A dialog to specify the order that debit acts should be allocated to a credit act.
 *
 * @author Tim Anderson
 */
public class AllocationDialog extends PopupDialog {

    /**
     * The debit acts.
     */
    private final List<FinancialAct> objects;

    /**
     * The debits with allocation blocks.
     */
    private final Map<FinancialAct, AllocationBlock> blocked;

    /**
     * The balance calculator.
     */
    private final BalanceCalculator calculator;

    /**
     * The total to allocate.
     */
    private final BigDecimal total;

    /**
     * The allocation of debits to the credit
     */
    private final Map<FinancialAct, BigDecimal> allocations = new HashMap<>();

    /**
     * The layout context.
     */
    private final LayoutContext layout;

    /**
     * The split pane display the debit table and claims table.
     */
    private final SplitPane pane;

    /**
     * Split pane separator.
     */
    private final Extent separator;

    /**
     * Container to display the claims associated with the selected invoice.
     */
    private Component container;

    /**
     * The allocation table, if there is more than one debit to allocate against.
     */
    private SequencedTable<FinancialAct> sequenced;

    /**
     * Constructs an {@link AllocationDialog}.
     *
     * @param allocation the credit allocation
     * @param context    the context
     * @param help       the help context
     */
    public AllocationDialog(CreditAllocation allocation, Context context, HelpContext help) {
        super(Messages.format("customer.credit.allocate.title", allocation.getDisplayName()), "ChildEditDialog",
              OK_CANCEL, help);
        setModal(true);
        objects = new ArrayList<>(allocation.getDebits());
        calculator = new BalanceCalculator(ServiceHelper.getArchetypeService());
        total = calculator.getAllocatable(allocation.getCredit());
        layout = new DefaultLayoutContext(context, help);
        calculateAllocation();
        blocked = allocation.getBlocked();
        IMTableModel<FinancialAct> model = createBlockedTableModel(allocation, allocations, layout);
        PagedIMTable<FinancialAct> table = new PagedIMTable<>(model);
        table.getTable().addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onSelected(table.getSelected());
            }
        });
        table.setResultSet(new ListResultSet<>(objects, 20));
        Label label = LabelFactory.create(true, true);
        label.setStyleName(Styles.BOLD);
        String reason = getAllocationReason(allocation);
        label.setText(Messages.format("customer.credit.allocate.message", allocation.getDisplayName(), reason));
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, label);
        if (objects.size() > 1) {
            // allow the allocation order to be set
            sequenced = new SequencedTable<FinancialAct>(table) {
                @Override
                public List<FinancialAct> getObjects() {
                    return objects;
                }

                @Override
                public void swap(FinancialAct object1, FinancialAct object2) {
                    int index1 = objects.indexOf(object1);
                    int index2 = objects.indexOf(object2);
                    if (index1 != -1 && index2 != -1) {
                        objects.set(index2, object1);
                        objects.set(index1, object2);
                        calculateAllocation();
                        table.setResultSet(createResultSet());
                        table.setSelected(object1);
                        enableNavigation(true);
                    }
                }
            };
            sequenced.layout(column, getFocusGroup());
        } else {
            column.add(table.getComponent());
            getFocusGroup().add(table.getFocusGroup());
        }

        container = ColumnFactory.create(Styles.LARGE_INSET);
        pane = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "BrowserCRUDWorkspace.Layout",
                                       container, ColumnFactory.create(Styles.LARGE_INSET, column));
        separator = StyleSheetHelper.getExtent(SplitPane.class, pane.getStyleName(),
                                               SplitPane.PROPERTY_SEPARATOR_POSITION);
        getLayout().add(pane);
        if (!objects.isEmpty()) {
            table.setSelected(objects.get(0));
            onSelected(table.getSelected());
        } else {
            onSelected(null);
        }
    }

    /**
     * Returns the debit acts, in the order they should be allocated.
     *
     * @return the debit acts
     */
    public List<FinancialAct> getDebits() {
        return objects;
    }

    /**
     * Swaps two debits. This will allocate to the debit highest in the table first.
     *
     * @param debit1 the first debit
     * @param debit2 the second debit
     */
    public void swap(FinancialAct debit1, FinancialAct debit2) {
        sequenced.swap(debit1, debit2);
    }

    /**
     * Returns the gap claims and their associated invoices.
     *
     * @return the gap claims and their invoices
     */
    public List<GapClaimAllocation> getGapClaimAllocations() {
        Map<FinancialAct, Set<FinancialAct>> claimsToInvoices = new HashMap<>();
        for (Map.Entry<FinancialAct, AllocationBlock> entry : blocked.entrySet()) {
            FinancialAct invoice = entry.getKey();
            if (entry.getValue() instanceof GapClaimAllocationBlock) {
                List<FinancialAct> claims = ((GapClaimAllocationBlock) entry.getValue()).getGapClaims();
                for (FinancialAct claim : claims) {
                    Set<FinancialAct> invoices = claimsToInvoices.computeIfAbsent(claim, k -> new HashSet<>());
                    invoices.add(invoice);
                }
            }
        }
        List<GapClaimAllocation> result = new ArrayList<>();
        InsuranceFactory factory = ServiceHelper.getBean(InsuranceFactory.class);
        for (Map.Entry<FinancialAct, Set<FinancialAct>> entry : claimsToInvoices.entrySet()) {
            FinancialAct claim = entry.getKey();
            Set<FinancialAct> invoices = entry.getValue();
            result.add(getGapClaimAllocation(claim, invoices, factory));
        }
        return result;
    }

    /**
     * Returns the reason the allocation dialog is being displayed.
     *
     * @param block the allocation block
     * @return a reason
     */
    protected String getAllocationReason(AllocationBlock block) {
        return (block instanceof GapClaimAllocationBlock) ? Messages.get("customer.credit.allocate.claim") : null;
    }

    /**
     * Creates a table model to display debits blocked from automatic allocation.
     *
     * @param allocation  the allocation
     * @param allocations the amounts allocated to each debit
     * @param layout      the layout context
     * @return a new table model
     */
    protected IMTableModel<FinancialAct> createBlockedTableModel(CreditAllocation allocation,
                                                                 Map<FinancialAct, BigDecimal> allocations,
                                                                 LayoutContext layout) {
        return new BlockedDebitAllocationTableModel(allocation, allocations, layout);
    }

    /**
     * Renders an allocation block for a charge.
     *
     * @param charge the charge
     * @param block  the allocation block
     * @return the block component, or {@code null} if the allocation block shouldn't be displayed
     */
    protected Component getAllocationBlock(FinancialAct charge, AllocationBlock block) {
        if (block instanceof GapClaimAllocationBlock) {
            List<FinancialAct> claims = ((GapClaimAllocationBlock) block).getGapClaims();
            PagedIMTable<FinancialAct> table = new PagedIMTable<>(new ClaimTableModel(layout));
            table.setResultSet(new ListResultSet<>(claims, 20));
            Label label = LabelFactory.create(null, Styles.BOLD);
            label.setText(Messages.format("customer.credit.allocate.claimedby", DescriptorHelper.getDisplayName(charge),
                                          claims.size()));
            return ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, table.getComponent());
        }
        return null;
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        List<GapClaimAllocation> gapClaims = getGapClaimAllocations();
        Iterator<GapClaimAllocation> iterator = gapClaims.iterator();
        if (iterator.hasNext()) {
            checkGapClaims(iterator, () -> AllocationDialog.super.onOK());
        } else {
            super.onOK();
        }
    }

    /**
     * Returns the reason the allocation dialog is being displayed.
     *
     * @param allocation the allocation
     * @return a reason
     */
    private String getAllocationReason(CreditAllocation allocation) {
        String result = null;
        Collection<AllocationBlock> blocks = allocation.getBlocked().values();
        if (!blocks.isEmpty()) {
            result = getAllocationReason(blocks.iterator().next());
        }
        if (result == null) {
            result = "blocked for an unknown reason";
        }
        return result;
    }

    /**
     * Returns the allocation for a gap claim.
     *
     * @param claim    the claim
     * @param invoices thhe invoices associated with the claim
     * @param factory  the insurance factory
     * @return a new gap claim allocation
     */
    private GapClaimAllocation getGapClaimAllocation(FinancialAct claim, Set<FinancialAct> invoices,
                                                     InsuranceFactory factory) {
        BigDecimal existingAllocation = BigDecimal.ZERO;
        BigDecimal newAllocation = BigDecimal.ZERO;
        for (FinancialAct invoice : invoices) {
            existingAllocation = existingAllocation.add(invoice.getAllocatedAmount());
            newAllocation = newAllocation.add(allocations.get(invoice));
        }
        return new GapClaimAllocation(claim, existingAllocation, newAllocation, factory);

    }

    /**
     * Verifies that the user wants to make gap claim allocations.
     * <p>
     * For each affected gap claim, popups a confirmation dialog indicating how the claim has been
     * affected, giving the user the option to OK (apply the allocation), or Cancel (re-allocate).
     * <p>
     * When OK is selected for each, the supplied {@code onAccept} will be run.
     *
     * @param iterator an iterator over the gap claim allocations
     * @param onAccept run this when the user accepts all of the gap claim allocations
     */
    private void checkGapClaims(Iterator<GapClaimAllocation> iterator, Runnable onAccept) {
        if (iterator.hasNext()) {
            GapClaimAllocation allocation = iterator.next();
            if (allocation.isAllocated()) {
                HelpContext help = getHelpContext().subtopic("payclaim");
                GapClaimAllocationDialog dialog = new GapClaimAllocationDialog(allocation, help);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        checkGapClaims(iterator, onAccept);
                    }
                });
                dialog.show();
            } else {
                checkGapClaims(iterator, onAccept);
            }
        } else {
            onAccept.run();
        }
    }

    /**
     * Calculates the allocation of the total to each of the debits.
     */
    private void calculateAllocation() {
        BigDecimal available = total;
        for (FinancialAct debit : objects) {
            BigDecimal amount;
            BigDecimal allocatable = calculator.getAllocatable(debit);
            if (allocatable.compareTo(available) <= 0) {
                available = available.subtract(allocatable);
                amount = allocatable;
            } else {
                amount = available;
                available = BigDecimal.ZERO;
            }
            allocations.put(debit, amount);
        }
    }

    /**
     * Invoked when a charge is selected.
     *
     * @param selected the charge. May be {@code null}
     */
    private void onSelected(FinancialAct selected) {
        container.removeAll();
        if (sequenced != null) {
            sequenced.enableNavigation(selected != null);
        }
        AllocationBlock block = blocked.get(selected);
        Component component = null;
        if (block != null) {
            component = getAllocationBlock(selected, block);
        }

        if (component != null) {
            container.add(component);
            pane.setSeparatorPosition(separator);
            pane.setResizable(true);
        } else {
            pane.setSeparatorPosition(new Extent(0));
            pane.setResizable(false);
        }
    }

    /**
     * Creates a result set of the debits.
     *
     * @return the result set
     */
    private ResultSet<FinancialAct> createResultSet() {
        return new ListResultSet<>(objects, 20);
    }

    private class ClaimTableModel extends DescriptorTableModel<FinancialAct> {

        /**
         * Constructs a {@link ClaimTableModel}.
         *
         * @param context the layout context
         */
        public ClaimTableModel(LayoutContext context) {
            super(new String[]{InsuranceArchetypes.CLAIM}, context);
        }

        /**
         * Returns an {@link ArchetypeNodes} that determines what nodes appear in the table.
         * This is only used when {@link #getNodeNames()} returns null or empty.
         *
         * @return the nodes to include
         */
        @Override
        protected ArchetypeNodes getArchetypeNodes() {
            return ArchetypeNodes.onlySimple("startTime", "patient", "insurerId", "author", "amount", "status",
                                             "status2", "benefitAmount", "benefitNotes").hidden(true);
        }

    }
}
