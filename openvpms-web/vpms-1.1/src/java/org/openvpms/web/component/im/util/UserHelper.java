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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.util;

import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.security.User;


/**
 * User helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserHelper {

    /**
     * Determines if an user is an administrator.
     *
     * @param user the user. May be <tt>null</tt>
     * @return <tt>true<//tt> if <tt>user</tt> is an administrator; othwerwise
     *         <tt>false</tt>
     */
    public static boolean isAdmin(User user) {
        if (user != null) {
            UserRules rules = new UserRules();
            return rules.isAdministrator(user);
        }
        return false;
    }
}
