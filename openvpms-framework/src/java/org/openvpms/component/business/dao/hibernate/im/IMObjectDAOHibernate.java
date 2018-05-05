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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
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
import org.openvpms.component.business.dao.hibernate.im.common.DeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
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
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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


/**
 * This is an implementation of the IMObject DAO for hibernate.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class IMObjectDAOHibernate implements IMObjectDAO, ContextHandler {

    /**
     * The hibernate session factory.
     */
    private final SessionFactory factory;

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
    private static final Log log = LogFactory.getLog(IMObjectDAOHibernate.class);


    /**
     * Constructs an {@link IMObjectDAOHibernate}.
     *
     * @param factory the session factory
     */
    public IMObjectDAOHibernate(SessionFactory factory) {
        this.factory = factory;
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
    @Transactional
    public void save(final IMObject object) {
        try {
            save(Collections.singletonList(object), getSession());
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToSaveIMObject, exception, object.getId());
        }
    }

    /**
     * Saves a collection of objects in a single transaction.
     *
     * @param objects the objects to save
     * @throws IMObjectDAOException if the request cannot complete
     */
    @Transactional
    public void save(final Collection<? extends IMObject> objects) {
        try {
            save(objects, getSession());
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToSaveCollectionOfObjects, exception);
        }
    }

    /**
     * Deletes an {@link IMObject}.
     *
     * @param object the object to delete
     * @throws IMObjectDAOException if the request cannot complete
     */
    @Transactional
    public void delete(final IMObject object) {
        try {
            Session session = getSession();
            Context context = getContext(session);
            DeleteHandler handler = handlerFactory.getHandler(object);
            handler.delete(object, session, context);
            updateIds(context);
        } catch (IMObjectDAOException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToDeleteIMObject, exception, object.getObjectReference());
        }
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    @Transactional(readOnly = true)
    public IPage<IMObject> get(IArchetypeQuery query) {
        return getQueryDelegator(query).get(query);
    }

    /**
     * Retrieves partially populated objects that match the query.
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     * <p>
     * All simple properties of the returned objects are populated - the
     * {@code nodes} argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param query the archetype query
     * @param nodes the collection node names
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    @Transactional(readOnly = true)
    public IPage<IMObject> get(IArchetypeQuery query, Collection<String> nodes) {
        return getQueryDelegator(query).get(query, nodes);
    }

    /**
     * Retrieves the objects matching the query.
     *
     * @param query the archetype query
     * @return a page of objects that match the query criteria
     * @throws IMObjectDAOException for any error
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public IPage<NodeSet> getNodes(IArchetypeQuery query, Collection<String> nodes) {
        return getQueryDelegator(query).getNodes(query, nodes);
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
     * <p>
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
     * @throws IMObjectDAOException a runtime exception if the request cannot complete
     */
    @Transactional(readOnly = true)
    public IPage<IMObject> get(String shortName, String instanceName,
                               String clazz, boolean activeOnly,
                               int firstResult, int maxResults) {
        clazz = assembler.getDOClassName(clazz);
        if (clazz == null) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }

        StringBuilder queryString = new StringBuilder();
        List<String> names = new ArrayList<>();
        List<Object> params = new ArrayList<>();
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
            executeQuery(queryString.toString(), null, new Params(names, params), collector, firstResult, maxResults);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToFindIMObjects, exception, shortName, instanceName, clazz);
        }
    }

    /**
     * Returns an object with the specified reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none exists. The object may be active or inactive
     * @throws IMObjectDAOException for any error
     */
    @Override
    @Transactional(readOnly=true)
    public IMObject get(Reference reference) {
        return getObject(reference, null);
    }

    /**
     * Returns an object with the specified reference.
     *
     * @param reference the object reference
     * @param active    if {@code true}, only return the object if it is active
     * @return the corresponding object, or <tt>null</tt> if none exists
     * @throws IMObjectDAOException for any error
     */
    @Transactional(readOnly=true)
    public IMObject get(final Reference reference, boolean active) {
        return getObject(reference, active);
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
     * @param count       if {@code true} counts the total no. of results,
     *                    returning it in {@link IPage#getTotalResults()}
     * @throws IMObjectDAOException for any error
     */
    @Transactional(readOnly = true)
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
     * <p>
     * This propagates identifier and version changes from the committed
     * {@code IMObjectDO}s to their corresponding {@code IMObject}s.
     *
     * @param context the assembly context
     */
    public void commit(Context context) {
        context.commit();
    }

    /**
     * Invoked on transaction rollback.
     * <p/>
     * This reverts identifier and version changes.
     *
     * @param context the assembly context
     */
    public void rollback(Context context) {
        context.rollback();
    }

    /**
     * Replaces the uses of one lookup with another.
     *
     * @param source the lookup to replace
     * @param target the lookup to replace {@code source} it with
     */
    @Transactional
    public void replace(final Lookup source, final Lookup target) {
        try {
            LookupReplacer replacer = new LookupReplacer(cache);
            replacer.replace(source, target, getSession());
        } catch (Throwable exception) {
            throw new IMObjectDAOException(FailedToSaveIMObject, exception, source.getId());
        }
    }

    /**
     * Returns the current hibernate session.
     * <p>
     * This ensures that the session flush mode is {@link FlushMode#COMMIT} to ensure that ids and versions get reverted
     * correctly if the transaction rolls back.
     *
     * @return the current hibernate session
     */
    private Session getSession() {
        Session session = factory.getCurrentSession();
        session.setFlushMode(FlushMode.COMMIT);
        return session;
    }

    /**
     * Returns an object given its reference.
     *
     * @param reference the reference
     * @param active    if {@code true}, only return the object if it is active. If {@code false}, only return the object
     *                  if it is inactive. If {@code null}, return active or inactive objects.
     * @return the object, or {@code null}
     */
    private IMObject getObject(Reference reference, Boolean active) {
        IMObject result = null;
        // first determine if the object is cached in the transaction context
        Session session = getSession();
        Context context = getContext(session);
        DOState state = context.getCached(reference);
        IMObject cached  = (state != null) ? state.getSource() : null;
        if (cached != null) {
            result = (active != null && active != cached.isActive()) ? null : cached;
        } else if (!reference.isNew()) {
            // no cached object. If the reference indicates the object is
            // committed, try and query it
            ArchetypeDescriptor desc = cache.getArchetypeDescriptor(reference.getArchetype());
            if (desc != null) {
                result = getObject(desc.getClassName(), reference, active);
            }
        }
        return result;
    }

    /**
     * This method will execute a query and paginate the result set.
     *
     * @param queryString the hql query
     * @param countQuery  the query to count results, or {@code null} if results are not being counted
     * @param params      the query parameters
     * @param collector   the collector
     * @param firstResult the first row to return
     * @param maxResults  the number of rows to return
     * @throws Exception for any error
     */
    private void executeQuery(String queryString, String countQuery, Params params, HibernateResultCollector collector,
                              int firstResult, int maxResults)
            throws Exception {
        Session session = getSession();
        collector.setFirstResult(firstResult);
        collector.setPageSize(maxResults);
        collector.setSession(session);
        Context context = getContext(session);
        collector.setContext(context);

        if (maxResults == 0 && countQuery != null) {
            // only want a count of the results matching the criteria
            int rowCount = count(countQuery, params, session);
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
                log.debug("The maximum number of rows is " + maxResults);
            }

            query.setCacheable(true);

            List rows = query.list();
            if (maxResults == ArchetypeQuery.ALL_RESULTS) {
                collector.setTotalResults(rows.size());
            } else if (countQuery != null) {
                int rowCount = count(countQuery, params, session);
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
            context.resolveDeferredReferences();
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
     * @param count     if {@code true} counts the total no. of rows, returning it in {@link IPage#getTotalResults()}
     * @throws Exception for any error
     */
    private void executeNamedQuery(String name, Map<String, Object> params, int firstRow, final int numOfRows,
                                   HibernateResultCollector collector, boolean count) throws Exception {
        Session session = getSession();
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
        context.resolveDeferredReferences();
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
        Query query = session.createQuery(queryString);
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
     * Retrieves an object given its class and reference.
     *
     * @param clazz     the object's class
     * @param reference the object reference
     * @param active    if {@code true}, only return the object if it is active. If {@code false}, only return the
     *                  object if it is inactive. If {@code null}, return active or inactive objects.
     * @return the corresponding object, or {@code null} if none is found
     */
    private IMObject getObject(String clazz, Reference reference, Boolean active) {
        clazz = assembler.getDOClassName(clazz);
        if (clazz == null) {
            throw new IMObjectDAOException(ClassNameMustBeSpecified);
        }
        StringBuilder queryString = new StringBuilder ();

        queryString.append("select entity from ");
        queryString.append(clazz);
        queryString.append(" as entity where entity.id = :id and entity.archetypeId.shortName = :shortName");
        if (active != null) {
            queryString.append(" and entity.active = :active");
        }
        Session session = getSession();
        Query query = session.createQuery(queryString.toString());
        query.setParameter("id", reference.getId());
        query.setParameter("shortName", reference.getArchetype());
        if (active != null) {
            query.setParameter("active", active);
        }
        List results = query.list();
        if (results.size() == 0) {
            return null;
        } else {
            Context context = getContext(session);
            IMObjectDO object = (IMObjectDO) results.get(0);
            IMObject result = assembler.assemble(object, context);
            context.resolveDeferredReferences();
            return result;
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
        List<DOState> toSave = new ArrayList<>();
        for (IMObject object : objects) {
            DOState state = assembler.assemble(object, context);
            if (state.isComplete()) {
                toSave.add(state);
            } else {
                context.addSaveDeferred(state);
            }
        }
        if (!toSave.isEmpty()) {
            save(toSave, context);
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
     * {@code IMObjectDO}s to their corresponding {@code IMObject}s.
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
     * @param failOnIncomplete if {@code true} and an object cannot be saved,
     *                         raises an exception
     * @throws IMObjectDAOException if an object cannot be saved
     */
    private void saveDeferred(Context context, boolean failOnIncomplete) {
        List<DOState> states = assembleDeferred(context);
        save(states, context);
        Set<DOState> saveDeferred = context.getSaveDeferred();
        if (!saveDeferred.isEmpty() && failOnIncomplete) {
            DOState state = saveDeferred.iterator().next();
            Set<DeferredAssembler> deferred = state.getDeferred();
            Reference ref = null;
            if (!deferred.isEmpty()) { // should never be empty
                DeferredAssembler assembler = deferred.iterator().next();
                ref = assembler.getReference();
            }
            throw new IMObjectDAOException(IMObjectDAOException.ErrorCode.ObjectNotFound, ref);

        }
    }

    /**
     * Attempts to assemble deferred objects.
     *
     * @param context the assembly context
     * @return the assembled objects
     */
    private List<DOState> assembleDeferred(Context context) {
        List<DOState> result = new ArrayList<>();
        boolean processed;
        do {
            processed = false;
            Set<DeferredAssembler> assemblers = new HashSet<>();
            Map<DOState, Set<DeferredAssembler>> deferred = DOState.getDeferred(context.getSaveDeferred());
            for (Map.Entry<DOState, Set<DeferredAssembler>> entry : deferred.entrySet()) {
                DOState state = entry.getKey();
                Set<DeferredAssembler> set = entry.getValue();
                if (!set.isEmpty()) {
                    assemblers.addAll(set);
                } else {
                    // state requires no more assembly
                    context.removeSaveDeferred(state);
                    result.add(state);
                }
            }
            if (!assemblers.isEmpty()) {
                for (DeferredAssembler assembler : assemblers) {
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
     * Saves data objects.
     *
     * @param states  the data object states
     * @param context the assembly context
     */
    private void save(List<DOState> states, Context context) {
        Session session = context.getSession();
        Collection<IMObjectDO> objects = DOState.getObjects(states);
        for (IMObjectDO object : objects) {
            session.saveOrUpdate(object);
        }
        DOState.updateIds(states, context);
        for (DOState state : states) {
            context.addSaved(state);
        }
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
        throw new IllegalArgumentException("Unsupported query: " + query.getClass().getName());
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

abstract class QueryDelegator {

    public IPage<IMObject> get(IArchetypeQuery query) {
        HibernateResultCollector<IMObject> collector = new IMObjectResultCollector();
        collector.setLoader(new DefaultObjectLoader());
        get(query, collector);
        return collector.getPage();
    }

    public IPage<IMObject> get(IArchetypeQuery query, Collection<String> nodes) {
        HibernateResultCollector<IMObject> collector = new IMObjectNodeResultCollector(cache, nodes);
        collector.setLoader(new DefaultObjectLoader());
        get(query, collector);
        return collector.getPage();
    }

    public IPage<NodeSet> getNodes(IArchetypeQuery query, Collection<String> nodes) {
        HibernateResultCollector<NodeSet> collector = new NodeSetResultCollector(cache, nodes);
        collector.setLoader(new DefaultObjectLoader());
        get(query, collector);
        return collector.getPage();
    }

    public abstract IPage<ObjectSet> getObjects(IArchetypeQuery query);

    protected abstract void get(IArchetypeQuery query, ResultCollector collector);
}

class DefaultQueryDelegator extends QueryDelegator {

    public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
        QueryBuilder builder = new QueryBuilder(cache, assembler);
        QueryContext context = builder.build((ArchetypeQuery) query);
        HibernateResultCollector<ObjectSet> collector
                = new ObjectSetResultCollector(context.getSelectNames(), context.getRefSelectNames(),
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

    private void get(QueryContext context, IArchetypeQuery query, ResultCollector collector) {
        String queryString = context.getQueryString();
        String countQuery = null;
        if (log.isDebugEnabled()) {
            log.debug("ArchetypeService.get: query " + queryString);
        }

        try {
            if (query.countResults()) {
                countQuery = context.getQueryString(true);
            }
            executeQuery(queryString, countQuery, new Params(context.getParameters()),
                         (HibernateResultCollector) collector, query.getFirstResult(), query.getMaxResults());
        } catch (Exception exception) {
            throw new IMObjectDAOException(FailedToExecuteQuery, exception, queryString);
        }
    }
}

class NamedQueryDelegator extends QueryDelegator {

    public IPage<ObjectSet> getObjects(IArchetypeQuery query) {
        NamedQuery q = (NamedQuery) query;
        List<String> names = (q.getNames() != null) ? new ArrayList<>(q.getNames()) : null;
        List<String> refNames = Collections.emptyList();
        HibernateResultCollector<ObjectSet> collector = new ObjectSetResultCollector(names, refNames, null);
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
