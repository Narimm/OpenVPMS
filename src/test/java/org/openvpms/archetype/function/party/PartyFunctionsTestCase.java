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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.function.party;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link PartyFunctions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PartyFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link PartyFunctions#getHomeTelephone(Party)} method.
     */
    @Test
    public void testGetHomeTelephone() {
        Party party = createCustomer();

        JXPathContext ctx = JXPathHelper.newContext(party);
        assertEquals("", ctx.getValue("party:getHomeTelephone(.)"));

        party.addContact(createPhone("12345", true, "HOME"));
        assertEquals("(03) 12345", ctx.getValue("party:getHomeTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getHomeTelephone(Act)} method.
     */
    @Test
    public void testActGetHomeTelephone() {
        Act act = (Act) create("act.customerEstimation");
        Party party = createCustomer();
        save(party);

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertEquals("", ctx.getValue("party:getHomeTelephone(.)"));

        party.addContact(createPhone("12345", true, "HOME"));
        save(party);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 12345", ctx.getValue("party:getHomeTelephone(.)"));
        assertEquals("", ctx.getValue("party:getWorkTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWorkTelephone(Party)} method.
     */
    @Test
    public void testGetWorkTelephone() {
        Party party = createCustomer();

        JXPathContext ctx = JXPathHelper.newContext(party);
        assertEquals("", ctx.getValue("party:getWorkTelephone(.)"));

        party.addContact(createPhone("56789", true, "WORK"));
        assertEquals("(03) 56789", ctx.getValue("party:getWorkTelephone(.)"));
        assertEquals("(03) 56789",
                     ctx.getValue("party:getHomeTelephone(.)")); // OVPMS-718
    }

    /**
     * Tests the {@link PartyFunctions#getWorkTelephone(Act)} method.
     */
    @Test
    public void testActGetWorkTelephone() {
        Act act = (Act) create("act.customerEstimation");
        Party party = createCustomer();
        save(party);

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertEquals("", ctx.getValue("party:getWorkTelephone(.)"));

        party.addContact(createPhone("56789", true, "WORK"));
        save(party);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 56789", ctx.getValue("party:getWorkTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Party)} method.
     */
    @Test
    public void testGetPatientMicrochip() {
        Party patient = TestHelper.createPatient(false);
        JXPathContext ctx = JXPathHelper.newContext(patient);

        assertEquals("", ctx.getValue("party:getPatientMicrochip(.)"));

        EntityIdentity microchip = (EntityIdentity) create(
                "entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);

        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Party)} method.
     */
    @Test
    public void testActGetPatientMicrochip() {
        Act act = (Act) create("act.customerEstimation");
        Party patient = TestHelper.createPatient(false);

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertEquals("", ctx.getValue("party:getPatientMicrochip(.)"));

        EntityIdentity microchip = (EntityIdentity) create(
                "entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);
        save(patient);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);

        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getReminders} methods given a customer.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRemindersByCustomer() {
        Party customer = TestHelper.createCustomer();
        checkGetReminders(customer, customer);
    }

    /**
     * Tests the {@link PartyFunctions#getReminders} methods given an act with a customer participation.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRemindersByAct() {
        Act act = (Act) create("act.customerAccountChargesInvoice");
        Party customer = createCustomer();
        save(customer);
        ActBean invoice = new ActBean(act);
        invoice.addNodeParticipation("customer", customer);

        checkGetReminders(act, customer);
    }

    /**
     * Tests the {@link PartyFunctions#getReminders} methods.
     *
     * @param context  the jxpath context. Either a customer or an act with a customer participation
     * @param customer the customer
     */
    @SuppressWarnings("unchecked")
    private void checkGetReminders(Object context, Party customer) {
        final int count = 10;
        Entity reminderType = ReminderTestHelper.createReminderType();

        Calendar calendar = new GregorianCalendar();

        // backdate the calendar 5 days. When excluding overdue reminders, reminders dated prior to the current date
        // will be ignored.
        calendar.add(Calendar.DAY_OF_YEAR, -5);
        for (int i = 0; i < count; ++i) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date dueDate = calendar.getTime();
            Party patient = TestHelper.createPatient(customer);
            ReminderTestHelper.createReminderWithDueDate(patient, reminderType, dueDate);
        }

        JXPathContext ctx = JXPathHelper.newContext(context);

        // get reminders excluding any reminders prior to the current date
        List<Act> reminders1 = (List<Act>) ctx.getValue("party:getReminders(., 1, 'YEARS')");
        assertEquals(6, reminders1.size());

        // get all reminders (i.e., including overdue)
        List<Act> reminders2 = (List<Act>) ctx.getValue("party:getReminders(., 12, 'MONTHS', 'true')");
        assertEquals(count, reminders2.size());
    }

    /**
     * Creates a new <em>contact.phoneNumber</em>.
     *
     * @param number    the phone number
     * @param preferred if <tt>true</tt>, marks the contact as the preferred
     *                  contact
     * @param purpose   the contact purpose. May be <tt>null</tt>
     * @return a new phone contact
     */
    private Contact createPhone(String number, boolean preferred,
                                String purpose) {
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("areaCode", "03");
        bean.setValue("telephoneNumber", number);
        bean.setValue("preferred", preferred);
        if (purpose != null) {
            Lookup lookup = TestHelper.getLookup("lookup.contactPurpose",
                                                 purpose);
            contact.addClassification(lookup);
        }
        return contact;
    }

    /**
     * Helper to create a new customer with no default contacts.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        Party party = TestHelper.createCustomer(false);
        Contact[] contacts = party.getContacts().toArray(new Contact[party.getContacts().size()]);
        for (Contact contact : contacts) {
            party.removeContact(contact);
        }
        return party;
    }

}
