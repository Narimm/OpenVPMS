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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.util;

import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.system.ServiceHelper;


/**
 * User helper.
 *
 * @author Tim Anderson
 */
public class UserHelper {

    /**
     * Determines if an user is an administrator.
     *
     * @param user the user. May be {@code null}
     * @return {@code true<//tt> if {@code user} is an administrator; otherwise {@code false}
     */
    public static boolean isAdmin(User user) {
        if (user != null) {
            UserRules rules = ServiceHelper.getBean(UserRules.class);
            return rules.isAdministrator(user);
        }
        return false;
    }
}
