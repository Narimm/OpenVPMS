/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.ContextHandler;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.DeferredAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DeferredReference;
import org.openvpms.component.business.dao.hibernate.im.common.DeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.DefaultObjectLoader;
import org.openvpms.component.business.dao.hibernate.im.entity.HibernateResultCollector;
import org.openvpms.component.business.dao.hibernate.im.entity.IMObjectNodeResultCollector;
import org.openvpms.component.business.dao.hibernate.im.entity.IMObjectResultCollector;
import org.openvpms.component.business.dao.hibernate.im.entity.NodeSetResultCollector;
import org.openvpms.component.business.dao.hibernate.im.entity.ObjectSetResultCollector;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupReplacer;
import org.openvpms.component.business.dao.hibernate.im.query.QueryBuilder;
import org.openvpms.component.business.dao.hibernate.im.query.QueryContext;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.ClassNameMustBeSpecified;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.FailedToDeleteIMObject;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.FailedToExecuteNamedQuery;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.FailedToExecuteQuery;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.FailedToFindIMObjects;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.FailedToSaveCollectionOfObjects;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.FailedToSaveIMObject;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.InvalidQueryString;


/**
 * This is an implementation of the IMObject DAO for hibernate.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class IMObjectDAOHibernate extends HibernateDaoSupport
        implements IMObjectDAO, ContextHandler {

    /**
     * Transaction helper.
     */
    private TransactionTemplate txnTemplate;

    /**
     * The handler factory.
     */
    private DeleteHandlerFactory handlerFactory;

    /**
     * The assembler.
     */
    private CompoundAssembler assembler;

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
        assembler = new AssemblerImpl(cache);
        handlerFactory = new DeleteHandlerFactory(assembler, cache);
    }

    /**
     * Saves an object.
     *
     * @param object the object to save
     * @throws IMObjectDAOException if the request cannot complete
     */
    public void save(final IMObject object) {
        try {
            update(new HibernateCallback<Object>() {
                public Object doInHibernate(Session session) throws HibernateException {
                    save(object, session);
                    return null;
                }
            });
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToSaveIMObject, exception,
                                           object.getId());
        }
    }

    /**
     * Saves a collection of objects in a single transaction.
     *
     * @param objects the objects to save
     * @throws IMObjectDAOException if the request cannot complete
     */
    public void save(final Collection<? extends IMObject> objects) {
        try {
            update(new HibernateCallback<Object>() {
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

    /**
     * Deletes an {@link IMObject}.
     *
     * @param object the object to delete
     * @throws IMObjectDAOException if the request cannot complete
     */
    public void delete(final IMObject object) {
        try {
            update(new HibernateCallback<Object>() {
                public Object doInHibernate(Session session)
                        throws HibernateException {
                    Context context = getContext(session);
                    DeleteHandler handler
                            = handlerFactory.getHandler(object);
                    handler.delete(object, session, context);
                    updateIds(context);
                    return null;
                }
            });
        } catch (IMObjectDAOException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToDeleteIMObject, exception,
                                           object.getObjectReference());
        }
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    public IPage<IMObject> get(IArchetypeQuery query) {
        return getQueryDelegator(query).get(query);
    }

    /**
     * Retrieves partially populated objects that match the query.
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     * <p/>
     * All simple properties of the returned objects are populated - the
     * <code>nodes</code> argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param query the archetype query
     * @param nodes the collection node names
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    public IPage<IMObject> get(IArchetypeQuery query,
                               Collection<String> nodes) {
        return getQueryDelegator(query).get(query, nodes);
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
        return getQueryDelegator(query).getObjects(query);
    }

    /**
     * Retrieves the nodes from the objects that match the query criteria.
     *
     * @param query the archetype query
     * @param nodes the node names
     * @return the nodes for each object that matches the query criteria
     * @throws IMObjectDAOException for any error
     */
    public IPage<NodeSet> getNodes(IArchetypeQuery query,
                                   Collection<String> nodes) {
        return getQueryDelegator(query).getNodes(query, nodes);
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
            executeQuery(queryString, new Params(parameters),
                         (HibernateResultCollector) collector,
                         firstResult, maxResults, count);
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToExecuteQuery, exception,
                                           queryString);
        }
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
        StringBuilder shortName = new StringBuilder();
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
        clazz = assembler.getDOClassName(clazz);
        if (clazz == null) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }

        StringBuilder queryString = new StringBuilder();
        List<String> names = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        boolean andRequired = false;

        queryString.append("from ");
        queryString.append(clazz);
        queryString.append(" as entity");

        // check to see if one or more of the values have been specified
        if (!StringUtils.isEmpty(shortName) || !StringUtils.isEmpty(instanceName)) {
            queryString.append(" where ");
        }

        // process the shortName
        if (!StringUtils.isEmpty(shortName)) {
            names.add("shortName");
            andRequired = true;
            if (shortName.endsWith("*") || shortName.startsWith("*")) {
                queryString.append(" entity.archetypeId.shortName like :shortName");
                params.add(shortName.replace("*", "%"));
            } else {
                queryString.append(" entity.archetypeId.shortName = :shortName");
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
            if (instanceName.endsWith("*") || instanceName.startsWith("*")) {
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

        try {
            HibernateResultCollector<IMObject> collector = new IMObjectResultCollector();
            executeQuery(queryString.toString(), new Params(names, params), collector, firstResult, maxResults, true);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToFindIMObjects, exception, shortName, instanceName, clazz);
        }
    }

    /**
     * Returns an object with the specified reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none exists
     * @throws IMObjectDAOException for any error
     */
    public IMObject get(final IMObjectReference reference) {
        // first determine if the object is cached in the transaction context
        IMObject result = execute(new HibernateCallback<IMObject>() {
            public IMObject doInHibernate(Session session) {
                Context context = getContext(session);
                DOState state = context.getCached(reference);
                return (state != null) ? state.getSource() : null;
            }
        });
        if (result == null && !reference.isNew()) {
            // no cached object. If the reference indicates the object is 
            // committed, try and query it
            ArchetypeDescriptor desc = cache.getArchetypeDescriptor(
                    reference.getArchetypeId());
            if (desc != null) {
                result = get(desc.getClassName(), reference);
            }
        }
        return result;
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
                              (HibernateResultCollector) collector, count);
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToExecuteNamedQuery,
                                           exception);
        }
    }

    /**
     * Invoked just prior to commit.
     * </p>
     * Attempts to save any deferred objects.
     *
     * @param context the assembly context
     */
    public void preCommit(Context context) {
        if (!context.getSaveDeferred().isEmpty()) {
            saveDeferred(context, true);
        }
    }

    /**
     * Invoked after successful commit.
     * <p/>
     * This propagates identifier and version changes from the committed
     * <tt>IMObjectDO</tt>s to their corresponding <tt>IMObject</tt>s.
     *
     * @param context the assembly context
     */
    public void commit(Context context) {
        updateIds(context);
    }

    /**
     * Invoked on transaction rollback.
     * <p/>
     * This reverts identifier and version changes
     *
     * @param context the assembly context
     */
    public void rollback(Context context) {
        for (DOState state : context.getSaved()) {
            state.rollbackIds();
        }
    }

    /**
     * Replaces the uses of one lookup with another.
     *
     * @param source the lookup to replace
     * @param target the lookup to replace <tt>source</tt> it with
     */
    public void replace(final Lookup source, final Lookup target) {
        try {
            update(new HibernateCallback<Object>() {
                public Object doInHibernate(Session session) throws HibernateException {
                    LookupReplacer replacer = new LookupReplacer(cache);
                    replacer.replace(source, target, session);
                    return null;
                }
            });
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToSaveIMObject, exception, source.getId());
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
        template.setFlushMode(HibernateTemplate.FLUSH_COMMIT);

        // note - need to expose the native session in order for
        // Session.equals() to work in Context.getContext()
        template.setExposeNativeSession(true);

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
     * @param count       if <tt>true</tt>, count the results matching the criteria
     * @throws Exception for any error
     */
    private void executeQuery(final String queryString, final Params params,
                              final HibernateResultCollector collector,
                              final int firstResult,
                              final int maxResults, final boolean count)
            throws Exception {
        execute(new HibernateCallback<Object>() {

            public Object doInHibernate(Session session) throws
                                                         HibernateException {
                collector.setFirstResult(firstResult);
                collector.setPageSize(maxResults);
                collector.setSession(session);
                Context context = getContext(session);
                collector.setContext(context);

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
                    resolveDeferredReferences(context);
                }
                return null;
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
     * @param count     if <tt>true</tt> counts the total no. of rows, returning it in {@link IPage#getTotalResults()}
     * @throws Exception for any error
     */
    private void executeNamedQuery(final String name,
                                   final Map<String, Object> params,
                                   final int firstRow, final int numOfRows,
                                   final HibernateResultCollector collector,
                                   final boolean count) throws Exception {
        execute(new HibernateCallback<Object>() {
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
                collector.setSession(session);
                Context context = getContext(session);
                collector.setContext(context);

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
                resolveDeferredReferences(context);
                return null;
            }
        });
    }

    /**
     * Counts the total no. of rows that would be returned by a query.
     *
     * @param queryString the query string
     * @param params      the query parameters
     * @param session     the hibernate session
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
            this.names = names.toArray(new String[names.size()]);
            this.values = values.toArray();
        }

        public Params(Map<String, Object> params) {
            names = params.keySet().toArray(new String[params.keySet().size()]);
            values = new Object[names.length];
            for (int i = 0; i < names.length; ++i) {
                values[i] = params.get(names[i]);
            }
        }

        public void setParameters(Query query) {
            for (int i = 0; i < names.length; ++i) {
                if (values[i] instanceof Collection) {
                    query.setParameterList(names[i], (Collection) values[i]);
                } else if (values[i] instanceof Object[]) {
                    query.setParameterList(names[i], (Object[]) values[i]);
                } else {
                    query.setParameter(names[i], values[i]);
                }
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
     * Retrieves an object given its class and reference.
     *
     * @param clazz     the object's class
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none is found
     */
    private IMObject get(String clazz, final IMObjectReference reference) {
        clazz = assembler.getDOClassName(clazz);
        if (clazz == null) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }
        final StringBuffer queryString = new StringBuffer();

        queryString.append("select entity from ");
        queryString.append(clazz);
        queryString.append(" as entity where entity.id = :id and entity.archetypeId.shortName = :shortName");

        return execute(new HibernateCallback<IMObject>() {
            public IMObject doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(queryString.toString());
                query.setParameter("id", reference.getId());
                query.setParameter("shortName", reference.getArchetypeId().getShortName());
                List results = query.list();
                if (results.size() == 0) {
                    return null;
                } else {
                    Context context = getContext(session);
                    IMObjectDO object = (IMObjectDO) results.get(0);
                    IMObject result = assembler.assemble(object, context);
                    resolveDeferredReferences(context);
                    return result;
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
        Context context = getContext(session);
        boolean deferred = !context.getSaveDeferred().isEmpty();
        save(object, context);
        if (deferred) {
            saveDeferred(context, false);
        }
        if (!context.isSynchronizationActive()) { // todo - required?
            preCommit(context);
        }
    }

    /**
     * Saves an object.
     *
     * @param object  the object to save
     * @param context the context
     */
    private void save(IMObject object, Context context) {
        DOState state = assembler.assemble(object, context);
        if (state.isComplete()) {
            save(state, context);
        } else {
            context.addSaveDeferred(state);
        }
    }

    /**
     * Save a collection of objects.
     *
     * @param objects the objects to save
     * @param session the session to use
     */
    private void save(Collection<? extends IMObject> objects, Session session) {
        Context context = getContext(session);
        boolean deferred = !context.getSaveDeferred().isEmpty();
        for (IMObject object : objects) {
            save(object, context);
        }
        if (deferred || context.getSaveDeferred().size() > 1) {
            saveDeferred(context, false);
        }
        if (!context.isSynchronizationActive()) {
            preCommit(context);
        }
    }

    /**
     * Propagates identifier and version changes from the committed
     * <tt>IMObjectDO</tt>s to their corresponding <tt>IMObject</tt>s.
     *
     * @param context the assembly context
     */
    private void updateIds(Context context) {
        for (DOState state : context.getSaved()) {
            state.updateIds(context);
        }
    }

    /**
     * Attempts to assemble and save deferred objects.
     *
     * @param context          the assembly context
     * @param failOnIncomplete if <tt>true</tt> and an object cannot be saved,
     *                         raises an exception
     * @throws IMObjectDAOException if an object cannot be saved
     */
    private void saveDeferred(Context context, boolean failOnIncomplete) {
        List<DOState> states = assembleDeferred(context);
        for (DOState state : states) {
            save(state, context);
        }
        Set<DOState> saveDeferred = context.getSaveDeferred();
        if (!saveDeferred.isEmpty() && failOnIncomplete) {
            DOState state = saveDeferred.iterator().next();
            Set<DeferredAssembler> deferred = state.getDeferred();
            IMObjectReference ref = null;
            if (!deferred.isEmpty()) { // should never be empty
                DeferredAssembler assembler = deferred.iterator().next();
                ref = assembler.getReference();
            }
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.ObjectNotFound, ref);

        }
    }

    /**
     * Attempts to assemble deferred objects.
     *
     * @param context the assembly context
     * @return the assembled objects
     */
    private List<DOState> assembleDeferred(Context context) {
        List<DOState> result = new ArrayList<DOState>();
        boolean processed;
        do {
            processed = false;
            DOState[] states = context.getSaveDeferred().toArray(
                    new DOState[context.getSaveDeferred().size()]);
            Set<DeferredAssembler> deferred = new HashSet<DeferredAssembler>();
            for (DOState state : states) {
                Set<DeferredAssembler> set = state.getDeferred();
                if (!set.isEmpty()) {
                    deferred.addAll(set);
                } else {
                    context.removeSaveDeferred(state);
                    result.add(state);
                }
            }
            if (!deferred.isEmpty()) {
                for (DeferredAssembler assembler : deferred) {
                    if (context.getCached(assembler.getReference()) != null) {
                        assembler.assemble(context);
                        processed = true;
                    }
                }
            }
        } while (processed);
        return result;
    }

    /**
     * Saves a data object.
     *
     * @param state   the data object state
     * @param context the assembly context
     */
    private void save(DOState state, Context context) {
        Session session = context.getSession();
        for (IMObjectDO object : state.getObjects()) {
            session.saveOrUpdate(object);
        }
        state.updateIds(context);
        context.addSaved(state);
    }

    /**
     * Resolves deferred references.
     *
     * @param context the assembly context
     */
    private void resolveDeferredReferences(Context context) {
        List<DeferredReference> deferred = context.getDeferredReferences();
        if (!deferred.isEmpty()) {
            Map<Class<? extends IMObjectDOImpl>, List<DeferredReference>> map
                    = new HashMap<Class<? extends IMObjectDOImpl>, List<DeferredReference>>();
            for (DeferredReference ref : deferred) {
                IMObjectDO object = ref.getObject();
                if (Hibernate.isInitialized(object)) {
                    ref.update(object.getObjectReference());
                } else {
                    List<DeferredReference> list = map.get(ref.getType());
                    if (list == null) {
                        list = new ArrayList<DeferredReference>();
                        map.put(ref.getType(), list);
                    }
                    list.add(ref);
                }
            }
            if (!map.isEmpty()) {
                for (Map.Entry<Class<? extends IMObjectDOImpl>,
                        List<DeferredReference>> entry : map.entrySet()) {
                    Class<? extends IMObjectDOImpl> type = entry.getKey();
                    List<DeferredReference> refs = entry.getValue();
                    Map<Long, IMObjectDO> objects = new HashMap<Long, IMObjectDO>();
                    for (DeferredReference ref : refs) {
                        IMObjectDO object = ref.getObject();
                        objects.put(object.getId(), object);
                    }
                    Map<Long, IMObjectReference> resolvedRefs
                            = context.getReferences(objects, type);
                    for (DeferredReference ref : refs) {
                        IMObjectReference resolved
                                = resolvedRefs.get(ref.getObject().getId());
                        if (resolved != null) {
                            ref.update(resolved);
                        }
                    }
                }
                map.clear();
            }
            deferred.clear();
        }
    }

    /**
     * Check whether write operations are allowed on the given Session.
     * <p>Default implementation throws an InvalidDataAccessApiUsageException
     * in case of FlushMode.NEVER. Can be overridden in subclasses.
     *
     * @param template the hibernate template
     * @param session  the current session
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
    private Object update(final HibernateCallback<Object> callback) {
        final HibernateTemplate template = getHibernateTemplate();
        getHibernateTemplate().setExposeNativeSession(true);
        return txnTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                return template.execute(new HibernateCallback<Object>() {
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
     * Returns the assembly context for the specified session,
     * creating one if it doesn't exist.
     *
     * @param session the session
     * @return the corresponding assembly context
     */
    private Context getContext(Session session) {
        Context context = Context.getContext(assembler, session);
        context.setContextHandler(this);
        return context;
    }

    /**
     * Executes an hibernate callback.
     *
     * @param callback the callback
     * @return the result of the callback
     */
    private <T> T execute(HibernateCallback<T> callback) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setExposeNativeSession(true);
        return template.execute(callback);
    }

    /**
     * Returns a new query delegator for the specified query.
     *
     * @param query the query
     * @return a new query delegator for the query
     */
    private QueryDelegator getQueryDelegator(IArchetypeQuery query) {
        if (query instanceof ArchetypeQuery) {
            return new DefaultQueryDelegator();
        } else if (query instanceof NamedQuery) {
            return new NamedQueryDelegator();
        }
        throw new IllegalArgumentException("Unsupported query: "
                                           + query.getClass().getName());
    }

    abstract class QueryDelegator {

        public IPage<IMObject> get(IArchetypeQuery query) {
            HibernateResultCollector<IMObject> collector
                    = new IMObjectResultCollector();
            collector.setLoader(new DefaultObjectLoader());
            get(query, collector);
            return collector.getPage();
        }

        public IPage<IMObject> get(IArchetypeQuery query,
                                   Collection<String> nodes) {
            HibernateResultCollector<IMObject> collector
                    = new IMObjectNodeResultCollector(cache, nodes);
            collector.setLoader(new DefaultObjectLoader());
            get(query, collector);
            return collector.getPage();
        }

        public IPage<NodeSet> getNodes(IArchetypeQuery query,
                                       Collection<String> nodes) {
            HibernateResultCollector<NodeSet> collector
                    = new NodeSetResultCollector(cache, nodes);
            collector.setLoader(new DefaultObjectLoader());
            get(query, collector);
            return collector.getPage();
        }

        public abstract IPage<ObjectSet> getObjects(IArchetypeQuery query);

        protected abstract void get(IArchetypeQuery query,
                                    ResultCollector collector);
    }

    class DefaultQueryDelegator extends QueryDelegator {

        public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
            QueryBuilder builder = new QueryBuilder(cache, assembler);
            QueryContext context = builder.build((ArchetypeQuery) query);
            HibernateResultCollector<ObjectSet> collector
                    = new ObjectSetResultCollector(context.getSelectNames(),
                                                   context.getRefSelectNames(),
                                                   context.getSelectTypes());
            collector.setLoader(new DefaultObjectLoader());
            get(context, query, collector);
            return collector.getPage();
        }

        protected void get(IArchetypeQuery query, ResultCollector collector) {
            QueryBuilder builder = new QueryBuilder(cache, assembler);
            QueryContext context = builder.build((ArchetypeQuery) query);
            get(context, query, collector);
        }

        private void get(QueryContext context, IArchetypeQuery query,
                         ResultCollector collector) {
            if (log.isDebugEnabled()) {
                log.debug("ArchetypeService.get: query "
                          + context.getQueryString());
            }

            IMObjectDAOHibernate.this.get(context.getQueryString(),
                                          context.getParameters(),
                                          collector, query.getFirstResult(),
                                          query.getMaxResults(),
                                          query.countResults());
        }
    }

    class NamedQueryDelegator extends QueryDelegator {

        public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
            NamedQuery q = (NamedQuery) query;
            List<String> names = (q.getNames() != null) ?
                                 new ArrayList<String>(q.getNames()) : null;
            List<String> refNames = Collections.emptyList();
            HibernateResultCollector<ObjectSet> collector
                    = new ObjectSetResultCollector(names, refNames, null);
            collector.setLoader(new DefaultObjectLoader());
            get(query, collector);
            return collector.getPage();
        }

        protected void get(IArchetypeQuery query, ResultCollector collector) {
            NamedQuery q = (NamedQuery) query;
            getByNamedQuery(q.getQuery(), q.getParameters(), collector,
                            q.getFirstResult(), q.getMaxResults(),
                            q.countResults());
        }
    }

}
