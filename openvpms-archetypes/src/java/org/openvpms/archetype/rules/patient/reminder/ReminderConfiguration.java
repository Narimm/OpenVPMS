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
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.IMObject;
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
     * The period prior to a reminder due date to start sending email reminders.
     */
    private final Period emailPeriod;

    /**
     * The period after a email reminder item start time when it should no longer be sent.
     */
    private final Period emailCancelPeriod;

    /**
     * The period prior to a reminder due date to start sending SMS reminders.
     */
    private final Period smsPeriod;

    /**
     * The period after an SMS reminder item start time when it should no longer be sent.
     */
    private final Period smsCancelPeriod;

    /**
     * The period prior to a reminder due date to print reminders.
     */
    private final Period printPeriod;

    /**
     * The period after an print reminder item start time when it should no longer be printed.
     */
    private final Period printCancelPeriod;

    /**
     * The period prior to a reminder due date to export reminders.
     */
    private final Period exportPeriod;

    /**
     * The period after an export reminder item start time when it should no longer be exported.
     */
    private final Period exportCancelPeriod;

    /**
     * The period prior to a reminder due date to list reminders.
     */
    private final Period listPeriod;

    /**
     * The period after a list reminder item start time when it should no longer be listed.
     */
    private final Period listCancelPeriod;

    /**
     * Determines if reminders should be emailed as attachments.
     */
    private final boolean emailAttachments;

    /**
     * Constructs a {@link ReminderConfiguration}.
     *
     * @param config  the <em>entity.reminderConfigurationType</em>.
     * @param service the archetype service
     */
    public ReminderConfiguration(IMObject config, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(config, service);
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
     * Returns the period prior to a reminder due date to start sending email reminders.
     *
     * @return the email period
     */
    public Period getEmailPeriod() {
        return emailPeriod;
    }

    /**
     * Returns the period after a email reminder item start time when it should no longer be sent.
     *
     * @return the email cancel period
     */
    public Period getEmailCancelPeriod() {
        return emailCancelPeriod;
    }

    /**
     * Returns the start time for an email reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the email reminder item start time
     */
    public Date getEmailStartTime(Date dueDate) {
        return DateRules.minus(dueDate, getEmailPeriod());
    }

    /**
     * Returns the date after the specified start time, when an email reminder item should no longer be sent.
     *
     * @param startTime the start time
     * @return the cancel date
     */
    public Date getEmailCancelDate(Date startTime) {
        return DateRules.plus(startTime, getEmailCancelPeriod());
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
     * Returns the period after an SMS reminder item start time when it should no longer be sent.
     *
     * @return the SMS cancel period
     */
    public Period getSMSCancelPeriod() {
        return smsCancelPeriod;
    }

    /**
     * Returns the start time for an SMS reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the SMS reminder item start time
     */
    public Date getSMSStartTime(Date dueDate) {
        return DateRules.minus(dueDate, getSMSPeriod());
    }

    /**
     * Returns the date after the specified start time, when an SMS reminder item should no longer be sent.
     *
     * @param startTime the start time
     * @return the cancel date
     */
    public Date getSMSCancelDate(Date startTime) {
        return DateRules.plus(startTime, getSMSCancelPeriod());
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
     * Returns the period after a print reminder item start time when it should no longer be printed.
     *
     * @return the print cancel period
     */
    public Period getPrintCancelPeriod() {
        return printCancelPeriod;
    }

    /**
     * Returns the start time for a print reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the print reminder item start time
     */
    public Date getPrintStartTime(Date dueDate) {
        return DateRules.minus(dueDate, getPrintPeriod());
    }

    /**
     * Returns the date after the specified start time, when print reminder item should no longer be printed.
     *
     * @param startTime the start time
     * @return the cancel date
     */
    public Date getPrintCancelDate(Date startTime) {
        return DateRules.plus(startTime, getPrintCancelPeriod());
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
     * Returns the period after an export reminder item start time when it should no longer be exported.
     *
     * @return the export cancel period
     */
    public Period getExportCancelPeriod() {
        return exportCancelPeriod;
    }

    /**
     * Returns the start time for an export reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the export reminder item start time
     */
    public Date getExportStartTime(Date dueDate) {
        return DateRules.minus(dueDate, getExportPeriod());
    }

    /**
     * Returns the date after the specified start time, when an export reminder item should no longer be exported.
     *
     * @param startTime the start time
     * @return the cancel date
     */
    public Date getExportCancelDate(Date startTime) {
        return DateRules.plus(startTime, getExportCancelPeriod());
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
     * Returns the period after a list reminder item start time when it should no longer be listed.
     *
     * @return the list cancel period
     */
    public Period getListCancelPeriod() {
        return listCancelPeriod;
    }

    /**
     * Returns the start time for a list reminder item, <em>PRIOR</em> to the due date.
     *
     * @param dueDate the due date
     * @return the list reminder item start time
     */
    public Date getListStartTime(Date dueDate) {
        return DateRules.minus(dueDate, getListPeriod());
    }

    /**
     * Returns the date after the specified start time, when a list reminder item should no longer be listed.
     *
     * @param startTime the start time
     * @return the cancel date
     */
    public Date getListCancelDate(Date startTime) {
        return DateRules.plus(startTime, getListCancelPeriod());
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
     * Determines if reminders should be emailed as attachments.
     *
     * @return {@code true} if reminders should be emailed as attachments, {@code false} if they should be displayed
     * within the body of the email
     */
    public boolean getEmailAttachments() {
        return emailAttachments;
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
