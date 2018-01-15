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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.util;

import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.app.Context;
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

    /**
     * Determines if a user is a clinician.
     *
     * @param user the user. May be {@code null}
     * @return {@code true} if the user is a clinician
     */
    public static boolean isClinician(User user) {
        return (user != null && ServiceHelper.getBean(UserRules.class).isClinician(user));
    }

    /**
     * Determines if the logged in user should be used for clinician fields.
     * This returns {@code true} if the context has:
     * <ul>
     * <li> a practice, with useLoggedInClinician set; and</li>
     * <li> a user, who is a clinician</li>
     * </ul>
     *
     * @param context the context
     * @return {@code true} if the context user should be used for clinician fields
     */
    public static boolean useLoggedInClinician(Context context) {
        boolean result = false;
        User user = context.getUser();
        Party practice = context.getPractice();
        if (user != null && practice != null) {
            if (new IMObjectBean(practice).getBoolean("useLoggedInClinician") && isClinician(user)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the name for a user, given their login name.
     *
     * @param loginName the login name. May be {@code null}
     * @return the name, or {@code loginName} if the user doesn't exist
     */
    public static String getName(String loginName) {
        String result = null;
        if (loginName != null) {
            User user = ServiceHelper.getBean(UserRules.class).getUser(loginName);
            result = user != null ? user.getName() : loginName;
        }
        return result;
    }

}
