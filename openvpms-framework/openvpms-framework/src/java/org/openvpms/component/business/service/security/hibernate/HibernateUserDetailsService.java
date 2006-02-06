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


package org.openvpms.component.business.service.security.hibernate;

// java core
import java.util.List;

// acegi-security
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;

// spring-dao
import org.springframework.dao.DataAccessException;

// openvpms-framework
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;

/**
 * This is the user details services, that is used by the acegi security
 * framework. It is used to retrieve the user details including credentials
 * and authorizations.
 * <p>
 * This will delegate to the request to the hibernate DAO.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class HibernateUserDetailsService implements UserDetailsService {
    /**
     * The DAO instance it will use
     */
    private IMObjectDAO dao;


    /**
     * Must instantiate an instance of this class with the specified dao
     * 
     * @param dao
     *            the dao to use
     */
    public HibernateUserDetailsService(IMObjectDAO dao) {
        this.dao = dao;
    }
    
    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        List<IMObject> user = dao.get("system", "security",
                "user", username, User.class.getName(), true);
        if (user.size() == 0) {
            throw new UsernameNotFoundException("User: " + username + 
                    " is invalid.");
        } else if (user.size() > 1) {
            throw new UsernameNotFoundException("Multiple users with user name: " 
                    + username); 
        } 
        
        return (UserDetails)user.get(0);
    }
}
