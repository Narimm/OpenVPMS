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

package org.openvpms.component.business.service.entity;

// openvpms-domain
import org.openvpms.component.business.domain.im.Entity;

/**
 * This is a generic service interface, which provides standard CRUD (create,
 * retrieve, u[date and delete) functionality.
 * <p>
 * This interface uses generics.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IEntityService<T extends Entity> {
    /**
     * Create an entity of the specified type. The type defines the archetype
     * that will be created. An archetype restricts the instances of the domain
     * class by declaring constraints on it.
     * <p>
     * The specified type is mapped to an actual archetype through an
     * {@link org.openvpms.component.business.service.archetype.IArchetypeRegistry}.
     * The entries within the registry are responsible for mapping the archetype
     * to the appropriate domain class object.
     * <p>
     * The returned object is a default (but not necessarily valid) representation
     * of the archetype. The returned object is transient
     * 
     * @param archetype
     *            the archetype to create
     * @return T a empty archetype with the appropriate default entries
     * @throws UUIDServiceException
     *             a runtime exception
     * 
     */
    public T createEntity(String archetype) throws EntityServiceException;

    /**
     * Remove the specified entity. If the entity cannot be removed for whatever
     * reason then raise a {@link UUIDServiceException}
     * 
     * @param entity
     *            the entity to remove
     * @throws UUIDServiceException
     *             a runtime exception
     */
    public void removeEntity(T entity) throws EntityServiceException;

    /**
     * Update the specified entity. The entity is validated against its
     * archetype before it is updated. If the entity is invalid or it cannot be 
     * updated the {@link UUIDServiceException} excpetion is thrown.
     * <p>
     * The updateEntity method implies both save and upate semantics
     * 
     * @param entity
     *            the entity to update
     * @throws UUIDServiceException
     *             a runtime exception
     */
    public void updateEntity(T entity) throws EntityServiceException;

    /**
     * Retrieve all the entities that match the specified criteria. The criteria
     * can be a a query or a reference to a declared query. The parameters are
     * used to constrain the query.
     * <p>
     * This method will return zero, one or more entities, that match the
     * specified criteria.
     * <p>
     * If there is an error in the query, or the passed in parameters or the
     * query fails to execute then raise a {@link UUIDServiceException}.
     * 
     * @param searchCriteria
     *            the search crtieria or reference to a search
     * @param searchParams
     *            the parameters used to constrain the search
     * @return T[] an array (which may be empty} of matching entries
     * @throws UUIDServiceException
     *             a runtime exception if the request cannot be serviced
     */
    public T[] findEntities(String searchCriteria, Object[] searchParams)
            throws EntityServiceException;
}
