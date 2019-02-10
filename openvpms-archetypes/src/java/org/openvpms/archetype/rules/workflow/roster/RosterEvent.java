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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow.roster;

import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;

/**
 * Roster event {@link PropertySet} keys.
 *
 * @author Tim Anderson
 */
public class RosterEvent extends ScheduleEvent {

    /**
     * The user reference.
     */
    public static final String USER_REFERENCE = "user.objectReference";

    /**
     * The user name.
     */
    public static final String USER_NAME = "user.name";

    /**
     * The location reference.
     */
    public static final String LOCATION_REFERENCE = "location.objectReference";

    /**
     * The location name.
     */
    public static final String LOCATION_NAME = "location.name";

}
