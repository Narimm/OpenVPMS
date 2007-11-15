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

package org.openvpms.archetype.rules.user;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;

import java.util.Iterator;


/**
 * User rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserRules {

    /**
     * Returns the user with the specified username (login name).
     *
     * @param username the user name
     * @return the corresponding user, or <tt>null</tt> if none is found
     */
    public User getUser(String username) {
        ArchetypeQuery query = new ArchetypeQuery("security.user",
                                                  true, true);
        query.add(new NodeConstraint("username", username));
        query.setMaxResults(1);
        Iterator<User> iterator = new IMObjectQueryIterator<User>(query);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Determines if a user is a clinician.
     *
     * @param user the user
     * @return <tt>true</tt> if the user is a clinician,
     *         otherwise <tt>false</tt>
     */
    public boolean isClinician(User user) {
        for (Lookup lookup : user.getClassifications()) {
            if (TypeHelper.isA(lookup, "lookup.userType")
                    && "CLINICIAN".equals(lookup.getCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a user has administrator priviledges.
     * TODO - needs to be updated for OVPMS-702.
     *
     * @return <tt>true</tt> if the user is an administrator
     */
    public boolean isAdministrator(User user) {
        return "admin".equals(user.getUsername());
    }
}
