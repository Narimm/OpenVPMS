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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.*;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.dao.im.common.ResultCollectorFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is an implementation of the IMObject DAO for hibernate. It uses the
 * Spring Framework's template classes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectDAOHibernate extends HibernateDaoSupport
        implements IMObjectDAO {
    /**
     * The result collector factory.
     */
    private ResultCollectorFactory collectorFactory;

    /**
     * The logger.
     */
    private static final Logger logger
            = Logger.getLogger(IMObjectDAOHibernate.class);

    /**
     * Default constructor.
     */
    public IMObjectDAOHibernate() {
        collectorFactory = new HibernateResultCollectorFactory();
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
        //template.setAllowCreate(true);
        //template.setFlushMode(HibernateTemplate.FLUSH_NEVER);

        return template;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#save(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void save(IMObject object) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.saveOrUpdate(object);
            tx.commit();
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }

            throw new IMObjectDAOException(FailedToSaveIMObject, exception,
                                           object.getUid());

        } finally {
            clearCache();
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#save(java.util.Collection)
     */
    public void save(Collection<IMObject> objects) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            for (Object object : objects) {
                session.saveOrUpdate(object);
            }
            tx.commit();
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }

            throw new IMObjectDAOException(FailedToSaveCollectionOfObjects,
                                           exception);

        } finally {
            clearCache();
            session.close();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#delete(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void delete(IMObject object) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.delete(object);
            tx.commit();
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }

            throw new IMObjectDAOException(FailedToDeleteIMObject, exception,
                                           object.getUid());

        } finally {
            clearCache();
            session.close();
        }
    }

    /**
     * Deletes a collection of objects.
     * Deletion is performed in a single transaction.
     *
     * @param objects the objects to delete
     * @throws IMObjectDAOException if the request cannot complete
     */
    public void delete(Collection<IMObject> objects) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            for (IMObject object : objects) {
                if (!object.isNew()) {
                    // detached objects must be reloaded into the current
                    // session, in order to avoid NonUniqueObjectException
                    // errors.
                    object = reload(object, session);
                }
                if (object != null) {
                    session.delete(object);
                }
            }
            tx.commit();
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }

            throw new IMObjectDAOException(FailedToDeleteCollectionOfObjects,
                                           exception);

        } finally {
            clearCache();
            session.close();
        }
    }

    /**
     * Execute a get using the specified query string, the query
     * parameters and the result collector. The first row and the number of rows
     * is used to control the paging of the result set.
     *
     * @param queryString the query string
     * @param parameters  the query parameters
     * @param firstResult the first result to retrieve
     * @param maxResults  the maximum number of results to return
     * @param count       if <code>true</code> counts the total no. of results,
     *                    returning it in {@link IPage#getTotalResults()}
     * @throws IMObjectDAOException for any error
     */
    public void get(String queryString, Map<String, Object> parameters,
                    ResultCollector collector, int firstResult, int maxResults,
                    boolean count) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("query=" + queryString
                        + ", parameters=" + parameters);
            }
            executeQuery(queryString, new Params(parameters), collector,
                         firstResult, maxResults, count);
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToExecuteQuery, exception);
        }
    }

    /**
     * Returns the result collector factory.
     *
     * @return the result collector factory
     */
    public ResultCollectorFactory getResultCollectorFactory() {
        return collectorFactory;
    }

    /**
     * Sets the result collector factory.
     *
     * @param factory the result collector factory
     */
    public void setResultCollectorFactory(ResultCollectorFactory factory) {
        collectorFactory = factory;
    }

    /**
     * Retrieve the objects that matches the specified search criteria.
     * This is a very generic method that provides a mechanism to return
     * objects based on, one or more criteria.
     * <p/>
     * All parameters are optional and can either denote an exact or partial
     * match semantics. If a parameter has a '*' at the start or end of the
     * value then it will perform a wildcard match.  If not '*' is specified in
     * the value then it will only return objects with the exact value.
     * <p/>
     * If two or more parameters are specified then it will return entities
     * that matching all criteria.
     * <p/>
     * The results will be returned in a {@link Page} object, which may contain
     * a subset of the total result set. The caller can then use the context
     * information in the {@link Page} object to make subsequent calls.
     *
     * @param rmName       the reference model name
     * @param entityName   the entity name
     * @param conceptName  the concept name
     * @param instanceName the instance name
     * @param clazz        the fully qualified name of the class to search for
     * @param activeOnly   indicates whether to return active objects.
     * @param firstResult  the first result to retrieve
     * @param maxResults   the maximum number of results to return
     * @return IPage<IMObject>
     *         the results and associated context information
     * @throws IMObjectDAOException a runtime exception if the request cannot
     *                              complete
     * @deprecated replaced by {@link #get(String, String, String, boolean,
     *             int, int)}
     */
    @Deprecated
    public IPage<IMObject> get(String rmName, String entityName,
                               String conceptName, String instanceName,
                               String clazz, boolean activeOnly,
                               int firstResult, int maxResults) {
        StringBuffer shortName = new StringBuffer();
        if (entityName != null) {
            shortName.append(entityName);
        } else {
            shortName.append("*");
        }
        shortName.append(".");
        if (conceptName != null) {
            shortName.append(conceptName);
        } else {
            shortName.append("*");
        }
        return get(shortName.toString(), instanceName, clazz, activeOnly,
                   firstResult, maxResults);
    }

    /**
     * Retrieve the objects that matches the specified search criteria.
     * This is a very generic method that provides a mechanism to return
     * objects based on, one or more criteria.
     * <p/>
     * All parameters are optional and can either denote an exact or partial
     * match semantics. If a parameter has a '*' at the start or end of the
     * value then it will perform a wildcard match.  If not '*' is specified in
     * the value then it will only return objects with the exact value.
     * <p/>
     * If two or more parameters are specified then it will return entities
     * that matching all criteria.
     * <p/>
     * The results will be returned in a {@link Page} object, which may contain
     * a subset of the total result set. The caller can then use the context
     * information in the {@link Page} object to make subsequent calls.
     *
     * @param shortName    the archetype short name
     * @param instanceName the instance name
     * @param clazz        the fully qualified name of the class to search for
     * @param activeOnly   indicates whether to return active objects.
     * @param firstResult  the first result to retrieve
     * @param maxResults   the maximum number of results to return
     * @return a page of the results
     * @throws IMObjectDAOException a runtime exception if the request cannot
     *                              complete
     */
    public IPage<IMObject> get(String shortName, String instanceName,
                               String clazz, boolean activeOnly,
                               int firstResult, int maxResults) {
        if (StringUtils.isEmpty(clazz)) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }

        try {
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<Object> params = new ArrayList<Object>();
            boolean andRequired = false;

            queryString.append("from ");
            queryString.append(clazz);
            queryString.append(" as entity");

            // check to see if one or more of the values have been specified
            if (!StringUtils.isEmpty(shortName)
                    || !StringUtils.isEmpty(instanceName)) {
                queryString.append(" where ");
            }

            // process the shortName
            if (!StringUtils.isEmpty(shortName)) {
                names.add("shortName");
                andRequired = true;
                if (shortName.endsWith("*") || shortName.startsWith("*")) {
                    queryString.append(
                            " entity.archetypeId.shortName like :shortName");
                    params.add(shortName.replace("*", "%"));
                } else {
                    queryString.append(
                            " entity.archetypeId.shortName = :shortName");
                    params.add(shortName);
                }

            }

            // process the instance name
            if (!StringUtils.isEmpty(instanceName)) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("instanceName");
                andRequired = true;
                if (instanceName.endsWith("*")
                        || instanceName.startsWith("*")) {
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

            ResultCollector<IMObject> collector
                    = collectorFactory.createIMObjectCollector();
            executeQuery(queryString.toString(), new Params(names, params),
                         collector, firstResult, maxResults, true);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    FailedToFindIMObjects, exception, shortName, instanceName,
                    clazz);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getById(org.openvpms.component.business.domain.archetype.ArchetypeId,
     *      long)
     */
    public IMObject getById(String clazz, long id) {
        if (StringUtils.isEmpty(clazz)) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }
        try {
            StringBuffer queryString = new StringBuffer();

            queryString.append("select entity from ");
            queryString.append(clazz);
            queryString.append(" as entity where entity.id = :uid");

            // let's use the session directly
            Session session = getHibernateTemplate().getSessionFactory()
                    .openSession();
            try {
                Query query = session.createQuery(queryString.toString());
                query.setParameter("uid", id);
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
            throw new IMObjectDAOException(FailedToFindIMObject, exception);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getByLinkId(java.lang.String,
     *      java.lang.String)
     */
    public IMObject getByLinkId(String clazz, String linkId) {
        if (StringUtils.isEmpty(clazz)) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }
        try {
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
            throw new IMObjectDAOException(FailedToFindIMObjectReference,
                                           exception);
        }
    }

    /**
     * Execute a get using the specified named query, the query
     * parameters and the result collector. The first result and the number of
     * results is used to control the paging of the result set.
     *
     * @param query       the query name
     * @param parameters  the query parameters
     * @param collector   the result collector
     * @param firstResult the first result to retrieve
     * @param maxResults  the maximum number of results to return
     * @param count       if <code>true</code> counts the total no. of results,
     *                    returning it in {@link IPage#getTotalResults()}
     * @throws IMObjectDAOException for any error
     */
    public void getByNamedQuery(String query, Map<String, Object> parameters,
                                ResultCollector collector, int firstResult,
                                int maxResults, boolean count) {
        try {
            executeNamedQuery(query, parameters, firstResult, maxResults,
                              collector, count);
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToExecuteNamedQuery,
                                           exception);
        }
    }

    /**
     * This method will execute a query and paginate the result set.
     *
     * @param queryString the hql query
     * @param params      the query parameters
     * @param collector   the collector
     * @param firstResult the first row to return
     * @param maxResults  the number of rows to return
     * @param count       if <code>true</code>
     */
    private void executeQuery(String queryString, Params params,
                              ResultCollector collector, int firstResult,
                              int maxResults, boolean count)
            throws Exception {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            session.setFlushMode(FlushMode.MANUAL);
            Query query = session.createQuery(queryString);
            params.setParameters(query);

            // set the first result
            if (firstResult != 0) {
                query.setFirstResult(firstResult);
            }

            // set the maximum number fo rows
            if (maxResults != ArchetypeQuery.ALL_RESULTS) {
                query.setMaxResults(maxResults);
                logger.debug("The maximum number of rows is " + maxResults);
            }

            query.setCacheable(true);

            List rows = query.list();
            collector.setFirstResult(firstResult);
            collector.setPageSize(maxResults);
            if (maxResults == ArchetypeQuery.ALL_RESULTS) {
                collector.setTotalResults(rows.size());
            } else if (count) {
                int rowCount = count(queryString, params, session);
                if (rowCount < rows.size()) {
                    // rows deleted since initial query
                    rowCount = rows.size();
                }
                collector.setTotalResults(rowCount);
            } else {
                collector.setTotalResults(-1);
            }
            for (Object object : rows) {
                collector.collect(object);
            }
        } finally {
            session.close();
        }
    }

    /**
     * This method will execute a query and paginate the result set
     *
     * @param name      the name of query
     * @param params    the name and value of the parameters
     * @param firstRow  the first row to return
     * @param numOfRows the number of rows to return
     * @param collector the collector
     * @param count     if <code>true</code> counts the total no. of rows,
     *                  returning it in {@link IPage#getTotalResults()}
     */
    @SuppressWarnings("unchecked")
    private void executeNamedQuery(String name, Map<String, Object> params,
                                   int firstRow, int numOfRows,
                                   ResultCollector collector,
                                   boolean count) throws Exception {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            session.setFlushMode(FlushMode.MANUAL);
            Query query = session.getNamedQuery(name);
            Params p = new Params(params);
            p.setParameters(query);

            // set first row
            if (firstRow != 0) {
                query.setFirstResult(firstRow);
            }

            // set maximum rows
            if (numOfRows != ArchetypeQuery.ALL_RESULTS) {
                query.setMaxResults(numOfRows);
                logger.debug("The maximum number of rows is " + numOfRows);
            }

            List<Object> rows = query.list();
            collector.setFirstResult(firstRow);
            collector.setPageSize(numOfRows);
            if (numOfRows == ArchetypeQuery.ALL_RESULTS) {
                collector.setTotalResults(rows.size());
            } else if (count) {
                int rowCount = countNamedQuery(name, p, session);
                if (rowCount < rows.size()) {
                    // rows deleted since initial query
                    rowCount = rows.size();
                }
                collector.setTotalResults(rowCount);
            } else {
                collector.setTotalResults(-1);
            }
            for (Object object : rows) {
                collector.collect(object);
            }
        } finally {
            session.close();
        }
    }

    /**
     * Counts the total no. of rows that would be returned by a query.
     *
     * @param queryString the query string
     * @param params      the query parameters
     * @return the total no. of rows that would be returned by the query
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    private int count(String queryString, Params params, Session session) {
        int indexOfFrom = queryString.indexOf("from");
        if (indexOfFrom == -1) {
            throw new IMObjectDAOException(InvalidQueryString, queryString);
        }

        Query query = session.createQuery("select count(*) "
                + queryString.substring(indexOfFrom));

        params.setParameters(query);
        return ((Number) query.list().get(0)).intValue();
    }

    /**
     * Counts the total no. of rows that would be returned by a named query.
     *
     * @param name    the query name
     * @param params  the query parameters
     * @param session the session
     * @return the total no. of rows that would be returned by the query
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    private int countNamedQuery(String name, Params params, Session session) {
        Query query = session.getNamedQuery(name);
        params.setParameters(query);

        int result = 0;
        ScrollableResults results = null;
        try {
            results = query.scroll(ScrollMode.FORWARD_ONLY);
            if (results.last()) {
                result = results.getRowNumber() + 1;
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return result;
    }

    /**
     * Clears the second level cache, to ensure that changes to the database
     * are reflected in retrieved objects.
     */
    @SuppressWarnings("unchecked")
    private void clearCache() {
        try {
            SessionFactory factory = getSessionFactory();
            factory.evictQueries();
            Map metaData = factory.getAllClassMetadata();
            Set<String> entityNames = (Set<String>) metaData.keySet();
            for (String entityName : entityNames) {
                factory.evictEntity(entityName);
            }
        } catch (Throwable exception) {
            logger.warn(exception, exception);
        }
    }

    /**
     * Reloads an object into a session.
     *
     * @param object  the object to load
     * @param session the session to use
     * @return the reloaded object
     * @throws StaleObjectStateException if the object has subsequently been
     *                                   deleted/changed
     * @throws HibernateException        for any other error
     */
    private IMObject reload(IMObject object, Session session) {
        StringBuffer queryString = new StringBuffer("from ");
        String clazz = object.getClass().getName();
        queryString.append(clazz);
        queryString.append(" as o where o.uid = :uid and o.version = :version");
        Query query = session.createQuery(queryString.toString());
        query.setParameter("uid", object.getUid());
        query.setParameter("version", object.getVersion());
        List rows = query.list();
        if (rows.isEmpty()) {
            throw new StaleObjectStateException(clazz, object.getUid());
        }
        IMObject result = (IMObject) rows.get(0);
        if (result.getVersion() != object.getVersion()) {
            throw new StaleObjectStateException(clazz, object.getUid());
        }
        return result;
    }

    /**
     * Helper to map query parameters into a form used by hibernate.
     */
    private static class Params {

        private String[] names;
        private Object[] values;

        public Params(List<String> names, List<Object> values) {
            this.names = names.toArray(new String[0]);
            this.values = values.toArray();
        }

        public Params(Map<String, Object> params) {
            names = params.keySet().toArray(new String[0]);
            values = new Object[names.length];
            for (int i = 0; i < names.length; ++i) {
                values[i] = params.get(names[i]);
            }
        }

        public void setParameters(Query query) {
            for (int i = 0; i < names.length; ++i) {
                query.setParameter(names[i], values[i]);
            }
        }

        public String[] getNames() {
            return names;
        }

        public Object[] getValues() {
            return values;
        }
    }

}
