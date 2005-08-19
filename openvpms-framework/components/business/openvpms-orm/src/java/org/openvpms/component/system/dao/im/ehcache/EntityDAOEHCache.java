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

package org.openvpms.component.system.dao.im.ehcache;

// spring-orm
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

// ehcache
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.CacheManager;

// openvpms-domain
import org.openehr.rm.support.identification.ObjectID;
import org.openvpms.component.business.domain.im.Entity;

// openvpms-service
import org.openvpms.component.system.dao.im.EntityDAOException;
import org.openvpms.component.system.dao.im.IEntityDAO;

/**
 * This is an ehcache implementation of a DAO. This implementation is really
 * only useful for testing. The ehcache component is a simple local cacheManager 
 * implementation that also has the ability to overflow to disk.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityDAOEHCache<T extends Entity> extends HibernateDaoSupport 
        implements IEntityDAO<T> {

    /**
     * The cache manager 
     */
    private CacheManager cacheManager;
    
    /**
     * Create a default instance of the cacheManager 
     */
    public EntityDAOEHCache() 
    throws EntityDAOException {
        try {
            cacheManager = CacheManager.create();
        } catch (Exception exception) {
            throw new EntityDAOException(EntityDAOException.ErrorCode.FailedToInitializeService,
                    new Object[] {"EntityDAOEHCache"}, exception);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#insert(T)
     */
    public void insert(T entity) throws EntityDAOException {
        getCache().put(new Element(entity.getUid(), entity));
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#update(T)
     */
    public void update(T entity) throws EntityDAOException {
        getCache().put(new Element(entity.getUid(), entity));
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#delete(T)
     */
    public void delete(T entity) throws EntityDAOException {
        getCache().remove(entity.getUid());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.dao.im.IEntityDAO#findById(java.lang.String)
     */
    public T findById(String id) throws EntityDAOException {
        try {
            return (T)getCache().get(id).getValue();
        } catch (Exception exception) {
            throw new EntityDAOException(EntityDAOException.ErrorCode.FailedToFindEntities,
                    new Object[] {id}, exception);
        }
    }
    
    /**
     * Return the appropriate cacheManager for this entity type
     * 
     * @return
     */
    protected Cache getCache() {
        return cacheManager.getCache(this.getClass().getName());
    }
}
