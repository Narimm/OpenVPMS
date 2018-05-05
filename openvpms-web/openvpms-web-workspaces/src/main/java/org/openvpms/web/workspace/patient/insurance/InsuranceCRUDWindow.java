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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.patient.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.Claim.Status;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ActCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.insurance.claim.ClaimEditDialog;
import org.openvpms.web.workspace.patient.insurance.claim.ClaimSubmitter;

import java.util.Date;
import java.util.function.Consumer;

import static org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes.CLAIM;
import static org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes.POLICY;

/**
 * CRUD window for patient insurance policies.
 *
 * @author Tim Anderson
 */
public class InsuranceCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * New policy button identifier.
     */
    private static final String NEW_ID = "button.newpolicy";

    /**
     * Claim button identifier.
     */
    private static final String CLAIM_ID = "button.claim";

    /**
     * Submit button identifier.
     */
    private static final String SUBMIT_ID = "button.submit";

    /**
     * Cancel claim button identifier.
     */
    private static final String CANCEL_CLAIM_ID = "button.cancelclaim";

    /**
     * Settle claim button id.
     */
    private static final String SETTLE_CLAIM_ID = "button.settleclaim";

    /**
     * Decline claim button id.
     */
    private static final String DECLINE_CLAIM_ID = "button.declineclaim";


    /**
     * Constructs an {@link InsuranceCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public InsuranceCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(POLICY, Act.class), InsuranceActions.INSTANCE, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(CLAIM_ID, action(POLICY, this::claim, "patient.insurance.claim.title"));
        buttons.add(SUBMIT_ID, action(CLAIM, this::submit, "patient.insurance.submit.title"));
        buttons.add(CANCEL_CLAIM_ID, action(CLAIM, this::cancelClaim, "patient.insurance.cancel.title"));
        buttons.add(SETTLE_CLAIM_ID, action(CLAIM, this::settleClaim, "patient.insurance.settle.title"));
        buttons.add(DECLINE_CLAIM_ID, action(CLAIM, this::declineClaim, "patient.insurance.decline.title"));
        buttons.add(createPrintButton());
    }

    /**
     * Helper to create a new button with id {@link #NEW_ID} linked to {@link #create()} to create policies.
     *
     * @return a new button
     */
    @Override
    protected Button createNewButton() {
        return ButtonFactory.create(NEW_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                create();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        Act object = getObject();
        buttons.setEnabled(CLAIM_ID, enable && TypeHelper.isA(object, POLICY));
        buttons.setEnabled(SUBMIT_ID, enable && getActions().canSubmit(object));
        buttons.setEnabled(CANCEL_CLAIM_ID, enable && getActions().canCancelClaim(object));
        buttons.setEnabled(SETTLE_CLAIM_ID, enable && getActions().canSettleClaim(object));
        buttons.setEnabled(DECLINE_CLAIM_ID, enable && getActions().canDeclineClaim(object));
        enablePrintPreview(buttons, enable && TypeHelper.isA(object, CLAIM));
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected InsuranceActions getActions() {
        return (InsuranceActions) super.getActions();
    }

    /**
     * Print an object.
     *
     * @param object the object to print
     */
    @Override
    protected void print(Act object) {
        if (TypeHelper.isA(object, CLAIM)) {
            ClaimSubmitter submitter = new ClaimSubmitter(getContext(), getHelpContext());
            submitter.print(object, createRefreshAction(object, "printdialog.title"));
        } else {
            super.print(object);
        }
    }

    /**
     * Makes a claim against a policy.
     *
     * @param policy the policy
     */
    protected void claim(Act policy) {
        Date expiryDate = policy.getActivityEndTime();
        if (expiryDate != null && expiryDate.compareTo(new Date()) <= 0) {
            ConfirmationDialog.show(Messages.get("patient.insurance.policy.expired.title"),
                                    Messages.get("patient.insurance.policy.expired.message"),
                                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            checkExistingClaims(policy);
                        }
                    });
        } else {
            checkExistingClaims(policy);
        }
    }

    /**
     * Submits a claim.
     *
     * @param object the claim
     */
    protected void submit(Act object) {
        if (Status.PENDING.isA(object.getStatus())) {
            HelpContext edit = createEditTopic(object);
            LayoutContext context = createLayoutContext(edit);
            ClaimEditDialog dialog = (ClaimEditDialog) edit(createEditor(object, context));
            dialog.addWindowPaneListener(new WindowPaneListener() {
                @Override
                public void onClose(WindowPaneEvent event) {
                    onRefresh(object);
                }
            });
            dialog.show();
            dialog.submit();
        } else if (Status.POSTED.isA(object.getStatus())) {
            ClaimSubmitter submitter = new ClaimSubmitter(getContext(), getHelpContext());
            submitter.submit(object, createRefreshAction(object, "patient.insurance.submit.title"));
        }
    }

    /**
     * Invoked to cancel a claim.
     * <p>
     * Three modes are supported:
     * <ol>
     * <li>the claim is submitted via an {@link InsuranceService}, and the service supports cancellation<br/>
     * The claim is cancelled via {@link InsuranceService#cancel(Claim, String)}</li>
     * <li>the claim is submitted via an {@link InsuranceService}, but the service doesn't support cancellation.<br/>
     * The claim status is not changed. The insurer is responsible for updating the claim.</li>
     * <li>the claim is NOT submitted via an {@link InsuranceService}<br/>
     * The claim status is updated to CANCELLED if the user accepts a prompt</li>
     * </ol>
     *
     * @param object the claim
     */
    protected void cancelClaim(Act object) {
        if (getActions().canCancelClaim(object)) {
            ClaimSubmitter submitter = new ClaimSubmitter(getContext(), getHelpContext().subtopic("cancel"));
            submitter.cancel(object, createRefreshAction(object, "patient.insurance.cancel.title"));
        }
    }

    /**
     * Invoked to mark a claim as settled. This only applies to claims not submitted via an {@link InsuranceService}.
     *
     * @param object the claim
     */
    protected void settleClaim(Act object) {
        if (getActions().canSettleClaim(object)) {
            ClaimSubmitter submitter = new ClaimSubmitter(getContext(), getHelpContext().subtopic("settle"));
            submitter.settle(object, createRefreshAction(object, "patient.insurance.settle.title"));
        }
    }

    /**
     * Invoked to mark a claim as declined. This only applies to claims not submitted via an {@link InsuranceService}.
     */
    protected void declineClaim(Act object) {
        if (getActions().canDeclineClaim(object)) {
            ClaimSubmitter submitter = new ClaimSubmitter(getContext(), getHelpContext().subtopic("decline"));
            submitter.decline(object, createRefreshAction(object, "patient.insurance.decline.title"));
        }
    }

    /**
     * Checks if a policy has existing outstanding claims before creating a new one.
     */
    protected void checkExistingClaims(Act policy) {
        if (!getActions().hasExistingClaims(policy)) {
            createClaim(policy);
        } else {
            ConfirmationDialog.show(Messages.get("patient.insurance.claim.existing.title"),
                                    Messages.get("patient.insurance.claim.existing.message"),
                                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            createClaim(policy);
                        }
                    });
        }
    }

    /**
     * Creates a new claim for a policy.
     *
     * @param policy the policy
     */
    protected void createClaim(Act policy) {
        InsuranceRules rules = ServiceHelper.getBean(InsuranceRules.class);
        Act claim = rules.createClaim(policy);
        edit(claim, null);
    }

    /**
     * Creates an action to refresh the workspace once an operation has completed.
     *
     * @param object the object being operated on
     * @param title  the title resource bundle key, used when displaying an error dialog if the action failed
     * @return a new action
     */
    private Consumer<Throwable> createRefreshAction(Act object, String title) {
        return throwable -> {
            if (throwable != null) {
                ErrorHelper.show(Messages.get(title),
                                 DescriptorHelper.getDisplayName(object), object, throwable);
            }
            InsuranceCRUDWindow.this.onRefresh(object);
        };
    }

    private static class InsuranceActions extends ActActions<Act> {

        public static final InsuranceActions INSTANCE = new InsuranceActions();

        /**
         * Determines if an act can be edited.
         *
         * @param act the act to check
         * @return {@code true} if the act isn't locked
         */
        @Override
        public boolean canEdit(Act act) {
            boolean result;
            if (TypeHelper.isA(act, CLAIM)) {
                result = Status.PENDING.isA(act.getStatus());
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
                if (TypeHelper.isA(act, POLICY)) {
                    result = new ActBean(act).getValues("claims").isEmpty();
                } else if (TypeHelper.isA(act, CLAIM)) {
                    result = Status.PENDING.isA(act.getStatus());
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
            return TypeHelper.isA(act, CLAIM) && Status.PENDING.isA(act.getStatus());
        }

        /**
         * Determines if an act is a claim that can be submitted.
         *
         * @param act the act
         * @return {@code true} if the act is a claim that can be submitted
         */
        public boolean canSubmit(Act act) {
            String status = act.getStatus();
            return TypeHelper.isA(act, CLAIM) && (Status.PENDING.isA(status) || Status.POSTED.isA(status));
        }

        /**
         * Determines if an act is a policy with outstanding claims.
         *
         * @param act the act
         * @return {@code true} if the act is a policy with outstanding claims
         */
        public boolean hasExistingClaims(Act act) {
            if (TypeHelper.isA(act, POLICY)) {
                ActBean bean = new ActBean(act);
                for (Act claim : bean.getNodeActs("claims")) {
                    String status = claim.getStatus();
                    if (!Status.CANCELLED.isA(status) && !Status.DECLINED.isA(claim.getStatus())
                        && !Status.SETTLED.isA(status)) {
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
            if (TypeHelper.isA(act, CLAIM)) {
                return Status.PENDING.isA(act.getStatus());
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
            return TypeHelper.isA(act, CLAIM)
                   && (Status.PENDING.isA(status) || Status.POSTED.isA(status) || Status.SUBMITTED.isA(status)
                       || Status.ACCEPTED.isA(status));
        }

        /**
         * Determines if a claim can be flagged as settled.
         *
         * @param act the claim
         * @return {@code true} if the claim can be flagged as settled
         */
        public boolean canSettleClaim(Act act) {
            String status = act.getStatus();
            return TypeHelper.isA(act, CLAIM)
                   && (Status.SUBMITTED.isA(status) || Status.ACCEPTED.isA(status));
        }

        /**
         * Determines if a claim can be flagged as declined.
         *
         * @param act the claim
         * @return {@code true} if the claim can be flagged as declined
         */
        public boolean canDeclineClaim(Act act) {
            String status = act.getStatus();
            return TypeHelper.isA(act, CLAIM)
                   && (Status.SUBMITTED.isA(status) || Status.ACCEPTED.isA(status));
        }
    }
}
