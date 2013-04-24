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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import echopointng.KeyStrokes;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.focus.FocusCommand;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.component.util.Vetoable;
import org.openvpms.web.resource.util.Messages;

import java.util.Map;


/**
 * Dialog to send emails.
 *
 * @author Tim Anderson
 */
public class MailDialog extends PopupDialog {

    /**
     * Send button identifier.
     */
    public static final String SEND_ID = "send";

    /**
     * The mailer.
     */
    private final Mailer mailer;

    /**
     * The mail editor.
     */
    private final MailEditor editor;

    /**
     * The document browser. May be {@code null}
     */
    private final Browser<Act> documents;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Don't send button identifier.
     */
    private static final String DONT_SEND_ID = "dontSend";

    /**
     * Edit button identifier.
     */
    private static String EDIT_ID = "edit";

    /**
     * Attach button identifier.
     */
    private static final String ATTACH_ID = "attach";

    /**
     * Attach file button identifier.
     */
    private static final String ATTACH_FILE_ID = "attachFile";

    /**
     * The editor button identifiers.
     */
    private static final String[] SEND_ATTACH_ALL_CANCEL = {SEND_ID, ATTACH_ID, ATTACH_FILE_ID, CANCEL_ID};

    /**
     * The editor button identifiers.
     */
    private static final String[] SEND_ATTACH_FILE_CANCEL = {SEND_ID, ATTACH_FILE_ID, CANCEL_ID};

    /**
     * The cancel confirmation button identifiers.
     */
    private static final String[] EDIT_DONT_SEND = {EDIT_ID, DONT_SEND_ID};


    /**
     * Constructs a {@code MailDialog}.
     *
     * @param mailContext the mail context
     * @param context     the context
     * @param help        the help context
     */
    public MailDialog(MailContext mailContext, Context context, HelpContext help) {
        this(mailContext, (Contact) null, context, help);
    }

    /**
     * Constructs a {@code MailDialog}.
     *
     * @param mailContext the mail context
     * @param preferred   the preferred contact. May be {@code null}
     * @param context     the context
     * @param help        the help context
     */
    public MailDialog(MailContext mailContext, Contact preferred, Context context, HelpContext help) {
        this(mailContext, preferred, mailContext.createAttachmentBrowser(), context, help);
    }

    /**
     * Constructs a {@code MailDialog}.
     *
     * @param mailContext the mail context
     * @param documents   the document browser. May be {@code null}
     * @param context     the context
     * @param help        the help context
     */
    public MailDialog(MailContext mailContext, Browser<Act> documents, Context context, HelpContext help) {
        this(mailContext, null, documents, context, help);
    }

    /**
     * Constructs a {@code MailDialog}.
     *
     * @param mailContext the mail context
     * @param preferred   the preferred contact. May be {@code null}
     * @param documents   the document browser. May be {@code null}
     * @param context     context
     * @param help        the help context
     */
    public MailDialog(MailContext mailContext, Contact preferred, Browser<Act> documents, Context context,
                      HelpContext help) {
        this(Messages.get("mail.write"), mailContext, preferred, documents, context, help);
    }

    /**
     * Constructs a {@code MailDialog}.
     *
     * @param title       the window title
     * @param mailContext the mail context
     * @param preferred   the preferred contact to display. May be {@code null}
     * @param documents   the document browser. May be {@code null}
     * @param context     the context
     * @param help        the help context
     */
    public MailDialog(String title, MailContext mailContext, Contact preferred, Browser<Act> documents,
                      Context context, HelpContext help) {
        super(title, "MailDialog", documents != null ? SEND_ATTACH_ALL_CANCEL : SEND_ATTACH_FILE_CANCEL, help);
        setModal(true);
        setDefaultCloseAction(CANCEL_ID);
        this.mailer = new DefaultMailer();
        this.documents = documents;
        this.context = context;
        editor = new MailEditor(mailContext, preferred, context, help);
        Map<String, Object> variables = mailContext.getVariables();
        if (variables != null) {
            editor.declareVariables(variables);
        }

        getLayout().add(editor.getComponent());
        getFocusGroup().add(editor.getFocusGroup());
        setCancelListener(new VetoListener() {
            public void onVeto(Vetoable action) {
                onCancel(action);
            }
        });
        getButtons().addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_M, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMacro();
            }
        });
        editor.getFocusGroup().setFocus();
    }

    /**
     * Returns the mail editor.
     *
     * @return the mail editor
     */
    public MailEditor getMailEditor() {
        return editor;
    }

    /**
     * Invoked just prior to the dialog closing.
     */
    @Override
    protected void onClosing() {
        try {
            editor.dispose();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates the layout split pane.
     *
     * @return a new split pane
     */
    @Override
    protected SplitPane createSplitPane() {
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "PopupWindow.Layout");
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
        if (ATTACH_ID.equals(button)) {
            attach();
        } else if (ATTACH_FILE_ID.equals(button)) {
            attachFile();
        } else if (SEND_ID.equals(button)) {
            if (send()) {
                close(SEND_ID);
            }
        } else {
            super.onButton(button);
        }
    }

    /**
     * Attaches a document from the document browser.
     */
    private void attach() {
        final FocusCommand focus = new FocusCommand();
        final BrowserDialog<Act> dialog = new BrowserDialog<Act>(Messages.get("mail.attach.title"), documents,
                                                                 getHelpContext().subtopic("attach"));
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                focus.restore();
                if (BrowserDialog.OK_ID.equals(dialog.getAction())) {
                    DocumentAct selected = (DocumentAct) documents.getSelected();
                    if (selected != null) {
                        attachDocument(selected);
                    }
                }
            }
        });
        documents.query();
        dialog.show();
    }

    /**
     * Attaches a file.
     */
    private void attachFile() {
        final FocusCommand focus = new FocusCommand();
        UploadListener listener = new DocumentUploadListener() {
            protected void upload(Document document) {
                focus.restore();
                editor.addAttachment(document);
            }
        };
        UploadDialog dialog = new UploadDialog(listener, getHelpContext().subtopic("attachFile"));
        dialog.show();
    }

    /**
     * Sends the email.
     *
     * @return {@code true} if the mail was sent
     */
    private boolean send() {
        boolean result = false;
        try {
            Validator validator = new Validator();
            if (editor.validate(validator)) {
                mailer.setFrom(editor.getFrom());
                mailer.setFromName(editor.getFromName());
                mailer.setTo(editor.getTo());
                mailer.setSubject(editor.getSubject());
                mailer.setBody(editor.getMessage());
                for (IMObjectReference attachment : editor.getAttachments()) {
                    Document document = (Document) IMObjectHelper.getObject(attachment, context);
                    if (document != null) {
                        mailer.addAttachment(document);
                    }
                }
                mailer.send();
                result = true;
            } else {
                ValidationHelper.showError(Messages.get("mail.error.title"), validator, "mail.error.message", false);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    /**
     * Attaches the document associated with a document act.
     *
     * @param act the document act
     */
    private void attachDocument(DocumentAct act) {
        Document document = (Document) IMObjectHelper.getObject(act.getDocument(), context);
        if (document != null) {
            editor.addAttachment(document);
        } else {
            HelpContext help = getHelpContext();
            DocumentGenerator generator = new DocumentGenerator(act, context, help, new DocumentGenerator.Listener() {
                public void generated(Document document) {
                    editor.addAttachment(document);
                }
            });
            generator.generate();
        }
    }

    /**
     * Invoked when the 'cancel' button is pressed. This prompts for confirmation if the editor has a message body
     * or attachments.
     *
     * @param action the action to veto if cancel is selected
     */
    private void onCancel(final Vetoable action) {
        if (!editor.getAttachments().isEmpty() || !StringUtils.isEmpty(editor.getMessage())) {
            final ConfirmationDialog dialog = new ConfirmationDialog(Messages.get("mail.cancel.title"),
                                                                     Messages.get("mail.cancel.message"),
                                                                     EDIT_DONT_SEND);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent e) {
                    if (EDIT_ID.equals(dialog.getAction())) {
                        action.veto(true);
                    } else {
                        action.veto(false);
                    }
                }
            });
            dialog.show();
        } else {
            action.veto(false);
        }
    }

    /**
     * Displays the macros.
     */
    protected void onMacro() {
        MacroDialog dialog = new MacroDialog(context, getHelpContext());
        dialog.show();
    }

}
