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

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Properties;

/**
 * This is an in-memory user details manager, which is primarily useful for
 * testing the authorization.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class InMemoryUserDetailsService implements UserDetailsService {
    /**
     * holds a list of declared users
     */
    private UserMap userMap;
    
    
    /**
     * Default constructor
     */
    public InMemoryUserDetailsService() {
        // no op
    }
    
    /**
     * @return Returns the userMap.
     */
    public UserMap getUserMap() {
        return userMap;
    }

    /**
     * @param userMap The userMap to set.
     */
    public void setUserMap(UserMap userMap) {
        this.userMap = userMap;
    }

    /**
     * Set the user map from a property list.
     * 
     * @param props
     *            a property list
     */
    public void setUserMap(Properties props) {
        UserMap userMap = new UserMap();
        this.userMap = UserMapEditor.addUsersFromProperties(userMap, props);
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        return userMap.getUser(username);
    }

}