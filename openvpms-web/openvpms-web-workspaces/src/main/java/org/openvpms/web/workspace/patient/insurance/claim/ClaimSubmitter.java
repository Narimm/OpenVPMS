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

package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.Declaration;
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
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.insurance.CancelClaimDialog;

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
     * @param context the context
     * @param help    the help context
     */
    public ClaimSubmitter(Context context, HelpContext help) {
        this.context = context;
        this.help = help;
        factory = ServiceHelper.getBean(InsuranceFactory.class);
        insuranceServices = ServiceHelper.getBean(InsuranceServices.class);
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
                final Claim claim = state.getClaim();
                Party insurer = (Party) claim.getPolicy().getInsurer();
                String title = Messages.get("patient.insurance.submit.title");
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
        } catch (Throwable exception) {
            listener.accept(exception);
        }
    }

    /**
     * Submits a finalised claim.
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
        final Claim claim = factory.createClaim(act);
        Party insurer = (Party) claim.getPolicy().getInsurer();
        String title = Messages.get("patient.insurance.submit.title");
        if (insuranceServices.canSubmit(insurer)) {
            final InsuranceService service = insuranceServices.getService(insurer);
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
        Party insurer = (Party) claim.getPolicy().getInsurer();
        String title = Messages.get("patient.insurance.cancel.title");
        if (insuranceServices.canSubmit(insurer)) {
            InsuranceService service = getInsuranceService(insurer);
            if (service.canCancel(claim)) {
                String message = Messages.format("patient.insurance.cancel.online", service.getName());
                final CancelClaimDialog dialog = new CancelClaimDialog(title, message, help);
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
            final CancelClaimDialog dialog = new CancelClaimDialog(title, message, help);
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
        Party insurer = (Party) claim.getPolicy().getInsurer();
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
        Party insurer = (Party) claim.getPolicy().getInsurer();
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
        IMObjectBean bean = new IMObjectBean(act);
        final List<IMObject> objects = new ArrayList<>();
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
        final ClaimPrintDialog dialog = new ClaimPrintDialog(claim, message, objects, help);
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
     * @throws InsuranceException if the claim is invalid
     */
    protected ClaimState prepare(ClaimEditor editor) {
        ClaimState result = null;
        if (editor.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (editor.generateAttachments()) {
                Claim claim = factory.createClaim(editor.getObject());
                Party insurer = (Party) claim.getPolicy().getInsurer();
                InsuranceService service = null;
                if (insuranceServices.canSubmit(insurer)) {
                    service = getInsuranceService(insurer);
                    if (service.canValidateClaims()) {
                        service.validate(claim);
                    }
                }
                result = new ClaimState(editor.getObject(), claim, service);
            }
        } else {
            ErrorHelper.show(Messages.get("patient.insurance.submit.title"),
                             Messages.get("patient.insurance.noinvoice"));
        }
        return result;
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
            Party supplier = (Party) claim.getPolicy().getInsurer();
            IMObjectBean bean = new IMObjectBean(supplier);
            Entity template = bean.getTarget("template", Entity.class, active());
            if (template != null) {
                return new StaticDocumentTemplateLocator(new DocumentTemplate(template,
                                                                              ServiceHelper.getArchetypeService()));
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
     * @param claim   the claim to submit
     * @param service the service to submit to
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
                                runProtected(listener, () -> {
                                    if (ACCEPT_ID.equals(action)) {
                                        service.submit(claim, declaration);
                                    }
                                });
                            }
                        });
            } else {
                service.submit(claim, null);
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

    private MailContext createMailContext(Claim claim) {
        Context local = new LocalContext(context);
        Party insurer = (Party) claim.getPolicy().getInsurer();
        local.setSupplier(insurer);
        return new InsurerMailContext(local, help);

    }

    private static class ClaimState {

        private final Act act;

        private final Claim claim;

        private final InsuranceService service;

        public ClaimState(Act act, Claim claim, InsuranceService service) {
            this.act = act;
            this.claim = claim;
            this.service = service;
        }

        public Act getAct() {
            return act;
        }

        public Claim getClaim() {
            return claim;
        }

        public InsuranceService getService() {
            return service;
        }
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
                final MailDialog mailer = mail(claim, selected);
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
}

