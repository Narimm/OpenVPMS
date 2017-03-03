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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.joda.time.Period;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;

/**
 * Used to determine the periods when reminders should be sent.
 *
 * @author Tim Anderson
 */
public class ReminderConfiguration {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The configuration.
     */
    private final IMObjectBean bean;

    /**
     * The location to use when a customer doesn't have a practice location.
     */
    private Party location;

    /**
     * The period prior to a reminder due date to start sending email reminders.
     */
    private final Period emailPeriod;

    /**
     * The period after a email reminder item send date when it should no longer be sent.
     */
    private final Period emailCancelPeriod;

    /**
     * The period prior to a reminder due date to start sending SMS reminders.
     */
    private final Period smsPeriod;

    /**
     * The period after an SMS reminder item send date when it should no longer be sent.
     */
    private final Period smsCancelPeriod;

    /**
     * The period prior to a reminder due date to print reminders.
     */
    private final Period printPeriod;

    /**
     * The period after an print reminder item send date when it should no longer be printed.
     */
    private final Period printCancelPeriod;

    /**
     * The period prior to a reminder due date to export reminders.
     */
    private final Period exportPeriod;

    /**
     * The period after an export reminder item send date when it should no longer be exported.
     */
    private final Period exportCancelPeriod;

    /**
     * The period prior to a reminder due date to list reminders.
     */
    private final Period listPeriod;

    /**
     * The period after a list reminder item send date when it should no longer be listed.
     */
    private final Period listCancelPeriod;

    /**
     * Determines if reminders should be emailed as attachments.
     */
    private final boolean emailAttachments;

    /**
     * The customer grouped reminder template.
     */
    private DocumentTemplate customerTemplate;

    /**
     * The patient grouped reminder template.
     */
    private DocumentTemplate patientTemplate;

    /**
     * Constructs a {@link ReminderConfiguration}.
     *
     * @param config  the <em>entity.reminderConfigurationType</em>.
     * @param service the archetype service
     */
    public ReminderConfiguration(IMObject config, IArchetypeService service) {
        this.service = service;
        bean = new IMObjectBean(config, service);
        emailPeriod = getPeriod(bean, "email");
        emailCancelPeriod = getPeriod(bean, "emailCancel");
        smsPeriod = getPeriod(bean, "sms");
        smsCancelPeriod = getPeriod(bean, "smsCancel");
        printPeriod = getPeriod(bean, "print");
        printCancelPeriod = getPeriod(bean, "printCancel");
        exportPeriod = getPeriod(bean, "export");
        exportCancelPeriod = getPeriod(bean, "exportCancel");
        listPeriod = getPeriod(bean, "list");
        listCancelPeriod = getPeriod(bean, "listCancel");
        emailAttachments = bean.getBoolean("emailAttachments");
    }

    /**
     * Returns the location to use when a customer doesn't have a preferred practice location.
     *
     * @return the location. May be {@code null}
     */
    public Party getLocation() {
        if (location == null) {
            location = (Party) bean.getNodeTargetObject("location");
        }
        return location;
    }

    /**
     * Returns the period prior to a reminder due date to start sending email reminders.
     *
     * @return the email period
     */
    public Period getEmailPeriod() {
        return emailPeriod;
    }

    /**
     * Returns the period after a email reminder item send date when it should no longer be sent.
     *
     * @return the email cancel period
     */
    public Period getEmailCancelPeriod() {
        return emailCancelPeriod;
    }

    /**
     * Returns the send date for an email reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the email reminder item send date
     */
    public Date getEmailSendDate(Date dueDate) {
        return DateRules.minus(dueDate, getEmailPeriod());
    }

    /**
     * Returns the date after the specified send date, when an email reminder item should no longer be sent.
     *
     * @param sendDate the send date
     * @return the cancel date
     */
    public Date getEmailCancelDate(Date sendDate) {
        return DateRules.plus(sendDate, getEmailCancelPeriod());
    }

    /**
     * Returns the period prior to a reminder due date to start sending SMS reminders.
     *
     * @return the SMS period
     */
    public Period getSMSPeriod() {
        return smsPeriod;
    }

    /**
     * Returns the period after an SMS reminder item send date when it should no longer be sent.
     *
     * @return the SMS cancel period
     */
    public Period getSMSCancelPeriod() {
        return smsCancelPeriod;
    }

    /**
     * Returns the send date for an SMS reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the SMS reminder item send date
     */
    public Date getSMSSendDate(Date dueDate) {
        return DateRules.minus(dueDate, getSMSPeriod());
    }

    /**
     * Returns the date after the specified send date, when an SMS reminder item should no longer be sent.
     *
     * @param sendDate the send date
     * @return the cancel date
     */
    public Date getSMSCancelDate(Date sendDate) {
        return DateRules.plus(sendDate, getSMSCancelPeriod());
    }

    /**
     * Returns the period prior to a reminder due date to print reminders.
     *
     * @return the print period
     */
    public Period getPrintPeriod() {
        return printPeriod;
    }

    /**
     * Returns the period after a print reminder item send date when it should no longer be printed.
     *
     * @return the print cancel period
     */
    public Period getPrintCancelPeriod() {
        return printCancelPeriod;
    }

    /**
     * Returns the send date for a print reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the print reminder item send date
     */
    public Date getPrintSendDate(Date dueDate) {
        return DateRules.minus(dueDate, getPrintPeriod());
    }

    /**
     * Returns the date after the specified send date, when print reminder item should no longer be printed.
     *
     * @param sendDate the send date
     * @return the cancel date
     */
    public Date getPrintCancelDate(Date sendDate) {
        return DateRules.plus(sendDate, getPrintCancelPeriod());
    }

    /**
     * Returns the period prior to a reminder due date to export reminders.
     *
     * @return the export period
     */
    public Period getExportPeriod() {
        return exportPeriod;
    }

    /**
     * Returns the period after an export reminder item send date when it should no longer be exported.
     *
     * @return the export cancel period
     */
    public Period getExportCancelPeriod() {
        return exportCancelPeriod;
    }

    /**
     * Returns the send date for an export reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the export reminder item send date
     */
    public Date getExportSendDate(Date dueDate) {
        return DateRules.minus(dueDate, getExportPeriod());
    }

    /**
     * Returns the date after the specified send date, when an export reminder item should no longer be exported.
     *
     * @param sendDate the send date
     * @return the cancel date
     */
    public Date getExportCancelDate(Date sendDate) {
        return DateRules.plus(sendDate, getExportCancelPeriod());
    }

    /**
     * Returns the period prior to a reminder due date to list reminders.
     *
     * @return the list period
     */
    public Period getListPeriod() {
        return listPeriod;
    }

    /**
     * Returns the period after a list reminder item send date when it should no longer be listed.
     *
     * @return the list cancel period
     */
    public Period getListCancelPeriod() {
        return listCancelPeriod;
    }

    /**
     * Returns the send date for a list reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the list reminder item send date
     */
    public Date getListSendDate(Date dueDate) {
        return DateRules.minus(dueDate, getListPeriod());
    }

    /**
     * Returns the date after the specified send date, when a list reminder item should no longer be listed.
     *
     * @param sendDate the send date
     * @return the cancel date
     */
    public Date getListCancelDate(Date sendDate) {
        return DateRules.plus(sendDate, getListCancelPeriod());
    }

    /**
     * Returns the maximum lead time required from the specified date to send email or SMS, print, export or list
     * reminders.
     *
     * @param date the date to start from
     * @return the maximum lead time
     */
    public Date getMaxLeadTime(Date date) {
        Date result = DateRules.plus(date, getEmailPeriod());
        result = DateRules.max(result, DateRules.plus(date, getSMSPeriod()));
        result = DateRules.max(result, DateRules.plus(date, getPrintPeriod()));
        result = DateRules.max(result, DateRules.plus(date, getExportPeriod()));
        result = DateRules.max(result, DateRules.plus(date, getListPeriod()));
        return result;
    }

    /**
     * Returns the send date for a reminder item.
     *
     * @param dueDate   the reminder due date
     * @param archetype the reminder item archetype
     * @return the send date, or {@code null} if the archetype is unknown
     */
    public Date getSendDate(Date dueDate, String archetype) {
        switch (archetype) {
            case ReminderArchetypes.EMAIL_REMINDER:
                return getEmailSendDate(dueDate);
            case ReminderArchetypes.SMS_REMINDER:
                return getSMSSendDate(dueDate);
            case ReminderArchetypes.PRINT_REMINDER:
                return getPrintSendDate(dueDate);
            case ReminderArchetypes.EXPORT_REMINDER:
                return getExportSendDate(dueDate);
            case ReminderArchetypes.LIST_REMINDER:
                return getListSendDate(dueDate);
        }
        return null;
    }

    /**
     * Returns the date after the specified send date, when a reminder item should no longer be sent.
     *
     * @param sendDate  the reminder item send date
     * @param archetype the reminder item archetype
     * @return the cancel date, or {@code null} if the archetype is unknown
     */
    public Date getCancelDate(Date sendDate, String archetype) {
        switch (archetype) {
            case ReminderArchetypes.EMAIL_REMINDER:
                return getEmailCancelDate(sendDate);
            case ReminderArchetypes.SMS_REMINDER:
                return getSMSCancelDate(sendDate);
            case ReminderArchetypes.PRINT_REMINDER:
                return getPrintCancelDate(sendDate);
            case ReminderArchetypes.EXPORT_REMINDER:
                return getExportCancelDate(sendDate);
            case ReminderArchetypes.LIST_REMINDER:
                return getListCancelDate(sendDate);
        }
        return null;
    }

    /**
     * Determines if reminders should be emailed as attachments.
     *
     * @return {@code true} if reminders should be emailed as attachments, {@code false} if they should be displayed
     * within the body of the email
     */
    public boolean getEmailAttachments() {
        return emailAttachments;
    }

    /**
     * Returns the customer grouped reminder template.
     *
     * @return the customer grouped reminder template, or {@code null} if none is defined
     */
    public DocumentTemplate getCustomerGroupedReminderTemplate() {
        if (customerTemplate == null) {
            Entity template = (Entity) bean.getNodeTargetObject("customerTemplate");
            if (template != null) {
                customerTemplate = new DocumentTemplate(template, service);
            }
        }
        return customerTemplate;
    }

    /**
     * Returns the patient grouped reminder template.
     *
     * @return the patient grouped reminder template, or {@code null} if none is defined
     */
    public DocumentTemplate getPatientGroupedReminderTemplate() {
        if (patientTemplate == null) {
            Entity template = (Entity) bean.getNodeTargetObject("patientTemplate");
            if (template != null) {
                patientTemplate = new DocumentTemplate(template, service);
            }
        }
        return patientTemplate;
    }

    /**
     * Returns the reminder grouping policy for reminder types that indicate {@link ReminderType.GroupBy#CUSTOMER}.
     *
     * @return the reminder grouping policy
     */
    public ReminderGroupingPolicy getGroupByCustomerPolicy() {
        return getPolicy(getCustomerGroupedReminderTemplate());
    }

    /**
     * Returns the reminder grouping policy for reminder types that indicate {@link ReminderType.GroupBy#PATIENT}.
     *
     * @return the reminder grouping policy
     */
    public ReminderGroupingPolicy getGroupByPatientPolicy() {
        return getPolicy(getPatientGroupedReminderTemplate());
    }

    /**
     * Returns the reminder grouping policy for a template.
     *
     * @param template the template
     * @return the reminder grouping policy
     */
    private ReminderGroupingPolicy getPolicy(DocumentTemplate template) {
        ReminderGroupingPolicy policy;
        if (template == null) {
            policy = ReminderGroupingPolicy.NONE;
        } else {
            boolean email = template.getEmailTemplate() != null;
            boolean sms = template.getSMSTemplate() != null;
            policy = ReminderGroupingPolicy.getPolicy(true, email, sms);
        }
        return policy;
    }

    /**
     * Helper to return a period.
     *
     * @param bean   the <em>entity.reminderConfigurationType</em> bean
     * @param prefix the node prefix
     * @return the period
     */
    private Period getPeriod(IMObjectBean bean, String prefix) {
        DateUnits units = DateUnits.fromString(bean.getString(prefix + "Units"), DateUnits.DAYS);
        return units.toPeriod(bean.getInt(prefix + "Interval"));
    }
}
