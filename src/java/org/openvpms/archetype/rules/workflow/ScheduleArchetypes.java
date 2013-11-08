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

package org.openvpms.archetype.rules.workflow;


/**
 * Schedule archetype short names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ScheduleArchetypes {

    /**
     * Organisation schedule archetype short name.
     */
    public static String ORGANISATION_SCHEDULE = "party.organisationSchedule";


    /**
     * Organisation work list archetype short name.
     */
    public static String ORGANISATION_WORKLIST = "party.organisationWorkList";

    /**
     * The appointment archetype short name.
     */
    public static String APPOINTMENT = "act.customerAppointment";

    /**
     * The task archetype short name.
     */
    public static String TASK = "act.customerTask";

    /**
     * Appointment type archetype short name.
     */
    public static final String APPOINTMENT_TYPE = "entity.appointmentType";

    /**
     * Task type archetype short name.
     */
    public static final String TASK_TYPE = "entity.taskType";

    /**
     * Schedule-appointment type entity relationship archetype short name.
     */
    public static final String SCHEDULE_APPOINTMENT_TYPE_RELATIONSHIP = "entityRelationship.scheduleAppointmentType";

    /**
     * Worklist-task type entity relationship archetype short name.
     */
    public static final String WORKLIST_TASK_TYPE_RELATIONSHIP = "entityRelationship.worklistTaskType";

    /**
     * The appointment type participation archetype short name.
     */
    public static String APPOINTMENT_TYPE_PARTICIPATION = "participation.appointmentType";

    /**
     * Schedule participation archetype short name.
     */
    public static String SCHEDULE_PARTICIPATION = "participation.schedule";

    /**
     * Worklist participation archetype short name.
     */
    public static String WORKLIST_PARTICIPATION = "participation.worklist";

    /**
     * Task type participation archetype short name.
     */
    public static String TASK_TYPE_PARTICIPATION = "participation.taskType";
}

