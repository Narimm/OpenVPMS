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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.service.archetype.assertion;

import java.util.Map;

/**
 * These assertions are applied to archetype and parts of archetypes. These
 * are all static functions that take an object and property map as arguments
 * and return a boolean as a result.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeAssertions {

    /**
     * Default constructor
     */
    public ArchetypeAssertions() {
    }

    /**
     * 
     */
    public static boolean isArchetypeShortNameInRange(Object target, Map properties) {
        return true;
    }
}
