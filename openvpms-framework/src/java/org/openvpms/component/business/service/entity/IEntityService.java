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

// openvpms-framework
import java.util.List;

import org.openvpms.component.business.domain.im.common.Entity;


/**
 * This service interface, provides standard CRUD (create, retrieve, update and 
 * delete) functionality for entities. It is a generic service that can be used
 * to operate on subtypes.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IEntityService {
    /**
     * Create an entity using the specified shortName. The short name is a 
     * reference to the archetype that is used to create the object. An archetype 
     * restricts the instances of the domain class by declaring constraints on 
     * it.
     * <p>
     * The returned object is a default (but not necessarily valid) representation
     * of the archetype. The returned object is transient
     * 
     * @param shortName
     *            the short name to the archetype
     * @return Entity
     * @throws EntityServiceException
     *             a runtime exception
     * 
     */
    public Entity create(String shortName);

    /**
     * Insert the specified {@link Entity}. This service is now responsible for
     * managing the entity.
     * 
     * @param entity
     *            the entity to insert
     * @throws EntityServiceException                      
     */
    public void insert(Entity entity);
    
    /**
     * Remove the specified entity. If the entity cannot be removed for whatever
     * reason then raise a {@link EntityServiceException}.
     * 
     * @param entity
     *            the entity to remove
     * @throws EntityServiceException
     *             a runtime exception
     */

    public void remove(Entity entity);

    /**
     * The save should be used in preference to the {@link #insert(Entity)} or
     * {@link #update(Entity)} methods. This will check that whether the entity
     * is new and if it is do an insert otherwise do an update.
     * 
     * @param entity
     *            the entity to insert or update
     * @throws EntityServiceException
     *             a runtime exception
     */
    public void save(Entity entity);
    
    /**
     * Update the specified entity. The entity is validated against its
     * archetype before it is updated. If the entity is invalid or it cannot be 
     * updated the {@link EntityServiceException} exception is thrown.
     * <p>
     * The updateEntity method implies both save and upate semantics
     * 
     * @param entity
     *            the entity to update
     * @throws EntityServiceException
     *             a runtime exception
     */
    public void update(Entity entity);

    /**
     * Uses the specified criteria to return zero, one or more matching . 
     * entities. This is a very generic query which will constrain the 
     * returned set on one or more of the supplied values.
     * <p>
     * Each of the parameters can denote an exact match or a partial match. If
     * a partial match is required then the last character of the value must be
     * a '*'. In every other case the search will look for an exact match.
     * <p>
     * All the values are optional. In the case where all the values are null
     * then all the entities will be returned. In the case where two or more 
     * values are specified (i.e. rmName and entityName) then only entities 
     * satisfying both conditions will be returned.
     * 
     * @param rmName
     *            the reference model name (partial or complete)
     * @param entityName
     *            the name of the entity
     * @param concept
     *            the concept name
     * @param instanceName
     *            the particular instance name
     * @return List                                   
     * @throws EntityServiceException
     *            a runtime exception                         
     */
    public List get(String rmName, String entityName, 
            String conceptName, String instanceName);
    
    /**
     * Return a list of {@link Entity} instances with the specified 
     * short name
     * 
     * @param shortName
     *            the short name
     * @return List
     * @throws EntityServiceException            
     */
    public List getByShortName(String shortName);
}

