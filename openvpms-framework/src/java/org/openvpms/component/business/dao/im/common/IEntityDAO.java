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

package org.openvpms.component.business.dao.im.common;

// openvpms-domain
import java.util.List;

import org.openvpms.component.business.domain.im.common.Entity;

/**
 * This interface provides data access object (DAO) support for objects of 
 * type {@link Entity}. The class includes the capability to perform insert,
 * delete, update and remove data.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IEntityDAO {
    /**
     * Insert the specified {@link Entity}.
     * 
     * @param entity
     *            the entity to insert
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public void insert(Entity entity);

    /**
     * Update the specified {@link Entity}
     * 
     * @param entity
     *            the entity to update
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public void update(Entity entity);

    /**
     * This method can be used to do a insert or an update of the entity. This 
     * method should be called in preference to {@link #insert(Entity)} or
     * {@link #update(Entity)}. 
     * 
     * @param entity
     *            the entity to save
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public void save(Entity entity);

    /**
     * Delete the specified {@link Entity}
     * 
     * @param entity
     *            the entity to delete
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public void delete(Entity entity);

    /**
     * Retrieve all the entities that matches the specified search criteria.
     * This is a very generic method that provides a mechanism to return 
     * entities based on, one or more criteria.
     * <p>
     * All parameters are optional and can either denote an exact or partial
     * match semantics. If a parameter ends with '*' then it will do a partial
     * match and return all entities that start with the specified value. 
     * Alternatively, if a parameter does not end with a '*' then it will only
     * return entities with the exact value. 
     * <p>
     * If two or more parameters are specified then it will return entities
     * that match both criteria.
     * 
     * @param rmName
     *            the reference model name
     * @param entityName
     *            the entity name
     * @param conceptName
     *            the concept name
     * @param instanceName
     *            the instance name                                    
     * @param return
     *            List
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public List find(String rmName, String entityName, String conceptName, 
            String instanceName);
}
