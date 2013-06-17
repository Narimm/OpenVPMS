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

package org.openvpms.web.component.im.sms;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.sms.Connection;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.mail.MailConnectionFactory;
import org.openvpms.sms.mail.MailMessage;
import org.openvpms.sms.mail.MailMessageFactory;
import org.openvpms.sms.mail.template.MailTemplate;
import org.openvpms.sms.mail.template.MailTemplateFactory;
import org.openvpms.sms.mail.template.TemplatedMailMessageFactory;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Renders a sample SMS mail message for a given <em>entity.SMSEmail*</em>.
 *
 * @author Tim Anderson
 */
class EmailSMSSampler extends AbstractModifiable {

    /**
     * The configuration.
     */
    private Entity config;

    /**
     * The SMS editor.
     */
    private SMSEditor sms;

    /**
     * The from address.
     */
    private TextField from;

    /**
     * The to address.
     */
    private TextField to;

    /**
     * The reply-to address.
     */
    private TextField replyTo;

    /**
     * The subject.
     */
    private TextField subject;

    /**
     * The text.
     */
    private TextArea text;

    /**
     * The mail template factory.
     */
    private MailTemplateFactory templateFactory;

    /**
     * Listeners for modification events.
     */
    private ModifiableListeners listeners = new ModifiableListeners();

    /**
     * The send SMS button.
     */
    private Button send;

    /**
     * Displays validation status.
     */
    private Label status;

    /**
     * Send SMS button identifier.
     */
    private static final String SEND_SMS_ID = "sms.send";


    /**
     * Constructs a <tt>EmailSMSSampler</tt>.
     *
     * @param config the configuration
     */
    public EmailSMSSampler(Entity config) {
        this.config = config;
        IMObjectBean bean = new IMObjectBean(config);
        sms = new SMSEditor();
        sms.setMessage(Messages.get("sms.sample.message"));
        sms.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                refresh();
            }
        });
        from = TextComponentFactory.create(40);
        from.setStyleName("edit");
        from.setEnabled(false);
        to = TextComponentFactory.create(40);
        to.setStyleName("edit");
        to.setEnabled(false);
        if (bean.hasNode("replyTo") || bean.hasNode("replyToExpression")) {
            replyTo = TextComponentFactory.create(40);
            replyTo.setStyleName("edit");
            replyTo.setEnabled(false);
        }
        if (bean.hasNode("subject") || bean.hasNode("subjectExpression")) {
            subject = TextComponentFactory.create(40);
            subject.setStyleName("edit");
            subject.setEnabled(false);
        }
        text = TextComponentFactory.createTextArea(40, 15);
        text.setStyleName("edit");
        text.setEnabled(false);
        status = LabelFactory.create(true);
        templateFactory = new MailTemplateFactory(ServiceHelper.getArchetypeService());
        send = ButtonFactory.create(SEND_SMS_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                if (isValid()) {
                    sendSMS();
                }
            }
        });
        refresh();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return sms.getFocusGroup();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <tt>true</tt> if the object has been modified
     */
    public boolean isModified() {
        return sms.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        sms.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        listeners.addListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        listeners.addListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component smsEdit = sms.getComponent();
        Column editCol = ColumnFactory.create("CellSpacing", LabelFactory.create("sms.title", "bold"), smsEdit,
                                              RowFactory.create(send));

        Grid resultGrid = GridFactory.create(2,
                                             LabelFactory.create("sms.mail.from"), from,
                                             LabelFactory.create("sms.mail.to"), to);
        if (replyTo != null) {
            resultGrid.add(LabelFactory.create("sms.mail.replyTo"));
            resultGrid.add(replyTo);
        }
        if (subject != null) {
            resultGrid.add(LabelFactory.create("sms.mail.subject"));
            resultGrid.add(subject);
        }
        resultGrid.add(LabelFactory.create("sms.mail.text"));
        resultGrid.add(text);
        Column resultCol = ColumnFactory.create("CellSpacing", LabelFactory.create("sms.email.title", "bold"),
                                                resultGrid);

        Column statusCol = ColumnFactory.create("CellSpacing", LabelFactory.create("sms.email.status.title", "bold"),
                                                status);

        // lay out the SMS editor, resulting email and status side-by-side, with all aligned in the top of the row
        Row row = RowFactory.create("WideCellSpacing", editCol, resultCol, statusCol);
        RowLayoutData layout = new RowLayoutData();
        layout.setAlignment(Alignment.ALIGN_TOP);
        editCol.setLayoutData(layout);
        resultCol.setLayoutData(layout);
        statusCol.setLayoutData(layout);

        // add a title and inset everything
        Label title = LabelFactory.create("sms.sample.title", "bold");
        ColumnLayoutData titleLayout = new ColumnLayoutData();
        titleLayout.setAlignment(Alignment.ALIGN_CENTER);
        title.setLayoutData(titleLayout);
        Column column = ColumnFactory.create("WideCellSpacing", title, row);
        return ColumnFactory.create("Inset", column);
    }

    /**
     * Sends an SMS, if the configuration and SMS are valid.
     */
    public void sendSMS() {
        if (isValid()) {
            MailTemplate template = templateFactory.getTemplate(config);
            MailMessageFactory factory = new TemplatedMailMessageFactory(template);
            MailConnectionFactory connectionFactory = new MailConnectionFactory(ServiceHelper.getMailSender(),
                                                                                factory);
            Connection connection = connectionFactory.createConnection();
            connection.send(sms.getPhone(), sms.getMessage());
        }
    }

    /**
     * Refreshes the display based on the updated configuration.
     */
    public void refresh() {
        String errorMessage = null;
        boolean valid = false;
        String fromStr = null;
        String toStr = null;
        String replyToStr = null;
        String subjectStr = null;
        String textStr = null;

        try {
            if (validateConfig() == null) {
                MailTemplate template = templateFactory.getTemplate(config);
                MailMessageFactory factory = new TemplatedMailMessageFactory(template);
                MailMessage mail = factory.createMessage(sms.getPhone(), sms.getMessage());
                fromStr = mail.getFrom();
                toStr = mail.getTo();
                replyToStr = mail.getReplyTo();
                textStr = mail.getText();
                subjectStr = mail.getSubject();
                valid = isValid();
            }
        } catch (SMSException exception) {
            errorMessage = exception.getI18nMessage().getMessage();
        } catch (Throwable exception) {
            errorMessage = exception.getLocalizedMessage();
        }
        from.setText(fromStr);
        to.setText(toStr);
        if (replyTo != null) {
            replyTo.setText(replyToStr);
        }
        if (subject != null) {
            subject.setText(subjectStr);
        }
        text.setText(textStr);
        send.setEnabled(valid);
        if (valid) {
            status.setText(Messages.get("sms.email.status.ok"));
        } else if (errorMessage != null) {
            status.setText(errorMessage);
        } else {
            status.setText(Messages.get("sms.email.status.incomplete"));
        }
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    protected boolean doValidation(Validator validator) {
        boolean result = false;
        List<ValidatorError> errors = validateConfig();
        if (errors != null) {
            validator.add(this, errors);
        } else if (validator.validate(sms)) {
            MailTemplate template = templateFactory.getTemplate(config);
            MailMessageFactory factory = new TemplatedMailMessageFactory(template);
            try {
                String phone = sms.getPhone();
                String message = sms.getMessage();
                MailMessage mail = factory.createMessage(phone, message);
                if (!StringUtils.isEmpty(mail.getFrom()) && !StringUtils.isEmpty(mail.getTo())
                    && !StringUtils.isEmpty(mail.getText())) {
                    result = true;
                }
            } catch (Throwable exception) {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Validates the configuration.
     *
     * @return validation errors, or <tt>null</tt> if there are none
     */
    private List<ValidatorError> validateConfig() {
        return ValidationHelper.validate(config, ServiceHelper.getArchetypeService());
    }

}
