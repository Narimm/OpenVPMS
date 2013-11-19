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
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openvpms.archetype.test.TestHelper.getLookup;

/**
 * Tests the {@link ReminderCSVExporter} class.
 *
 * @author Tim Anderson
 */
public class ReminderCSVExporterTestCase extends ArchetypeServiceTest {

    /**
     * The exporter.
     */
    private ReminderCSVExporter exporter;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The practice rules.
     */
    private PracticeRules practiceRules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeService service = getArchetypeService();
        practiceRules = new PracticeRules(service);
        PartyRules partyRules = new PartyRules(service);
        handlers = new DocumentHandlers();
        ILookupService lookups = LookupServiceHelper.getLookupService();
        exporter = new ReminderCSVExporter(practiceRules, partyRules, service, lookups, handlers);
    }

    /**
     * Tests export using comma separated values.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testCSVExport() throws IOException {
        Party customer = createCustomer("Foo", "F", "Bar", "Embedded, Commas");
        checkExport(customer, true);
    }

    /**
     * Tests export using tab separated values.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testTabExport() throws IOException {
        Party customer = createCustomer("Foo", "F", "Bar", "Embedded\tTabs");
        checkExport(customer, false);
    }

    /**
     * Tests export with embedded quotes.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testEmbeddedQuotes() throws IOException {
        Party customer = createCustomer("Foo", "F", "Bar", "\"Embedded Quotes\"");
        checkExport(customer, true);
    }

    /**
     * Checks export.
     *
     * @param customer       the customer
     * @param commaSeparated if {@code true} use comma separated values, otherwise use tab separated values
     * @throws IOException for  any I/O error
     */
    private void checkExport(Party customer, boolean commaSeparated) throws IOException {
        Party practice = practiceRules.getPractice();
        IMObjectBean practiceBean = new IMObjectBean(practice);
        practiceBean.setValue("reminderExportFormat", commaSeparated ? "COMMA" : "TAB");
        practiceBean.save();

        if (commaSeparated) {
            assertEquals(',', exporter.getSeparator());
        } else {
            assertEquals('\t', exporter.getSeparator());
        }

        Contact address = TestHelper.createLocationContact("Twenty Second Avenue", "SAWTELL",
                                                           "Sawtell", "NSW", "New South Wales", "2452");
        customer.addContact(address);
        customer.addContact(TestHelper.createPhoneContact("03", "1234 5678"));
        Contact mobile = TestHelper.createPhoneContact(null, "5678 1234");
        IMObjectBean phoneBean = new IMObjectBean(mobile);
        phoneBean.setValue("sms", true);
        phoneBean.setValue("preferred", false);
        customer.addContact(mobile);
        customer.addContact(TestHelper.createEmailContact("foo@bar.com"));
        Party patient = createPatient(customer);

        Entity reminderType = ReminderTestHelper.createReminderType();

        Act reminder = ReminderTestHelper.createReminder(patient, reminderType);
        ActBean reminderBean = new ActBean(reminder);
        reminderBean.setValue("lastSent", TestHelper.getDate("2013-06-05"));
        reminderBean.save();

        ReminderEvent event = createReminderEvent(customer, address, patient, reminderType, reminder);
        Document document = exporter.export(Arrays.asList(event));
        List<String[]> lines = readCSV(document);
        assertNotNull(lines);
        assertEquals(2, lines.size());
        assertArrayEquals(ReminderCSVExporter.HEADER, lines.get(0));

        IMObjectBean bean = new IMObjectBean(customer);
        String firstName = bean.getString("firstName");
        String initials = bean.getString("initials");
        String lastName = bean.getString("lastName");
        String companyName = bean.getString("companyName");

        String[] expected = {getId(customer), "Mr", firstName, initials, lastName, companyName, "Twenty Second Avenue",
                             "Sawtell", "New South Wales", "2452", "(03) 1234 5678", "5678 1234", "foo@bar.com",
                             getId(patient), patient.getName(), "Canine", "Kelpie", "Male", "Black", "2013-02-01",
                             getId(reminderType), reminderType.getName(),
                             getDate(reminder.getActivityEndTime()), "0", "2013-06-05"};
        assertArrayEquals(expected, lines.get(1));
    }

    /**
     * Creates a new {@code EXPORT} reminder event.
     *
     * @param customer     the customer
     * @param contact      the customer contact
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param reminder     the reminder
     * @return a new reminder event
     */
    private ReminderEvent createReminderEvent(Party customer, Contact contact, Party patient, Entity reminderType,
                                              Act reminder) {
        return new ReminderEvent(ReminderEvent.Action.EXPORT, reminder,
                                 new ReminderType(reminderType, getArchetypeService()), patient, customer, contact,
                                 null);
    }

    /**
     * Creates  a new customer.
     *
     * @param firstName   the customer's first name
     * @param initials    the customer's initials. May be {@code null}
     * @param lastName    the customer's last name
     * @param companyName the company name. May be {@code null}
     * @return a new customer
     */
    private Party createCustomer(String firstName, String initials, String lastName, String companyName) {
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        IMObjectBean bean = new IMObjectBean(customer);
        getLookup("lookup.personTitle", "MR", "Mr", true);
        bean.setValue("title", "MR");
        bean.setValue("firstName", firstName);
        bean.setValue("initials", initials);
        bean.setValue("lastName", lastName);
        bean.setValue("companyName", companyName);
        bean.save();
        return customer;
    }

    /**
     * Reads a CSV document.
     *
     * @param document the document to read
     * @return the lines read
     * @throws IOException for any I/O error
     */
    private List<String[]> readCSV(Document document) throws IOException {
        InputStreamReader reader = new InputStreamReader(handlers.get(document).getContent(document));
        CSVReader csv = new CSVReader(reader, exporter.getSeparator());
        return csv.readAll();
    }

    /**
     * Creats a patient.
     *
     * @param customer the patient owner
     * @return the new patient
     */
    private Party createPatient(Party customer) {
        Lookup species = getLookup("lookup.species", "CANINE", "Canine", true);
        getLookup("lookup.breed", "KELPIE", "Kelpie", species, "lookupRelationship.speciesBreed");
        Party patient = TestHelper.createPatient(customer);
        IMObjectBean patientBean = new IMObjectBean(patient);
        patientBean.setValue("breed", "KELPIE");
        patientBean.setValue("sex", "MALE");
        patientBean.setValue("dateOfBirth", TestHelper.getDate("2013-02-01"));
        patientBean.setValue("colour", "Black");
        patientBean.save();
        return patient;
    }

    /**
     * Converts a date to a string.
     *
     * @param date the date
     * @return the string value of the date
     */
    private String getDate(Date date) {
        return new java.sql.Date(date.getTime()).toString();
    }

    /**
     * Converts an object id to a string.
     *
     * @param object the object
     * @return the object's id, as a string
     */
    private String getId(IMObject object) {
        return Long.toString(object.getId());
    }

}
