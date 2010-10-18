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

package org.openvpms.archetype.rules.user;


/**
 * User archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserArchetypes {

    /**
     * Default user.
     */
    public static final String USER = "security.user";

    /**
     * User for ESCI web services.
     */
    public static final String ESCI_USER = "user.esci";

    /**
     * User archetype short names.
     */
    public static final String[] USER_ARCHETYPES = {USER, ESCI_USER};

    /**
     * Clinician participation.
     */
    public static final String CLINICIAN_PARTICIPATION
            = "participation.clinician";

    /**
     * Author participation.
     */
    public static final String AUTHOR_PARTICIPATION = "participation.author";
}
