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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.system.common.query.ObjectSet;


/**
 * Schedule event {@link ObjectSet ObjectSet} keys.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ScheduleEvent {

    /**
     * The act reference.
     */
    public static final String ACT_REFERENCE = "act.objectReference";

    /**
     * The act start time.
     */
    public static final String ACT_START_TIME = "act.startTime";

    /**
     * The act end time.
     */
    public static final String ACT_END_TIME = "act.endTime";

    /**
     * The act status.
     */
    public static final String ACT_STATUS = "act.status";

    /**
     * The act reason.
     */
    public static final String ACT_REASON = "act.reason";

    /**
     * The act description.
     */
    public static final String ACT_DESCRIPTION = "act.description";

    /**
     * The schedule reference.
     */
    public static final String SCHEDULE_REFERENCE
            = "schedule.objectReference";

    /**
     * The schedule name.
     */
    public static final String SCHEDULE_NAME = "schedule.name";

    /**
     * The customer reference.
     */
    public static final String CUSTOMER_REFERENCE = "customer.objectReference";

    /**
     * The customer name.
     */
    public static final String CUSTOMER_NAME = "customer.name";

    /**
     * The patient reference.
     */
    public static final String PATIENT_REFERENCE = "patient.objectReference";

    /**
     * The patient name.
     */
    public static final String PATIENT_NAME = "patient.name";

    /**
     * The schedule type reference.
     */
    public static final String SCHEDULE_TYPE_REFERENCE
            = "scheduleType.objectReference";

    /**
     * The schedule type name.
     */
    public static final String SCHEDULE_TYPE_NAME = "scheduleType.name";

    /**
     * The clinician reference.
     */
    public static final String CLINICIAN_REFERENCE
            = "clinician.objectReference";

    /**
     * The clinician name.
     */
    public static final String CLINICIAN_NAME = "clinician.name";

    /**
     * The arrival time. Only applies to appointment events.
     */
    public static final String ARRIVAL_TIME = "arrivalTime";

}
