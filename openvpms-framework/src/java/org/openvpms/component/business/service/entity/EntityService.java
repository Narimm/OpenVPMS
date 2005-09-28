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
import org.openvpms.component.business.dao.im.common.IEntityDAO;
import org.openvpms.component.business.dao.im.common.EntityDAOException;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeRecord;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * This is an implementation of a generic entity service, which provides basic
 * CRUD functionality. The user should extend this class if the specific entity
 * service requires additional functionality.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityService implements IEntityService {
    /**
     * Cache a reference to an archetype archetype service..
     */
    private IArchetypeService archetypeService;
    
    /**
     * The DAO instance it will use 
     */
    private IEntityDAO dao;
    
    /**
     * Instantiate an instance of this service passing it a reference to the 
     * archetype servie and a DAO.
     * <p>
     * The archetype service is mandatory, since it provides a means of mapping
     * a name (i.e. party.person) to a archetype identifier and retrieving 
     * various archetype information
     * <p>
     * The data access object is required to interact with persistent or non-
     * persistent store. The store is used to cache the data.
     * 
     * @param archetypeService
     *            the archetype service reference
     * @param dao
     *            the reference to the data access object it will use for the 
     *            service             
     * @throws EntityServiceException
     *             a runtime exception that is raised if the service cannot be
     *             instatiated
     */
    public EntityService(IArchetypeService archetypeService, IEntityDAO dao) {
        this.archetypeService = archetypeService;
        this.dao = dao;
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#create(java.lang.String)
     */
    public Entity create(String shortName) {
        // ensure that we can retrieve an arhetype record for the
        // specified short name
        ArchetypeRecord record = archetypeService.getArchetypeRecord(shortName);
        if (record == null) {
            throw  new EntityServiceException(
                    EntityServiceException.ErrorCode.FailedToLocateArchetype,
                    new Object[] { shortName });
        }
        
        // create and return the party object
        return (Entity)archetypeService.createDefaultObject(record.getArchetypeId());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#insert(org.openvpms.component.business.domain.im.common.Entity)
     */
    public void insert(Entity entity) {
        if (archetypeService.validateObject(entity)) {
            try {
                dao.insert(entity);
            } catch (EntityDAOException exception) {
                throw new EntityServiceException(
                        EntityServiceException.ErrorCode.FailedToCreateEntity,
                        new Object[]{entity.toString()}, exception);
            }
        } else {
            throw new EntityServiceException(
                    EntityServiceException.ErrorCode.InvalidEntityObject,
                    new Object[]{entity, entity.getArchetypeId()});
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#findEntities(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public Entity[] findEntities(String rmName, String entityName, String conceptName, String instanceName) {
        try {
            return dao.find(rmName, entityName, conceptName, instanceName);
        } catch (EntityDAOException exception) {
            throw new EntityServiceException(
                    EntityServiceException.ErrorCode.FailedToFindEntity,
                    new Object[]{rmName, entityName, conceptName, instanceName}, 
                    exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#remove(org.openvpms.component.business.domain.im.common.Entity)
     */
    public void remove(Entity entity) {
        try {
            dao.delete(entity);
        } catch (EntityDAOException exception) {
            throw new EntityServiceException(
                    EntityServiceException.ErrorCode.FailedToDeleteEntity,
                    new Object[]{entity.getArchetypeId().toString(),
                            entity.toString()}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#update(org.openvpms.component.business.domain.im.common.Entity)
     */
    public void update(Entity entity) {
        if (archetypeService.validateObject(entity)) {
            try {
                dao.update(entity);
            } catch (EntityDAOException exception) {
                throw new EntityServiceException(
                        EntityServiceException.ErrorCode.FailedToUpdateEntity,
                        new Object[]{entity.toString()}, exception);
            }
        } else {
            throw new EntityServiceException(
                    EntityServiceException.ErrorCode.FailedToUpdateEntity,
                    new Object[]{entity.getArchetypeId().toString(),
                            entity.toString()});
        }
    }
}
