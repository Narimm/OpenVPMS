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

// spring-framework
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

// commons-lang
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

// openvpms-framework
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

/**
 * This is an implementation of the IMObject DAO for hibernate. It uses the
 * Spring Framework's template classes.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectDAOHibernate extends HibernateDaoSupport implements
        IMObjectDAO {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(IMObjectDAOHibernate.class);

    /**
     * Default constructor
     */
    public IMObjectDAOHibernate() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.orm.hibernate3.support.HibernateDaoSupport#createHibernateTemplate(org.hibernate.SessionFactory)
     */
    @Override
    protected HibernateTemplate createHibernateTemplate(
            SessionFactory sessionFactory) {
        HibernateTemplate template = super
                .createHibernateTemplate(sessionFactory);
        template.setCacheQueries(true);
        template.setAllowCreate(true);

        return template;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#save(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void save(IMObject object) {
        try {
            getHibernateTemplate().saveOrUpdate(object);
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToSaveIMObject,
                    new Object[] { new Long(object.getUid()) }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#delete(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void delete(IMObject object) {
        try {
            getHibernateTemplate().delete(object);
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToDeleteIMObject,
                    new Object[] { new Long(object.getUid()) });
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#get(java.lang.String, java.util.Map, int, int)
     */
    @SuppressWarnings("unchecked")
    public IPage<IMObject> get(String queryString, Map<String, Object> valueMap, int firstRow, int numOfRows) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Query String: " + queryString.toString() +
                        "Parameters: " + valueMap.toString());
                
            }
             
            return executeQuery(queryString, new ArrayList<String>(valueMap.keySet()), 
                    new ArrayList<Object>(valueMap.values()), firstRow, numOfRows, 
                    new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                IMObjectDAOException.ErrorCode.FailedToExecuteQuery,
                new Object[] { queryString }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#get(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, boolean, int, int, PagingCriteria,
     *      java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    public IPage<IMObject> get(String rmName, String entityName,
            String conceptName, String instanceName, String clazz,
            boolean activeOnly, int firstRow, int numOfRows) {
        try {
            // check that rm has been specified
            if (StringUtils.isEmpty(clazz)) {
                throw new IMObjectDAOException(
                        IMObjectDAOException.ErrorCode.ClassNameMustBeSpecified,
                        new Object[] {});
            }

            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<Object> params = new ArrayList<Object>();
            boolean andRequired = false;

            queryString.append("from ");
            queryString.append(clazz);
            queryString.append(" as entity");

            // check to see if one or more of the values have been specified
            if ((StringUtils.isEmpty(rmName) == false)
                    || (StringUtils.isEmpty(entityName) == false)
                    || (StringUtils.isEmpty(conceptName) == false)
                    || (StringUtils.isEmpty(instanceName) == false)) {
                queryString.append(" where ");
            }

            // process the rmName
            if (StringUtils.isEmpty(rmName) == false) {
                names.add("rmName");
                andRequired = true;
                if ((rmName.endsWith("*")) || rmName.startsWith("*")) {
                    queryString
                            .append(" entity.archetypeId.rmName like :rmName");
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
                    queryString
                            .append(" entity.archetypeId.entityName like :entityName");
                    params.add(entityName.replace("*", "%"));
                } else {
                    queryString
                            .append(" entity.archetypeId.entityName = :entityName");
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
                if ((conceptName.endsWith("*"))
                        || (conceptName.startsWith("*"))) {
                    queryString
                            .append(" entity.archetypeId.concept like :conceptName");
                    params.add(conceptName.replace("*", "%"));
                } else {
                    queryString
                            .append(" entity.archetypeId.concept = :conceptName");
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
                if ((instanceName.endsWith("*"))
                        || (instanceName.startsWith("*"))) {
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
                queryString.append(" entity.active = 1");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Executing " + queryString + " with names " 
                            + names.toString() + " and params " + params.toString());
            }
            
            return executeQuery(queryString.toString(), names, params,
                    firstRow, numOfRows, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindIMObjects,
                    new Object[] { rmName, entityName, conceptName,
                            instanceName, clazz }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getByArchetypeShortNames(java.lang.String[])
     */
    public List<IMObject> getByArchetypeShortNames(String[] shortNames) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getById(org.openvpms.component.business.domain.archetype.ArchetypeId,
     *      long)
     */
    public IMObject getById(String clazz, long id) {
        try {
            // check that rm has been specified
            if (StringUtils.isEmpty(clazz)) {
                throw new IMObjectDAOException(
                        IMObjectDAOException.ErrorCode.ClassNameMustBeSpecified,
                        new Object[] {});
            }

            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<Object> params = new ArrayList<Object>();

            queryString.append("select entity from ");
            queryString.append(clazz);
            queryString.append(" as entity where entity.id = :uid");
            names.add("uid");
            params.add(new Long(id));

            // let's use the session directly
            Session session = getHibernateTemplate().getSessionFactory()
                    .openSession();
            try {
                Query query = session.createQuery(queryString.toString());
                query.setParameter("uid", new Long(id));
                List result = query.list();
                if (result.size() == 0) {
                    return null;
                } else {
                    return (IMObject) result.get(0);
                }
            } finally {
                session.close();
            }
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindIMObject,
                    new Object[] { clazz, new Long(id) }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getByLinkId(java.lang.String,
     *      java.lang.String)
     */
    public IMObject getByLinkId(String clazz, String linkId) {
        try {
            // check that rm has been specified
            if (StringUtils.isEmpty(clazz)) {
                throw new IMObjectDAOException(
                        IMObjectDAOException.ErrorCode.ClassNameMustBeSpecified,
                        new Object[] {});
            }

            StringBuffer queryString = new StringBuffer();

            queryString.append("select entity from ");
            queryString.append(clazz);
            queryString.append(" as entity where entity.linkId = :linkId");

            // let's use the session directly
            Session session = getHibernateTemplate().getSessionFactory()
                    .openSession();
            try {
                Query query = session.createQuery(queryString.toString());
                query.setParameter("linkId", linkId);
                List result = query.list();
                if (result.size() == 0) {
                    return null;
                } else {
                    return (IMObject) result.get(0);
                }
            } finally {
                session.close();
            }
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindIMObjectReference,
                    new Object[] { clazz, linkId }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getByNamedQuery(java.lang.String,
     *      java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public IPage<IMObject> getByNamedQuery(String name,
            Map<String, Object> params, int firstRow, int numOfRows) {
        try {
            return executeNamedQuery(name, params, firstRow, numOfRows, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToExecuteNamedQuery,
                    new Object[] { name }, exception);
        }
    }

    /**
     * This method will execute a query and paginate the result set
     * 
     * @param queryString
     *            the hql query
     * @param names
     *            the name of each parameter
     * @param params
     *            the value of each parameter
     * @param firstRow
     *            the first row to return
     * @param numOfRows
     *            the number of rows to return
     * @param page
     *            the page to populate                                                            
     */
    @SuppressWarnings("unchecked")
    private IPage executeQuery(String queryString, List<String> names,
            List<Object> params, int firstRow, int numOfRows, Page page)
            throws Exception {
        int totalNumOfRows = 0;

        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            // first query the number of rows
            Query query = null;
            if (numOfRows != ArchetypeQuery.ALL_ROWS) {
                int indexOfFrom = queryString.indexOf("from");
                if (indexOfFrom >= 0) {
                    query = session.createQuery("select count(*) " 
                            + queryString.substring(indexOfFrom));
                } else {
                    throw new IMObjectDAOException(
                            IMObjectDAOException.ErrorCode.InvalidQueryString,
                            new Object[] {queryString});
                }
                
                // set the parameters if specified
                if (names != null) {
                    for (int index = 0; index < names.size(); index++) {
                        query.setParameter(names.get(index), params.get(index));
                    }
                }
                totalNumOfRows = ((Integer) query.list().get(0)).intValue();
                if (logger.isDebugEnabled()) {
                    logger.debug("The number of rows returned is "
                                    + totalNumOfRows);
                }
            }
    
            // now execute the query
            query = session.createQuery(queryString);
            if (names != null) {
                for (int index = 0; index < names.size(); index++) {
                    query.setParameter(names.get(index), params.get(index));
                }
            }

            // set the first row
            if (firstRow != 0) {
                query.setFirstResult(firstRow);
            }
    
            // set the maximum number fo rows
            if (numOfRows != ArchetypeQuery.ALL_ROWS) {
                query.setMaxResults(numOfRows);
                logger.debug("The maximum number of rows is " + numOfRows);
            }
    
            List<IMObject> rows = query.list();
            page.setFirstRow(firstRow);
            page.setNumOfRows(numOfRows);
            if (numOfRows == ArchetypeQuery.ALL_ROWS) {
                page.setTotalNumOfRows(rows.size());
            } else {
                page.setTotalNumOfRows(totalNumOfRows);
            }
            page.setRows(rows);
    
            return page;
        } finally {
            session.close();
        }
    }

    /**
     * This method will execute a query and paginate the result set
     * 
     * @param names
     *            the name of query
     * @param params
     *            the name and value of the parameters
     * @param firstRow
     *            the first row to return
     * @param numOfRows
     *            the number of rows to return
     * @param page
     *            the page to populate                                                            
     */
    @SuppressWarnings("unchecked")
    private IPage executeNamedQuery(String name, Map<String, Object> params, 
            int firstRow, int numOfRows, Page page) throws Exception {
        int totalNumOfRows = 0;

        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            Query query = null;


            // get the count first....IS THIS THE BEST APPROACH FOR NAMED
            // QUERIES
            if (numOfRows != ArchetypeQuery.ALL_ROWS) {
                query = session.getNamedQuery(name);
                for (String key : params.keySet()) {
                    query.setParameter(key, params.get(key));
                }
                totalNumOfRows = ((Integer) query.list().get(0)).intValue();
                if (logger.isDebugEnabled()) {
                    logger.debug("The number of rows returned is "
                            + totalNumOfRows);
                }
            }
            
            query = session.getNamedQuery(name);
            for (String key : params.keySet()) {
                query.setParameter(key, params.get(key));
            }

            // set first row
            if (firstRow != 0) {
                query.setFirstResult(firstRow);
            }

            // set maximum rows
            if (numOfRows != ArchetypeQuery.ALL_ROWS) {
                query.setMaxResults(numOfRows);
                logger.debug("The maximum number of rows is " + numOfRows);
            }
            

            List<IMObject> rows = query.list();
            page.setFirstRow(firstRow);
            page.setNumOfRows(numOfRows);
            if (numOfRows == ArchetypeQuery.ALL_ROWS) {
                page.setTotalNumOfRows(rows.size());
            } else {
                page.setTotalNumOfRows(totalNumOfRows);
            }
            page.setRows(rows);

            return page;
        } finally {
            session.close();
        }
    }
}
