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
import org.openvpms.archetype.rules.act.DefaultActCopyHandler;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.customer.CustomerMailContext;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
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
     * Constructs a {@code MessagingCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param help       the help context
     */
    public MessagingCRUDWindow(Archetypes<Act> archetypes, HelpContext help) {
        super(archetypes, DefaultIMObjectActions.<Act>getInstance(), help);
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
     * @return the mail context. May be {@code null}
     */
    @Override
    public MailContext getMailContext() {
        MailContext context = null;
        if (getObject() != null) {
            context = CustomerMailContext.create(getObject(), GlobalContext.getInstance(), getHelpContext());
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
        boolean user = TypeHelper.isA(getObject(), MessageArchetypes.USER);
        buttons.setEnabled(REPLY_ID, enable && user);
        buttons.setEnabled(FORWARD_ID, enable && user);
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
        Act reply = copyObject();
        LayoutContext layoutContext = createLayoutContext();
        UserMessageEditor editor = new UserMessageEditor(reply, null, layoutContext);
        Message message = new Message(getObject());

        editor.setTo(editor.getFrom());
        editor.setFrom(layoutContext.getContext().getUser());
        String subject = Messages.get("workflow.messaging.reply.subject", message.getSubject());
        String text = Messages.get("workflow.messaging.reply.body", message.getSent(), message.getFromName(),
                                   message.getMessage());
        editor.setSubject(subject);
        editor.setMessage(text);
        edit(editor, Messages.get("workflow.messaging.reply.title"));
    }

    /**
     * Invoked when the 'forward' button is pressed.
     */
    private void onForward() {
        Act forward = copyObject();
        LayoutContext layoutContext = createLayoutContext();
        UserMessageEditor editor = new UserMessageEditor(forward, null, layoutContext);
        Message message = new Message(getObject());

        String subject = Messages.get("workflow.messaging.forward.subject", message.getSubject());
        String text = Messages.get("workflow.messaging.forward.body", message.getSubject(), message.getSent(),
                                   message.getFromName(), message.getToName(), message.getMessage());
        editor.setTo(null);
        editor.setSubject(subject);
        editor.setMessage(text);
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

    private Act copyObject() {
        DefaultActCopyHandler handler = new DefaultActCopyHandler();
        handler.setCopy(Act.class, Participation.class);
        IMObjectCopier copier = new IMObjectCopier(handler);
        return (Act) copier.apply(getObject()).get(0);
    }

    /**
     * Helper to extract message properties.
     */
    private static class Message {

        /**
         * The act.
         */
        private final ActBean bean;

        /**
         * Constructs a {@code Message}.
         *
         * @param act the message act
         */
        public Message(Act act) {
            bean = new ActBean(act);
        }

        /**
         * Returns the 'from' user.
         *
         * @return the 'from' user. May be {@code null}
         */
        public User getFrom() {
            return (User) bean.getNodeParticipant("from");
        }

        /**
         * Returns the 'to' user.
         *
         * @return the 'to' user. May be {@code null}
         */
        public User getTo() {
            return (User) bean.getNodeParticipant("to");
        }

        /**
         * Returns the message subject.
         *
         * @return the message subject
         */
        public String getSubject() {
            return bean.getString("description", "");
        }

        /**
         * Returns the message body.
         *
         * @return the message body
         */
        public String getMessage() {
            return bean.getString("message", "");
        }

        /**
         * Returns the 'from' user name.
         *
         * @return the 'from' user name
         */
        public String getFromName() {
            User from = getFrom();
            return (from != null) ? from.getName() : "";
        }

        /**
         * Returns the 'to' user name.
         *
         * @return the 'to' user name
         */
        public String getToName() {
            User to = getTo();
            return (to != null) ? to.getName() : null;
        }

        /**
         * Returns the date when the message was sent.
         *
         * @return the date when the message was sent
         */
        public String getSent() {
            return DateHelper.formatDateTime(bean.getDate("startTime"), false);
        }
    }
}
