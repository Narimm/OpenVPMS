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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.component.system.common.query.Constraints.in;

/**
 * Tests the {@link ReminderExportBatchProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderExportBatchProcessorTestCase extends ArchetypeServiceTest {

    /**
     * The test practice.
     */
    private Party practice;

    /**
     * The test location.
     */
    private Party location;

    /**
     * The reminder types.
     */
    private ReminderTypes reminderTypes;

    /**
     * The reminder rules.
     */
    @Autowired
    private ReminderRules reminderRules;

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * The reminder configuration.
     */
    private ReminderConfiguration config;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        this.practice = TestHelper.getPractice();
        location = TestHelper.createLocation();
        IArchetypeService service = getArchetypeService();
        reminderTypes = new ReminderTypes(service);
        config = new ReminderConfiguration(create(ReminderArchetypes.CONFIGURATION), service);
    }

    /**
     * Tests export.
     */
    @Test
    public void testExport() {
        Contact contact1 = TestHelper.createLocationContact("103 Stafford Drive", "SALE", "VIC", "3085");
        Party customer1 = TestHelper.createCustomer("MS", "J", "Smith", contact1);
        Party patient1A = TestHelper.createPatient("Fido", customer1, true);
        Party patient1B = TestHelper.createPatient("Spot", customer1, true);
        Contact contact2 = TestHelper.createLocationContact("91 Smith Rd", "KONGWAK", "VIC", "3086");
        Party customer2 = TestHelper.createCustomer("MR", "K", "Aardvark", contact2);
        Party patient2A = TestHelper.createPatient("Fluffy", customer2, true);

        Entity reminderType1 = ReminderTestHelper.createReminderType();
        Entity reminderType2 = ReminderTestHelper.createReminderType();

        Date send = DateRules.getToday();
        Date due = DateRules.getTomorrow();

        // set up reminders for patient1A
        Act item1A1 = ReminderTestHelper.createExportReminder(send, due, ReminderItemStatus.PENDING, 0);
        Act item1A2 = ReminderTestHelper.createExportReminder(send, due, ReminderItemStatus.PENDING, 0);
        Act reminder1A1 = ReminderTestHelper.createReminder(due, patient1A, reminderType1, item1A1);
        Act reminder1A2 = ReminderTestHelper.createReminder(due, patient1A, reminderType2, item1A2);

        // set up reminders for patient1B
        Act item1B1 = ReminderTestHelper.createExportReminder(send, due, ReminderItemStatus.PENDING, 0);
        Act reminder1B1 = ReminderTestHelper.createReminder(due, patient1B, reminderType1, item1B1);

        // set up reminders for patient2A
        Act item2A1 = ReminderTestHelper.createExportReminder(send, due, ReminderItemStatus.PENDING, 0);
        Act reminder2A1 = ReminderTestHelper.createReminder(due, patient2A, reminderType2, item2A1);

        // limit the query to the items above
        ReminderItemQueryFactory factory = new ReminderItemQueryFactory(ReminderArchetypes.EXPORT_REMINDER) {
            @Override
            public ArchetypeQuery createQuery() {
                ArchetypeQuery query = super.createQuery();
                query.add(in("item.id", item1A1.getId(), item1A2.getId(), item1B1.getId(), item2A1.getId()));
                return query;
            }
        };

        List<ReminderEvent> exported = new ArrayList<>();
        ReminderItemQuerySource source = new ReminderItemQuerySource(factory, reminderTypes, config);
        ReminderExportProcessor processor = new ReminderExportProcessor(reminderTypes, reminderRules,
                                                                        patientRules, location, practice,
                                                                        getArchetypeService(), config,
                                                                        Mockito.mock(CommunicationLogger.class)) {
            @Override
            protected void export(List<ReminderEvent> reminders) {
                exported.addAll(reminders);
            }
        };
        ReminderExportBatchProcessor batchProcessor = new ReminderExportBatchProcessor(source, processor);
        batchProcessor.process();

        assertEquals(4, exported.size());

        // reminders are ordered on customer and patient
        check(exported.get(0), reminder2A1, item2A1, patient2A, customer2, contact2);
        check(exported.get(1), reminder1A1, item1A1, patient1A, customer1, contact1);
        check(exported.get(2), reminder1A2, item1A2, patient1A, customer1, contact1);
        check(exported.get(3), reminder1B1, item1B1, patient1B, customer1, contact1);
    }

    /**
     * Verifies that a reminder event matches that expected.
     *
     * @param event    the reminder event
     * @param reminder the reminder
     * @param item     the reminder item
     * @param patient  the patient
     * @param customer the customer
     * @param contact  the contact
     */
    private void check(ReminderEvent event, Act reminder, Act item, Party patient, Party customer, Contact contact) {
        assertEquals(event.getReminder(), reminder);
        assertEquals(event.getItem(), item);
        assertEquals(event.getPatient(), patient);
        assertEquals(event.getCustomer(), customer);
        assertEquals(event.getContact(), contact);
    }

}
