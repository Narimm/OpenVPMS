package org.openvpms.archetype.rules.patient.reminder;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createExportReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createPrintReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSReminder;
import static org.openvpms.archetype.test.TestHelper.createPatient;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Tests the {@link ReminderQueueIterator}.
 *
 * @author Tim Anderson
 */
public class ReminderQueueIteratorTestCase extends ArchetypeServiceTest {

    /**
     * Tests the iterator.
     */
    @Test
    public void testIterator() {
        Entity reminderType1 = createReminderType(1, DateUnits.YEARS);
        Entity reminderType2 = createReminderType(1, DateUnits.YEARS);
        Party customer1 = TestHelper.createCustomer();
        Party patient1 = createPatient(customer1);
        Party customer2 = TestHelper.createCustomer();
        Party patient2 = createPatient(customer2);

        Act reminder1 = createReminder(patient1, reminderType1);
        Act reminder2 = createReminder(patient2, reminderType1);
        Act reminder3 = createReminder(patient1, reminderType1);
        Act reminder4 = createReminder(patient2, reminderType1);
        Act reminder5 = createReminder(patient1, reminderType1);
        Act reminder6 = createReminder(patient2, reminderType1);
        Act reminder7 = createReminder(patient1, reminderType2);
        reminder5.setStatus(ReminderStatus.COMPLETED); // won't be returned
        reminder6.setStatus(ReminderStatus.CANCELLED); // won't be returned

        save(reminder1);
        save(reminder2);
        save(reminder3);
        save(reminder4);
        save(reminder5);
        save(reminder6);
        save(reminder7);

        ReminderQueueIterator iterator1 = createIterator(DateRules.getTomorrow(), reminderType1, 1);
        checkIterator(iterator1, reminder1, reminder2, reminder3, reminder4);

        ReminderQueueIterator iterator2 = createIterator(DateRules.getYesterday(), reminderType1, 1);
        checkIterator(iterator2); // reminders not due

        ReminderQueueIterator iterator3 = createIterator(DateRules.getTomorrow(), reminderType2, 1);
        checkIterator(iterator3, reminder7);
    }

    /**
     * Verifies that updates to reminders doesn't affect iteration, when {@link ReminderQueueIterator#updated()} is
     * used.
     */
    @Test
    public void testUpdateIterator() {
        Entity reminderType = createReminderType(1, DateUnits.YEARS);
        Party customer1 = TestHelper.createCustomer();
        Party patient1 = createPatient(customer1);
        Party customer2 = TestHelper.createCustomer();
        Party patient2 = createPatient(customer2);

        Act reminder1 = createReminder(patient1, reminderType);
        Act reminder2 = createReminder(patient2, reminderType);
        Act reminder3 = createReminder(patient1, reminderType);
        Act reminder4 = createReminder(patient2, reminderType);
        save(reminder1);
        save(reminder2);
        save(reminder3);
        save(reminder4);

        ReminderQueueIterator iterator = createIterator(DateRules.getTomorrow(), reminderType, 2);

        checkNext(iterator, reminder1);
        reminder1.setStatus(ActStatus.COMPLETED);
        save(reminder1);
        iterator.updated();

        checkNext(iterator, reminder2);
        reminder2.setStatus(ActStatus.CANCELLED);
        save(reminder2);
        iterator.updated();

        checkNext(iterator, reminder3);

        checkNext(iterator, reminder4);
        reminder4.setStatus(ActStatus.COMPLETED);
        save(reminder4);
        iterator.updated();

        assertFalse(iterator.hasNext());
    }

    /**
     * Tests the behaviour of the query for reminders with reminder items.
     */
    @Test
    public void testRemindersWithItems() {
        Date date = DateRules.getToday();
        Entity reminderType = createReminderType(1, DateUnits.YEARS);
        Party customer = TestHelper.createCustomer();
        Party patient = createPatient(customer);

        Act email1 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act sms1 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        createReminder(date, patient, reminderType, IN_PROGRESS, email1, sms1);

        Act email2 = createSMSReminder(date, date, ReminderItemStatus.ERROR, 0);
        Act sms2 = createSMSReminder(date, date, ReminderItemStatus.ERROR, 0);
        createReminder(date, patient, reminderType, IN_PROGRESS, email2, sms2);

        Act email3 = createPrintReminder(date, date, ReminderItemStatus.COMPLETED, 0);
        Act print3 = createPrintReminder(date, date, ReminderItemStatus.COMPLETED, 0);
        Act reminder3 = createReminder(date, patient, reminderType, IN_PROGRESS, email3, print3);

        Act email4 = createExportReminder(date, date, ReminderItemStatus.CANCELLED, 0);
        Act export4 = createExportReminder(date, date, ReminderItemStatus.CANCELLED, 0);
        Act reminder4 = createReminder(date, patient, reminderType, IN_PROGRESS, email4, export4);

        Act email5 = createExportReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act sms5 = createExportReminder(date, date, ReminderItemStatus.COMPLETED, 0);
        createReminder(date, patient, reminderType, IN_PROGRESS, email5, sms5);

        ReminderQueueIterator iterator = createIterator(DateRules.getTomorrow(), reminderType, 2);
        checkIterator(iterator, reminder3, reminder4);
    }

    /**
     * Verifies that reminders are returned, and in id order.
     *
     * @param iterator  the iterator
     * @param reminders the expected reminders
     */
    private void checkIterator(Iterator<Act> iterator, Act... reminders) {
        List<Act> list = Arrays.asList(reminders);
        List<Act> actual = new ArrayList<>();
        CollectionUtils.addAll(actual, iterator);
        assertEquals(list, actual);
    }

    /**
     * Verifies that the iterator returns the next expected reminders.
     *
     * @param iterator the iterator
     * @param act      the expected reminder
     */
    private void checkNext(Iterator<Act> iterator, Act act) {
        assertTrue(iterator.hasNext());
        Act next = iterator.next();
        assertEquals(act, next);
    }

    /**
     * Creates a new iterator that only returns reminders with the specified reminder type.
     *
     * @param date         the due date
     * @param reminderType the reminder type
     * @param pageSize     the page size
     * @return a new iterator
     */
    private ReminderQueueIterator createIterator(Date date, final Entity reminderType, int pageSize) {
        ReminderQueueQueryFactory factory = new ReminderQueueQueryFactory() {
            @Override
            public ArchetypeQuery createQuery(Date date) {
                ArchetypeQuery query = super.createQuery(date);
                query.add(join("reminderType", "r").add(eq("entity", reminderType)));
                return query;
            }
        };
        return new ReminderQueueIterator(factory, date, pageSize, getArchetypeService());
    }
}
