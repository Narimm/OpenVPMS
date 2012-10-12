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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.messaging;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.customer.CustomerMailContext;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Messaging CRUD window.
 *
 * @author Tim Anderson
 */
public class MessagingCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * Message archetypes that may be created by this workspace.
     */
    private static final Archetypes<Act> MESSAGES = Archetypes.create(MessageArchetypes.USER, Act.class);

    /**
     * The reply button identifier.
     */
    private static final String REPLY_ID = "reply";

    /**
     * The forward button identifier.
     */
    private static final String FORWARD_ID = "forward";

    /**
     * The completed button identifier.
     */
    private static final String COMPLETED_ID = "completed";


    /**
     * Constructs a <tt>MessagingCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public MessagingCRUDWindow(Archetypes<Act> archetypes) {
        super(archetypes, DefaultIMObjectActions.<Act>getInstance());
    }

    /**
     * Creates and edits a new object.
     * <p/>
     * This implementation restricts creation to <em>act.userMessage</em> objects.
     */
    @Override
    public void create() {
        onCreate(MESSAGES);
    }

    /**
     * Returns the mail context.
     *
     * @return the mail context. May be <tt>null</tt>
     */
    @Override
    public MailContext getMailContext() {
        MailContext context = null;
        if (getObject() != null) {
            context = CustomerMailContext.create(getObject(), GlobalContext.getInstance());
        }
        if (context == null) {
            context = super.getMailContext();
        }
        return context;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        Button reply = ButtonFactory.create(REPLY_ID, new ActionListener() {
            public void onAction(ActionEvent e) {
                onReply();
            }
        });
        Button forward = ButtonFactory.create(FORWARD_ID, new ActionListener() {
            public void onAction(ActionEvent e) {
                onForward();
            }
        });

        Button completed = ButtonFactory.create(COMPLETED_ID, new ActionListener() {
            public void onAction(ActionEvent e) {
                onCompleted();
            }
        });
        buttons.add(createNewButton());
        buttons.add(reply);
        buttons.add(forward);
        buttons.add(createDeleteButton());
        buttons.add(completed);
        buttons.add(createPrintButton());
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
        buttons.setEnabled(REPLY_ID, enable);
        buttons.setEnabled(FORWARD_ID, enable);
        buttons.setEnabled(COMPLETED_ID, enable);
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act object, boolean isNew) {
        onRefresh(object);
    }

    /**
     * Invoked when the 'reply' button is pressed.
     */
    private void onReply() {
        UserMessageEditor editor = new UserMessageEditor(getObject(), null, createLayoutContext());
        User from = editor.getFrom();
        String subject = editor.getSubject();
        String message = editor.getMessage();

        String date = DateHelper.formatDateTime(editor.getStartTime(), false);
        String fromName = (from != null) ? from.getName() : "";

        User newFrom = GlobalContext.getInstance().getUser();
        editor.setFrom(newFrom);
        editor.setTo(from);
        String newSubject = Messages.get("workflow.messaging.reply.subject", subject);
        String newMessage = Messages.get("workflow.messaging.reply.body", date, fromName, message);
        editor.setSubject(newSubject);
        editor.setMessage(newMessage);
        edit(editor, Messages.get("workflow.messaging.reply.title"));
    }

    /**
     * Invoked when the 'forward' button is pressed.
     */
    private void onForward() {
        UserMessageEditor editor = new UserMessageEditor(getObject(), null, createLayoutContext());
        User from = editor.getFrom();
        User to = editor.getTo();

        String subject = editor.getSubject();
        String message = editor.getMessage();
        String date = DateHelper.formatDateTime(editor.getStartTime(), false);
        String fromName = (from != null) ? from.getName() : "";
        String toName = (to != null) ? to.getName() : null;

        String newSubject = Messages.get("workflow.messaging.forward.subject", subject);
        String newMessage = Messages.get("workflow.messaging.forward.body", subject, date, fromName, toName, message);
        editor.setTo(null);
        editor.setSubject(newSubject);
        editor.setMessage(newMessage);
        edit(editor, Messages.get("workflow.messaging.forward.title"));
    }

    /**
     * Displays a dialog to perform editing.
     *
     * @param editor the editor
     * @param title  the dialog title
     */
    private void edit(IMObjectEditor editor, String title) {
        EditDialog dialog = edit(editor);
        if (dialog != null) {
            dialog.setTitle(title);
        }
    }

    /**
     * Invoked when the 'completed' button is pressed.
     */
    private void onCompleted() {
        Act act = getObject();
        if (!ActStatus.COMPLETED.equals(act.getStatus())) {
            act = IMObjectHelper.reload(act);
            if (act != null) {
                act.setStatus(ActStatus.COMPLETED);
                SaveHelper.save(act);
            }
            onRefresh(act);
        }
    }
}
