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
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
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
     * Send button identifier.
     */
    private static final String SEND_ID = "send";

    /**
     * Attach button identifier.
     */
    private static final String ATTACH_ID = "attach";

    /**
     * The button identifiers.
     */
    private static final String[] SEND_ATTACH_CANCEL = {SEND_ID, ATTACH_ID, CANCEL_ID};


    /**
     * Constructs a <tt>MailDialog</tt>.
     *
     * @param title     the window title
     * @param practice  the practice
     * @param addresses the available addresses to send to
     */
    public MailDialog(String title, Party practice, String[] addresses) {
        super(title, "MailDialog", SEND_ATTACH_CANCEL);
        setModal(true);
        setDefaultCloseAction(CANCEL_ID);
        if (practice == null) {
            throw new IllegalArgumentException("Argument 'practice' is null");
        }
        Contact email = getEmail(practice);
        String name = practice.getName();
        if (email == null) {
            throw new IllegalStateException("Practice " + name + " has no email contacts");
        }
        IMObjectBean bean = new IMObjectBean(email);
        String from = bean.getString("emailAddress");
        if (StringUtils.isEmpty(from)) {
            throw new IllegalStateException("Practice " + name + " email contact address is empty");
        }

        this.mailer = new DefaultMailer();
        editor = new MailEditor(addresses);
        editor.setFrom(from);
        getLayout().add(editor.getComponent());
        getFocusGroup().add(0, editor.getFocusGroup());
        setCancelListener(new VetoListener() {
            public void onVeto(Vetoable action) {
                onCancel(action);
            }
        });
    }

    /**
     * Processes a user request to close the window (via the close button).
     * <p/>
     * If there is an {@link #defaultCloseAction}, this will be invoked.
     */
    @Override
    public void userClose() {
        try {
            editor.dispose();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        super.userClose();
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
        } else if (SEND_ID.equals(button)) {
            if (send()) {
                onClose();
            }
        } else {
            super.onButton(button);
        }
    }

    /**
     * Attaches a document.
     */
    private void attach() {
        UploadListener listener = new DocumentUploadListener() {
            protected void upload(Document document) {
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
                mailer.setTo(editor.getAddress());
                mailer.setSubject(editor.getSubject());
                mailer.setBody(editor.getMessage());
                mailer.send();
                result = true;
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    /**
     * Invoked when the 'cancel' button is pressed. This prompts for confirmation if the editor has changed.
     *
     * @param action the action to veto if cancel is selected
     */
    private void onCancel(final Vetoable action) {
        final ConfirmationDialog dialog = new ConfirmationDialog(Messages.get("mail.cancel.title"),
                                                                 Messages.get("mail.cancel.message"));
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    action.veto(false);
                } else {
                    action.veto(true);
                }
            }
        });
        dialog.show();
    }

    /**
     * Returns an email address for the practice.
     *
     * @param practice the practice
     * @return an email contact, or <tt>null</tt> if none is configured
     */
    private Contact getEmail(Party practice) {
        return new PartyRules().getContact(practice, ContactArchetypes.EMAIL, null);
    }

}
