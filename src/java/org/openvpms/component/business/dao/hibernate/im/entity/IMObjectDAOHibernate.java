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
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.query.NodeSet;
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
public class IMObjectDAOHibernate extends HibernateDaoSupport implements
                                                              IMObjectDAO {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(IMObjectDAOHibernate.class);

    /**
     * The loader.
     */
    private IMObjectLoader loader = new DefaultIMObjectLoader();

    /**
     * Default constructor
     */
    public IMObjectDAOHibernate() {
        super();
    }

    /**
     * Sets a loader to load {@link IMObject} instances.
     *
     * @param loader the loader
     */
    public void setIMObjectLoader(IMObjectLoader loader) {
        this.loader = loader;
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
            if (tx !=null) {
                tx.rollback();
            }

            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToSaveIMObject,
                    new Object[] { object.getUid() }, exception);

        } finally {
            clearCache();
            session.close();
        }
        /**
        try {
            getHibernateTemplate().saveOrUpdate(object);
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToSaveIMObject,
                    new Object[] { new Long(object.getUid()) }, exception);
        }
        */
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#save(java.util.Collection)
     */
    public void save(Collection objects) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            for (Object object : objects) {
                session.saveOrUpdate(object);
            }
            tx.commit();
        } catch (Exception exception) {
            if (tx !=null) {
                tx.rollback();
            }

            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToSaveCollectionOfObjects,
                    new Object[] { objects.size() }, exception);

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
            if (tx !=null) {
                tx.rollback();
            }

            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToDeleteIMObject,
                    new Object[] { object.getUid() });

        } finally {
            clearCache();
            session.close();
        }
    }

    /**
     * Execute a get using the specified query string and a map of the
     * values. The first row and the number of rows is used to control
     * the paging of the result set.
     *
     * @param queryString the query string
     * @param valueMap    the values applied to the query
     * @param firstRow    the first row to retrieve
     * @param numOfRows   the maximum number of rows to return
     * @param count       if <code>true</code> counts the total no. of rows,
     *                    returning it in {@link IPage#getTotalResults()}
     * @return IPage<IMObject>
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    @SuppressWarnings("unchecked")
    public IPage<IMObject> get(String queryString, Map<String, Object> valueMap,
                               int firstRow, int numOfRows, boolean count) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("query=" + queryString
                        + ", parameters=" + valueMap);
            }
            IMObjectCollector collector = new IMObjectCollector();
            executeQuery(queryString, new Params(valueMap), firstRow,
                         numOfRows, collector, count);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToExecuteQuery,
                    new Object[]{queryString}, exception);
        }
    }

    /**
     * Execute a get using the specified query string and a map of the values.
     * This retrieves partially populated objects and may be used to selectively
     * load parts of object graphs to improve performance.
     * <p/>
     * All simple properties of the returned objects are populated - the
     * <code>nodes</code> argument is used to specify which collection nodes to
     * populate. If empty, no collections will be loaded, and the behaviour of
     * accessing them is undefined.
     *
     * @param archetypes
     * @param nodes
     * @param queryString the query string
     * @param valueMap    the values applied to the query
     * @param firstRow    the first row to retrieve
     * @param numOfRows   the maximum number of rows to return
     * @param count       if <code>true</code> counts the total no. of rows,
     *                    returning it in {@link IPage#getTotalResults()}
     * @return the nodes for each object that matches the query criteria
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    public IPage<IMObject> get(Map<String, ArchetypeDescriptor> archetypes,
                               Collection<String> nodes,
                               String queryString,
                               Map<String, Object> valueMap, int firstRow,
                               int numOfRows, boolean count) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("nodes=" + archetypes + ", query=" + queryString
                        + ", parameters=" + valueMap);
            }

            IMObjectNodeCollector collector
                    = new IMObjectNodeCollector(archetypes, nodes);
            executeQuery(queryString, new Params(valueMap), firstRow,
                         numOfRows, collector, count);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToExecuteQuery,
                    new Object[]{queryString}, exception);
        }
    }

    /**
     * Execute a get using the specified query string and a map of the values.
     * The first row and the number of rows is used to control the paging of the
     * result set.
     *
     * @param archetypes  the archetype descriptors of the objects being
     *                    queried, keyed on short name
     * @param queryString the query string
     * @param valueMap    the values applied to the query
     * @param firstRow    the first row to retrieve
     * @param numOfRows   the maximum number of rows to return
     * @param count       if <code>true</code> counts the total no. of rows,
     *                    returning it in {@link IPage#getTotalResults()}
     * @return the nodes for each object that matches the query criteria
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    public IPage<NodeSet> getNodes(Map<String, ArchetypeDescriptor> archetypes,
                                   Collection<String> nodes, String queryString,
                                   Map<String, Object> valueMap, int firstRow,
                                   int numOfRows, boolean count){
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("nodes=" + nodes + ", query=" + queryString
                        + ", parameters=" + valueMap);
            }

            NodeCollector collector = new NodeCollector(archetypes, nodes);
            executeQuery(queryString, new Params(valueMap), firstRow,
                         numOfRows, collector, count);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToExecuteQuery,
                    new Object[]{queryString}, exception);
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
            if (!StringUtils.isEmpty(rmName)
                    || !StringUtils.isEmpty(entityName)
                    || !(StringUtils.isEmpty(conceptName))
                    || !(StringUtils.isEmpty(instanceName))) {
                queryString.append(" where ");
            }

            // process the rmName
            if (!StringUtils.isEmpty(rmName)) {
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
            if (!StringUtils.isEmpty(entityName)) {
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
            if (!StringUtils.isEmpty(conceptName)) {
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
            if (!StringUtils.isEmpty(instanceName)) {
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

            IMObjectCollector collector = new IMObjectCollector();
            executeQuery(queryString.toString(), new Params(names, params),
                         firstRow, numOfRows, collector, true);
            return collector.getPage();
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
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindIMObject,
                    new Object[] { clazz, id }, exception);
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

    /**
     * Execute the specified named query using the specified parameter
     * list.
     *
     * @param name      the name of the query
     * @param params    a map of param name and param value.
     * @param firstRow  the first row to retrieve
     * @param numOfRows the maximum number of rows to return
     * @param count       if <code>true</code> counts the total no. of rows,
     *                    returning it in {@link IPage#getTotalResults()}
     * @return IPage<IMObject>
     *         the results and associated context information
     * @throws IMObjectDAOException if there is an error processing the request
     */
    @SuppressWarnings("unchecked")
    public IPage<IMObject> getByNamedQuery(String name,
                                           Map<String, Object> params,
                                           int firstRow, int numOfRows,
                                           boolean count) {
        try {
            Collector collector = new IMObjectCollector();
            executeNamedQuery(name, params, firstRow, numOfRows, collector,
                              count);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToExecuteNamedQuery,
                    new Object[] { name }, exception);
        }
    }

    /**
     * This method will execute a query and paginate the result set.
     *
     * @param queryString the hql query
     * @param params      the query parameters
     * @param firstRow    the first row to return
     * @param numOfRows   the number of rows to return
     * @param collector   the collector
     * @param count       if <code>true</code>
     */
    @SuppressWarnings("unchecked")
    private void executeQuery(String queryString, Params params, int firstRow,
                              int numOfRows, Collector collector, boolean count)
            throws Exception {

        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
        	session.setFlushMode(FlushMode.NEVER);
            Query query = session.createQuery(queryString);
            params.setParameters(query);

            // set the first row
            if (firstRow != 0) {
                query.setFirstResult(firstRow);
            }

            // set the maximum number fo rows
            if (numOfRows != ArchetypeQuery.ALL_RESULTS) {
                query.setMaxResults(numOfRows);
                logger.debug("The maximum number of rows is " + numOfRows);
            }

            query.setCacheable(true);

            List<IMObject> rows = query.list();
            collector.setFirstRow(firstRow);
            collector.setNumOfRows(numOfRows);
            if (numOfRows == ArchetypeQuery.ALL_RESULTS) {
                collector.setTotalNumOfRows(rows.size());
            } else if (count) {
                int rowCount = count(queryString, params, session);
                if (rowCount < rows.size()) {
                    // rows deleted since initial query
                    rowCount = rows.size();
                }
                collector.setTotalNumOfRows(rowCount);
            } else {
                collector.setTotalNumOfRows(-1);
            }
            for (IMObject object : rows) {
                collector.collect(object);
            }
        } finally {
            session.close();
        }
    }

    /**
     * This method will execute a query and paginate the result set
     *
     * @param name
     *            the name of query
     * @param params
     *            the name and value of the parameters
     * @param firstRow
     *            the first row to return
     * @param numOfRows
     *            the number of rows to return
     * @param collector the collector
     * @param count       if <code>true</code> counts the total no. of rows,
     *                    returning it in {@link IPage#getTotalResults()}
     */
    @SuppressWarnings("unchecked")
    private void executeNamedQuery(String name, Map<String, Object> params,
                                    int firstRow, int numOfRows,
                                    Collector collector, boolean count) throws Exception {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
        	session.setFlushMode(FlushMode.NEVER);
            Query query = session.getNamedQuery(name);
            for (String key : params.keySet()) {
                query.setParameter(key, params.get(key));
            }

            // set first row
            if (firstRow != 0) {
                query.setFirstResult(firstRow);
            }

            // set maximum rows
            if (numOfRows != ArchetypeQuery.ALL_RESULTS) {
                query.setMaxResults(numOfRows);
                logger.debug("The maximum number of rows is " + numOfRows);
            }

            List<IMObject> rows = query.list();
            collector.setFirstRow(firstRow);
            collector.setNumOfRows(numOfRows);
            if (numOfRows == ArchetypeQuery.ALL_RESULTS) {
                collector.setTotalNumOfRows(rows.size());
            } else if (count) {
                int rowCount = countNamedQuery(query);
                if (rowCount < rows.size()) {
                    // rows deleted since initial query
                    rowCount = rows.size();
                }
                collector.setTotalNumOfRows(rowCount);
            } else {
                collector.setTotalNumOfRows(-1);
            }
            for (IMObject object : rows) {
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
     * @param params    the query parameters
     * @return the total no. of rows that would be returned by the query
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    private int count(String queryString, Params params, Session session) {
        int indexOfFrom = queryString.indexOf("from");
        if (indexOfFrom == -1) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.InvalidQueryString,
                    new Object[]{queryString});
        }

        Query query = session.createQuery("select count(*) "
                + queryString.substring(indexOfFrom));

        params.setParameters(query);
        return (Integer) query.list().get(0);
    }

    /**
     * Counts the total no. of rows that would be returned by a named query.
     *
     * @param query the named query
     * @return the total no. of rows that would be returned by the query
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    private int countNamedQuery(Query query) {
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

    private interface Collector<T> {

        void setFirstRow(int first);

        void setNumOfRows(int rows);

        void setTotalNumOfRows(int rows);

        void collect(IMObject object);

        IPage<T> getPage();
    }

    private static abstract class AbstractCollector<T> implements Collector<T> {

        private Page<T> page = new Page<T>();

        public void setFirstRow(int first) {
            page.setFirstResult(first);
        }

        public void setNumOfRows(int rows) {
            page.setPageSize(rows);
        }

        public void setTotalNumOfRows(int rows) {
            page.setTotalResults(rows);
        }

        public IPage<T> getPage() {
            page.setResults(getRows());
            return page;
        }

        protected abstract List<T> getRows();

    }

    private class IMObjectCollector extends AbstractCollector<IMObject> {

        private List<IMObject> result = new ArrayList<IMObject>();

        public void collect(IMObject object) {
            loader.load(object);
            result.add(object);
        }

        protected List<IMObject> getRows() {
            return result;
        }
    }

    private abstract class AbstractNodeCollector<T>
            extends AbstractCollector<T> {

        private final Map<String, ArchetypeDescriptor> archetypes;
        private final Collection<String> names;

        public AbstractNodeCollector(Map<String, ArchetypeDescriptor> archetypes,
                                     Collection<String> nodes) {
            this.archetypes = archetypes;
            this.names = nodes;
        }

        protected List<NodeDescriptor> getDescriptors(IMObject object) {
            String shortName = object.getArchetypeId().getShortName();
            ArchetypeDescriptor archetype = archetypes.get(shortName);
            List<NodeDescriptor> nodes = new ArrayList<NodeDescriptor>();
            for (String name : names) {
                nodes.add(archetype.getNodeDescriptor(name));
            }
            return nodes;
        }

        protected Object loadValue(NodeDescriptor descriptor, IMObject object) {
            Object value = descriptor.getValue(object);
            if (value instanceof Collection) {
                for (Object elt : (Collection) value) {
                    if (elt instanceof IMObject) {
                        loader.load((IMObject) elt);
                    }
                }
            }
            return value;
        }

    }

    private class NodeCollector extends AbstractNodeCollector<NodeSet> {

        private List<NodeSet> result = new ArrayList<NodeSet>();

        public NodeCollector(Map<String, ArchetypeDescriptor> archetypes,
                             Collection<String> nodes) {
            super(archetypes, nodes);
        }

        public void collect(IMObject object) {
            NodeSet nodes = new NodeSet(object.getObjectReference());
            for (NodeDescriptor descriptor : getDescriptors(object)) {
                Object value = loadValue(descriptor, object);
                nodes.set(descriptor.getName(), value);
            }
            result.add(nodes);
        }

        protected List<NodeSet> getRows() {
            return result;
        }
    }

    private class IMObjectNodeCollector
            extends AbstractNodeCollector<IMObject> {

        private List<IMObject> result = new ArrayList<IMObject>();

        public IMObjectNodeCollector(
                Map<String, ArchetypeDescriptor> archetypes,
                Collection<String> nodes) {
            super(archetypes, nodes);
        }

        public void collect(IMObject object) {
            for (NodeDescriptor descriptor : getDescriptors(object)) {
                loadValue(descriptor, object);
            }
            result.add(object);
        }

        protected List<IMObject> getRows() {
            return result;
        }
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

        public String[]  getNames() {
            return names;
        }

        public Object[] getValues() {
            return values;
        }
    }

}
