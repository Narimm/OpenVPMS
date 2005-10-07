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
    public List find(String rmName, String entityName, String conceptName, String instanceName) {
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
                if (rmName.endsWith("*")) {
                    queryString.append(" entity.archetypeId.rmName like :rmName");
                } else {
                    queryString.append(" entity.archetypeId.rmName = :rmName");
                }
                
                names.add("rmName");
                params.add(rmName);
                andRequired = true;
            }
            
            // process the entity name
            if (StringUtils.isEmpty(entityName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                if (entityName.endsWith("*")) {
                    queryString.append(" entity.archetypeId.entityName like :entityName");
                } else {
                    queryString.append(" entity.archetypeId.entityName = :entityName");
                }
                
                names.add("entityName");
                params.add(entityName);
                andRequired = true;
            }
            
            // process the concept name
            if (StringUtils.isEmpty(conceptName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                if (entityName.endsWith("*")) {
                    queryString.append(" entity.archetypeId.concept like :conceptName");
                } else {
                    queryString.append(" entity.archetypeId.concept = :conceptName");
                }
                
                names.add("conceptName");
                params.add(conceptName);
                andRequired = true;
            }
            
            // process the instance name
            if (StringUtils.isEmpty(instanceName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                if (entityName.endsWith("*")) {
                    queryString.append(" entity.name like :instanceName");
                } else {
                    queryString.append(" entity.name = :instanceName");
                }
                
                names.add("instanceName");
                params.add(instanceName);
                andRequired = true;
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
