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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.component.business.domain.im.common.EntityRelationship;
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
     * <p/>
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
        Entity reminder = (Entity) create("entity.reminderType");
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
     * Adds a document template to a reminder type.
     *
     * @param reminderType    the reminder type
     * @param reminderCount   the reminder count
     * @param overdueInterval the overdue interval
     * @param overdueUnits    the overdue interval units
     */
    public static void addTemplate(Entity reminderType, int reminderCount, int overdueInterval,
                                   DateUnits overdueUnits) {
        Entity template = (Entity) create("entity.documentTemplate");
        EntityBean templateBean = new EntityBean(template);
        templateBean.setValue("name", "ZDummyTemplate-" + System.currentTimeMillis());
        templateBean.setValue("archetype", "act.patientDocumentForm");
        addTemplate(reminderType, template, reminderCount, overdueInterval, overdueUnits);
    }

    /**
     * Adds a document template to a reminder type.
     *
     * @param reminderType    the reminder type
     * @param template        the document template
     * @param reminderCount   the reminder count
     * @param overdueInterval the overdue interval
     * @param overdueUnits    the overdue interval units
     * @return the template relationship
     */
    public static EntityRelationship addTemplate(Entity reminderType, Entity template, int reminderCount,
                                                 int overdueInterval, DateUnits overdueUnits) {
        EntityBean bean = new EntityBean(reminderType);
        EntityRelationship relationship = bean.addRelationship("entityRelationship.reminderTypeTemplate", template);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("reminderCount", reminderCount);
        relBean.setValue("interval", overdueInterval);
        relBean.setValue("units", overdueUnits.toString());
        save(reminderType, template);
        return relationship;
    }

    /**
     * Creates and saves a new reminder, calculating the due date from the start time and reminder type.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param startTime    the start time
     * @return a new reminder
     */
    public static Act createReminder(Party patient, Entity reminderType, Date startTime) {
        Act reminder = createReminder(patient, reminderType);
        reminder.setActivityStartTime(startTime);
        ReminderRules rules = new ReminderRules(ArchetypeServiceHelper.getArchetypeService(),
                                                new PatientRules(ArchetypeServiceHelper.getArchetypeService(),
                                                                 LookupServiceHelper.getLookupService()));
        rules.calculateReminderDueDate(reminder);
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
        Act act = (Act) create("act.patientReminder");
        ActBean bean = new ActBean(act);
        bean.setStatus(ActStatus.IN_PROGRESS);
        bean.setParticipant("participation.patient", patient);
        bean.setParticipant(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION, reminderType);
        return act;
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
        List<Act> result = new ArrayList<Act>();
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
        String file = "/vaccination first reminder.odt";
        String mimeType = "application/vnd.oasis.opendocument.text";
        InputStream stream = ReminderTestHelper.class.getResourceAsStream(file);
        assertNotNull(stream);

        DocumentHandlers handlers = new DocumentHandlers();
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
        TestHelper.save(document, template, act);
        return template;
    }
}
