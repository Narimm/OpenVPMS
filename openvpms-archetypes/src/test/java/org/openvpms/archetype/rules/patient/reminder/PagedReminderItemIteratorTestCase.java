package org.openvpms.archetype.rules.patient.reminder;

import org.junit.Test;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus.PENDING;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createListReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminder;

/**
 * Tests the {@link PagedReminderItemIterator}.
 *
 * @author Tim Anderson
 */
public class PagedReminderItemIteratorTestCase extends ArchetypeServiceTest {

    /**
     * Tests iteration with different page sizes.
     */
    @Test
    public void testIteration() {
        int total = 15;
        ReminderItemQueryFactory factory = createItems(total);

        for (int pageSize = 1; pageSize <= total + 1; pageSize++) {
            // run the same test for multiple page sizes
            checkCount(total, factory, pageSize);
        }
    }

    /**
     * Tests iteration when each item is updated as it is iterated over. This changes the iteration.
     * Each reminder should only be seen once.
     */
    @Test
    public void testUpdateAll() {
        int total = 15;
        final IArchetypeService service = getArchetypeService();
        for (int pageSize = 1; pageSize <= total + 1; pageSize++) {
            // run the same test for multiple page sizes
            ReminderItemQueryFactory factory = createItems(total);
            PagedReminderItemIterator iterator = new PagedReminderItemIterator(factory, pageSize, service);
            int count = update(iterator, 1);
            assertEquals(total, count);

            factory.setStatus(ReminderItemStatus.COMPLETED);
            checkCount(total, factory, pageSize);

            factory.setStatus(ReminderItemStatus.PENDING);
            checkCount(0, factory, pageSize);
        }
    }

    /**
     * Tests iteration when every second item is updated as it is iterated over. This changes the iteration.
     * Each reminder should only be seen once.
     */
    @Test
    public void testUpdateEverySecond() {
        int total = 15;
        final IArchetypeService service = getArchetypeService();
        for (int pageSize = 1; pageSize <= total + 1; pageSize++) {
            // run the same test for multiple page sizes
            ReminderItemQueryFactory factory = createItems(total);
            PagedReminderItemIterator iterator = new PagedReminderItemIterator(factory, pageSize, service);
            int count = update(iterator, 2);
            assertEquals(total, count);

            factory.setStatus(ReminderItemStatus.COMPLETED);
            checkCount(7, factory, pageSize);

            factory.setStatus(ReminderItemStatus.PENDING);
            checkCount(8, factory, pageSize);
        }
    }

    /**
     * Creates an iterator with the specified page size, and verifies the number of iterations matches the expected
     * count.
     *
     * @param expectedCount the expected count
     * @param factory       the query factory
     * @param pageSize      the page size
     */
    private void checkCount(int expectedCount, ReminderItemQueryFactory factory, int pageSize) {
        PagedReminderItemIterator iterator = new PagedReminderItemIterator(factory, pageSize, getArchetypeService());
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        assertEquals(expectedCount, count);
    }

    /**
     * Updates reminder items while iterating over them.
     *
     * @param iterator      the iterator
     * @param completeEvery determines the reminder items to complete (i.e. every 1st, 2nd etc)
     * @return the number of reminders iterated over
     */
    private int update(PagedReminderItemIterator iterator, int completeEvery) {
        int count = 0;
        int i = 0;
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            if (++i == completeEvery) {
                i = 0;
                Act item = (Act) set.get("item");
                item.setStatus(ReminderItemStatus.COMPLETED);
                save(item);
                iterator.updated();
            }
            ++count;
        }
        return count;
    }

    /**
     * Creates {@code count} <em>PENDING</em> reminders items and returns a query factory that returns only those items.
     *
     * @param count the number of reminder items to create
     * @return the query factory
     */
    private ReminderItemQueryFactory createItems(int count) {
        Entity reminderType = ReminderTestHelper.createReminderType(1, DateUnits.YEARS);
        Party location = TestHelper.createLocation();
        Party customer = TestHelper.createCustomer();
        IMObjectBean bean = new IMObjectBean(customer);
        bean.addNodeTarget("practice", location);
        bean.save();
        Party patient = TestHelper.createPatient(customer);
        Date today = DateRules.getToday();
        Date tomorrow = DateRules.getTomorrow();
        for (int i = 0; i < count; ++i) {
            Act item = createListReminder(today, tomorrow, PENDING, 0);
            createReminder(tomorrow, patient, reminderType, item);
        }
        ReminderItemQueryFactory factory = new ReminderItemQueryFactory();
        factory.setStatus(ReminderItemStatus.PENDING);
        factory.setLocation(new Location(location));
        return factory;
    }

}
