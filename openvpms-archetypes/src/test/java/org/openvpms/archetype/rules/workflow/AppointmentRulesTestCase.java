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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Tests the {@link AppointmentRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentRulesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the behaviour of {@link AppointmentRules#calculateEndTime} when
     * the schedule units are in minutes .
     */
    public void testCalculateEndTimeForMinsUnits() {
        Entity appointmentType = createAppointmentType();
        save(appointmentType);
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Date start = createTime(9, 0);
        Date end = AppointmentRules.calculateEndTime(start, schedule,
                                                     appointmentType);
        Date expected = createTime(9, 30);
        assertEquals(expected, end);
    }

    /**
     * Tests the behaviour of {@link AppointmentRules#calculateEndTime} when
     * the schedule units are in hours.
     */
    public void testCalculateEndTimeForHoursUnits() {
        Entity appointmentType = createAppointmentType();
        save(appointmentType);
        Party schedule = createSchedule(1, "HOURS", 3, appointmentType);
        Date start = createTime(9, 0);
        Date end = AppointmentRules.calculateEndTime(start, schedule,
                                                     appointmentType);
        Date expected = createTime(12, 0);
        assertEquals(expected, end);
    }

    /**
     * Tests the behaviour of
     * {@link AppointmentRules#hasOverlappingAppointments}.
     */
    public void testHasOverlappingAppointments() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);

        Entity appointmentType = createAppointmentType();
        Party schedule1 = createSchedule(15, "MINUTES", 2, appointmentType);
        Party schedule2 = createSchedule(15, "MINUTES", 2, appointmentType);
        save(schedule1);
        save(schedule2);

        Act appointment = createAppointment(start, end, schedule1);
        assertFalse(AppointmentRules.hasOverlappingAppointments(appointment));
        save(appointment);
        assertFalse(AppointmentRules.hasOverlappingAppointments(appointment));

        Act exactOverlap = createAppointment(start, end, schedule1);
        assertTrue(AppointmentRules.hasOverlappingAppointments(exactOverlap));

        Act overlap = createAppointment(createTime(9, 5), createTime(9, 10),
                                        schedule1);
        assertTrue(AppointmentRules.hasOverlappingAppointments(overlap));

        Act after = createAppointment(createTime(9, 15), createTime(9, 30),
                                      schedule1);
        assertFalse(AppointmentRules.hasOverlappingAppointments(after));

        Act before = createAppointment(createTime(8, 45), createTime(9, 0),
                                       schedule1);
        assertFalse(AppointmentRules.hasOverlappingAppointments(before));

        // now verify there are no overlaps for the same time but different
        // schedule
        Act appointment2 = createAppointment(start, end, schedule2);
        assertFalse(AppointmentRules.hasOverlappingAppointments(appointment2));
        save(appointment2);
        assertFalse(AppointmentRules.hasOverlappingAppointments(appointment2));
    }

    /**
     * Tests the behaviour of {@link AppointmentRules#hasOverlappingAppointments}
     * for an unpopulated appointment.
     */
    public void testHasOverlappingAppointmentsForEmptyAct() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Act appointment = createAppointment(start, end, schedule);
        save(appointment);

        Act empty = createAct("act.customerAppointment");
        empty.setActivityStartTime(null);
        empty.setActivityEndTime(null);

        assertFalse(AppointmentRules.hasOverlappingAppointments(empty));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        removeActs();
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @return a new act
     */
    protected Act createAppointment(Date startTime, Date endTime,
                                    Party schedule) {
        Act act = createAct("act.customerAppointment");
        Lookup reason = createLookup("lookup.appointmentReason", "XReason");
        Lookup status = createLookup("lookup.appointmentStatus", "XStatus");

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("reason", reason);
        bean.setValue("status", status);
        Party customer = (Party) create("party.customerperson");
        Entity appointmentType = (Entity) create("entity.appointmentType");
        bean.setParticipant("participation.customer", customer);
        bean.setParticipant("participation.schedule", schedule);
        bean.setParticipant("participation.appointmentType", appointmentType);
        return act;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected Act createAct(String shortName) {
        return (Act) create(shortName);
    }

    /**
     * Helper to create a new <em>entity.appointmentType</em>.
     *
     * @return a new appointment type
     */
    protected Entity createAppointmentType() {
        Entity appointmentType = (Entity) create("entity.appointmentType");
        appointmentType.setName("XAppointmentType");
        return appointmentType;
    }

    /**
     * Helper to create a new <code>party.organisationSchedule</em>.
     *
     * @param slotSize        the schedule slot size
     * @param slotUnits       the schedule slot units
     * @param noSlots         the appointment no. of slots
     * @param appointmentType the appointment type
     * @return a new schedule
     */
    protected Party createSchedule(int slotSize, String slotUnits,
                                   int noSlots, Entity appointmentType) {
        Party schedule = (Party) create("party.organisationSchedule");
        EntityBean bean = new EntityBean(schedule);
        bean.setValue("name", "XSchedule");
        bean.setValue("slotSize", slotSize);
        bean.setValue("slotUnits", slotUnits);
        EntityRelationship relationship = (EntityRelationship) create(
                "entityRelationship.scheduleAppointmentType");
        relationship.setSource(schedule.getObjectReference());
        relationship.setTarget(appointmentType.getObjectReference());
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("noSlots", noSlots);
        schedule.addEntityRelationship(relationship);
        return schedule;
    }

    /**
     * Helper to create a time with a fixed date.
     *
     * @param hour    the hour
     * @param minutes the minutes
     * @return a new time
     */
    private Date createTime(int hour, int minutes) {
        Calendar calendar = new GregorianCalendar(2006, 8, 22, hour, minutes);
        return calendar.getTime();
    }

    /**
     * Creates a lookup.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @return a new lookup
     */
    private Lookup createLookup(String shortName, String code) {
        Lookup lookup = (Lookup) create(shortName);
        lookup.setCode(code);
        return lookup;
    }

    /**
     * Remove any existing appointment acts that will interfere with the tests.
     */
    private void removeActs() {
        Date startDay = createTime(0, 0);
        Date endDay = createTime(23, 59);
        List rows = ArchetypeQueryHelper.getActs(
                getArchetypeService(), "act", "customerAppointment",
                startDay, endDay, null, null, null, true, 0,
                ArchetypeQuery.ALL_ROWS).getRows();
        for (Object object : rows) {
            Act act = (Act) object;
            remove(act);
        }
    }

}
