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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.PagingCriteria;

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
            boolean activeOnly, PagingCriteria pagingCriteria,
            String sortProperty, boolean ascending) {
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

            // determine if a sort order has been specified
            if (!StringUtils.isEmpty(sortProperty)) {
                queryString.append(" order by entity." + sortProperty);
                if (ascending) {
                    queryString.append(" asc");
                } else {
                    queryString.append(" desc");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Executing " + queryString + " with names " 
                            + names.toString() + " and params " + params.toString());
            }
            
            return executeQuery(queryString.toString(), names, params,
                    pagingCriteria, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindIMObjects,
                    new Object[] { rmName, entityName, conceptName,
                            instanceName, clazz }, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#get(java.lang.String[], java.lang.String, java.lang.String, boolean, org.openvpms.component.system.common.search.PagingCriteria, java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    public IPage<IMObject> get(String[] shortNames, String instanceName, String clazz, boolean activeOnly, PagingCriteria pagingCriteria, String sortProperty, boolean ascending) {
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
            if ((StringUtils.isEmpty(instanceName) == false) ||
                (shortNames != null && shortNames.length > 0)) {
                queryString.append(" where ");
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
                andRequired = true;
            }


            // process the rmName
            if (shortNames != null && shortNames.length > 0) {
                boolean orRequired = false;
                boolean appendClosingBrace = false;
                if (andRequired) {
                    queryString.append(" and (");
                    appendClosingBrace = true;
                }
                andRequired = true;
                for (int index = 0; index < shortNames.length; index++) {
                    // derive the entityName and the shortName
                    StringTokenizer tokens = new StringTokenizer(shortNames[index], ".");
                    
                    if (tokens.countTokens() != 2) {
                        // throws exception
                    }
                    
                    String entityName = tokens.nextToken();
                    String conceptName = tokens.nextToken();
                    
                    // open the expression
                    if (orRequired) {
                        queryString.append(" or ");
                    }
                    queryString.append("(");

                    // process the entity name
                    String ename = "entityName" + index; 
                    names.add(ename);
                    if ((entityName.endsWith("*")) || (entityName.startsWith("*"))) {
                        queryString.append(" entity.archetypeId.entityName like :" + ename);
                        params.add(entityName.replace("*", "%"));
                    } else {
                        queryString.append(" entity.archetypeId.entityName = :" + ename);
                        params.add(entityName);
                    }

                    // process the concept name
                    queryString.append(" and ");
                    String cname = "conceptName" + index; 
                    names.add(cname);
                    if ((conceptName.endsWith("*"))|| (conceptName.startsWith("*"))) {
                        queryString.append(" entity.archetypeId.concept like :" + cname);
                        params.add(conceptName.replace("*", "%"));
                    } else {
                        queryString.append(" entity.archetypeId.concept = :" + cname);
                        params.add(conceptName);
                    }
                    queryString.append(")");
                    orRequired = true;
                }
                
                if (appendClosingBrace) {
                    queryString.append(")");
                }
            }

            // determine if a sort order has been specified
            if (!StringUtils.isEmpty(sortProperty)) {
                queryString.append(" order by entity." + sortProperty);
                if (ascending) {
                    queryString.append(" asc");
                } else {
                    queryString.append(" desc");
                }
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Executing " + queryString + " with names " 
                            + names.toString() + " and params " + params.toString());
            }
            
            return executeQuery(queryString.toString(), names, params,
                    pagingCriteria, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindObjectsWithArchetypes,
                    new Object[] { shortNames }, exception);
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
            Map<String, Object> params, PagingCriteria pagingCriteria,
            String sortProperty, boolean ascending) {
        try {
            return executeNamedQuery(name, params, pagingCriteria, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToExecuteNamedQuery,
                    new Object[] { name }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IEntityDAO#getActs(long,
     *      java.lang.String, java.util.Date, java.util.Date, boolean)
     */
    @SuppressWarnings("unchecked")
    public IPage<Participation> getParticipations(IMObjectReference ref,
            String conceptName, Date startTimeFrom, Date startTimeThru,
            Date endTimeFrom, Date endTimeThru, boolean activeOnly, 
            PagingCriteria pagingCriteria, String sortProperty, boolean ascending) {
        try {
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<Object> params = new ArrayList<Object>();
            boolean andRequired = false;

            queryString.append("from ");
            queryString.append(Participation.class.getName());
            queryString.append(" as participation ");

            // check to see if one or more of the values have been specified
            if ((!StringUtils.isEmpty(conceptName)) || (ref != null)
                    || (startTimeFrom != null) || (startTimeThru != null)
                    || (endTimeFrom != null) || (endTimeThru != null)) {
                queryString.append(" where ");
            }

            // process the entityUid
            if (ref != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("linkId");
                andRequired = true;
                queryString.append(" participation.entity.linkId = :linkId");
                params.add(ref.getLinkId());
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
                            .append(" participation.archetypeId.concept like :conceptName");
                    params.add(conceptName.replace("*", "%"));
                } else {
                    queryString
                            .append(" participation.archetypeId.concept = :conceptName");
                    params.add(conceptName);
                }
            }

            // process the startTimeFrom
            if (startTimeFrom != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("startTimeFrom");
                andRequired = true;
                queryString
                        .append(" participation.activeStartTime >= :startTimeFrom");
                params.add(startTimeFrom);
            }

            // process the startTimeThru
            if (startTimeThru != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("startTimeThru");
                andRequired = true;
                queryString
                        .append(" participation.activeStartTime <= :startTimeThru");
                params.add(startTimeThru);
            }

            // process the endTimeFrom
            if (endTimeFrom != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("endTimeFrom");
                andRequired = true;
                queryString
                        .append(" participation.activeEndTime >= :endTimeFrom");
                params.add(endTimeFrom);
            }

            // process the endTimeThru
            if (endTimeThru != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("endTimeThru");
                andRequired = true;
                queryString
                        .append(" participation.activeEndTime >= :endTimeThru");
                params.add(endTimeThru);
            }

            // determine if we are only interested in active objects
            if (activeOnly) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                queryString.append(" participation.active = 1");
            }

            // determine if a sort order has been specified
            if (!StringUtils.isEmpty(sortProperty)) {
                queryString.append(" order by participation." + sortProperty);
                if (ascending) {
                    queryString.append(" asc");
                } else {
                    queryString.append(" desc");
                }
            }

            return executeQuery(queryString.toString(), names, params,
                    pagingCriteria, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindParticipations,
                    new Object[] { ref, conceptName, startTimeFrom, endTimeFrom },
                    exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getActs(long,
     *      java.lang.String, java.lang.String, java.util.Date, java.util.Date,
     *      java.util.Date, java.util.Date, java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    public IPage<Act> getActs(IMObjectReference ref, String pConceptName,
            String entityName, String aConceptName, Date startTimeFrom,
            Date startTimeThru, Date endTimeFrom, Date endTimeThru,
            String status, boolean activeOnly, PagingCriteria pagingCriteria, 
            String sortProperty, boolean ascending) {
        try {
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<Object> params = new ArrayList<Object>();
            boolean andRequired = false;

            queryString.append("select act from ");
            queryString.append(Act.class.getName());
            queryString.append(" as act ");
            queryString.append(" left outer join act.participations as participation ");

            // check to see if one or more of the values have been specified
            if ((!StringUtils.isEmpty(entityName))
                    || (!StringUtils.isEmpty(pConceptName))
                    || (!StringUtils.isEmpty(aConceptName))
                    || (!StringUtils.isEmpty(status)) || (ref != null)
                    || (startTimeFrom != null) || (startTimeThru != null)
                    || (endTimeFrom != null) || (endTimeThru != null)) {
                queryString.append(" where ");
            }

            // process the entityUid
            if (ref != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("linkId");
                andRequired = true;
                queryString.append(" participation.entity.linkId = :linkId");
                params.add(ref.getLinkId());
            }

            // process the participant concept name
            if (!StringUtils.isEmpty(pConceptName)) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("pConceptName");
                andRequired = true;
                if ((pConceptName.endsWith("*")) || (pConceptName.startsWith("*"))) {
                    queryString.append(" participation.archetypeId.concept like :pConceptName");
                    params.add(pConceptName.replace("*", "%"));
                } else {
                    queryString.append(" participation.archetypeId.concept = :pConceptName");
                    params.add(pConceptName);
                }
            }

            // process the act entity name
            if (!StringUtils.isEmpty(entityName)) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("entityName");
                andRequired = true;
                if ((entityName.endsWith("*")) || (entityName.startsWith("*"))) {
                    queryString.append(" act.archetypeId.entityName like :entityName");
                    params.add(entityName.replace("*", "%"));
                } else {
                    queryString.append(" act.archetypeId.entityName = :entityName");
                    params.add(entityName);
                }
            }

            // process the activity concept name
            if (!StringUtils.isEmpty(aConceptName)) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("aConceptName");
                andRequired = true;
                if ((aConceptName.endsWith("*"))|| (aConceptName.startsWith("*"))) {
                    queryString.append(" act.archetypeId.concept like :aConceptName");
                    params.add(aConceptName.replace("*", "%"));
                } else {
                    queryString.append(" act.archetypeId.concept = :aConceptName");
                    params.add(aConceptName);
                }
            }

            // process the startTimeFrom
            if (startTimeFrom != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("startTimeFrom");
                andRequired = true;
                queryString.append(" act.activityStartTime >= :startTimeFrom");
                params.add(startTimeFrom);
            }

            // process the startTimeThru
            if (startTimeThru != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("startTimeThru");
                andRequired = true;
                queryString.append(" act.activityStartTime <= :startTimeThru");
                params.add(startTimeThru);
            }

            // process the endTimeFrom
            if (endTimeFrom != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("endTimeFrom");
                andRequired = true;
                queryString.append(" act.activityEndTime >= :endTimeFrom");
                params.add(endTimeFrom);
            }

            // process the endTimeThru
            if (endTimeThru != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("endTimeThru");
                andRequired = true;
                queryString.append(" act.activityEndTime <= :endTimeThru");
                params.add(endTimeThru);
            }

            // process the status
            if (!StringUtils.isEmpty(status)) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("status");
                andRequired = true;
                queryString.append(" act.status = :status");
                params.add(status);
            }

            // determine if we are only interested in active objects
            if (activeOnly) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                queryString.append(" act.active = 1");
            }

            // determine if a sort order has been specified
            if (!StringUtils.isEmpty(sortProperty)) {
                queryString.append(" order by act." + sortProperty);
                if (ascending) {
                    queryString.append(" asc");
                } else {
                    queryString.append(" desc");
                }
            }

            return executeQuery(queryString.toString(), names, params,
                    pagingCriteria, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindActs,
                    new Object[] { entityName, aConceptName, startTimeFrom,
                            endTimeFrom, status }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.common.IMObjectDAO#getActs(java.lang.String,
     *      java.lang.String, java.util.Date, java.util.Date, java.util.Date,
     *      java.util.Date, java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    public IPage<Act> getActs(String entityName, String conceptName,
            Date startTimeFrom, Date startTimeThru, Date endTimeFrom,
            Date endTimeThru, String status, boolean activeOnly, 
            PagingCriteria pagingCriteria, String sortProperty, boolean ascending) {
        try {
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<Object> params = new ArrayList<Object>();
            boolean andRequired = false;

            queryString.append("from ");
            queryString.append(Act.class.getName());
            queryString.append(" as act ");

            // check to see if one or more of the values have been specified
            if ((!StringUtils.isEmpty(entityName))
                    || (!StringUtils.isEmpty(conceptName))
                    || (!StringUtils.isEmpty(status))
                    || (startTimeFrom != null) || (startTimeThru != null)
                    || (endTimeFrom != null) || (endTimeThru != null)) {
                queryString.append(" where ");
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
                            .append(" act.archetypeId.entityName like :entityName");
                    params.add(entityName.replace("*", "%"));
                } else {
                    queryString
                            .append(" act.archetypeId.entityName = :entityName");
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
                            .append(" act.archetypeId.concept like :conceptName");
                    params.add(conceptName.replace("*", "%"));
                } else {
                    queryString
                            .append(" act.archetypeId.concept = :conceptName");
                    params.add(conceptName);
                }
            }

            // process the startTimeFrom
            if (startTimeFrom != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("startTimeFrom");
                andRequired = true;
                queryString.append(" act.activityStartTime >= :startTimeFrom");
                params.add(startTimeFrom);
            }

            // process the startTimeThru
            if (startTimeThru != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("startTimeThru");
                andRequired = true;
                queryString.append(" act.activityStartTime <= :startTimeThru");
                params.add(startTimeThru);
            }

            // process the endTimeFrom
            if (endTimeFrom != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("endTimeFrom");
                andRequired = true;
                queryString.append(" act.activityEndTime >= :endTimeFrom");
                params.add(endTimeFrom);
            }

            // process the endTimeThru
            if (endTimeThru != null) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("endTimeThru");
                andRequired = true;
                queryString.append(" act.activityEndTime <= :endTimeThru");
                params.add(endTimeThru);
            }

            // process the status
            if (!StringUtils.isEmpty(status)) {
                if (andRequired) {
                    queryString.append(" and ");
                }

                names.add("status");
                andRequired = true;
                queryString.append(" act.status = :status");
                params.add(status);
            }

            // determine if we are only interested in active objects
            if (activeOnly) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                queryString.append(" act.active = 1");
            }

            // determine if a sort order has been specified
            if (!StringUtils.isEmpty(sortProperty)) {
                queryString.append(" order by act." + sortProperty);
                if (ascending) {
                    queryString.append(" asc");
                } else {
                    queryString.append(" desc");
                }
            }

            return executeQuery(queryString.toString(), names, params,
                    pagingCriteria, new Page<IMObject>());
        } catch (Exception exception) {
            throw new IMObjectDAOException(
                    IMObjectDAOException.ErrorCode.FailedToFindActs,
                    new Object[] { entityName, conceptName, startTimeFrom,
                            endTimeFrom, status }, exception);
        }
    }

    /**
     * This method will execute a query and paginate the result set
     * 
     */
    @SuppressWarnings("unchecked")
    private IPage executeQuery(String queryString, List<String> names,
            List<Object> params, PagingCriteria pagingCriteria, Page page)
            throws Exception {
        int totalNumOfRows = 0;
        int firstRow = 0;
        int numOfRows = PagingCriteria.ALL_ROWS;

        // check whetehr a paging criteria has been specified
        if (pagingCriteria != null) {
            firstRow = pagingCriteria.getFirstRow();
            numOfRows = pagingCriteria.getNumOfRows();
        }

        Session session = getHibernateTemplate().getSessionFactory()
            .openSession();
        try {
            // first query the number of rows
            Query query = null;
            if (numOfRows != PagingCriteria.ALL_ROWS) {
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
            if (numOfRows != PagingCriteria.ALL_ROWS) {
                query.setMaxResults(numOfRows);
                logger.debug("The maximum number of rows is " + numOfRows);
            }
    
            List<IMObject> rows = query.list();
            page.setPagingCriteria(new PagingCriteria(firstRow, rows.size()));
            if (numOfRows == PagingCriteria.ALL_ROWS) {
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
     */
    @SuppressWarnings("unchecked")
    private IPage executeNamedQuery(String name, Map<String, Object> params, 
            PagingCriteria pagingCriteria, Page page) throws Exception {
        int totalNumOfRows = 0;
        int firstRow = 0;
        int numOfRows = PagingCriteria.ALL_ROWS;

        Session session = getHibernateTemplate().getSessionFactory()
            .openSession();
        try {
            Query query = null;

            // check whetehr a paging criteria has been specified
            if (pagingCriteria != null) {
                firstRow = pagingCriteria.getFirstRow();
                numOfRows = pagingCriteria.getNumOfRows();
            }

            // get the count first....IS THIS THE BEST APPROACH FOR NAMED
            // QUERIES
            if (numOfRows != PagingCriteria.ALL_ROWS) {
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
            if (numOfRows != PagingCriteria.ALL_ROWS) {
                query.setMaxResults(numOfRows);
                logger.debug("The maximum number of rows is " + numOfRows);
            }
            

            List<IMObject> rows = query.list();
            page.setPagingCriteria(new PagingCriteria(firstRow, rows.size()));
            if (numOfRows == PagingCriteria.ALL_ROWS) {
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
