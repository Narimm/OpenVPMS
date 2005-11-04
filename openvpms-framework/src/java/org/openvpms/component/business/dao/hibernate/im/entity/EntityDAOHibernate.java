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

package org.openvpms.component.business.dao.hibernate.im.entity;

// spring-framework
import java.util.ArrayList;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

// openvpms-framework
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.dao.im.common.IEntityDAO;
import org.openvpms.component.business.dao.im.common.EntityDAOException;
import org.openvpms.component.business.domain.im.common.Entity;

/**
 * This is a hibernate implementation of the {@link IEntityDAO} interface, which
 * is based on the Spring Framework's {@link HibernateDaoSupport} object.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityDAOHibernate extends HibernateDaoSupport implements IEntityDAO {

    /**
     * Default constructor
     */
    public EntityDAOHibernate() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IEntityDAO#delete(org.openvpms.component.business.domain.im.common.Entity)
     */
    public void delete(Entity entity) {
        try {
            getHibernateTemplate().delete(entity);
        } catch (Exception exception) {
            throw new EntityDAOException(
                    EntityDAOException.ErrorCode.FailedToDeleteEntity,
                    new Object[]{entity});
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IEntityDAO#findById(org.openvpms.component.business.domain.im.common.Entity)
     */
    @SuppressWarnings("unchecked")
    public List get(String rmName, String entityName, String conceptName, String instanceName) {
        try {
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<String> params = new ArrayList<String>();
            boolean andRequired = false;
            
            queryString.append("select entity from org.openvpms.component.business.domain.im.common.Entity as entity");
            
            // check to see if one or more of the values have been specified
            if ((StringUtils.isEmpty(rmName) == false) ||
                (StringUtils.isEmpty(entityName) == false) ||
                (StringUtils.isEmpty(conceptName) == false) ||
                (StringUtils.isEmpty(instanceName) == false)) {
                queryString.append(" where ");
            }
            
            // process the rmName
            if (StringUtils.isEmpty(rmName) == false) {
                names.add("rmName");
                andRequired = true;
                if ((rmName.endsWith("*")) || rmName.startsWith("*")) {
                    queryString.append(" entity.archetypeId.rmName like :rmName");
                    params.add(rmName.replace("*", "%"));
                } else {
                    queryString.append(" entity.archetypeId.rmName = :rmName");
                    params.add(rmName);
                }
                
            }
            
            // process the entity name
            if (StringUtils.isEmpty(entityName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                names.add("entityName");
                andRequired = true;
                if ((entityName.endsWith("*")) || (entityName.startsWith("*"))) {
                    queryString.append(" entity.archetypeId.entityName like :entityName");
                    params.add(entityName.replace("*", "%"));
                } else {
                    queryString.append(" entity.archetypeId.entityName = :entityName");
                    params.add(entityName);
                }
                
            }
            
            // process the concept name
            if (StringUtils.isEmpty(conceptName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                names.add("conceptName");
                andRequired = true;
                if ((conceptName.endsWith("*")) || (conceptName.startsWith("*"))) {
                    queryString.append(" entity.archetypeId.concept like :conceptName");
                    params.add(conceptName.replace("*", "%"));
                } else {
                    queryString.append(" entity.archetypeId.concept = :conceptName");
                    params.add(conceptName);
                }
            }
            
            // process the instance name
            if (StringUtils.isEmpty(instanceName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                names.add("instanceName");
                if ((instanceName.endsWith("*")) || (instanceName.startsWith("*"))) {
                    queryString.append(" entity.name like :instanceName");
                    params.add(instanceName.replace("*", "%"));
                } else {
                    queryString.append(" entity.name = :instanceName");
                    params.add(instanceName);
                }
                
            }
            
            // now execute te query
           return getHibernateTemplate().findByNamedParam(
                   queryString.toString(),
                   (String[])names.toArray(new String[names.size()]),
                   params.toArray());
        } catch (Exception exception) {
            throw new EntityDAOException(
                    EntityDAOException.ErrorCode.FailedToFindEntities,
                    new Object[]{rmName, entityName, conceptName, instanceName},
                    exception);
        }
    }

    
    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IEntityDAO#getById(long)
     */
    public Entity getById(long id) {
        try {
            List results = getHibernateTemplate()
                .findByNamedQueryAndNamedParam("entity.getEntityById",
                    new String[] { "uid" },
                    new Object[] {new Long(id)});
            
            if (results.size() > 1) {
                throw new EntityDAOException(
                    EntityDAOException.ErrorCode.MultipleInstances,
                    new Object[]{id});
            } else if (results.size() == 0) {
                return null;
            } else {
                return (Entity)results.get(0);
            }
        } catch (Exception exception) {
            throw new EntityDAOException(
                    EntityDAOException.ErrorCode.FailedToFindEntity,
                    new Object[]{id}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IEntityDAO#insert(org.openvpms.component.business.domain.im.common.Entity)
     */
    public void insert(Entity entity) {
        try{
            getHibernateTemplate().save(entity);
        } catch (Exception exception) {
            throw new EntityDAOException(
                    EntityDAOException.ErrorCode.FailedToInsertEntity,
                    new Object[]{entity}, exception);
        }
}

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IEntityDAO#update(org.openvpms.component.business.domain.im.common.Entity)
     */
    public void update(Entity entity) {
        try {
            getHibernateTemplate().saveOrUpdate(entity);
        } catch (Exception exception) {
            throw new EntityDAOException(
                    EntityDAOException.ErrorCode.FailedToUpdateEntity,
                    new Object[]{entity}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IEntityDAO#save(org.openvpms.component.business.domain.im.common.Entity)
     */
    public void save(Entity entity) {
        try {
            getHibernateTemplate().saveOrUpdate(entity);
        } catch (Exception exception) {
            throw new EntityDAOException(
                    EntityDAOException.ErrorCode.FailedToSaveEntity,
                    new Object[]{entity}, exception);
        }
    }
}
