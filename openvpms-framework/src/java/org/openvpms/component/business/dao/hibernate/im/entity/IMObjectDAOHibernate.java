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

// java core
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//spring-framework
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

// commons-lang
import org.apache.commons.lang.StringUtils;

//openvpms-framework
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * This is an implementation of the IMObject DAO for hibernate. It uses the
 * Spring Framework's template classes.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class IMObjectDAOHibernate extends HibernateDaoSupport implements
        IMObjectDAO {

    /**
     *  Default constructor
     */
    public IMObjectDAOHibernate() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#save(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void save(IMObject object) {
        try {
            getHibernateTemplate().saveOrUpdate(object); 
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToSaveIMObject,
                    new Object[]{new Long(object.getUid())}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#delete(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void delete(IMObject object) {
        try {
            getHibernateTemplate().delete(object);
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToDeleteIMObject,
                    new Object[]{new Long(object.getUid())});
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<IMObject> get(String rmName, String entityName,
            String conceptName, String instanceName, String clazz, boolean activeOnly) {
        try {
            // check that rm has been specified
            if (StringUtils.isEmpty(clazz)) {
                throw new IMObjectDAOException(
                        IMObjectDAOException.ErrorCode.ClassNameMustBeSpecified,
                        new Object[]{});
            }
            
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<String> params = new ArrayList<String>();
            boolean andRequired = false;
            
            queryString.append("select entity from ");
            queryString.append(clazz);
            queryString.append(" as entity");
            
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
                andRequired = true;
                if ((instanceName.endsWith("*")) || (instanceName.startsWith("*"))) {
                    queryString.append(" entity.name like :instanceName");
                    params.add(instanceName.replace("*", "%"));
                } else {
                    queryString.append(" entity.name = :instanceName");
                    params.add(instanceName);
                }
            }
            
            // determine if we are only interested in active objects
            if (activeOnly) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                queryString.append(" entity.active = true");
            }
            
            // now execute te query
           return getHibernateTemplate().findByNamedParam(
                   queryString.toString(),
                   (String[])names.toArray(new String[names.size()]),
                   params.toArray());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindIMObjects,
                    new Object[]{rmName, entityName, conceptName, instanceName},
                    exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getByArchetypeShortNames(java.lang.String[])
     */
    public List<IMObject> getByArchetypeShortNames(String[] shortNames) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getById(org.openvpms.component.business.domain.archetype.ArchetypeId, long)
     */
    public IMObject getById(String clazz, long id) {
        try {
            // check that rm has been specified
            if (StringUtils.isEmpty(clazz)) {
                throw new IMObjectDAOException(
                        IMObjectDAOException.ErrorCode.ClassNameMustBeSpecified,
                        new Object[]{});
            }
            
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<Object> params = new ArrayList<Object>();
            
            queryString.append("select entity from ");
            queryString.append(clazz);
            queryString.append(" as entity where entity.id = :uid");
            names.add("uid");
            params.add(new Long(id));
            
            
            // now execute te query
           List result = getHibernateTemplate().findByNamedParam(
                   queryString.toString(),
                   (String[])names.toArray(new String[names.size()]),
                   params.toArray());
           if (result.size() == 0){
               return null;
           } else {
               return (IMObject)result.get(0);
           }
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindIMObject,
                    new Object[]{clazz, new Long(id)},
                    exception);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getByNamedQuery(java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public List<IMObject> getByNamedQuery(String name, Map<String, Object> params) {
        List<IMObject> results = null;
        try {
            String[] paramNames = (String[])params.keySet().toArray(
                    new String[params.size()]);
            Object[] paramValues = params.values().toArray();
            
            results = getHibernateTemplate().findByNamedQueryAndNamedParam(
                    name, paramNames, paramValues);
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToExecuteNamedQuery,
                    new Object[]{name}, exception);
        }
        
        return results;
    }
    
}
