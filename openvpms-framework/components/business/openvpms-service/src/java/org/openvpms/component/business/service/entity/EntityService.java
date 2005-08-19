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

// openvpms-orm
import org.openvpms.component.business.domain.im.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeRegistry;

// openvpms-service
import org.openvpms.component.system.dao.im.IEntityDAO;

/**
 * This is an implementation of a generic entity service, which provides basic
 * CRUD functionality. The user should extend this class if the specific entity
 * service requires additional functionality.
 * <p>
 * The following code snippet illustrates how to create an instance of a types
 * entity service
 * <p>
 * <code>
 * PersonService pService = new EntityService<Person>(registry);
 * </code>
 * 
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityService<T extends Entity> implements IServiceInterface<T> {
    /**
     * Cache a reference to an archetype registry. This is madatory for the 
     * service to operate correctly.
     */
    private IArchetypeRegistry registry;
    
    /**
     * The DAO instance it will use 
     */
    private IEntityDAO<T> dao;
    
    /**
     * Instantiate an instance of this service passing it a registry, the dao
     * and a reference to the terminology service.
     * <p>
     * The regitry reference is mandatory, since it provides a means of mapping
     * a name (i.e. party.person) to a archetype identifier.
     * <p>
     * The data access object is required to interact with persistent or non-
     * persistent store. The store is used to cache the data.
     * <p>
     * Finally, the terminology service is a means for accessing reference
     * information, such as the list of specifies or the list of breeds for a
     * particular specie.
     * 
     * @param registry
     *            the archetype registry
     * @param dao
     *            the reference to the data access object it will use for the 
     *            service             
     * @throws ServiceException
     *             a runtime exception tha is raised if the service cannot be
     *             instatiated
     */
    public EntityService(IArchetypeRegistry registry, IEntityDAO<T> dao) {
        this.registry = registry;
        this.dao = dao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.IServiceInterface#createEntity(java.lang.String)
     */
    public T createEntity(String type) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.IServiceInterface#findEntities(java.lang.String,
     *      java.lang.Object[])
     */
    public T[] findEntities(String searchCriteria, Object[] searchParams)
            throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.IServiceInterface#removeEntity(T)
     */
    public void removeEntity(T entity) throws ServiceException {
        try {
            dao.delete(entity);
        } catch (Exception exception) {
            throw new ServiceException(ServiceException.ErrorCode.FailedToDeleteEntity,
                    new Object[] {entity.getClass().getName(), "removeEntity"}, 
                    exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.IServiceInterface#updateEntity(T)
     */
    public void updateEntity(T entity) throws ServiceException {
        try {
            dao.update(entity);
        } catch (Exception exception) {
            throw new ServiceException(ServiceException.ErrorCode.FailedToUpdateEntity,
                    new Object[] {entity.getClass().getName(), "updateEntity"}, 
                    exception);
        }
    }

    /**
     * @return Returns the registry.
     */
    protected IArchetypeRegistry getRegistry() {
        return registry;
    }
}
