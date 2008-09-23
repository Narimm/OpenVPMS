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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;
import java.util.List;


/**
 * Appointment service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface AppointmentService {

    /**
     * Returns all appointments for the specified schedule and day.
     * Appointments are represented by {@link ObjectSet ObjectSets}, the
     * keys for which are 
     *
     * @param schedule an <em>party.organisationSchedule</em>
     * @param day      the day
     * @return a list of appointments
     */
    List<ObjectSet> getAppointments(Party schedule, Date day);

    /**
     * Returns all appointments for the specified schedule, and time range.
     *
     * @param schedule an <em>party.organisationSchedule</em>
     * @return a list of appointments
     */
    List<ObjectSet> getAppointments(Party schedule, Date from, Date to);
}
