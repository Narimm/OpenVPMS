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

package org.openvpms.archetype.rules.patient.reminder;

import au.com.bytecode.opencsv.CSVReader;
import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link ReminderCSVExporter} class.
 *
 * @author Tim Anderson
 */
public class ReminderCSVExporterTestCase extends ArchetypeServiceTest {

    @Test
    public void testExport() throws IOException {
        IArchetypeService service = getArchetypeService();
        PartyRules rules = new PartyRules(service);
        ILookupService lookups = LookupServiceHelper.getLookupService();
        DocumentHandlers handlers = new DocumentHandlers();

        ReminderCSVExporter exporter = new ReminderCSVExporter(rules, service, lookups, handlers);
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");
        bean.setValue("companyName", "Foo's Company");
        Contact contact = TestHelper.createLocationContact("Twenty Second Avenue", "SAWTELL", "NSW", "2452");
        customer.addContact(contact);
        bean.save();

        Party patient = TestHelper.createPatient(customer);
        ReminderType reminderType = new ReminderType(ReminderTestHelper.createReminderType(), service);
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType.getEntity());
        ReminderEvent event = new ReminderEvent(ReminderEvent.Action.EXPORT, reminder, reminderType, patient, customer,
                                                contact, null);
        Document document = exporter.export(Arrays.asList(event));
        InputStreamReader reader = new InputStreamReader(handlers.get(document).getContent(document));
        CSVReader csv = new CSVReader(reader);
        List<String[]> lines = csv.readAll();
        assertNotNull(lines);
        assertEquals(2, lines.size());
        assertArrayEquals(ReminderCSVExporter.HEADER, lines.get(0));
        String[] expected = {bean.getString("id"), "Mr", "Foo", "Bar", "Foo's Company", "Twenty Second Avenue",
                             "Sawtell", "New South Wales", "2452", "", "", Long.toString(patient.getId()),
                             patient.getName(), "Canine", "", "", "", "",
                             Long.toString(reminderType.getEntity().getId()), reminderType.getName(),
                             new java.sql.Date(reminder.getActivityEndTime().getTime()).toString(), "0", ""};
        assertArrayEquals(expected, lines.get(1));
    }
}
