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

package org.openvpms.archetype.rules.user;


/**
 * User archetypes.
 *
 * @author Tim Anderson
 */
public class UserArchetypes {

    /**
     * Default user.
     */
    public static final String USER = "security.user";

    /**
     * User group.
     */
    public static final String GROUP = "entity.userGroup";

    /**
     * User archetype short names.
     */
    public static final String[] USER_ARCHETYPES = {USER};

    /**
     * Clinician participation.
     */
    public static final String CLINICIAN_PARTICIPATION
            = "participation.clinician";

    /**
     * Author participation.
     */
    public static final String AUTHOR_PARTICIPATION = "participation.author";

    /**
     * User type.
     */
    public static final String USER_TYPE = "lookup.userType";

    /**
     * Classification code for <em>lookup.userType</em> indicating that a user is an administrator.
     */
    public static final String ADMINISTRATOR_USER_TYPE = "ADMINISTRATOR";

    /**
     * Classification code for <em>lookup.userType</em> indicating that a user is a clinician.
     */
    public static final String CLINICIAN_USER_TYPE = "CLINICIAN";
}
