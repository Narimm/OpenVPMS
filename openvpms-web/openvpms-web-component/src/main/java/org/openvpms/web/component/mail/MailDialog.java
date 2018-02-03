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

package org.openvpms.web.component.mail;

import echopointng.KeyStrokeListener;
import echopointng.KeyStrokes;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.doc.DocumentGeneratorFactory;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.MultiSelectBrowser;
import org.openvpms.web.component.im.query.MultiSelectBrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.button.ShortcutButtons;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.Vetoable;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusCommand;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import static org.openvpms.archetype.rules.doc.DocumentArchetypes.USER_EMAIL_TEMPLATE;


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
     * The mail editor.
     */
    private final MailEditor editor;

    /**
     * The document browser. May be {@code null}
     */
    private final MultiSelectBrowser<Act> documents;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * Edit button identifier.
     */
    private static String EDIT_ID = "button.edit";

    /**
     * Template button identifier.
     */
    private static final String TEMPLATE_ID = "button.template";

    /**
     * Don't send button identifier.
     */
    private static final String DONT_SEND_ID = "button.dontSend";

    /**
     * Attach button identifier.
     */
    private static final String ATTACH_ID = "button.attach";

    /**
     * Attach file button identifier.
     */
    private static final String ATTACH_FILE_ID = "button.attachFile";

    /**
     * The editor button identifiers.
     */
    private static final String[] NEW_SEND_ATTACH_ALL_CANCEL = {SEND_ID, TEMPLATE_ID, ATTACH_ID, ATTACH_FILE_ID,
                                                                CANCEL_ID};

    /**
     * The editor button identifiers.
     */
    private static final String[] NEW_SEND_ATTACH_FILE_CANCEL = {SEND_ID, TEMPLATE_ID, ATTACH_FILE_ID, CANCEL_ID};

    /**
     * The cancel confirmation button identifiers.
     */
    private static final String[] EDIT_DONT_SEND = {EDIT_ID, DONT_SEND_ID};


    /**
     * Constructs a {@link MailDialog}.
     *
     * @param mailContext the mail context
     * @param preferred   the preferred contact. May be {@code null}
     * @param context     the layout context
     */
    public MailDialog(MailContext mailContext, Contact preferred, LayoutContext context) {
        this(mailContext, preferred, mailContext.createAttachmentBrowser(), context);
    }

    /**
     * Constructs a {@link MailDialog}.
     *
     * @param mailContext the mail context
     * @param preferred   the preferred contact. May be {@code null}
     * @param documents   the document browser. May be {@code null}
     * @param context     the layout context
     */
    public MailDialog(MailContext mailContext, Contact preferred, MultiSelectBrowser<Act> documents,
                      LayoutContext context) {
        this(Messages.get("mail.write"), mailContext, preferred, documents, context);
    }

    /**
     * Constructs a {@link MailDialog}.
     *
     * @param title       the window title
     * @param mailContext the mail context
     * @param preferred   the preferred contact to display. May be {@code null}
     * @param documents   the document browser. May be {@code null}
     * @param context     the layout context
     */
    public MailDialog(String title, MailContext mailContext, Contact preferred, MultiSelectBrowser<Act> documents,
                      LayoutContext context) {
        this(title, new MailEditor(mailContext, preferred, context), documents, context);
    }

    /**
     * Constructs a {@link MailDialog}.
     *
     * @param editor    the mail editor
     * @param documents the document browser. May be {@code null}
     * @param context   the layout context
     */
    public MailDialog(MailEditor editor, MultiSelectBrowser<Act> documents, LayoutContext context) {
        this(Messages.get("mail.write"), editor, documents, context);
    }

    /**
     * Constructs a {@link MailDialog}.
     *
     * @param title     the window title
     * @param editor    the mail editor
     * @param documents the document browser. May be {@code null}
     * @param context   the layout context
     */
    public MailDialog(String title, MailEditor editor, MultiSelectBrowser<Act> documents, LayoutContext context) {
        super(title, "MailDialog", documents != null ? NEW_SEND_ATTACH_ALL_CANCEL : NEW_SEND_ATTACH_FILE_CANCEL,
              context.getHelpContext());
        setModal(true);
        setDefaultCloseAction(CANCEL_ID);
        this.documents = documents;
        this.context = context;
        this.editor = editor;

        getLayout().add(editor.getComponent());
        getFocusGroup().add(editor.getFocusGroup());
        setCancelListener(this::onCancel);
        ButtonSet buttons = getButtons();
        buttons.addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_M, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMacro();
            }
        });
        registerShortcuts();
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
        if (TEMPLATE_ID.equals(button)) {
            newFromTemplate();
        } else if (ATTACH_ID.equals(button)) {
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
     * Registers any dialog keyboard shortcuts directly on the mail editor.
     * <p>
     * This is required as the events would otherwise be swallowed by the editor.
     */
    protected void registerShortcuts() {
        final ShortcutButtons buttons = getButtons().getButtons();
        KeyStrokeListener listener = editor.getKeyStrokeListener();
        KeyStrokeListener shortcutListener = buttons.getKeyStrokeListener();
        int[] keys = shortcutListener.getKeyCombinations();
        if (keys.length > 0) {
            for (int key : keys) {
                String command = shortcutListener.getKeyCombinationCommand(key);
                if (command != null) {
                    listener.addKeyCombination(key, command);
                } else {
                    listener.addKeyCombination(key);
                }
            }
            // register a listener to pass events through to the dialog
            listener.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    buttons.processInput(event);
                }
            });
        }
    }

    /**
     * Creates a new {@link Mailer}.
     *
     * @param editor the mail editor
     * @return a new mailer
     */
    protected Mailer createMailer(MailEditor editor) {
        return ServiceHelper.getBean(MailerFactory.class).create(editor.getMailContext());
    }

    /**
     * Sends the email.
     *
     * @param mailer the mailer
     */
    protected void send(Mailer mailer) {
        mailer.send();
    }

    /**
     * Populates a mailer from an editor.
     *
     * @param mailer the mailer
     * @param editor the editor
     */
    protected void populate(Mailer mailer, MailEditor editor) {
        mailer.setFrom(editor.getFrom());
        mailer.setTo(editor.getTo());
        mailer.setCc(editor.getCc());
        mailer.setBcc(editor.getBcc());
        mailer.setSubject(editor.getSubject());
        mailer.setBody(editor.getMessage());
        for (IMObjectReference attachment : editor.getAttachments()) {
            Document document = (Document) IMObjectHelper.getObject(attachment, context.getContext());
            if (document != null) {
                mailer.addAttachment(document);
            }
        }
    }

    /**
     * Displays a popup of available email templates.
     * <p>
     * If there is already text presents, displays a  warning before proceeding.
     */
    private void newFromTemplate() {
        if (!StringUtils.isBlank(editor.getMessage())) {
            ConfirmationDialog.show(Messages.get("mail.replace.title"), Messages.get("mail.replace.message"),
                                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            onTemplate();
                        }
                    });
        } else {
            onTemplate();
        }
    }

    /**
     * Displays a popup of available email templates to select from.
     */
    private void onTemplate() {
        Query<Entity> query = QueryFactory.create(USER_EMAIL_TEMPLATE, context.getContext());
        Browser<Entity> browser = BrowserFactory.create(query, context);
        String title = Messages.format("imobject.select.title", DescriptorHelper.getDisplayName(USER_EMAIL_TEMPLATE));
        final BrowserDialog<Entity> dialog = new BrowserDialog<>(title, browser, getHelpContext().subtopic("template"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                Entity selected = dialog.getSelected();
                if (selected != null) {
                    editor.setContent(selected, true);
                }
            }
        });
        dialog.show();
    }

    /**
     * Attaches a document from the document browser.
     */
    private void attach() {
        final FocusCommand focus = new FocusCommand();
        String title = Messages.get("mail.attach.title");
        HelpContext help = getHelpContext().subtopic("attach");
        MultiSelectBrowserDialog<Act> dialog = new MultiSelectBrowserDialog<>(title, documents, help);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                focus.restore();
                if (BrowserDialog.OK_ID.equals(dialog.getAction())) {
                    for (Act act : documents.getSelections()) {
                        attachDocument((DocumentAct) act);
                    }
                }
                documents.clearSelections();
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
            Validator validator = new DefaultValidator();
            if (editor.validate(validator)) {
                Mailer mailer = createMailer(editor);
                populate(mailer, editor);
                send(mailer);
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
        Document document = (Document) IMObjectHelper.getObject(act.getDocument(), context.getContext());
        if (document != null) {
            editor.addAttachment(document);
        } else {
            HelpContext help = getHelpContext();
            DocumentGenerator.AbstractListener listener = new DocumentGenerator.AbstractListener() {
                public void generated(Document document) {
                    editor.addAttachment(document);
                }
            };
            DocumentGeneratorFactory factory = ServiceHelper.getBean(DocumentGeneratorFactory.class);
            DocumentGenerator generator = factory.create(act, context.getContext(), help, listener);
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
    private void onMacro() {
        MacroDialog dialog = new MacroDialog(context.getContext(), getHelpContext());
        dialog.show();
    }

}
