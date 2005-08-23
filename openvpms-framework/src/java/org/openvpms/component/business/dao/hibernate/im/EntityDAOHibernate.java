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

package org.openvpms.component.business.dao.hibernate.im;

import org.openvpms.component.business.dao.im.EntityDAOException;
import org.openvpms.component.business.dao.im.IEntityDAO;
import org.openvpms.component.business.domain.im.Entity;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * This is a basic DAO implementation, which provides simple CRUD features for
 * {@link Entity} objects. If a particular entity requires additional features
 * then simply subclass of this class.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityDAOHibernate<T extends Entity> extends HibernateDaoSupport 
        implements IEntityDAO<T> {

    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#insert(T)
     */
    public void insert(T entity) throws EntityDAOException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#update(T)
     */
    public void update(T entity) throws EntityDAOException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#delete(T)
     */
    public void delete(T entity) throws EntityDAOException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#findById(java.lang.String)
     */
    public T findById(String id) throws EntityDAOException {
        // TODO Auto-generated method stub
        return null;
    }
}
