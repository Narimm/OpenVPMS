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

package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Party;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.insurance.service.ClaimValidationStatus;
import org.openvpms.insurance.service.Declaration;
import org.openvpms.insurance.service.GapInsuranceService;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.StaticDocumentTemplateLocator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.mail.MailDialogFactory;
import org.openvpms.web.component.mail.MailEditor;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.print.DefaultBatchPrinter;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.patient.insurance.CancelClaimDialog;
import org.openvpms.web.workspace.workflow.payment.PaymentWorkflow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.openvpms.component.model.bean.Policies.active;

/**
 * Submits insurance claims.
 *
 * @author Tim Anderson
 */
public class ClaimSubmitter {

    /**
     * The context.
     */
    private final Context context;


    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The insurance services.
     */
    private final InsuranceServices insuranceServices;

    /**
     * The insurance factory.
     */
    private final InsuranceFactory factory;

    /**
     * Mail button id.
     */
    private static final String MAIL_ID = "button.mail";

    /**
     * Accept declaration button id.
     */
    private static final String ACCEPT_ID = "button.accept";

    /**
     * Decline declaration button it.
     */
    private static final String DECLINE_ID = "button.decline";


    /**
     * Constructs a {@link ClaimSubmitter}.
     *
     * @param service           the archetype service
     * @param factory           the insurance factory
     * @param insuranceServices the insurance services
     * @param context           the context
     * @param help              the help context
     */
    public ClaimSubmitter(IArchetypeService service, InsuranceFactory factory, InsuranceServices insuranceServices,
                          Context context, HelpContext help) {
        this.context = context;
        this.help = help;
        this.service = service;
        this.factory = factory;
        this.insuranceServices = insuranceServices;
    }

    /**
     * Returns a claim for a claim act.
     *
     * @param act the claim act
     * @return the claim
     */
    public Claim getClaim(Act act) {
        return factory.createClaim(act);
    }

    /**
     * Submits a claim being edited.
     *
     * @param editor   the claim editor
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    public void submit(ClaimEditor editor, Consumer<Throwable> listener) {
        try {
            if (!Claim.Status.PENDING.isA(editor.getObject().getStatus())) {
                throw new IllegalStateException("Claim must have PENDING status");
            }
            ClaimState state = prepare(editor);
            if (state == null) {
                listener.accept(null);
            } else {
                String title = Messages.get("patient.insurance.submit.title");
                ClaimValidationStatus status = state.getStatus();
                if (status != null && status.getStatus() == ClaimValidationStatus.Status.ERROR) {
                    ErrorHelper.show(title, status.getMessage(), new WindowPaneListener() {
                        @Override
                        public void onClose(WindowPaneEvent event) {
                            listener.accept(null);
                        }
                    });
                } else if (status != null && status.getStatus() == ClaimValidationStatus.Status.WARNING) {
                    String message = Messages.format("patient.insurance.submit.warning", status.getMessage());
                    ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO,
                                            new PopupDialogListener() {
                                                @Override
                                                public void onYes() {
                                                    submit(state, title, listener);
                                                }

                                                @Override
                                                public void onNo() {
                                                    listener.accept(null);
                                                }
                                            });
                } else {
                    submit(state, title, listener);
                }
            }
        } catch (Throwable exception) {
            listener.accept(exception);
        }
    }

    /**
     * Submits a finalised claim.
     * <p>
     * NOTE: this does not make any checks to determine if a gap claim can/can't be submitted, as by this stage
     * it is too late to change a gap to non-gap claim.
     *
     * @param act      the claim act
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    public void submit(Act act, Consumer<Throwable> listener) {
        if (!Claim.Status.POSTED.isA(act.getStatus())) {
            throw new IllegalStateException("Claim must have POSTED status");
        }
        if (verifyNoDuplicates(act)) {
            Claim claim = factory.createClaim(act);
            Party insurer = claim.getPolicy().getInsurer();
            String title = Messages.get("patient.insurance.submit.title");
            if (insuranceServices.canSubmit(insurer)) {
                InsuranceService service = insuranceServices.getService(insurer);
                String message = Messages.format("patient.insurance.submit.online", insurer.getName(),
                                                 service.getName());
                ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        submitWithDeclaration(claim, service, listener);
                    }

                    @Override
                    public void onNo() {
                        listener.accept(null);
                    }
                });
            } else {
                String message = Messages.format("patient.insurance.submit.offline", insurer.getName());
                ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        runProtected(listener, () -> claim.setStatus(Claim.Status.SUBMITTED));
                    }

                    @Override
                    public void onNo() {
                        listener.accept(null);
                    }
                });
            }
        } else {
            listener.accept(null);
        }
    }

    /**
     * Pays a gap claim.
     * <p>
     * If the claim hasn't been accepted, or no benefit amount has been received, this gives the user the option to wait
     * for it.
     *
     * @param act      the claim
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    public void pay(Act act, Consumer<Throwable> listener) {
        Claim claim = getClaim(act);
        if (!(claim instanceof GapClaimImpl)) {
            throw new IllegalArgumentException("Argument 'claim' is not a GapClaim");
        }
        GapClaimImpl gapClaim = (GapClaimImpl) claim;
        Claim.Status status = gapClaim.getStatus();
        GapClaim.GapStatus gapStatus = gapClaim.getGapStatus();
        if ((status == Claim.Status.SUBMITTED || status == Claim.Status.ACCEPTED)
            && gapStatus == GapClaim.GapStatus.PENDING) {
            waitForBenefit(gapClaim, listener);
        } else if (status == Claim.Status.ACCEPTED) {
            if (gapStatus == GapClaim.GapStatus.RECEIVED) {
                promptToPayClaim(gapClaim, false, listener);
            } else if (gapStatus == GapClaim.GapStatus.PAID) {
                notifyInsurerOfPayment(gapClaim, listener);
            } else {
                listener.accept(null);
            }
        }
    }

    /**
     * Prints a claim.
     *
     * @param act      the claim act
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    public void print(Act act, Consumer<Throwable> listener) {
        Claim claim = getClaim(act);
        print(act, claim, listener);
    }

    /**
     * Cancels a claim.
     *
     * @param act      the claim act
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    public void cancel(Act act, Consumer<Throwable> listener) {
        Claim claim = getClaim(act);
        Party insurer = claim.getPolicy().getInsurer();
        String title = Messages.get("patient.insurance.cancel.title");
        if (insuranceServices.canSubmit(insurer)) {
            InsuranceService service = getInsuranceService(insurer);
            if (service.canCancel(claim)) {
                String message = Messages.format("patient.insurance.cancel.online", service.getName());
                CancelClaimDialog dialog = new CancelClaimDialog(title, message, help);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        runProtected(listener, () -> service.cancel(claim, dialog.getReason()));
                    }
                });
                dialog.show();
            } else {
                InformationDialog.show(title, Messages.format("patient.insurance.cancel.unsupported",
                                                              service.getName()), new WindowPaneListener() {
                    @Override
                    public void onClose(WindowPaneEvent event) {
                        listener.accept(null);
                    }
                });
            }
        } else {
            String message = Messages.format("patient.insurance.cancel.offline", insurer.getName());
            CancelClaimDialog dialog = new CancelClaimDialog(title, message, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onYes() {
                    runProtected(listener, () -> claim.setStatus(Claim.Status.CANCELLED, dialog.getReason()));
                }

                @Override
                public void onNo() {
                    listener.accept(null);
                }
            });
            dialog.show();
        }
    }

    /**
     * Settles a claim.
     *
     * @param act      the claim act
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    public void settle(Act act, Consumer<Throwable> listener) {
        Claim claim = getClaim(act);
        Party insurer = claim.getPolicy().getInsurer();
        String title = Messages.get("patient.insurance.settle.title");
        if (insuranceServices.canSubmit(insurer)) {
            InformationDialog.show(title, Messages.format("patient.insurance.settle.online", insurer.getName()));
            listener.accept(null);
        } else {
            ConfirmationDialog.show(title, Messages.format("patient.insurance.settle.offline", insurer.getName()),
                                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            runProtected(listener, () -> claim.setStatus(Claim.Status.SETTLED));
                        }

                        @Override
                        public void onNo() {
                            listener.accept(null);
                        }
                    });
        }
    }

    /**
     * Declines a claim.
     *
     * @param act      the claim act
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    public void decline(Act act, Consumer<Throwable> listener) {
        Claim claim = getClaim(act);
        Party insurer = claim.getPolicy().getInsurer();
        String title = Messages.get("patient.insurance.decline.title");
        if (insuranceServices.canSubmit(insurer)) {
            InformationDialog.show(title, Messages.format("patient.insurance.decline.online", insurer.getName()));
            listener.accept(null);
        } else {
            ConfirmationDialog.show(title, Messages.format("patient.insurance.decline.offline", insurer.getName()),
                                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            runProtected(listener, () -> claim.setStatus(Claim.Status.DECLINED));
                        }

                        @Override
                        public void onNo() {
                            listener.accept(null);
                        }
                    });
        }
    }

    protected void submit(final ClaimState state, String title, final Consumer<Throwable> listener) {
        Claim claim = state.getClaim();
        Party insurer = claim.getPolicy().getInsurer();
        InsuranceService service = state.getService();
        if (service != null) {
            String message = Messages.format("patient.insurance.submit.online", insurer.getName(),
                                             service.getName());
            ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                @Override
                public void onYes() {
                    submitOnlineClaim(state, listener);
                }

                @Override
                public void onNo() {
                    listener.accept(null);
                }
            });
        } else {
            String message = Messages.format("patient.insurance.submit.offline", insurer.getName());
            ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                @Override
                public void onYes() {
                    submitOfflineClaim(state, listener);
                }

                @Override
                public void onNo() {
                    listener.accept(null);
                }
            });
        }
    }

    /**
     * Prints a claim.
     *
     * @param act      the claim act
     * @param claim    the claim
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    protected void print(Act act, Claim claim, Consumer<Throwable> listener) {
        IMObjectBean bean = service.getBean(act);
        List<IMObject> objects = new ArrayList<>();
        objects.add(act);
        int missingAttachment = 0;
        for (DocumentAct attachment : bean.getTargets("attachments", DocumentAct.class)) {
            if (attachment.getDocument() != null) {
                objects.add(attachment);
            } else {
                missingAttachment++;
                break;
            }
        }
        String message = null;
        if (missingAttachment != 0) {
            message = Messages.format("patient.insurance.print.noattachment", missingAttachment);
        }
        ClaimPrintDialog dialog = new ClaimPrintDialog(claim, message, objects, help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                List<IMObject> selected = dialog.getSelected();
                BatchPrinter printer = new ClaimBatchPrinter(claim, selected, listener);
                printer.print();
            }

            @Override
            public void onCancel() {
                listener.accept(null);
            }
        });
        dialog.show();
    }

    /**
     * Prepares a claim for finalisation.
     *
     * @param editor the claim editor
     * @return the claim state, or {@code null} if the claim cannot be prepared for finalisation
     * @throws InsuranceException for any error
     */
    protected ClaimState prepare(ClaimEditor editor) {
        ClaimState result = null;
        if (editor.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (verifyNoDuplicates(editor.getObject()) && editor.generateAttachments() && checkSubmission(editor)) {
                Claim claim = factory.createClaim(editor.getObject());
                Party insurer = claim.getPolicy().getInsurer();
                ClaimValidationStatus status = null;
                InsuranceService service = null;
                if (insuranceServices.canSubmit(insurer)) {
                    service = getInsuranceService(insurer);
                    status = service.validate(claim);
                }
                result = new ClaimState(editor.getObject(), claim, status, service);
            }
        } else {
            ErrorHelper.show(Messages.get("patient.insurance.submit.title"),
                             Messages.get("patient.insurance.noinvoice"));
        }
        return result;
    }

    /**
     * Determines if a claim can be submitted.
     * <p>
     * If the claim is not a gap claim, it can be submitted.<br/>
     * If the claim is a gap claim, it can only be submitted if the insurer is accepting gap claims at the current time.
     *
     * @param editor the claim editor
     * @return {@code true} if the claim can be submitted
     */
    protected boolean checkSubmission(ClaimEditor editor) {
        boolean gapClaim = editor.isGapClaim();
        boolean result;
        if (!gapClaim) {
            result = true;
        } else {
            GapClaimSubmitStatus status = new GapClaimSubmitStatus();
            result = status.check(editor.getInsurer(), editor.getGapClaimSubmitTimes(), true);
            if (!result) {
                InformationDialog.show(status.getMessage());
            }
        }
        return result;
    }

    /**
     * Creates a benefit dialog for a gap claim.
     *
     * @param claim the claim
     * @return a new {@link BenefitDialog}
     */
    protected BenefitDialog createBenefitDialog(GapClaimImpl claim) {
        return new BenefitDialog(claim, help.subtopic("benefit"));
    }

    /**
     * Checks a claim for duplicate invoices.
     * <p>
     * NOTE that it should not be possible to create claims with duplicates.
     *
     * @param claim the claim
     * @return {@code true} if the claim has no duplicates, {@code false} if it does
     */
    private boolean verifyNoDuplicates(Act claim) {
        boolean result = true;
        ClaimHelper helper = new ClaimHelper(service);
        IMObjectBean claimBean = service.getBean(claim);
        for (Act item : claimBean.getTargets("items", Act.class)) {
            IMObjectBean bean = service.getBean(item);
            for (Act charge : bean.getTargets("items", Act.class)) {
                Act otherClaim = helper.getClaim(charge, claim);
                if (otherClaim != null) {
                    result = false;
                    String error = Messages.format("patient.insurance.duplicatecharge", otherClaim.getId(),
                                                   DateFormatter.formatDate(otherClaim.getActivityStartTime(), false));
                    ErrorHelper.show(Messages.get("patient.insurance.submit.title"), error);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Notify the insurer that a claim has been fully or part paid.
     *
     * @param gapClaim the claim
     * @param listener the listener to notify on completion or failure
     */
    private void notifyInsurerOfPayment(GapClaimImpl gapClaim, Consumer<Throwable> listener) {
        runProtected(listener, () -> {
            Party insurer = gapClaim.getPolicy().getInsurer();
            GapInsuranceService service = (GapInsuranceService) getInsuranceService(insurer);
            service.notifyPayment(gapClaim);
        });
    }

    /**
     * Pay a claim.
     *
     * @param claim    the claim
     * @param amount   the amount to pay
     * @param paid     the amount already paid
     * @param listener the listener to notify, on completion or failure
     */
    private void payClaim(GapClaimImpl claim, BigDecimal amount, BigDecimal paid, Consumer<Throwable> listener) {
        if (MathRules.isZero(amount)) {
            // the claim has already been paid
            claim.fullyPaid();
            notifyInsurerOfPayment(claim, listener);
        } else {
            PaymentWorkflow workflow = new GapClaimPaymentWorkflow(amount, paid, claim, context, help);
            workflow.addTaskListener(new DefaultTaskListener() {
                @Override
                public void taskEvent(TaskEvent event) {
                    if (event.getTask() == workflow) {
                        // reload the claim, as it may have been updated externally while payment was being made
                        GapClaimImpl reloaded = claim.reload();
                        GapClaim.GapStatus status = reloaded.getGapStatus();
                        if (status == GapClaim.GapStatus.PAID) {
                            notifyInsurerOfPayment(reloaded, listener);
                        } else {
                            listener.accept(null);
                        }
                    }
                }
            });
            workflow.start();
        }
    }

    /**
     * Creates a {@link MailDialog} to email a claim.
     *
     * @param claim the claim
     * @param list  the attachments
     * @return a new dialog
     */
    private MailDialog mail(Claim claim, List<IMObject> list) {
        HelpContext email = help.subtopic("email");
        MailContext mailContext = createMailContext(claim);
        MailDialogFactory factory = ServiceHelper.getBean(MailDialogFactory.class);
        ReporterFactory reporterFactory = ServiceHelper.getBean(ReporterFactory.class);
        MailDialog dialog = factory.create(mailContext, new DefaultLayoutContext(context, email));
        MailEditor editor = dialog.getMailEditor();
        for (IMObject object : list) {
            if (TypeHelper.isA(object, InsuranceArchetypes.ATTACHMENT)) {
                DocumentAct act = (DocumentAct) object;
                Document document = (Document) IMObjectHelper.getObject(act.getDocument());
                if (document != null) {
                    editor.addAttachment(document);
                }
            } else {
                DocumentTemplateLocator locator = getDocumentTemplateLocator(claim, object);
                Reporter<IMObject> reporter = reporterFactory.create(object, locator, Reporter.class);
                reporter.setFields(ReportContextFactory.create(context));
                Document document = reporter.getDocument(Reporter.DEFAULT_MIME_TYPE, true);
                editor.addAttachment(document);
            }
        }
        return dialog;
    }

    /**
     * Returns a document template locator for an object.
     *
     * @param claim  the claim
     * @param object the object
     * @return a new template locator
     */
    private DocumentTemplateLocator getDocumentTemplateLocator(Claim claim, IMObject object) {
        if (TypeHelper.isA(object, InsuranceArchetypes.CLAIM)) {
            Party supplier = claim.getPolicy().getInsurer();
            IMObjectBean bean = service.getBean(supplier);
            Entity template = bean.getTarget("template", Entity.class, active());
            if (template != null) {
                return new StaticDocumentTemplateLocator(new DocumentTemplate(template, service));
            }
        }
        return new ContextDocumentTemplateLocator(object, context);
    }

    /**
     * Submits an offline claim.
     *
     * @param state    the claim state
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, the argument will be {@code null}
     */
    private void submitOfflineClaim(ClaimState state, Consumer<Throwable> listener) {
        runProtected(listener, false, () -> {
            Claim claim = state.getClaim();
            claim.finalise();
            claim.setStatus(Claim.Status.SUBMITTED);
            print(state.getAct(), claim, listener);
        });
    }

    /**
     * Submits an online claim.
     *
     * @param state    the claim state
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, or was cancelled by the user, the argument will
     *                 be {@code null}
     */
    private void submitOnlineClaim(ClaimState state, Consumer<Throwable> listener) {
        runProtected(listener, false, () -> {
            Claim claim = state.getClaim();
            claim.finalise();
            submitWithDeclaration(claim, state.getService(), listener);
        });
    }

    /**
     * Submits a claim to an {@link InsuranceService}, after accepting a declaration if required.
     *
     * @param claim    the claim to submit
     * @param service  the service to submit to
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, the argument will be {@code null}
     */
    private void submitWithDeclaration(Claim claim, InsuranceService service, Consumer<Throwable> listener) {
        runProtected(listener, false, () -> {
            Declaration declaration = service.getDeclaration(claim);
            if (declaration != null) {
                String text = declaration.getText();
                ConfirmationDialog.show(Messages.get("patient.insurance.declaration.title"), text,
                                        new String[]{ACCEPT_ID, DECLINE_ID}, new PopupDialogListener() {
                            @Override
                            public void onAction(String action) {
                                if (ACCEPT_ID.equals(action)) {
                                    if (claim instanceof GapClaim) {
                                        submitGapClaim((GapClaimImpl) claim, (GapInsuranceService) service, declaration,
                                                       listener);
                                    } else {
                                        runProtected(listener, () -> service.submit(claim, declaration));
                                    }
                                } else {
                                    listener.accept(null);
                                }
                            }
                        });
            } else {
                if (claim instanceof GapClaimImpl) {
                    submitGapClaim((GapClaimImpl) claim, (GapInsuranceService) service, null, listener);
                } else {
                    service.submit(claim, null);
                    listener.accept(null);
                }
            }
        });
    }

    /**
     * Submits a gap claim.
     *
     * @param claim       the claim to submit
     * @param service     the service to submit to
     * @param declaration the declaration
     * @param listener    the listener to notify on completion. If the operation fails, the exception will be passed as
     *                    the argument. If the operation is successful, the argument will be {@code null}
     */
    private void submitGapClaim(GapClaimImpl claim, GapInsuranceService service, Declaration declaration,
                                Consumer<Throwable> listener) {
        runProtected(listener, false, () -> {
            service.submit(claim, declaration);
            waitForBenefit(claim, listener);
        });
    }

    /**
     * Displays a dialog prompting to wait for a benefit to be received from the insurer.
     * <p>
     * If a benefit is received, it prompts the user to pay the gap or the full amount.
     *
     * @param claim    the claim
     * @param listener the listener to notify on completion. If the operation fails, the exception will be passed as
     *                 the argument. If the operation is successful, the argument will be {@code null}
     */
    private void waitForBenefit(GapClaimImpl claim, final Consumer<Throwable> listener) {
        BenefitDialog dialog = createBenefitDialog(claim);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                if (BenefitDialog.CANCEL_ID.equals(dialog.getAction())) {
                    listener.accept(null);
                } else {
                    GapClaimImpl currentClaim = dialog.getClaim();
                    Claim.Status status = currentClaim.getStatus();
                    if (status == Claim.Status.ACCEPTED) {
                        boolean payFull = BenefitDialog.PAY_FULL_CLAIM_ID.equals(dialog.getAction());
                        promptToPayClaim(currentClaim, payFull, listener);
                    } else if (status == Claim.Status.CANCELLING) {
                        info(Messages.get("patient.insurance.pay.title"),
                             Messages.get("patient.insurance.pay.cancelling"), listener);
                    } else if (status == Claim.Status.CANCELLED) {
                        info(Messages.get("patient.insurance.pay.title"),
                             Messages.get("patient.insurance.pay.cancelled"), listener);
                    } else if (status == Claim.Status.DECLINED) {
                        info(Messages.get("patient.insurance.pay.title"),
                             Messages.get("patient.insurance.pay.declined"), listener);
                    } else {
                        listener.accept(null);
                    }
                }
            }
        });
        dialog.show();
    }

    /**
     * Prompts the user to pay a gap claim.
     *
     * @param claim        the gap claim
     * @param payFullClaim if {@code true}, pre-select the full amount
     * @param listener     the listener to notify on completion. If the operation fails, the exception will be passed as
     *                     the argument. If the operation is successful, the argument will be {@code null}
     */
    private void promptToPayClaim(GapClaimImpl claim, boolean payFullClaim, Consumer<Throwable> listener) {
        GapPaymentPrompt dialog = new GapPaymentPrompt(claim, payFullClaim, help.subtopic("pay"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                payClaim(claim, dialog.getToPay(), dialog.getPaid(), listener);
            }

            @Override
            public void onCancel() {
                listener.accept(null);
            }
        });
        dialog.show();
    }

    /**
     * Helper to display a information dialog, notifying the listener when it closes.
     *
     * @param title    the dialog title
     * @param message  the message
     * @param listener the listener to notify
     */
    private void info(String title, String message, Consumer<Throwable> listener) {
        InformationDialog.show(title, message, new WindowPaneListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                listener.accept(null);
            }
        });
    }

    /**
     * Executes a {@code Runnable}.
     *
     * @param listener the listener to notify
     * @param runnable the {@code Runnable} to execute
     */
    private void runProtected(Consumer<Throwable> listener, Runnable runnable) {
        runProtected(listener, true, runnable);
    }

    /**
     * Executes a {@code Runnable}.
     *
     * @param listener        the listener to notify
     * @param notifyOnSuccess if {@code true}, notify the listener on success, otherwise only notify on failure
     * @param runnable        the {@code Runnable} to execute
     */
    private void runProtected(Consumer<Throwable> listener, boolean notifyOnSuccess, Runnable runnable) {
        try {
            runnable.run();
            if (notifyOnSuccess) {
                listener.accept(null);
            }
        } catch (Throwable exception) {
            listener.accept(exception);
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
     * Creates a mail context.
     *
     * @param claim the claim
     * @return a new mail context
     */
    private MailContext createMailContext(Claim claim) {
        Context local = new LocalContext(context);
        Party insurer = claim.getPolicy().getInsurer();
        local.setSupplier((org.openvpms.component.business.domain.im.party.Party) insurer);
        return new InsurerMailContext(local, help);
    }

    private class ClaimPrintDialog extends BatchPrintDialog {

        private final Claim claim;

        /**
         * Constructs a {@link ClaimPrintDialog}.
         *
         * @param claim   the claim
         * @param message the message to display. May be {@code null}
         * @param objects the objects to print. The boolean value indicates if the object should be selected by default
         * @param help    the help context
         */
        public ClaimPrintDialog(Claim claim, String message, List<IMObject> objects, HelpContext help) {
            super(Messages.get("printdialog.title"), message, new String[]{OK_ID, CANCEL_ID, MAIL_ID}, objects, help);
            this.claim = claim;
        }

        /**
         * Invoked when the mail button is pressed. Displays the selected documents in a mail editor.
         */
        protected void onMail() {
            List<IMObject> selected = getSelected();
            if (!selected.isEmpty()) {
                MailDialog mailer = mail(claim, selected);
                mailer.addWindowPaneListener(new WindowPaneListener() {
                    @Override
                    public void onClose(WindowPaneEvent event) {
                        if (MailDialog.SEND_ID.equals(mailer.getAction())) {
                            ClaimPrintDialog.this.close(MAIL_ID);
                        }
                    }
                });
                mailer.show();
            }
        }

        /**
         * Invoked when a button is pressed. This delegates to the appropriate
         * on*() method for the button if it is known, else sets the action to
         * the button identifier and closes the window.
         *
         * @param button the button identifier
         */
        @Override
        protected void onButton(String button) {
            if (MAIL_ID.equals(button)) {
                onMail();
            } else {
                super.onButton(button);
            }
        }
    }

    private class ClaimBatchPrinter extends DefaultBatchPrinter<IMObject> {

        private final Claim claim;

        private final Consumer<Throwable> listener;

        public ClaimBatchPrinter(Claim claim, List<IMObject> selected, Consumer<Throwable> listener) {
            super(selected, context, help);
            this.claim = claim;
            this.listener = listener;
        }

        /**
         * Creates a new document template locator to locate the template for the object being printed.
         *
         * @param object  the object to print
         * @param context the context
         * @return a new document template locator
         */
        @Override
        protected DocumentTemplateLocator createDocumentTemplateLocator(IMObject object, Context context) {
            return getDocumentTemplateLocator(claim, object);
        }

        /**
         * Invoked when printing completes.
         */
        @Override
        protected void completed() {
            listener.accept(null);
        }
    }

    private class GapClaimPaymentWorkflow extends PaymentWorkflow {

        /**
         * The amount already paid.
         */
        private final BigDecimal paid;

        /**
         * The claim.
         */
        private final GapClaimImpl claim;

        /**
         * Construct a {@link GapClaimPaymentWorkflow}.
         *
         * @param amount  the amount to pay
         * @param paid    the amount already paid
         * @param claim   the claim
         * @param context the context
         * @param help    the help context
         */
        public GapClaimPaymentWorkflow(BigDecimal amount, BigDecimal paid, GapClaimImpl claim, Context context,
                                       HelpContext help) {
            super(amount, false, context, help);
            this.paid = paid;
            this.claim = claim;
        }

        /**
         * Starts the task.
         *
         * @param context the task context
         */
        @Override
        public void start(TaskContext context) {
            super.start(context);
            PrintActTask print = new PrintActTask(CustomerAccountArchetypes.PAYMENT,
                                                  new CustomerMailContext(getContext())) {
                @Override
                protected void notifyPrintCancelled() {
                    notifySkipped();
                }
            };
            print.setRequired(false);
            print.setEnableSkip(false); // don't want a skip button. Cancel will act as skip
            addTask(print);
        }

        /**
         * Creates a payment task.
         *
         * @param amount the charge amount that triggered the payment workflow
         * @return a new payment task
         */
        @Override
        protected EditIMObjectTask createPaymentTask(BigDecimal amount) {
            return new GapPaymentEditTask(claim, amount, paid);
        }
    }

    private static class ClaimState {

        private final Act act;

        private final Claim claim;

        private final ClaimValidationStatus status;

        private final InsuranceService service;

        public ClaimState(Act act, Claim claim, ClaimValidationStatus status, InsuranceService service) {
            this.act = act;
            this.claim = claim;
            this.status = status;
            this.service = service;
        }

        public Act getAct() {
            return act;
        }

        public Claim getClaim() {
            return claim;
        }

        public ClaimValidationStatus getStatus() {
            return status;
        }

        public InsuranceService getService() {
            return service;
        }
    }
}

