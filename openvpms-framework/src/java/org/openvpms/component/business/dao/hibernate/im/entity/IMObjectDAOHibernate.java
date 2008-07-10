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
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.DeferredAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.ContextHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectSessionHandler;
import org.openvpms.component.business.dao.hibernate.impl.AssemblerImpl;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.*;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.business.service.archetype.query.QueryBuilder;
import org.openvpms.component.business.service.archetype.query.QueryContext;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is an implementation of the IMObject DAO for hibernate.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
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
    private final IMObjectSessionHandlerFactory handlerFactory;

    /**
     * The assembler.
     */
    private final CompoundAssembler assembler;

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
        assembler = new AssemblerImpl();
        handlerFactory = new IMObjectSessionHandlerFactory(this, assembler);
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
                                           object.getId());
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
                    Context context = getContext(session);
                    IMObjectSessionHandler handler
                            = handlerFactory.getHandler(object);
                    handler.delete(object, session, context);
                    return null;
                }
            });
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToDeleteIMObject, exception,
                                           object.getId());
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
            throw new IMObjectDAOException(FailedToExecuteQuery, exception);
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
        clazz = assembler.getDOClassName(clazz);
        if (clazz == null) {
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

            HibernateResultCollector<IMObject> collector
                    = new IMObjectResultCollector();
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
        try {
            return get(clazz, "id", id);
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToFindIMObject, exception);
        }
    }

    /**
     * Returns an object with the specified reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none exists
     * @throws IMObjectDAOException for any error
     */
    public IMObject get(IMObjectReference reference) {
        ArchetypeDescriptor desc = cache.getArchetypeDescriptor(
                reference.getArchetypeId());
        if (desc != null) {
            return getById(desc.getClassName(), reference.getId());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getByLinkId(java.lang.String,
     *      java.lang.String)
     * @deprecated no replacement
     */
    @Deprecated
    public IMObject getByLinkId(String clazz, String linkId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an object with the specified object reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none exists
     * @throws IMObjectDAOException if the request cannot complete
     */
    public IMObject getByReference(IMObjectReference reference) {
        return get(reference);
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

    public void preCommit(Context context) {
        if (!context.getSaveDeferred().isEmpty()) {
            saveDeferred(context);
        }
    }

    public void commit(Context context) {
        updateIds(context);
    }

    private void updateIds(Context context) {
        for (DOState state :context.getSaved()) {
            state.updateIds(context);
        }
    }


    private void saveDeferred(Context context) {
        DOState[] states = context.getSaveDeferred().toArray(new DOState[0]);
        boolean processed;
        do {
            processed = false;
            Set<DeferredAssembler> deferred = new HashSet<DeferredAssembler>();
            for (DOState state : states) {
                deferred.addAll(state.getDeferred());
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
        for (DOState state : states) {
            if (state.isComplete()) {
                save(state, context);
            } else {
                Set<DeferredAssembler> deferred = state.getDeferred();
                IMObjectReference ref = null;
                if (!deferred.isEmpty()) {
                    DeferredAssembler assembler = deferred.iterator().next();
                    ref = assembler.getReference();
                }
                throw new IMObjectDAOException(
                        IMObjectDAOException.ErrorCode.ObjectNotFound, ref);
            }
        }
    }

    private void save(DOState state, Context context) {
        IMObjectDO target = state.getObject();
        Session session = context.getSession();
        session.saveOrUpdate(target);
        context.addSaved(state);
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
                              final HibernateResultCollector collector,
                              final int firstResult,
                              final int maxResults, final boolean count)
            throws Exception {
        execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws
                                                         HibernateException {
//             session.setFlushMode(FlushMode.MANUAL);
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
        clazz = assembler.getDOClassName(clazz);
        if (clazz == null) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }
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
        Context context = getContext(session);
        save(object, context);
        if (!context.isSynchronizationActive()) {
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
    private void save(Collection<IMObject> objects, Session session) {
        Context context = getContext(session);
        for (IMObject object : objects) {
            save(object, context);
        }
        if (!context.isSynchronizationActive()) {
            preCommit(context);
        }
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
    private Object execute(HibernateCallback callback) {
        final HibernateTemplate template = getHibernateTemplate();
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
            HibernateResultCollector<ObjectSet> collector
                    = new ObjectSetResultCollector(q.getNames(), null);
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
