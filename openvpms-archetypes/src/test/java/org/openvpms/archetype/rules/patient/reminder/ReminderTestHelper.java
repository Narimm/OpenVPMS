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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertNotNull;


/**
 * Unit test helper for reminders.
 *
 * @author Tim Anderson
 */
public class ReminderTestHelper extends TestHelper {

    /**
     * Creates and saves a new <em>entity.reminderType</em>.
     *
     * @param groups a list of <em>lookup.reminderGroup</em>
     * @return a new reminder
     */
    public static Entity createReminderType(Lookup... groups) {
        return createReminderType(1, DateUnits.MONTHS, groups);
    }

    /**
     * Creates and saves a new <em>entity.reminderType</em>.
     * <p>
     * The cancelInterval will be set to {@code 2 * defaultInterval}.
     *
     * @param defaultInterval the default reminder interval
     * @param defaultUnits    the default reminder interval units
     * @param groups          a list of <em>lookup.reminderGroup</em>
     * @return a new reminder
     */
    public static Entity createReminderType(int defaultInterval, DateUnits defaultUnits, Lookup... groups) {
        return createReminderType(defaultInterval, defaultUnits, 2 * defaultInterval, defaultUnits, groups);
    }

    /**
     * Creates and saves a new <em>entity.reminderType</em>.
     *
     * @param defaultInterval the default reminder interval
     * @param defaultUnits    the default reminder interval units
     * @param cancelInterval  the cancel interval
     * @param cancelUnits     the cancel interval units. May be <tt>null</tt> to indicate no cancel units
     * @param groups          a list of <em>lookup.reminderGroup</em>
     * @return a new reminder
     */
    public static Entity createReminderType(int defaultInterval, DateUnits defaultUnits, int cancelInterval,
                                            DateUnits cancelUnits, Lookup... groups) {
        Entity reminder = (Entity) create(ReminderArchetypes.REMINDER_TYPE);
        EntityBean bean = new EntityBean(reminder);
        bean.setValue("name", "XReminderType");
        bean.setValue("defaultInterval", defaultInterval);
        bean.setValue("defaultUnits", defaultUnits.toString());
        if (cancelUnits != null) {
            bean.setValue("cancelInterval", cancelInterval);
            bean.setValue("cancelUnits", cancelUnits.toString());
        }
        for (Lookup group : groups) {
            reminder.addClassification(group);
        }
        bean.save();
        return reminder;
    }

    /**
     * Creates and saves a reminder count, optionally associated with a template and rules.
     *
     * @param count    the reminder count
     * @param template the document template. May be {@code null}
     * @param rules    the rules
     * @return a new reminder count
     */
    public static Entity createReminderCount(int count, int overdueInterval, DateUnits overdueUnits, Entity template,
                                             Entity... rules) {
        Entity entity = (Entity) create(ReminderArchetypes.REMINDER_COUNT);
        IMObjectBean bean = new IMObjectBean(entity);
        bean.setValue("count", count);
        bean.setValue("interval", overdueInterval);
        bean.setValue("units", overdueUnits);
        if (template != null) {
            bean.addNodeTarget("template", template);
        }

        int sequence = 0;
        for (Entity rule : rules) {
            IMObjectRelationship relationship = bean.addNodeTarget("rules", rule);
            IMObjectBean relationshipBean = new IMObjectBean(relationship);
            relationshipBean.setValue("sequence", sequence++);
        }
        bean.save();
        return entity;
    }

    /**
     * Adds a reminder count to a reminder type.
     *
     * @param reminderType    the reminder type
     * @param count           the reminder count
     * @param overdueInterval the overdue interval
     * @param overdueUnits    the overdue interval units
     * @param template        the document template. May be {@code null}
     * @param rules           the rules
     * @return the reminder count
     */
    public static Entity addReminderCount(Entity reminderType, int count, int overdueInterval, DateUnits overdueUnits,
                                          Entity template, Entity... rules) {
        Entity reminderCount = createReminderCount(count, overdueInterval, overdueUnits, template, rules);
        IMObjectBean bean = new IMObjectBean(reminderType);
        bean.addNodeTarget("counts", reminderCount);
        bean.save();
        return reminderCount;
    }

    /**
     * Creates an <em>entity.reminderRule</em> for contacts.
     *
     * @return a new rule
     */
    public static Entity createContactRule() {
        return createRule(true, false, false, false, false, false, ReminderRule.SendTo.ANY);
    }

    /**
     * Creates an <em>entity.reminderRule</em> for email.
     *
     * @return a new rule
     */
    public static Entity createEmailRule() {
        return createRule(false, true, false, false, false, false, ReminderRule.SendTo.ANY);
    }

    /**
     * Creates an <em>entity.reminderRule</em> for SMS.
     *
     * @return a new rule
     */
    public static Entity createSMSRule() {
        return createRule(false, false, true, false, false, false, ReminderRule.SendTo.ANY);
    }

    /**
     * Creates an <em>entity.reminderRule</em> for print.
     *
     * @return a new rule
     */
    public static Entity createPrintRule() {
        return createRule(false, false, false, true, false, false, ReminderRule.SendTo.ANY);
    }

    /**
     * Creates an <em>entity.reminderRule</em> for export.
     *
     * @return a new rule
     */
    public static Entity createExportRule() {
        return createRule(false, false, false, false, true, false, ReminderRule.SendTo.ANY);
    }

    /**
     * Creates an <em>entity.reminderRule</em> for list.
     *
     * @return a new rule
     */
    public static Entity createListRule() {
        return createRule(false, false, false, false, false, true, ReminderRule.SendTo.ANY);
    }

    /**
     * Creates an <em>entity.reminderRule</em>.
     *
     * @param contact if {@code true} send the reminder to the customer's reminder contacts
     * @param email   if {@code true}, and the customer has the appropriate contact, email the reminder
     * @param sms     if {@code true}, and the customer has the appropriate contact, SMS the reminder
     * @param print   if {@code true}, and the customer has the appropriate contact, print the reminder
     * @param export  if {@code true}, export the reminder
     * @param list    if {@code true}, list the reminder
     * @param sendTo  determines how many contacts are required for the rule to be satisfied
     * @return a new rule
     */
    public static Entity createRule(boolean contact, boolean email, boolean sms, boolean print, boolean export,
                                    boolean list, ReminderRule.SendTo sendTo) {
        Entity rule = (Entity) create(ReminderArchetypes.REMINDER_RULE);
        IMObjectBean bean = new IMObjectBean(rule);
        bean.setValue("contact", contact);
        bean.setValue("email", email);
        bean.setValue("sms", sms);
        bean.setValue("print", print);
        bean.setValue("export", export);
        bean.setValue("list", list);
        bean.setValue("sendTo", sendTo.toString());
        bean.save();
        return rule;
    }

    /**
     * Creates and saves a new reminder, calculating the due date from the given date and reminder type.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param createdTime  the reminder created time
     * @return a new reminder
     */
    public static Act createReminder(Party patient, Entity reminderType, Date createdTime) {
        Act reminder = createReminder(patient, reminderType);
        ActBean bean = new ActBean(reminder);
        bean.setValue("createdTime", createdTime);
        ReminderRules rules = new ReminderRules(ArchetypeServiceHelper.getArchetypeService(),
                                                new PatientRules(null, ArchetypeServiceHelper.getArchetypeService(),
                                                                 LookupServiceHelper.getLookupService()));
        Date due = rules.calculateReminderDueDate(createdTime, reminderType);
        reminder.setActivityStartTime(due);
        reminder.setActivityEndTime(due);
        save(reminder);
        return reminder;
    }

    /**
     * Creates and saves a new reminder with the specified due date.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param dueDate      the due date
     * @return a new reminder
     */
    public static Act createReminderWithDueDate(Party patient, Entity reminderType, Date dueDate) {
        Act reminder = createReminder(patient, reminderType);
        reminder.setActivityStartTime(dueDate);
        reminder.setActivityEndTime(dueDate);
        save(reminder);
        return reminder;
    }

    /**
     * Creates a new reminder.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @return a new reminder
     */
    public static Act createReminder(Party patient, Entity reminderType) {
        Act act = (Act) create(ReminderArchetypes.REMINDER);
        ActBean bean = new ActBean(act);
        Date due = new Date();
        bean.setValue("startTime", due);
        bean.setValue("endTime", due);
        bean.setStatus(ActStatus.IN_PROGRESS);
        bean.setParticipant("participation.patient", patient);
        bean.setParticipant(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION, reminderType);
        return act;
    }

    /**
     * Creates and saves a new {@code IN_PROGRESS} reminder.
     *
     * @param dueDate      the reminder due date
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param items        the reminder items
     * @return a new reminder
     */
    public static Act createReminder(Date dueDate, Party patient, Entity reminderType, Act... items) {
        return createReminder(dueDate, patient, reminderType, ReminderStatus.IN_PROGRESS, items);
    }

    /**
     * Creates a and saves a new reminder.
     *
     * @param dueDate      the reminder due date
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param status       the status
     * @param items        the reminder items
     * @return a new reminder
     */
    public static Act createReminder(Date dueDate, Party patient, Entity reminderType, String status, Act... items) {
        Act act = createReminder(patient, reminderType);
        act.setActivityStartTime(dueDate);
        act.setActivityEndTime(dueDate);
        act.setStatus(status);
        List<Act> toSave = new ArrayList<>();
        toSave.add(act);
        if (items.length > 0) {
            ActBean bean = new ActBean(act);
            for (Act item : items) {
                bean.addNodeRelationship("items", item);
                toSave.add(item);
            }
        }
        save(toSave);
        return act;
    }

    /**
     * Creates an email reminder item.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @param status  the status
     * @param count   the reminder count
     * @return a new email reminder item
     */
    public static Act createEmailReminder(Date send, Date dueDate, String status, int count) {
        return createReminderItem(ReminderArchetypes.EMAIL_REMINDER, send, dueDate, status, count);
    }

    /**
     * Creates an SMS reminder item.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @param status  the status
     * @param count   the reminder count
     * @return a new SMS reminder item
     */
    public static Act createSMSReminder(Date send, Date dueDate, String status, int count) {
        return createReminderItem(ReminderArchetypes.SMS_REMINDER, send, dueDate, status, count);
    }

    /**
     * Creates a print reminder item.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @param status  the status
     * @param count   the reminder count
     * @return a new print reminder item
     */
    public static Act createPrintReminder(Date send, Date dueDate, String status, int count) {
        return createReminderItem(ReminderArchetypes.PRINT_REMINDER, send, dueDate, status, count);
    }

    /**
     * Creates an export reminder item.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @param status  the status
     * @param count   the reminder count
     * @return a new export reminder item
     */
    public static Act createExportReminder(Date send, Date dueDate, String status, int count) {
        return createReminderItem(ReminderArchetypes.EXPORT_REMINDER, send, dueDate, status, count);
    }

    /**
     * Creates a list reminder item.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @param status  the status
     * @param count   the reminder count
     * @return a new list reminder item
     */
    public static Act createListReminder(Date send, Date dueDate, String status, int count) {
        return createReminderItem(ReminderArchetypes.LIST_REMINDER, send, dueDate, status, count);
    }

    /**
     * Creates and saves a new <em>lookup.reminderGroup</em> classification
     * lookup.
     *
     * @return a new lookup
     */
    public static Lookup createReminderGroup() {
        Lookup group = (Lookup) create("lookup.reminderGroup");
        group.setCode("XREMINDERGROUP_" + Math.abs(new Random().nextInt()));
        save(group);
        return group;
    }

    /**
     * Helper to create <tt>count</tt> reminders of a given reminder type.
     *
     * @param count        the no. to create
     * @param reminderType the reminder type
     * @return the reminders
     */
    public static List<Act> createReminders(int count, Entity reminderType) {
        List<Act> result = new ArrayList<>();
        Date dueDate = new Date();

        for (int i = 0; i < count; ++i) {
            Party customer = createCustomer();
            Party patient = createPatient(customer);
            Act reminder = createReminderWithDueDate(patient, reminderType, dueDate);
            result.add(reminder);
        }
        return result;
    }

    /**
     * Creates an <em>entity.documentTemplate</em> with associated document.
     *
     * @return the new template
     */
    public static Entity createDocumentTemplate() {
        return createDocumentTemplate(null, null);
    }

    /**
     * Creates an <em>entity.documentTemplate</em> with associated document.
     *
     * @param emailTemplate if {@code true}, add an email template
     * @param smsTemplate   if {@code true} add an SMS template
     * @return the new template
     */
    public static Entity createDocumentTemplate(boolean emailTemplate, boolean smsTemplate) {
        Entity email = (emailTemplate) ? createEmailTemplate("subject", "text") : null;
        Entity sms = (smsTemplate) ? createSMSTemplate("TEXT", "some plain text") : null;
        return createDocumentTemplate(email, sms);
    }

    /**
     * Creates an <em>entity.documentTemplate</em> with associated document.
     *
     * @param emailTemplate the email template. May be {@code null}
     * @param smsTemplate   the SMS template. May be {@code null}
     * @return the new template
     */
    public static Entity createDocumentTemplate(Entity emailTemplate, Entity smsTemplate) {
        String file = "/vaccination first reminder.odt";
        String mimeType = "application/vnd.oasis.opendocument.text";
        InputStream stream = ReminderTestHelper.class.getResourceAsStream(file);
        assertNotNull(stream);

        DocumentHandlers handlers = new DocumentHandlers(ArchetypeServiceHelper.getArchetypeService());
        DocumentHandler handler = handlers.get(file, mimeType);
        Document document = handler.create(file, stream, mimeType, -1);

        Entity template = (Entity) TestHelper.create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        EntityBean bean = new EntityBean(template);
        bean.setValue("name", "XDocumentTemplate");
        bean.setValue("archetype", PatientArchetypes.DOCUMENT_FORM);
        bean.save();

        DocumentAct act = (DocumentAct) TestHelper.create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        act.setFileName(document.getName());
        act.setMimeType(document.getMimeType());

        act.setDescription(DescriptorHelper.getDisplayName(document));
        act.setDocument(document.getObjectReference());
        ActBean actBean = new ActBean(act);
        actBean.addNodeParticipation("template", template);

        String name = template.getName();
        if (name == null) {
            name = document.getName();
        }
        template.setName(name);
        if (emailTemplate != null) {
            bean.addNodeTarget("email", emailTemplate);
        }
        if (smsTemplate != null) {
            bean.addNodeTarget("sms", smsTemplate);
        }
        save(document, template, act);
        return template;
    }

    /**
     * Adds an email template to a document template.
     *
     * @param template      the document template
     * @param emailTemplate the SMS template
     */
    public static void addEmailTemplate(Entity template, Entity emailTemplate) {
        IMObjectBean bean = new IMObjectBean(template);
        bean.addNodeTarget("email", emailTemplate);
        bean.save();
    }

    /**
     * Adds an SMS template to a document template.
     *
     * @param template    the document template
     * @param smsTemplate the SMS template
     */
    public static void addSMSTemplate(Entity template, Entity smsTemplate) {
        IMObjectBean bean = new IMObjectBean(template);
        bean.addNodeTarget("sms", smsTemplate);
        bean.save();
    }

    /**
     * Helper to create a text <em>entity.documentTemplateEmailSystem</em>.
     *
     * @param subject the email subject
     * @param message the email message
     * @return a new template
     */
    public static Entity createEmailTemplate(String subject, String message) {
        Entity entity = (Entity) TestHelper.create(DocumentArchetypes.SYSTEM_EMAIL_TEMPLATE);
        IMObjectBean bean = new IMObjectBean(entity);
        bean.setValue("name", subject);
        bean.setValue("subject", subject);
        bean.setValue("contentType", "TEXT");
        bean.setValue("content", message);
        bean.save();
        return entity;
    }

    /**
     * Helper to create and save SMS template.
     *
     * @param type       the content type
     * @param expression the content
     * @return a new template
     */
    public static Entity createSMSTemplate(String type, String expression) {
        Entity template = (Entity) create(DocumentArchetypes.REMINDER_SMS_TEMPLATE);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("name", type + " template");
        bean.setValue("contentType", type);
        bean.setValue("content", expression);
        bean.save();
        return template;
    }

    /**
     * Helper to create and save a new alert type.
     *
     * @param name the alert name
     * @return a new alert type
     */
    public static Entity createAlertType(String name) {
        return createAlertType(name, null);
    }

    /**
     * Helper to create and save a new alert type.
     *
     * @param name the alert name
     * @param type the alert type code. May be {@code null}
     * @return a new alert type
     */
    public static Entity createAlertType(String name, String type) {
        return createAlertType(name, type, false);
    }

    /**
     * Helper to create and save a new alert type.
     *
     * @param name        the alert name
     * @param type        the alert type code. May be {@code null}
     * @param interactive if {@code true}, the alert is interactive
     * @return a new alert type
     */
    public static Entity createAlertType(String name, String type, boolean interactive) {
        Entity entity = (Entity) create(PatientArchetypes.ALERT_TYPE);
        entity.setName(name);
        IMObjectBean bean = new IMObjectBean(entity);
        bean.setValue("interactive", interactive);
        if (type != null) {
            entity.addClassification(TestHelper.getLookup("lookup.patientAlertType", type));
        }
        save(entity);
        return entity;
    }

    /**
     * Helper to create and save a new alert type.
     *
     * @param name        the alert name
     * @param type        the alert type code. May be {@code null}
     * @param duration    the alert duration
     * @param units       the alert duration units
     * @param interactive if {@code true}, the alert is interactive
     * @return a new alert type
     */
    public static Entity createAlertType(String name, String type, int duration, DateUnits units, boolean interactive) {
        Entity entity = createAlertType(name, type, interactive);
        IMObjectBean bean = new IMObjectBean(entity);
        bean.setValue("duration", duration);
        bean.setValue("durationUnits", units.toString());
        bean.save();
        return entity;
    }

    /**
     * Helper to create and save an <em>act.patientAlert</tt> for a patient.
     *
     * @param patient the patient
     * @return a new alert
     */
    public static Act createAlert(Party patient, Entity alertType) {
        Act act = (Act) create(PatientArchetypes.ALERT);
        ActBean bean = new ActBean(act);
        bean.setNodeParticipant("patient", patient);
        bean.setNodeParticipant("alertType", alertType);
        bean.save();
        return act;
    }

    /**
     * Creates a reminder item.
     *
     * @param shortName the reminder item short name
     * @param send      the send date
     * @param dueDate   the due date
     * @param status    the status
     * @param count     the reminder count
     * @return a new list reminder item
     */
    private static Act createReminderItem(String shortName, Date send, Date dueDate, String status, int count) {
        Act act = (Act) create(shortName);
        act.setActivityStartTime(send);
        act.setActivityEndTime(dueDate);
        act.setStatus(status);
        ActBean bean = new ActBean(act);
        bean.setValue("count", count);
        return act;
    }

}
