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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.archetype.rules.patient.reminder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;


/**
 * Tests the {@link ReminderType} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
        ReminderType type = new ReminderType(bean.getEntity());
        assertEquals(DateUnits.YEARS, type.getDefaultUnits());
        assertEquals(DateUnits.YEARS, type.getCancelUnits());
    }

    /**
     * Tests the {@link ReminderType#getNextDueDate} method.
     */
    @Test
    public void testGetNextDueDate() {
        // check reminder type with no templates
        ReminderType type1 = new ReminderType(createReminderType());
        Date dueDate = getDate("2007-01-01");
        assertNull(type1.getNextDueDate(dueDate, 0));

        // check reminder type with 1 template
        EntityBean bean2 = createReminderTypeBean();
        addTemplate(bean2, 0, 3, DateUnits.MONTHS);
        ReminderType type2 = new ReminderType(bean2.getEntity());

        Date expected2 = getDate("2007-04-01");
        Date actual2 = type2.getNextDueDate(dueDate, 0);
        assertEquals(expected2, actual2);

        // check reminder type with 2 template
        EntityBean bean3 = createReminderTypeBean();
        addTemplate(bean3, 0, 3, DateUnits.MONTHS);
        addTemplate(bean3, 1, 1, DateUnits.YEARS);
        ReminderType type3 = new ReminderType(bean3.getEntity());

        Date expected3 = getDate("2008-01-01");
        Date actual3 = type3.getNextDueDate(dueDate, 1);
        assertEquals(expected3, actual3);
    }

    /**
     * Tests the {@link ReminderType#isDue} method.
     */
    @Test
    public void testIsDue() {
        // check isDue for a reminder type with no templates
        ReminderType type1 = new ReminderType(createReminderType());
        Date dueDate = getDate("2007-01-01");
        assertEquals(true, type1.isDue(dueDate, 0, null, null));
        assertEquals(true, type1.isDue(dueDate, 0, getDate("2007-01-01"), null));
        assertEquals(false, type1.isDue(dueDate, 0, getDate("2007-01-02"), null));
        assertEquals(false, type1.isDue(dueDate, 0, null, getDate("2006-12-31")));
        assertEquals(true, type1.isDue(dueDate, 0, getDate("2007-01-01"), getDate("2007-01-01")));

        // check isDue for a reminder type with 1 template
        EntityBean bean2 = createReminderTypeBean();
        addTemplate(bean2, 0, 3, DateUnits.MONTHS);
        ReminderType type2 = new ReminderType(bean2.getEntity());
        assertEquals(true, type2.isDue(dueDate, 0, null, null));
        assertEquals(true, type2.isDue(dueDate, 0, getDate("2007-04-01"), null));
        assertEquals(false, type2.isDue(dueDate, 0, getDate("2007-04-02"), null));
        assertEquals(false, type2.isDue(dueDate, 0, null, getDate("2007-03-31")));
        assertEquals(true, type2.isDue(dueDate, 0, getDate("2007-04-01"), getDate("2007-04-01")));
    }

    /**
     * Verifies that times are ignored by {@link ReminderType#isDue}.
     */
    @Test
    public void testIsDueTimeIgnored() {
        ReminderType type = new ReminderType(createReminderType());
        Date dueDate = getDatetime("2007-01-01 10:53:22");
        assertEquals(true, type.isDue(dueDate, 0, getDatetime("2007-01-01 11:00:00"), null));
        assertEquals(true, type.isDue(dueDate, 0, null, getDatetime("2007-01-01 10:52:00")));
    }

    /**
     * Test the {@link ReminderType#isInteractive()} method.
     */
    @Test
    public void testIsInteractive() {
        Entity entity = createReminderType();
        EntityBean bean = new EntityBean(entity);
        ReminderType t1 = new ReminderType(entity);
        assertFalse(t1.isInteractive());
        bean.setValue("interactive", true);
        assertFalse(t1.isInteractive()); // cached - any good reason for this? TODO
        ReminderType t2 = new ReminderType(entity);
        assertTrue(t2.isInteractive());
        bean.setValue("interactive", false);
        assertTrue(t2.isInteractive());
        ReminderType t3 = new ReminderType(entity);
        assertFalse(t3.isInteractive());

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

        ReminderType type = new ReminderType(bean.getEntity());
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

        ReminderType type = new ReminderType(bean.getEntity());
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
        ReminderType type = new ReminderType(bean.getEntity());
        assertEquals(expected, type.shouldCancel(dueDate, cancelDate));
    }

    /**
     * Helper to create a new <em>entity.reminderType</em>.
     *
     * @return a new reminder type
     */
    private Entity createReminderType() {
        return (Entity) create("entity.reminderType");
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
     * Adds a (dummy) template to a reminder type.
     *
     * @param reminderType  the reminder type
     * @param reminderCount the reminder count
     * @param interval      the interval
     * @param units         the interval units
     */
    private void addTemplate(EntityBean reminderType, int reminderCount, int interval, DateUnits units) {
        Entity dummy = (Entity) create("entity.documentTemplate");
        EntityRelationship template = reminderType.addNodeRelationship("templates", dummy);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("reminderCount", reminderCount);
        bean.setValue("interval", interval);
        bean.setValue("units", units.toString());
    }
}
