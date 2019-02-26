package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;

/**
 * Displays a summary of a gap claim.
 *
 * @author Tim Anderson
 */
public class GapClaimSummary {

    /**
     * The claim.
     */
    private final GapClaimImpl claim;

    /**
     * The amount already paid.
     */
    private final BigDecimal paid;

    /**
     * Determines if gap payment is allowed.
     */
    private final boolean gapAllowed;

    /**
     * The amount remaining for gap payment.
     */
    private final BigDecimal remainingGap;

    /**
     * The amount remaining for full payment.
     */
    private final BigDecimal remainingFull;

    /**
     * The component.
     */
    private Grid component;


    /**
     * Constructs a {@link GapClaimSummary}.
     *
     * @param claim the claim
     */
    public GapClaimSummary(GapClaimImpl claim) {
        this(claim, claim.getAllocated());
    }

    /**
     * Constructs a {@link GapClaimSummary}.
     *
     * @param claim     the claim
     * @param allocated the allocated amount. This is the amount already paid on the invoices on the claim
     */
    public GapClaimSummary(GapClaimImpl claim, BigDecimal allocated) {
        this.claim = claim;
        BigDecimal benefit = claim.getBenefitAmount();
        BigDecimal gapAmount = claim.getGapAmount();
        BigDecimal total = claim.getTotal();
        paid = allocated;
        gapAllowed = paid.compareTo(gapAmount) <= 0 && !MathRules.isZero(benefit);

        GapClaim.GapStatus gapStatus = claim.getGapStatus();
        if (gapAllowed && gapStatus != GapClaim.GapStatus.PENDING) {
            remainingGap = gapAmount.subtract(paid);
        } else {
            remainingGap = BigDecimal.ZERO;
        }
        if (paid.compareTo(total) <= 0) {
            remainingFull = total.subtract(paid);
        } else {
            remainingFull = BigDecimal.ZERO;
        }
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            ComponentGrid grid = new ComponentGrid();
            doLayout(claim, grid);
            component = grid.createGrid();

        }
        return component;
    }

    /**
     * The amount already paid towards the claim.
     *
     * @return the amount paid
     */
    public BigDecimal getPaid() {
        return paid;
    }

    public boolean isGapAllowed() {
        return gapAllowed;
    }

    /**
     * The amount remaining to pay the gap.
     *
     * @return the remaining gap amount
     */
    public BigDecimal getRemainingGapAmount() {
        return remainingGap;
    }

    /**
     * The amount remaining to pay the full claim.
     *
     * @return the remaining full claim amount
     */
    public BigDecimal getRemainingFullAmount() {
        return remainingFull;
    }

    /**
     * Lays out the summary in a grid.
     *
     * @param claim the claim
     * @param grid  the grid
     */
    protected void doLayout(GapClaimImpl claim, ComponentGrid grid) {
        Component benefitAmount;
        if (claim.getGapStatus() == GapClaim.GapStatus.PENDING) {
            TextField field = TextComponentFactory.create(20);
            field.setEnabled(false);
            field.setText(Messages.get("patient.insurance.pay.nobenefit"));
            benefitAmount = field;
        } else {
            benefitAmount = createAmount(claim.getBenefitAmount());
        }
        grid.add(createLabel(DescriptorHelper.getDisplayName(InsuranceArchetypes.CLAIM, "patient")),
                 createLabel(claim.getPatient().getName()));
        grid.add(createLabel(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "insurer")),
                 createLabel(claim.getInsurer().getName()));
        grid.add(LabelFactory.create("patient.insurance.pay.total"), createAmount(claim.getTotal()));
        grid.add(LabelFactory.create("patient.insurance.pay.benefit"), benefitAmount);
        String benefitNotes = claim.getBenefitNotes();
        if (!StringUtils.isEmpty(benefitNotes)) {
            int maxLength = benefitNotes.length() < 100 ? benefitNotes.length() : 100;
            TextComponent field = ReadOnlyComponentFactory.getText(benefitNotes, 20, maxLength, Styles.DEFAULT);
            grid.add(createLabel(DescriptorHelper.getDisplayName(InsuranceArchetypes.CLAIM, "benefitNotes")), field);
        }
        grid.add(LabelFactory.create("patient.insurance.pay.gap"), createAmount(claim.getGapAmount()));
        grid.add(LabelFactory.create("patient.insurance.pay.paid"), createAmount(paid));
    }

    /**
     * Helper to create a read-only text field displaying a currency amount.
     *
     * @param amount the amount
     * @return a new text field
     */
    protected TextComponent createAmount(BigDecimal amount) {
        return TextComponentFactory.createAmount(amount, 10, true);
    }

    /**
     * Helper to create a label.
     *
     * @param text the label text
     * @return a new label
     */
    private Label createLabel(String text) {
        Label label = LabelFactory.create();
        label.setText(text);
        return label;
    }

}
