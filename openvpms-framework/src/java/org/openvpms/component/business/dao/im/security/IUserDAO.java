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

package org.openvpms.component.business.dao.im.security;

import java.util.List;

import org.openvpms.component.business.domain.im.audit.AuditRecord;
import org.openvpms.component.business.domain.im.security.User;

/**
 * This interface provides data access object (DAO) support for objects of 
 * type {@link User}. The class includes the capability to perform save 
 * and query data. 
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IUserDAO {

	/**
     * Retrieve the {@link User} with the specified name
     * 
     * @param name
     *            the user name
     * @return List<User>
     * @throws UserDAOException            
     */
    public List<User> getByUserName(String name);
    

}
