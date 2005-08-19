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

package org.openvpms.component.system.dao.im;

// openvpms-domain
import org.openvpms.component.business.domain.im.Entity;

/**
 * This interface provides generic data access object (DAO) support for objects
 * of type {@link Entity}. The class includes the caoability to perform insert,
 * delete, update and remove data.
 * <p>
 * DAO's that require additional features should extend this class
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IEntityDAO<T extends Entity> {
    /**
     * Insert the specified {@link Entity}.
     * 
     * @param entity
     *            the entity to insert
     * @throws EntityDAOException
     *             if the request cannot be completed
     */
    public void insert(T entity) throws EntityDAOException;

    /**
     * Update the specified {@link Entity}
     * 
     * @param entity
     *            the entity to update
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public void update(T entity) throws EntityDAOException;

    /**
     * Delete the specified {@link Entity}
     * 
     * @param entity
     *            the entity to delete
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public void delete(T entity) throws EntityDAOException;

    /**
     * Retrieve the entity with the specified id
     * 
     * @param id
     *            the id of the entity to retrieve
     * @param return
     *            Entity
     * @throws EntityDAOException
     *             a runtime exception if the request cannot complete
     */
    public T findById(String id) throws EntityDAOException;
}
