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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.Claim.Status;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.Declaration;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.print.DefaultBatchPrinter;
import org.openvpms.web.component.workspace.ActCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.insurance.claim.ClaimEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * CRUD window for patient insurance policies.
 *
 * @author Tim Anderson
 */
public class InsuranceCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * The insurance services.
     */
    private final InsuranceServices insuranceServices;

    /**
     * Claim button identifier.
     */
    private static final String CLAIM_ID = "button.claim";

    /**
     * Accept declaration button id.
     */
    private static final String ACCEPT_ID = "button.accept";

    /**
     * Decline declaration button it.
     */
    private static final String DECLINE_ID = "button.decline";

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
        super(Archetypes.create(InsuranceArchetypes.POLICY, Act.class), InsuranceActions.INSTANCE, context, help);
        insuranceServices = ServiceHelper.getBean(InsuranceServices.class);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(ButtonFactory.create(CLAIM_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onClaim();
            }
        }));
        buttons.add(createPostButton());
        buttons.add(ButtonFactory.create(SUBMIT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onSubmit();
            }
        }));
        buttons.add(ButtonFactory.create(CANCEL_CLAIM_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCancelClaim();
            }
        }));
        buttons.add(ButtonFactory.create(SETTLE_CLAIM_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onSettleClaim();
            }
        }));
        buttons.add(ButtonFactory.create(DECLINE_CLAIM_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onDeclineClaim();
            }
        }));
        buttons.add(createPrintButton());
    }


    /**
     * Posts the act. For claims, this generates any attachments first.
     *
     * @param act the act to post
     * @return {@code true} if the act was saved
     */
    @Override
    protected boolean post(Act act) {
        boolean result = false;
        if (TypeHelper.isA(act, InsuranceArchetypes.CLAIM)) {
            ClaimEditor editor = new ClaimEditor((FinancialAct) act, null, createLayoutContext(getHelpContext()));
            if (editor.generateAttachments()) {
                InsuranceFactory factory = ServiceHelper.getBean(InsuranceFactory.class);
                Claim claim = factory.createClaim(act);
                Party insurer = claim.getPolicy().getInsurer();
                if (insuranceServices.canSubmit(insurer)) {
                    InsuranceService service = getInsuranceService(insurer);
                    if (service.canValidateClaims()) {
                        service.validate(claim);
                    }
                }
                claim.finalise();
                onPosted(act);
            } else {
                edit(editor);

                // need to display the error popup after the dialog, to ensure it displays on top
                editor.checkAttachments();
            }
        } else {
            result = super.post(act);
        }
        return result;
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
        buttons.setEnabled(CLAIM_ID, enable && TypeHelper.isA(object, InsuranceArchetypes.POLICY));
        buttons.setEnabled(POST_ID, enable && getActions().canPost(object));
        buttons.setEnabled(SUBMIT_ID, enable && getActions().canSubmit(object));
        buttons.setEnabled(CANCEL_CLAIM_ID, enable && getActions().canCancelClaim(object));
        buttons.setEnabled(SETTLE_CLAIM_ID, enable && getActions().canSettleClaim(object));
        buttons.setEnabled(DECLINE_CLAIM_ID, enable && getActions().canDeclineClaim(object));
        enablePrintPreview(buttons, enable && TypeHelper.isA(object, InsuranceArchetypes.CLAIM));
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
     * Invoked when posting of an act is complete, either by saving the act
     * with <em>POSTED</em> status, or invoking {@link #onPost()}.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(Act act) {
        setObject(act);
        onSubmit();
    }

    /**
     * Print an object.
     *
     * @param object the object to print
     */
    @Override
    protected void print(Act object) {
        if (TypeHelper.isA(object, InsuranceArchetypes.CLAIM)) {
            ActBean bean = new ActBean(object);
            final List<IMObject> objects = new ArrayList<>();
            objects.add(object);
            int missingAttachment = 0;
            for (DocumentAct attachment : bean.getNodeActs("attachments", DocumentAct.class)) {
                if (attachment.getDocument() != null) {
                    objects.add(attachment);
                } else {
                    missingAttachment++;
                    break;
                }
            }
            String title = Messages.get("printdialog.title");
            String message = null;
            if (missingAttachment != 0) {
                message = Messages.format("patient.insurance.print.noattachment", missingAttachment);
            }
            final BatchPrintDialog dialog = new BatchPrintDialog(title, message, BatchPrintDialog.OK_CANCEL,
                                                                 objects, getHelpContext());
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    BatchPrinter printer = new DefaultBatchPrinter<>(dialog.getSelected(), getContext(),
                                                                     getHelpContext());
                    printer.print();
                }
            });
            dialog.show();
        } else {
            super.print(object);
        }
    }

    /**
     * Invoked when the 'Claim' button is pressed.
     */
    protected void onClaim() {
        Act object = IMObjectHelper.reload(getObject());
        if (TypeHelper.isA(object, InsuranceArchetypes.POLICY)) {
            if (!getActions().hasExistingClaims(object)) {
                Act act = (Act) IMObjectCreator.create(InsuranceArchetypes.CLAIM);
                if (act != null) {
                    ActBean bean = new ActBean(act);
                    Act policy = getObject();
                    bean.addNodeRelationship("policy", policy);
                    edit(act, null);
                }
            } else {
                ErrorDialog.show(Messages.get("patient.insurance.claim.existing.title"),
                                 Messages.get("patient.insurance.claim.existing.message"));
            }
        }
    }

    /**
     * Invoked when the 'Submit' button is pressed.
     */
    protected void onSubmit() {
        Act object = IMObjectHelper.reload(getObject());
        if (getActions().canSubmit(object)) {
            final Claim claim = getClaim(object);
            Party insurer = claim.getPolicy().getInsurer();
            String title = Messages.get("patient.insurance.submit.title");
            if (insuranceServices.canSubmit(insurer)) {
                final InsuranceService service = insuranceServices.getService(insurer);
                ConfirmationDialog.show(title, Messages.format("patient.insurance.submit.online", service.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                                submitWithDeclaration(claim, service);
                            }
                        });
            } else {
                ConfirmationDialog.show(title, Messages.format("patient.insurance.submit.offline", insurer.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                                claim.setStatus(Status.SUBMITTED);
                                onRefresh(object);
                            }
                        });
            }
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
     */
    protected void onCancelClaim() {
        final Act object = IMObjectHelper.reload(getObject());
        if (object != null && getActions().canCancelClaim(object)) {
            final Claim claim = getClaim(object);
            Party insurer = claim.getPolicy().getInsurer();
            String title = Messages.get("patient.insurance.cancel.title");
            if (insuranceServices.canSubmit(insurer)) {
                InsuranceService service = getInsuranceService(insurer);
                if (service.canCancel(claim)) {
                    String message = Messages.format("patient.insurance.cancel.online", service.getName());
                    final CancelClaimDialog dialog = new CancelClaimDialog(title, message, getHelpContext());
                    dialog.addWindowPaneListener(new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            service.cancel(claim, dialog.getReason());
                            onRefresh(object);
                        }
                    });
                    dialog.show();
                } else {
                    InformationDialog.show(title, Messages.format("patient.insurance.cancel.unsupported",
                                                                  service.getName()));
                }
            } else {
                String message = Messages.format("patient.insurance.cancel.offline", insurer.getName());
                final CancelClaimDialog dialog = new CancelClaimDialog(title, message, getHelpContext());
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        claim.setStatus(Status.CANCELLED, dialog.getReason());
                        onRefresh(object);
                    }
                });
                dialog.show();
            }
        }
    }

    /**
     * Invoked to mark a claim as settled. This only applies to claims not submitted via an {@link InsuranceService}.
     */
    protected void onSettleClaim() {
        final Act object = IMObjectHelper.reload(getObject());
        if (object != null && getActions().canSettleClaim(object)) {
            Claim claim = getClaim(object);
            Party insurer = claim.getPolicy().getInsurer();
            String title = Messages.get("patient.insurance.settle.title");
            if (insuranceServices.canSubmit(insurer)) {
                InformationDialog.show(title, Messages.format("patient.insurance.settle.online", insurer.getName()));
            } else {
                ConfirmationDialog.show(title, Messages.format("patient.insurance.settle.offline", insurer.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                                claim.setStatus(Status.SETTLED);
                                onRefresh(object);
                            }
                        });
            }
        }
    }

    /**
     * Invoked to mark a claim as declined. This only applies to claims not submitted via an {@link InsuranceService}.
     */
    protected void onDeclineClaim() {
        final Act object = IMObjectHelper.reload(getObject());
        if (object != null && getActions().canDeclineClaim(object)) {
            Claim claim = getClaim(object);
            Party insurer = claim.getPolicy().getInsurer();
            String title = Messages.get("patient.insurance.decline.title");
            if (insuranceServices.canSubmit(insurer)) {
                InformationDialog.show(title, Messages.format("patient.insurance.decline.online", insurer.getName()));
            } else {
                ConfirmationDialog.show(title, Messages.format("patient.insurance.decline.offline", insurer.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                                claim.setStatus(Status.DECLINED);
                                onRefresh(object);
                            }
                        });
            }
        }
    }

    /**
     * Returns the insurance service for an insurer.
     *
     * @param insurer the insurer
     * @return the insurance service
     */
    private InsuranceService getInsuranceService(Party insurer) {
        return insuranceServices.getService(insurer);
    }

    /**
     * Returns a claim for a claim act.
     *
     * @param claim the claim act
     * @return the claim
     */
    private Claim getClaim(Act claim) {
        InsuranceFactory factory = ServiceHelper.getBean(InsuranceFactory.class);
        return factory.createClaim(claim);
    }

    /**
     * Submits a claim to an {@link InsuranceService}, after accepting a declaration if required.
     *
     * @param claim   the claim to submit
     * @param service the service to submit to
     */
    private void submitWithDeclaration(final Claim claim, final InsuranceService service) {
        Declaration declaration = service.getDeclaration();
        if (declaration != null) {
            String text = declaration.getText();
            ConfirmationDialog.show(Messages.get("patient.insurance.declaration.title"), text,
                                    new String[]{ACCEPT_ID, DECLINE_ID}, new PopupDialogListener() {
                        @Override
                        public void onAction(String action) {
                            if (ACCEPT_ID.equals(action)) {
                                submit(claim, service);
                            }
                        }
                    });
        } else {
            submit(claim, service);
        }
    }

    /**
     * Submits a claim to an {@link InsuranceService}.
     *
     * @param claim   the claim
     * @param service the service to submit to
     */
    private void submit(Claim claim, InsuranceService service) {
        service.submit(claim);
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
            if (TypeHelper.isA(act, InsuranceArchetypes.CLAIM)) {
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
                if (TypeHelper.isA(act, InsuranceArchetypes.POLICY)) {
                    result = new ActBean(act).getValues("claims").isEmpty();
                } else if (TypeHelper.isA(act, InsuranceArchetypes.CLAIM)) {
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
            return TypeHelper.isA(act, InsuranceArchetypes.CLAIM) && Status.PENDING.isA(act.getStatus());
        }

        /**
         * Determines if an act is a claim that can be submitted.
         *
         * @param act the act
         * @return {@code true} if the act is a claim that can be submitted
         */
        public boolean canSubmit(Act act) {
            return TypeHelper.isA(act, InsuranceArchetypes.CLAIM) && Status.POSTED.isA(act.getStatus());
        }

        /**
         * Determines if an act is a policy with outstanding claims.
         *
         * @param act the act
         * @return {@code true} if the act is a policy with outstanding claims
         */
        public boolean hasExistingClaims(Act act) {
            if (TypeHelper.isA(act, InsuranceArchetypes.POLICY)) {
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
            return TypeHelper.isA(act, InsuranceArchetypes.CLAIM)
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
            return TypeHelper.isA(act, InsuranceArchetypes.CLAIM)
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
            return TypeHelper.isA(act, InsuranceArchetypes.CLAIM)
                   && (Status.SUBMITTED.isA(status) || Status.ACCEPTED.isA(status));
        }
    }
}
