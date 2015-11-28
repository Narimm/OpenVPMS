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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.service.SMSService;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.ReminderMissingDocTemplate;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.TemplateMissingSMSText;


/**
 * Sends reminders via SMS.
 *
 * @author Tim Anderson
 */
public class ReminderSMSProcessor extends AbstractReminderProcessor {

    /**
     * The SMS service.
     */
    private final SMSService service;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderSMSProcessor.class);

    /**
     * Constructs a {@link ReminderSMSProcessor}.
     *
     * @param service       the SMS service
     * @param groupTemplate the template for grouped reminders
     * @param context       the context
     */
    public ReminderSMSProcessor(SMSService service, DocumentTemplate groupTemplate, Context context) {
        super(groupTemplate, context);
        this.service = service;
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be {@code null}
     */
    protected void process(List<ReminderEvent> events, String shortName, DocumentTemplate documentTemplate) {
        ReminderEvent event = events.get(0);
        Contact contact = event.getContact();
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(documentTemplate, shortName, getContext());
        documentTemplate = locator.getTemplate();
        if (documentTemplate == null) {
            throw new ReportingException(ReminderMissingDocTemplate);
        }
        String text = documentTemplate.getSMS();
        if (StringUtils.isEmpty(text)) {
            throw new ReportingException(TemplateMissingSMSText);
        }
        String phoneNumber = SMSHelper.getPhone(contact);
        if (StringUtils.isEmpty(phoneNumber)) {
            Party customer = event.getCustomer();
            log.error("Contact has no phone number for customer=" + customer.getName() + " (" + customer.getId() + ")");
        } else {
            try {
                service.send(phoneNumber, text, event.getCustomer(), event.getPatient(), contact,
                             getContext().getLocation());
            } catch (Throwable exception) {
                throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
            }
        }
    }

}
