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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.function.reminder;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link ReminderFunctions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ReminderFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ReminderFunctions#getReminders(Party, int, String)} and
     * {@link ReminderFunctions#getReminders(Party, int, String, boolean)} methods.
     */
    @Test
    public void testGetRemindersByCustomer() {
        Party customer = TestHelper.createCustomer();
        checkGetReminders(customer, customer);
    }

    /**
     * Tests the {@link ReminderFunctions#getReminders(Act, int, String)} and
     * {@link ReminderFunctions#getReminders(Act, int, String, boolean)} methods.
     */
    @Test
    public void testGetRemindersByAct() {
        Act act = (Act) create("act.customerAccountChargesInvoice");
        Party customer = TestHelper.createCustomer();
        ActBean invoice = new ActBean(act);
        invoice.addNodeParticipation("customer", customer);

        checkGetReminders(act, customer);
    }

    /**
     * Tests the {@link ReminderFunctions#getDocumentFormReminder} method.
     */
    @Test
    public void testGetDocumentFormReminder() {
        Party patient = TestHelper.createPatient();
        DocumentAct form = (DocumentAct) create(PatientArchetypes.DOCUMENT_FORM);
        ActBean formBean = new ActBean(form);
        formBean.addNodeParticipation("patient", patient);
        save(form);

        // verify a form not associated with any invoice item nor product returns null
        assertNull(getDocumentFormReminder(form));

        // create an invoice item and associate the form with it
        Act item = FinancialTestHelper.createItem(CustomerAccountArchetypes.INVOICE_ITEM, Money.ONE,
                                                  patient, TestHelper.createProduct());
        ActBean itemBean = new ActBean(item);
        itemBean.addNodeRelationship("documents", form);
        save(item, form);

        // associate a single reminder with the invoice item, and verify it is returned by getDocumentFormReminder()
        Act reminder = ReminderTestHelper.createReminder(patient, ReminderTestHelper.createReminderType());
        itemBean.addNodeRelationship("reminders", reminder);
        save(item, reminder);
        assertEquals(reminder, getDocumentFormReminder(form));
    }

    /**
     * Tests the {@link ReminderFunctions#getReminders} methods.
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
        List<Act> reminders1 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(6, reminders1.size());

        // get all reminders (i.e., including overdue)
        List<Act> reminders2 = (List<Act>) ctx.getValue("reminder:getReminders(., 12, 'MONTHS', 'true')");
        assertEquals(count, reminders2.size());
    }

    /**
     * Invokes the reminder:getDocumentFormReminder() jxpath function with the supplied context.
     *
     * @param context the context object
     * @return the resulting reminder act. May be <tt>null</tt>
     */
    private Act getDocumentFormReminder(Object context) {
        JXPathContext ctx = JXPathHelper.newContext(context);
        return (Act) ctx.getValue("reminder:getDocumentFormReminder(.)");
    }

}
