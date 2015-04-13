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


package org.openvpms.component.business.service.security.memory;

// java-core
import java.util.HashMap;
import java.util.Map;

import org.openvpms.component.business.domain.im.security.User;

/**
 * Contain for {@link User} objects.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class UserMap {
    /**
     * caches all the users
     */
    private Map<String, User> userMap = new HashMap<String, User>();
    
    /**
     * Default constructor
     */
    public UserMap() {
        // no op
    }
    
    /**
     * Return all the {@link User} in the cache
     * 
     * @return User[]
     */
    public User[] getUsers() {
        return (User[])userMap.values().toArray(new User[userMap.size()]);
    }
    
    /**
     * Return the {@link User} instance for the specified username
     * 
     * @param username
     *            the username to search for
     * @return User
     *            the User or null if one does not exist            
     */
    public User getUser(String username) {
        return userMap.get(username);
    }
    
    /**
     * Add a new {@link User} to the cache
     * 
     * @param user
     *            the user to add
     */
    public void addUser(User user) {
        userMap.put(user.getUsername(), user);
    }
}
