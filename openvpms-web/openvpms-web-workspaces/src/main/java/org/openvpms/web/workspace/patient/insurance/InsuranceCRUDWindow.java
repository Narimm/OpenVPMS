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
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.print.DefaultBatchPrinter;
import org.openvpms.web.component.workspace.ActCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.insurance.claim.ClaimEditDialog;
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
     * Accept button id.
     */
    private static final String ACCEPT_ID = "button.accept";

    /**
     * Decline button it.
     */
    private static final String DECLINE_ID = "button.decline";

    /**
     * Constructs an {@link InsuranceCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public InsuranceCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(InsuranceArchetypes.POLICY, Act.class), InsuranceActions.INSTANCE, context, help);
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
                Claim claim = factory.createClaim(editor.getObject());
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
        buttons.setEnabled(SUBMIT_ID, enable && getActions().canSubmit(object));
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
     * Creates a new edit dialog.
     *
     * @param editor the editor
     * @return a new edit dialog
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        if (editor instanceof ClaimEditor) {
            return new ClaimEditDialog((ClaimEditor) editor, getContext());
        }
        return super.createEditDialog(editor);
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
            final InsuranceService service = getInsuranceService(insurer);
            String title = Messages.get("patient.insurance.submit.title");
            if (service != null) {
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
                            }
                        });
            }
        }
    }

    /**
     * Invoked to cancel a claim.
     */
    protected void onCancelClaim() {
        final Act object = IMObjectHelper.reload(getObject());
        if (getActions().canCancelClaim(object)) {
            final Claim claim = getClaim(object);
            Party insurer = claim.getPolicy().getInsurer();
            final InsuranceService service = getInsuranceService(insurer);
            String title = Messages.get("patient.insurance.cancel.title");
            if (service != null) {
                ConfirmationDialog.show(title, Messages.format("patient.insurance.cancel.online", service.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                                service.cancel(claim);
                                onRefresh(object);
                            }
                        });
            } else {
                ConfirmationDialog.show(title, Messages.format("patient.insurance.submit.offline", insurer.getName()),
                                        ConfirmationDialog.YES_NO, new PopupDialogListener() {
                            @Override
                            public void onYes() {
                                claim.setStatus(Status.CANCELLED);
                                onRefresh(object);
                            }
                        });
            }
        }
    }

    private InsuranceService getInsuranceService(Party insurer) {
        InsuranceServices insuranceServices = ServiceHelper.getBean(InsuranceServices.class);
        return insuranceServices.getServiceForInsurer(insurer);
    }

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
                   && (Status.PENDING.isA(status) || Status.POSTED.isA(status) || Status.SUBMITTED.isA(status));
        }
    }
}
