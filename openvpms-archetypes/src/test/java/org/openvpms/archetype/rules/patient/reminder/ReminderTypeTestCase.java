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

import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;
import static org.openvpms.archetype.test.TestHelper.getDate;


/**
 * Tests the {@link ReminderType} class.
 *
 * @author Tim Anderson
 */
public class ReminderTypeTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ReminderType#getDueDate} method.
     */
    @Test
    public void testGetDueDate() {
        Date start = getDate("2007-01-01");
        Date days30 = getDate("2007-01-31");
        Date month1 = getDate("2007-02-01");
        Date month3 = getDate("2007-04-01");
        Date year2 = getDate("2009-01-01");
        checkGetDueDate(start, days30, 30, DateUnits.DAYS);
        checkGetDueDate(start, month1, 1, DateUnits.MONTHS);
        checkGetDueDate(start, month3, 3, DateUnits.MONTHS);
        checkGetDueDate(start, year2, 2, DateUnits.YEARS);
    }

    /**
     * Tests the {@link ReminderType#getCancelDate} method.
     */
    @Test
    public void testGetCancelDate() {
        Date start = getDate("2007-01-01");
        Date days30 = getDate("2007-01-31");
        Date month1 = getDate("2007-02-01");
        Date month3 = getDate("2007-04-01");
        Date year2 = getDate("2009-01-01");
        checkGetCancelDate(start, days30, 30, DateUnits.DAYS);
        checkGetCancelDate(start, month1, 1, DateUnits.MONTHS);
        checkGetCancelDate(start, month3, 3, DateUnits.MONTHS);
        checkGetCancelDate(start, year2, 2, DateUnits.YEARS);
    }

    /**
     * Tests the {@link ReminderType#shouldCancel} method.
     */
    @Test
    public void testShouldCancel() {
        Date dueDate = getDate("2007-01-01");
        Date days30 = getDate("2007-01-31");
        Date month3 = getDate("2007-04-01");
        Date year2 = getDate("2009-01-01");

        checkShouldCancel(dueDate, days30, false, 31, DateUnits.DAYS);
        checkShouldCancel(dueDate, days30, true, 30, DateUnits.DAYS);

        checkShouldCancel(dueDate, month3, false, 4, DateUnits.MONTHS);
        checkShouldCancel(dueDate, month3, true, 2, DateUnits.MONTHS);

        checkShouldCancel(dueDate, year2, false, 3, DateUnits.YEARS);
        checkShouldCancel(dueDate, year2, true, 1, DateUnits.YEARS);
    }

    /**
     * Verifies that reminder types that have no units for default or cancel intervals return something sensible.
     */
    @Test
    public void testNoUnits() {
        EntityBean bean = createReminderTypeBean();
        bean.setValue("defaultUnits", null);
        bean.setValue("cancelUnits", null);
        ReminderType type = new ReminderType(bean.getEntity(), getArchetypeService());
        assertEquals(DateUnits.YEARS, type.getDefaultUnits());
        assertEquals(DateUnits.YEARS, type.getCancelUnits());
    }

    /**
     * Tests the {@link ReminderType#getNextDueDate} method.
     */
    @Test
    public void testGetNextDueDate() {
        // check reminder type with no templates
        ReminderType type1 = new ReminderType(createReminderType(), getArchetypeService());
        Date dueDate = getDate("2007-01-01");
        assertNull(type1.getNextDueDate(dueDate, 0));

        // check reminder type with 1 template
        EntityBean bean2 = createReminderTypeBean();
        bean2.setValue("defaultInterval", 1);

        addReminderCount(bean2.getEntity(), 0, 3, DateUnits.MONTHS);
        ReminderType type2 = new ReminderType(bean2.getEntity(), getArchetypeService());

        Date expected2 = getDate("2007-04-01");
        Date actual2 = type2.getNextDueDate(dueDate, 0);
        assertEquals(expected2, actual2);

        // check reminder type with 2 template
        EntityBean bean3 = createReminderTypeBean();
        bean3.setValue("defaultInterval", 1);
        addReminderCount(bean3.getEntity(), 0, 3, DateUnits.MONTHS);
        addReminderCount(bean3.getEntity(), 1, 1, DateUnits.YEARS);
        ReminderType type3 = new ReminderType(bean3.getEntity(), getArchetypeService());

        Date expected3 = getDate("2008-01-01");
        Date actual3 = type3.getNextDueDate(dueDate, 1);
        assertEquals(expected3, actual3);
    }

    /**
     * Test the {@link ReminderType#isInteractive()} method.
     */
    @Test
    public void testIsInteractive() {
        Entity entity = createReminderType();
        EntityBean bean = new EntityBean(entity);
        ReminderType t1 = new ReminderType(entity, getArchetypeService());
        assertFalse(t1.isInteractive());
        bean.setValue("interactive", true);
        assertFalse(t1.isInteractive()); // cached - any good reason for this? TODO
        ReminderType t2 = new ReminderType(entity, getArchetypeService());
        assertTrue(t2.isInteractive());
        bean.setValue("interactive", false);
        assertTrue(t2.isInteractive());
        ReminderType t3 = new ReminderType(entity, getArchetypeService());
        assertFalse(t3.isInteractive());
    }

    /**
     * Verifies that reminder counts are returned in the correct order.
     */
    @Test
    public void testGetReminderCounts() {
        Entity entity = createReminderType();
        Entity contact0 = ReminderTestHelper.createContactRule();
        Entity email0 = ReminderTestHelper.createEmailRule();
        Entity sms0 = ReminderTestHelper.createSMSRule();
        ReminderTestHelper.addReminderCount(entity, 0, 0, DateUnits.DAYS, null, contact0, email0, sms0);
        Entity email1 = ReminderTestHelper.createEmailRule();
        Entity sms1 = ReminderTestHelper.createSMSRule();
        ReminderTestHelper.addReminderCount(entity, 1, 30, DateUnits.DAYS, null, email1, sms1);
        Entity list2 = ReminderTestHelper.createListRule();
        ReminderTestHelper.addReminderCount(entity, 2, 60, DateUnits.DAYS, null, list2);

        ReminderType reminderType = new ReminderType(entity, getArchetypeService());
        checkReminderCount(reminderType.getReminderCount(0), 0, 0, DateUnits.DAYS, contact0, email0, sms0);
        checkReminderCount(reminderType.getReminderCount(1), 1, 30, DateUnits.DAYS, email1, sms1);
        checkReminderCount(reminderType.getReminderCount(2), 2, 60, DateUnits.DAYS, list2);
        assertNull(reminderType.getReminderCount(3));

        List<ReminderCount> counts = reminderType.getReminderCounts();
        assertEquals(3, counts.size());
        checkReminderCount(counts.get(0), 0, 0, DateUnits.DAYS, contact0, email0, sms0);
        checkReminderCount(counts.get(1), 1, 30, DateUnits.DAYS, email1, sms1);
        checkReminderCount(counts.get(2), 2, 60, DateUnits.DAYS, list2);
    }

    /**
     * Verifies a {@link ReminderCount} matches that expected.
     *
     * @param count            the reminder count
     * @param expectedCount    the expected count
     * @param expectedInterval the expected overdue interval
     * @param expectedUnits    the expected overdue units
     * @param expectedRules    the expected rules
     */
    private void checkReminderCount(ReminderCount count, int expectedCount, int expectedInterval,
                                    DateUnits expectedUnits, Entity... expectedRules) {
        assertEquals(expectedCount, count.getCount());
        assertEquals(expectedInterval, count.getInterval());
        assertEquals(expectedUnits, count.getUnits());
        List<ReminderRule> rules = count.getRules();
        assertEquals(expectedRules.length, rules.size());
        for (int i = 0; i < expectedRules.length; ++i) {
            ReminderRule expected = new ReminderRule(expectedRules[i], getArchetypeService());
            assertEquals(expected, rules.get(i));
        }
    }

    /**
     * Verifies that due dates are correctly calculated.
     *
     * @param start    the start date
     * @param expected the expected due date
     * @param interval the interval
     * @param units    the interval units
     */
    private void checkGetDueDate(Date start, Date expected, int interval, DateUnits units) {
        EntityBean bean = createReminderTypeBean();
        bean.setValue("defaultInterval", interval);
        bean.setValue("defaultUnits", units.toString());

        ReminderType type = new ReminderType(bean.getEntity(), getArchetypeService());
        assertEquals(interval, type.getDefaultInterval());
        assertEquals(units, type.getDefaultUnits());
        Date due = type.getDueDate(start);
        assertEquals(expected, due);
    }

    /**
     * Verifies that cancel dates are correctly calculated.
     *
     * @param start    the start date
     * @param expected the expected cancel date
     * @param interval the cancel interval
     * @param units    the cancel interval units
     */
    private void checkGetCancelDate(Date start, Date expected, int interval, DateUnits units) {
        EntityBean bean = createReminderTypeBean();
        bean.setValue("cancelInterval", interval);
        bean.setValue("cancelUnits", units.toString());

        ReminderType type = new ReminderType(bean.getEntity(), getArchetypeService());
        assertEquals(interval, type.getCancelInterval());
        assertEquals(units, type.getCancelUnits());

        Date cancel = type.getCancelDate(start);
        assertEquals(expected, cancel);
    }

    /**
     * Tests the {@link ReminderType#shouldCancel} method.
     *
     * @param dueDate    the due date
     * @param cancelDate the cancel date
     * @param expected   the expected result
     * @param interval   the cancel interval
     * @param units      the cancel inverval units
     */
    private void checkShouldCancel(Date dueDate, Date cancelDate, boolean expected, int interval, DateUnits units) {
        EntityBean bean = createReminderTypeBean();
        bean.setValue("cancelInterval", interval);
        bean.setValue("cancelUnits", units.toString());
        ReminderType type = new ReminderType(bean.getEntity(), getArchetypeService());
        assertEquals(expected, type.shouldCancel(dueDate, cancelDate));
    }

    /**
     * Helper to create a new <em>entity.reminderType</em> wrapped by a bean.
     *
     * @return a new reminder type
     */
    private EntityBean createReminderTypeBean() {
        return new EntityBean(createReminderType());
    }

    /**
     * Adds a reminder count to a reminder type.
     *
     * @param reminderType  the reminder type
     * @param reminderCount the reminder count
     * @param interval      the interval
     * @param units         the interval units
     */
    private void addReminderCount(Entity reminderType, int reminderCount, int interval, DateUnits units) {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        template.setName("XTemplate-" + System.currentTimeMillis());
        save(template);
        ReminderTestHelper.addReminderCount(reminderType, reminderCount, interval, units, template);
    }
}
