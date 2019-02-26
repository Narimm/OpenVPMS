package org.openvpms.web.workspace.patient.insurance;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.system.ServiceHelper;

import static org.openvpms.archetype.rules.insurance.InsuranceArchetypes.CLAIM;
import static org.openvpms.archetype.rules.insurance.InsuranceArchetypes.POLICY;

/**
 * Actions for insurance policies and claims.
 *
 * @author Tim Anderson
 */
public class InsuranceActions extends ActActions<Act> {

    /**
     * The singleton intance.
     */
    public static final InsuranceActions INSTANCE = new InsuranceActions();

    /**
     * Default constructor.
     */
    private InsuranceActions() {
        super();
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't locked
     */
    @Override
    public boolean canEdit(Act act) {
        boolean result;
        if (act.isA(CLAIM)) {
            result = Claim.Status.PENDING.isA(act.getStatus());
        } else {
            result = super.canEdit(act);
        }
        return result;
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't locked
     */
    @Override
    public boolean canDelete(Act act) {
        boolean result = super.canDelete(act);
        if (result) {
            if (act.isA(POLICY)) {
                result = new ActBean(act).getValues("claims").isEmpty();
            } else if (act.isA(CLAIM)) {
                result = Claim.Status.PENDING.isA(act.getStatus());
            }
        }
        return result;
    }

    /**
     * Determines if an act can be posted (i.e finalised).
     * <p>
     * This implementation returns {@code true} if the act status isn't {@code POSTED} or {@code CANCELLED}.
     *
     * @param act the act to check
     * @return {@code true} if the act can be posted
     */
    @Override
    public boolean canPost(Act act) {
        return act.isA(CLAIM) && Claim.Status.PENDING.isA(act.getStatus());
    }

    /**
     * Determines if an act is a claim that can be submitted.
     *
     * @param act the act
     * @return {@code true} if the act is a claim that can be submitted
     */
    public boolean canSubmit(Act act) {
        String status = act.getStatus();
        return act.isA(CLAIM) && (Claim.Status.PENDING.isA(status) || Claim.Status.POSTED.isA(status));
    }

    /**
     * Determines if an act is a policy with outstanding claims.
     *
     * @param act the act
     * @return {@code true} if the act is a policy with outstanding claims
     */
    public boolean hasExistingClaims(Act act) {
        if (act.isA(POLICY)) {
            ActBean bean = new ActBean(act);
            for (Act claim : bean.getNodeActs("claims")) {
                String status = claim.getStatus();
                if (!Claim.Status.CANCELLED.isA(status) && !Claim.Status.DECLINED.isA(claim.getStatus())
                    && !Claim.Status.SETTLED.isA(status)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if an act is unfinalised, for the purposes of printing.
     *
     * @param act the act
     * @return {@code true} if the act is unfinalised, otherwise {@code false}
     */
    @Override
    public boolean isUnfinalised(Act act) {
        if (act.isA(CLAIM)) {
            return Claim.Status.PENDING.isA(act.getStatus());
        }
        return super.isUnfinalised(act);
    }

    /**
     * Determines if a confirmation should be displayed before printing an unfinalised act.
     *
     * @return {@code true}
     */
    @Override
    public boolean warnWhenPrintingUnfinalisedAct() {
        return true;
    }

    /**
     * Determines if a claim can be cancelled.
     *
     * @param act the claim act
     * @return {@code true} if the claim can be cancelled
     */
    public boolean canCancelClaim(Act act) {
        String status = act.getStatus();
        return act.isA(CLAIM)
               && (Claim.Status.PENDING.isA(status) || Claim.Status.POSTED.isA(status)
                   || Claim.Status.SUBMITTED.isA(status)
                   || Claim.Status.ACCEPTED.isA(status));
    }

    /**
     * Determines if a claim can be flagged as settled.
     *
     * @param act the claim
     * @return {@code true} if the claim can be flagged as settled
     */
    public boolean canSettleClaim(Act act) {
        String status = act.getStatus();
        return act.isA(CLAIM) && (Claim.Status.SUBMITTED.isA(status) || Claim.Status.ACCEPTED.isA(status));
    }

    /**
     * Determines if a claim is a gap claim that can be paid.
     *
     * @param act the claim
     * @return {@code true} if the claim is a gap claim that can be paid
     */
    public boolean canPayClaim(Act act) {
        boolean result = false;
        String status = act.getStatus();
        if (act.isA(CLAIM) && (Claim.Status.SUBMITTED.isA(status) || Claim.Status.ACCEPTED.isA(status))) {
            IMObjectBean bean = ServiceHelper.getArchetypeService().getBean(act);
            if (bean.getBoolean("gapClaim")) {
                String benefitStatus = act.getStatus2();
                if (GapClaim.GapStatus.RECEIVED.isA(benefitStatus) || GapClaim.GapStatus.PENDING.isA(benefitStatus) ||
                    GapClaim.GapStatus.PAID.isA(benefitStatus)) {
                    // include PAID status, as the insurer still needs to be notified of payment
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Determines if a claim can be flagged as declined.
     *
     * @param act the claim
     * @return {@code true} if the claim can be flagged as declined
     */
    public boolean canDeclineClaim(Act act) {
        String status = act.getStatus();
        return act.isA(CLAIM) && (Claim.Status.SUBMITTED.isA(status) || Claim.Status.ACCEPTED.isA(status));
    }
}
