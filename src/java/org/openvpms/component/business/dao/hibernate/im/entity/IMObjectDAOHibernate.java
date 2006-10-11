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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
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
            session.close();
        }
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#get(java.lang.String, java.util.Map, int, int)
     */
    @SuppressWarnings("unchecked")
    public IPage<IMObject> get(String queryString, Map<String, Object> valueMap, int firstRow, int numOfRows) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("query=" + queryString
                        + ", parameters=" + valueMap);
            }
            IMObjectCollector collector = new IMObjectCollector();
            executeQuery(queryString, new Params(valueMap), firstRow,
                         numOfRows, collector);
            return collector.getPage();
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                IMObjectDAOException.ErrorCode.FailedToExecuteQuery,
                new Object[] { queryString }, exception);
        }
    }

    /**
     * Execute a get using the specified query string and a map of the values.
     * The first row and the number of rows is used to control the paging of the
     * result set.
     *
     * @param nodes       the names of the nodes to return
     * @param queryString the query string
     * @param valueMap    the values applied to the query
     * @param firstRow    the first row to retrieve
     * @param numOfRows   the maximum number of rows to return
     * @return the nodes for each object that matches the query criteria
     * @throws IMObjectDAOException a runtime exception, raised if the request
     *                              cannot complete.
     */
    public IPage<NodeSet> get(List<NodeDescriptor> nodes, String queryString,
                              Map<String, Object> valueMap, int firstRow,
                              int numOfRows) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("nodes=" + nodes + ", query=" + queryString
                        + ", parameters=" + valueMap);
            }

            NodeCollector collector = new NodeCollector(nodes);
            executeQuery(queryString, new Params(valueMap), firstRow,
                         numOfRows, collector);
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
                         firstRow, numOfRows, collector);
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
     * @param params the query parameters
     * @param firstRow
     *            the first row to return
     * @param numOfRows
     *            the number of rows to return
     * @param collector
     */
    @SuppressWarnings("unchecked")
    private void executeQuery(String queryString, Params params, int firstRow,
                              int numOfRows, Collector collector)
            throws Exception {
        int totalNumOfRows = 0;

        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            // first query the number of rows
            Query query;
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
                params.setParameters(query);
                totalNumOfRows = (Integer) query.list().get(0);
                if (logger.isDebugEnabled()) {
                    logger.debug("The number of rows returned is "
                                    + totalNumOfRows);
                }
            }

            // now execute the query
            query = session.createQuery(queryString);
            params.setParameters(query);

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
            collector.setFirstRow(firstRow);
            collector.setNumOfRows(numOfRows);
            if (numOfRows == ArchetypeQuery.ALL_ROWS) {
                collector.setTotalNumOfRows(rows.size());
            } else {
                collector.setTotalNumOfRows(totalNumOfRows);
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
     * @param page
     *            the page to populate
     */
    @SuppressWarnings("unchecked")
    private IPage executeNamedQuery(String name, Map<String, Object> params,
                                    int firstRow, int numOfRows, Page page) throws Exception {
        int totalNumOfRows = 0;

        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            Query query;


            // get the count first....IS THIS THE BEST APPROACH FOR NAMED
            // QUERIES
            if (numOfRows != ArchetypeQuery.ALL_ROWS) {
                query = session.getNamedQuery(name);
                for (String key : params.keySet()) {
                    query.setParameter(key, params.get(key));
                }
                totalNumOfRows = (Integer) query.list().get(0);
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
            page.setFirstRow(first);
        }

        public void setNumOfRows(int rows) {
            page.setNumOfRows(rows);
        }

        public void setTotalNumOfRows(int rows) {
            page.setTotalNumOfRows(rows);
        }

        public IPage<T> getPage() {
            page.setRows(getRows());
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

    private class NodeCollector extends AbstractCollector<NodeSet> {

        private List<NodeDescriptor> descriptors = new ArrayList<NodeDescriptor>();

        private List<NodeSet> result = new ArrayList<NodeSet>();

        public NodeCollector(List<NodeDescriptor> nodes) {
            this.descriptors = nodes;
        }

        public void collect(IMObject object) {
            NodeSet nodes
                    = new NodeSet(object.getObjectReference());
            for (NodeDescriptor descriptor : descriptors) {
                Object value = descriptor.getValue(object);
                nodes.set(descriptor.getName(), value);
            }
            result.add(nodes);
        }

        protected List<NodeSet> getRows() {
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
