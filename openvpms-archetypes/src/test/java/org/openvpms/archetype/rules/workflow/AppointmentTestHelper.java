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

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;


/**
 * Appointment test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentTestHelper extends TestHelper {

    /**
     * Helper to create and save a new <em>entity.appointmentType</em>.
     *
     * @return a new appointment type
     */
    public static Entity createAppointmentType() {
        Entity appointmentType = (Entity) create("entity.appointmentType");
        appointmentType.setName("XAppointmentType");
        save(appointmentType);
        return appointmentType;
    }

    /**
     * Helper to create and save a <code>party.organisationSchedule</em>.
     *
     * @return a new schedule
     */
    public static Party createSchedule() {
        return createSchedule(15, "MINUTES", 2, createAppointmentType());
    }

    /**
     * Helper to create and save new <code>party.organisationSchedule</em>.
     *
     * @param slotSize        the schedule slot size
     * @param slotUnits       the schedule slot units
     * @param noSlots         the appointment no. of slots
     * @param appointmentType the appointment type
     * @return a new schedule
     */
    public static Party createSchedule(int slotSize, String slotUnits,
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
        bean.save();
        return schedule;
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        Party schedule) {
        Party customer = TestHelper.createCustomer();
        return createAppointment(startTime, endTime, schedule, customer, null);
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient. May be <code>null</code>
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        Party schedule, Party customer,
                                        Party patient) {
        Act act = (Act) create("act.customerAppointment");
        Lookup reason = createLookup("lookup.appointmentReason", "XReason");

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("reason", reason.getCode());
        bean.setValue("status", AppointmentStatus.IN_PROGRESS);
        Entity appointmentType = (Entity) create("entity.appointmentType");
        bean.setParticipant("participation.customer", customer);
        if (patient != null) {
            bean.setParticipant("participation.patient", patient);
        }
        bean.setParticipant("participation.schedule", schedule);
        bean.setParticipant("participation.appointmentType", appointmentType);
        return act;
    }

    /**
     * Creates a lookup.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @return a new lookup
     */
    private static Lookup createLookup(String shortName, String code) {
        Lookup lookup = (Lookup) create(shortName);
        lookup.setCode(code);
        return lookup;
    }

}
