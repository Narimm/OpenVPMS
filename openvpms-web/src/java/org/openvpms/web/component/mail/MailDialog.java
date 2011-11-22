/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.mail;

import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.focus.FocusCommand;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.component.util.Vetoable;
import org.openvpms.web.resource.util.Messages;


/**
 * Dialog to send emails.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailDialog extends PopupDialog {

    /**
     * The mailer.
     */
    private final Mailer mailer;

    /**
     * The mail editor.
     */
    private final MailEditor editor;

    /**
     * The document browser. May be <tt>null</tt>
     */
    private final Browser<Act> documents;

    /**
     * Send button identifier.
     */
    private static final String SEND_ID = "send";

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
     * Constructs a <tt>MailDialog</tt>.
     *
     * @param context the mail context
     */
    public MailDialog(MailContext context) {
        this(context, (Contact) null);
    }

    /**
     * Constructs a <tt>MailDialog</tt>.
     *
     * @param context   the mail context
     * @param preferred the preferred contact. May be <tt>null</tt>
     */
    public MailDialog(MailContext context, Contact preferred) {
        this(context, preferred, context.createAttachmentBrowser());
    }

    /**
     * Constructs a <tt>MailDialog</tt>.
     *
     * @param context   the mail context
     * @param documents the document browser. May be <tt>null</tt>
     */
    public MailDialog(MailContext context, Browser<Act> documents) {
        this(context, null, documents);
    }

    /**
     * Constructs a <tt>MailDialog</tt>.
     *
     * @param context   the mail context
     * @param preferred the preferred contact. May be <tt>null</tt>
     * @param documents the document browser. May be <tt>null</tt>
     */
    public MailDialog(MailContext context, Contact preferred, Browser<Act> documents) {
        this(Messages.get("mail.write"), context, preferred, documents);
    }

    /**
     * Constructs a <tt>MailDialog</tt>.
     *
     * @param title     the window title
     * @param context   the mail context
     * @param preferred the preferred contact to display. May be <tt>null</tt>
     * @param documents the document browser. May be <tt>null</tt>
     */
    public MailDialog(String title, MailContext context, Contact preferred, Browser<Act> documents) {
        super(title, "MailDialog", documents != null ? SEND_ATTACH_ALL_CANCEL : SEND_ATTACH_FILE_CANCEL);
        setModal(true);
        setDefaultCloseAction(CANCEL_ID);
        this.mailer = new DefaultMailer();
        this.documents = documents;
        editor = new MailEditor(context.getFromAddresses(), context.getToAddresses(), preferred);
        getLayout().add(editor.getComponent());
        getFocusGroup().add(0, editor.getFocusGroup());
        setCancelListener(new VetoListener() {
            public void onVeto(Vetoable action) {
                onCancel(action);
            }
        });
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
                onClose();
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
        final BrowserDialog<Act> dialog = new BrowserDialog<Act>(Messages.get("mail.attach.title"), documents);
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
        UploadDialog dialog = new UploadDialog(listener);
        dialog.show();
    }

    /**
     * Sends the email.
     *
     * @return <tt>true</tt> if the mail was sent
     */
    private boolean send() {
        boolean result = false;
        try {
            if (editor.isValid()) {
                mailer.setFrom(editor.getFrom());
                mailer.setFromName(editor.getFromName());
                mailer.setTo(editor.getTo());
                mailer.setSubject(editor.getSubject());
                mailer.setBody(editor.getMessage());
                for (IMObjectReference attachment : editor.getAttachments()) {
                    Document document = (Document) IMObjectHelper.getObject(attachment);
                    if (document != null) {
                        mailer.addAttachment(document);
                    }
                }
                mailer.send();
                result = true;
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
        DocumentGenerator generator = new DocumentGenerator(act, new DocumentGenerator.Listener() {
            public void generated(Document document) {
                editor.addAttachment(document);
            }
        });
        generator.generate();
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

}
