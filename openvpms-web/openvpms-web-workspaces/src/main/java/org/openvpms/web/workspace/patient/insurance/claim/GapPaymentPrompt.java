package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;

/**
 * Prompts to make a gap or full payment on an insurance claim.
 *
 * @author Tim Anderson
 */
class GapPaymentPrompt extends ModalDialog {

    /**
     * The gap payment button.
     */
    private final RadioButton gapButton;

    /**
     * The full payment button.
     */
    private final RadioButton fullButton;

    /**
     * The claim summary.
     */
    private final GapClaimSummary summary;

    /**
     * Constructs a {@link GapPaymentPrompt}.
     *
     * @param claim        the claim
     * @param payFullClaim if {@code true}, select the 'full claim' option
     * @param help         the help context
     */
    public GapPaymentPrompt(GapClaimImpl claim, boolean payFullClaim, HelpContext help) {
        super(Messages.get("patient.insurance.pay.title"), "MediumWidthHeightDialog", OK_CANCEL, help);
        summary = new GapClaimSummary(claim);
        GapClaim.GapStatus gapStatus = claim.getGapStatus();

        ButtonGroup group = new ButtonGroup();
        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                enableOK();
            }
        };
        gapButton = ButtonFactory.create("patient.insurance.pay.gap", group, listener);
        fullButton = ButtonFactory.create("patient.insurance.pay.full", group, listener);
        group.addButton(gapButton);
        group.addButton(fullButton);
        boolean gapAllowed = summary.isGapAllowed();
        if (!gapAllowed) {
            gapButton.setEnabled(false);
        }
        if (payFullClaim || !gapAllowed) {
            fullButton.setSelected(true);
        }

        ComponentGrid grid = new ComponentGrid();
        BigDecimal paid = getPaid();
        if (MathRules.isZero(paid)) {
            grid.add(LabelFactory.create("patient.insurance.pay"));
        } else {
            grid.add(LabelFactory.create("patient.insurance.pay.remaining"));
        }
        TextComponent gapField = TextComponentFactory.createAmount(summary.getRemainingGapAmount(), 10, true);
        if (!gapButton.isEnabled()) {
            gapField.setStyleName(Styles.EDIT); // will display as grey
        }
        grid.add(gapButton, gapField);
        grid.add(fullButton, TextComponentFactory.createAmount(summary.getRemainingFullAmount(), 10, true));

        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, summary.getComponent(), grid.createGrid());
        Label message;
        if (gapAllowed) {
            if (gapStatus == GapClaim.GapStatus.PENDING) {
                message = LabelFactory.create("patient.insurance.pay.nobenefitdesc", true, true);
            } else {
                message = LabelFactory.create("patient.insurance.pay.gapdesc", true, true);
            }
        } else {
            message = LabelFactory.create("patient.insurance.pay.fulldesc", true, true);
        }
        column.add(message);

        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
        enableOK();
    }

    /**
     * Determines if the gap payment option is selected.
     *
     * @return {@code true} if the gap payment option is selected
     */
    public boolean payGap() {
        return gapButton.isSelected();
    }

    /**
     * Determines if the gap payment option is selected.
     *
     * @param payGap if the gap payment option is selected
     */
    public void setPayGap(boolean payGap) {
        if (gapButton.isEnabled()) {
            gapButton.setSelected(payGap);
            enableOK();
        }
    }

    /**
     * Determines if the full payment option is selected.
     *
     * @return {@code true} if the full payment option is selected
     */
    public boolean payFull() {
        return fullButton.isSelected();
    }

    /**
     * Determines if the full payment option is selected.
     *
     * @param payFull if the full payment option is selected
     */
    public void setPayFull(boolean payFull) {
        if (fullButton.isEnabled()) {
            fullButton.setSelected(payFull);
            enableOK();
        }
    }

    /**
     * Returns the amount already paid.
     *
     * @return the paid amount
     */
    public BigDecimal getPaid() {
        return summary.getPaid();
    }

    /**
     * Returns the amount to pay, based on the gap or full payment selection.
     *
     * @return the amount to pay
     */
    public BigDecimal getToPay() {
        return (payGap()) ? summary.getRemainingGapAmount() : summary.getRemainingFullAmount();
    }

    /**
     * Enables/disables the OK button on selection based on the selection of the gap/full radio.
     */
    protected void enableOK() {
        getButtons().setEnabled(OK_ID, payGap() || payFull());
    }

    /**
     * Invoked when the 'OK' button is pressed.
     */
    @Override
    protected void onOK() {
        if (payFull() || payGap()) {
            super.onOK();
        }
    }
}
