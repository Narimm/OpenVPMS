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
 *  $Id: IPartyService.java 87 2005-09-08 11:07:07Z jalateras $
 */

package org.openvpms.component.business.service.entity;

// openvpms-framework
import org.openvpms.component.business.domain.im.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.party.PartyServiceException;


/**
 * This service interface, provides standard CRUD (create, retrieve, update and 
 * delete) functionality.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-09-08 21:07:07 +1000 (Thu, 08 Sep 2005) $
 */
public interface IEntityService {
    /**
     * Create an Entity using the specified shortName. The short name is a 
     * reference to the archetype that is used to create the object. An archetype 
     * restricts the instances of the domain class by declaring constraints on 
     * it.
     * <p>
     * The returned object is a default (but not necessarily valid) representation
     * of the archetype. The returned object is transient
     * 
     * @param shortName
     *            the short name to the archetype
     * @return Party
     * @throws EntityServiceException
     *             a runtime exception
     * 
     */
    public Party createEntity(String shortName);

    /**
     * Insert the specified {@link Entity}. This service is now responsible for
     * managing the Entity.
     * 
     * @param party
     *            the party to insert
     * @throws EntityServiceException                      
     */
    public void insertEntity(Entity entity);
    
    /**
     * Remove the specified Entity. If the Entity cannot be removed for whatever
     * reason then raise a {@link EntityServiceException} is raised.
     * 
     * @param party
     *            the party to remove
     * @throws EntityServiceException
     *             a runtime exception
     */
    public void removeEntity(Entity entity);

    /**
     * Update the specified Entity. The Entity is validated against its
     * archetype before it is updated. If the Entity is invalid or it cannot be 
     * updated the {@link EntityServiceException} exception is thrown.
     * <p>
     * The updateEntity method implies both save and upate semantics
     * 
     * @param entity
     *            the entity to update
     * @throws EntityServiceException
     *             a runtime exception
     */
    public void updateEntity(Entity entity);

    /**
     * Retrieve all the entities that match the specified criteria. The criteria
     * can be a a query or a reference to a declared query. The parameters are
     * used to constrain the query.
     * <p>
     * This method will return zero, one or more entities, that match the
     * specified criteria.
     * <p>
     * If there is an error in the query, or the passed in parameters or the
     * query fails to execute then raise a {@link EntityServiceException}.
     * 
     * @param searchCriteria
     *            the search crtieria or reference to a search
     * @param searchParams
     *            the parameters used to constrain the search
     * @return Entity[] an array (which may be empty} of matching entries
     * @throws EntityServiceException
     *             a runtime exception if the request cannot be serviced
     */
    public Entity[] findEntities(String searchCriteria, Object[] searchParams);
}
