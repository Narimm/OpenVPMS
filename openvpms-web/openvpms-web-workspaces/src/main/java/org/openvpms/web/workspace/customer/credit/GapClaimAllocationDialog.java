package org.openvpms.web.workspace.customer.credit;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.insurance.claim.GapClaimSummary;

/**
 * Dialog to display allocation to a gap claim.
 *
 * @author Tim Anderson
 */
public class GapClaimAllocationDialog extends ModalDialog {

    /**
     * The allocation.
     */
    private final GapClaimAllocation allocation;

    /**
     * The message.
     */
    private final String message;

    /**
     * Constructs a {@link GapClaimAllocationDialog}.
     *
     * @param allocation the claim allocation
     * @param help       the help context
     */
    public GapClaimAllocationDialog(GapClaimAllocation allocation, HelpContext help) {
        super(Messages.get("patient.insurance.pay.title"), "MediumWidthHeightDialog", OK_CANCEL, help);

        this.allocation = allocation;
        message = getMessage(allocation);

        Label content = LabelFactory.create(true, true);
        content.setText(message);
        Component summary = new AllocationSummary(allocation).getComponent();
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, summary, content);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Returns the gap claim allocation.
     *
     * @return the gap claim allocation
     */
    public GapClaimAllocation getAllocation() {
        return allocation;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns a message describing the allocation.
     *
     * @param allocation the allocation
     * @return the message
     */
    protected String getMessage(GapClaimAllocation allocation) {
        StringBuilder result = new StringBuilder();
        GapClaimAllocation.Status status = allocation.getStatus();
        if (allocation.benefitPending()) {
            result.append(Messages.format("customer.credit.allocate.gap.nobenefit"));
            if (status == GapClaimAllocation.Status.NO_BENEFIT_PARTIAL_PAYMENT) {
                result.append(Messages.get("customer.credit.allocate.gap.nobenefit.partial"));
            } else {
                result.append(Messages.get("customer.credit.allocate.gap.nobenefit.full"));
            }
        } else {
            if (status == GapClaimAllocation.Status.ALLOCATION_LESS_THAN_GAP) {
                result.append(Messages.format("customer.credit.allocate.gap.less",
                                              allocation.getGapAmount().subtract(allocation.getAllocation())));
            } else if (status == GapClaimAllocation.Status.ALLOCATION_EQUAL_TO_GAP) {
                result.append(Messages.get("customer.credit.allocate.gap.equal"));
            } else if (status == GapClaimAllocation.Status.ALLOCATION_GREATER_THAN_GAP) {
                // paying more than the gap amount, but less than the claim total
                result.append(Messages.format("customer.credit.allocate.gap.partial",
                                              allocation.getTotal().subtract(allocation.getAllocation())));
            } else {
                // paying the claim
                result.append(Messages.get("customer.credit.allocate.gap.full"));
            }
        }
        return result.toString();
    }

    private class AllocationSummary extends GapClaimSummary {

        public AllocationSummary(GapClaimAllocation allocation) {
            super(allocation.getClaim(), allocation.getExistingAllocation());
        }

        @Override
        protected void doLayout(GapClaimImpl claim, ComponentGrid grid) {
            super.doLayout(claim, grid);
            grid.add(LabelFactory.create("customer.credit.allocate.gap.currentpayment"),
                     createAmount(allocation.getNewAllocation()));
        }
    }
}
