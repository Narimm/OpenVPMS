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


package org.openvpms.component.business.service.security;

// java core

import java.util.List;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.openvpms.component.business.dao.im.security.IUserDAO;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.dao.DataAccessException;


/**
 * This is the user details services, used by the acegi security framework.
 * It is used to retrieve the user details including credentials and
 * authorizations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class UserService implements UserDetailsService {

    /**
     * The DAO used for persisting records
     */
    private IUserDAO dao;
    
    /**
     * Constructs a new <code>UserService</code.
     *
     * @param service the archetype service to use
     */
    public UserService(IUserDAO dao) {
        this.dao = dao;
    }

	public IUserDAO getDao() {
		return dao;
	}

	public void setDao(IUserDAO dao) {
		this.dao = dao;
	}

	/* (non-Javadoc)
    * @see org.acegisecurity.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
    */
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
    	List<User> users = null;
    	try {
            users = dao.getByUserName(username);    		
    	} catch (Exception exception) {
            throw new UsernameNotFoundException("User: " + username +
            " is invalid.");    		
    	}
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User: " + username +
                    " is invalid.");
        } else if (users.size() > 1) {
            throw new UsernameNotFoundException(
                    "Multiple users with user name: " + username);
        }

        return (UserDetails) users.get(0);
    }

}
