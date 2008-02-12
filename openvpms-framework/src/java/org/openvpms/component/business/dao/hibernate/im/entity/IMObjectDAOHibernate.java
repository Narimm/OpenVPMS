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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.*;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.dao.im.common.ResultCollectorFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * This is an implementation of the IMObject DAO for hibernate.
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
     * Transaction helper.
     */
    private TransactionTemplate txnTemplate;

    /**
     * The handler factory.
     */
    private final IMObjectSessionHandlerFactory handlerFactory;

    /**
     * The archetype descriptor cache.
     * This is used to resolve {@link IMObjectReference}s.
     */
    private IArchetypeDescriptorCache cache;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(IMObjectDAOHibernate.class);


    /**
     * Default constructor.
     */
    public IMObjectDAOHibernate() {
        collectorFactory = new HibernateResultCollectorFactory();
        handlerFactory = new IMObjectSessionHandlerFactory(this);
    }

    /**
     * Sets the transaction manager.
     *
     * @param manager the transaction manager
     */
    public void setTransactionManager(PlatformTransactionManager manager) {
        txnTemplate = new TransactionTemplate(manager);
    }

    /**
     * Sets the archetype descriptor cache.
     *
     * @param cache the archetype descriptor cache
     */
    public void setArchetypeDescriptorCache(IArchetypeDescriptorCache cache) {
        this.cache = cache;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#save(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void save(final IMObject object) {
        try {
            update(new HibernateCallback() {
                public Object doInHibernate(Session session)
                        throws HibernateException {
                    save(object, session);
                    return null;
                }
            });
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToSaveIMObject, exception,
                                           object.getUid());
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#save(java.util.Collection)
     */
    public void save(final Collection<IMObject> objects) {
        try {
            update(new HibernateCallback() {
                public Object doInHibernate(Session session)
                        throws HibernateException {
                    save(objects, session);
                    return null;
                }
            });
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToSaveCollectionOfObjects,
                                           exception);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#delete(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void delete(final IMObject object) {
        try {
            update(new HibernateCallback() {
                public Object doInHibernate(Session session)
                        throws HibernateException {
                    IMObjectSessionHandler handler
                            = handlerFactory.getHandler(object);
                    handler.delete(object, session);
                    return null;
                }
            });
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToDeleteIMObject, exception,
                                           object.getUid());
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
            if (log.isDebugEnabled()) {
                log.debug("query=" + queryString
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

            if (log.isDebugEnabled()) {
                log.debug("Executing " + queryString + " with names "
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
    public IMObject getById(String clazz, final long id) {
        if (StringUtils.isEmpty(clazz)) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }
        try {
            return get(clazz, "id", id);
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
            return get(clazz, "linkId", linkId);
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToFindIMObjectReference,
                                           exception);
        }
    }

    /**
     * Returns an object with the specified object reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none exists
     * @throws IMObjectDAOException if the request cannot complete
     */
    public IMObject getByReference(IMObjectReference reference) {
        ArchetypeDescriptor desc = cache.getArchetypeDescriptor(
                reference.getArchetypeId());
        if (desc != null) {
            return getByLinkId(desc.getClassName(), reference.getLinkId());
        }
        return null;
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

    /*
    * (non-Javadoc)
    *
    * @see org.springframework.orm.hibernate3.support.HibernateDaoSupport#createHibernateTemplate(org.hibernate.SessionFactory)
    */
    @Override
    protected HibernateTemplate createHibernateTemplate(
            SessionFactory sessionFactory) {
        HibernateTemplate template
                = super.createHibernateTemplate(sessionFactory);
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_COMMIT);

        return template;
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
    private void executeQuery(final String queryString, final Params params,
                              final ResultCollector collector,
                              final int firstResult,
                              final int maxResults, final boolean count)
            throws Exception {
        execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws
                                                         HibernateException {
//             session.setFlushMode(FlushMode.MANUAL);
                collector.setFirstResult(firstResult);
                collector.setPageSize(maxResults);

                if (maxResults == 0 && count) {
                    // only want a count of the results matching the criteria
                    int rowCount = count(queryString, params, session);
                    collector.setTotalResults(rowCount);
                } else {
                    Query query = session.createQuery(queryString);
                    params.setParameters(query);

                    // set the first result
                    if (firstResult != 0) {
                        query.setFirstResult(firstResult);
                    }

                    // set the maximum number of rows
                    if (maxResults != ArchetypeQuery.ALL_RESULTS) {
                        query.setMaxResults(maxResults);
                        log.debug("The maximum number of rows is "
                                + maxResults);
                    }

                    query.setCacheable(true);

                    List rows = query.list();
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
                }
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
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
    private void executeNamedQuery(final String name,
                                   final Map<String, Object> params,
                                   final int firstRow, final int numOfRows,
                                   final ResultCollector collector,
                                   final boolean count) throws Exception {
        execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            public Object doInHibernate(Session session)
                    throws HibernateException {
                // session.setFlushMode(FlushMode.MANUAL);
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
                    log.debug("The maximum number of rows is " + numOfRows);
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
                return null;
            }
        });
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

    /**
     * Retrieves an object given its class, identity property name and identity
     * value.
     *
     * @param clazz the object's class
     * @param name  the identity property name
     * @param value the identity property value
     * @return the corresponding object, or <tt>null</tt> if none is found
     */
    private IMObject get(String clazz, final String name, final Object value) {
        final StringBuffer queryString = new StringBuffer();

        queryString.append("select entity from ");
        queryString.append(clazz);
        queryString.append(" as entity where entity.");
        queryString.append(name);
        queryString.append(" = :");
        queryString.append(name);

        return (IMObject) execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                Query query = session.createQuery(queryString.toString());
                query.setParameter(name, value);
                List result = query.list();
                if (result.size() == 0) {
                    return null;
                } else {
                    return (IMObject) result.get(0);
                }
            }
        });
    }

    /**
     * Save an object.
     *
     * @param object  the object to save
     * @param session the session to use
     */
    private void save(IMObject object, Session session) {
        IMObjectSessionHandler handler = handlerFactory.getHandler(object);
        IMObject source = handler.save(object, session);
        CommitSync sync = new CommitSync(handler, object, source);
        TransactionSynchronizationManager.registerSynchronization(sync);
    }

    /**
     * Save a collection of objects.
     *
     * @param objects the objects to save
     * @param session the session to use
     */
    private void save(Collection<IMObject> objects, Session session) {
        CommitSync sync = new CommitSync();
        for (IMObject object : objects) {
            IMObjectSessionHandler handler = handlerFactory.getHandler(object);
            IMObject source = handler.save(object, session);
            sync.add(handler, object, source);
        }
        TransactionSynchronizationManager.registerSynchronization(sync);
    }

    /**
     * Check whether write operations are allowed on the given Session.
     * <p>Default implementation throws an InvalidDataAccessApiUsageException
     * in case of FlushMode.NEVER. Can be overridden in subclasses.
     *
     * @param session the current session
     * @throws InvalidDataAccessApiUsageException
     *          if write operations are not allowed
     * @see HibernateTemplate#checkWriteOperationAllowed(Session)
     */
    private void checkWriteOperationAllowed(HibernateTemplate template,
                                            Session session)
            throws InvalidDataAccessApiUsageException {
        if (template.isCheckWriteOperations()
                && template.getFlushMode() != HibernateTemplate.FLUSH_EAGER
                && FlushMode.MANUAL.equals(session.getFlushMode())) {
            throw new InvalidDataAccessApiUsageException(
                    "Write operations are not allowed in read-only mode "
                            + "(FlushMode.MANUAL) - turn your Session into "
                            + "FlushMode.AUTO or remove 'readOnly' marker from "
                            + "transaction definition");
        }
    }

    /**
     * Executes an update within a transaction.
     *
     * @param callback the callback to execute
     * @return the result of the callback
     */
    private Object update(final HibernateCallback callback) {
        final HibernateTemplate template = getHibernateTemplate();
        return txnTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                return template.execute(new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException {
                        checkWriteOperationAllowed(template, session);
                        return template.execute(callback);
                    }
                });
            }
        });
    }

    /**
     * Executes an hibernate callback.
     *
     * @param callback the callback
     * @return the result of the callback
     */
    private Object execute(HibernateCallback callback) {
        final HibernateTemplate template = getHibernateTemplate();
        return template.execute(callback);
    }


    /**
     * Helper to update the ids and versions of a set of objects on
     * transaction commit.
     */
    private static class CommitSync extends TransactionSynchronizationAdapter {

        /**
         * A list of objects to update the ids and versions of at transaction
         * commit.
         */
        private List<Sync> list = new ArrayList<Sync>();

        /**
         * Creates a new <tt>CommitSync</tt>.
         */
        public CommitSync() {
        }

        /**
         * Creates a new <tt>CommitSync</tt>, with an object to update at
         * commit.
         *
         * @param handler the handler to perform the update
         * @param target  the object to update
         * @param source  the object to update from
         */
        public CommitSync(IMObjectSessionHandler handler, IMObject target,
                          IMObject source) {
            add(handler, target, source);
        }

        /**
         * Register an object to update at commit.
         *
         * @param handler the handler to perform the update
         * @param target  the object to update
         * @param source  the object to update from
         */
        public void add(IMObjectSessionHandler handler, IMObject target,
                        IMObject source) {
            list.add(new Sync(handler, target, source));
        }

        @Override
        public void afterCompletion(int status) {
            if (status == STATUS_COMMITTED) {
                for (Sync sync : list) {
                    sync.sync();
                }
            }
        }

        private static class Sync {

            private final IMObjectSessionHandler handler;
            private final IMObject target;
            private final IMObject source;

            public Sync(IMObjectSessionHandler handler, IMObject target,
                        IMObject source) {
                this.handler = handler;
                this.target = target;
                this.source = source;
            }

            /**
             * Updates the target object with the identifier and version of the
             * source, including any direct children.
             */
            public void sync() {
                handler.updateIds(target, source);
            }

        }

    }

}
