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

package org.openvpms.component.business.dao.hibernate.im.lookup;

// spring-framework
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// spring-hibernate
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

// log4j
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

// openvpms-framework
import org.openvpms.component.business.dao.im.lookup.ILookupDAO;
import org.openvpms.component.business.dao.im.lookup.LookupDAOException;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;

/**
 * This is a hibernate implementation of the {@link ILookupDAO} interface, which
 * is based on the Spring Framework's {@link HibernateDaoSupport} object.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupDAOHibernate extends HibernateDaoSupport implements
        ILookupDAO {
    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(LookupDAOHibernate.class);

    /**
     * Default constructor
     */
    public LookupDAOHibernate() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#delete(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void delete(Lookup lookup) {
        try {
            getHibernateTemplate().delete(lookup);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToDeleteLookup,
                    new Object[] { lookup }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#delete(org.openvpms.component.business.domain.im.lookup.LookupRelationship)
     */
    public void delete(LookupRelationship relationship) {
        try {
            getHibernateTemplate().delete(relationship);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToDeleteLookupRelationship,
                    new Object[] { relationship }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getLookups(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Lookup> getLookupsByConcept(String concept) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            Query query = session.getNamedQuery("lookup.getByConcept");
            query.setParameter("concept", concept);
            return  (List<Lookup>)query.list();
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetLookupsByConcept,
                    new Object[] { concept }, exception);
        } finally {
            session.close();  
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List get(String rmName, String entityName, String conceptName, String instanceName) {
        try {
            StringBuffer queryString = new StringBuffer();
            List<String> names = new ArrayList<String>();
            List<String> params = new ArrayList<String>();
            boolean andRequired = false;
            
            queryString.append("select lookup from org.openvpms.component.business.domain.im.lookup.Lookup as lookup");
            
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
                    queryString.append(" lookup.archetypeId.rmName like :rmName");
                    params.add(rmName.replace("*", "%"));
                } else {
                    queryString.append(" lookup.archetypeId.rmName = :rmName");
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
                    queryString.append(" lookup.archetypeId.entityName like :entityName");
                    params.add(entityName.replace("*", "%"));
                } else {
                    queryString.append(" lookup.archetypeId.entityName = :entityName");
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
                    queryString.append(" lookup.archetypeId.concept like :conceptName");
                    params.add(conceptName.replace("*", "%"));
                } else {
                    queryString.append(" lookup.archetypeId.concept = :conceptName");
                    params.add(conceptName);
                }
            }
            
            // process the instance name
            if (StringUtils.isEmpty(instanceName) == false) {
                if (andRequired) {
                    queryString.append(" and ");
                }
                
                names.add("instanceName");
                if ((instanceName.endsWith("*")) || (instanceName.startsWith("*"))) {
                    queryString.append(" lookup.name like :instanceName");
                    params.add(instanceName.replace("*", "%"));
                } else {
                    queryString.append(" lookup.name = :instanceName");
                    params.add(instanceName);
                }
                
            }
            
            // now execute te query
            Session session = getHibernateTemplate().getSessionFactory().openSession();
            try {
                Query query = session.createQuery(queryString.toString());
                for (int index = 0; index < names.size(); index++) {
                    query.setParameter(names.get(index), params.get(index));
                }
                return query.list();
            } finally {
              session.close();  
            }
            /**
            return getHibernateTemplate().findByNamedParam(
                   queryString.toString(),
                   (String[])names.toArray(new String[names.size()]),
                   params.toArray());
           **/
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToFindLookups,
                    new Object[]{rmName, entityName, conceptName, instanceName},
                    exception);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getSourceLookups(java.lang.String,
     *      org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    @SuppressWarnings("unchecked")
    public List<Lookup> getSourceLookups(String type, Lookup target) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            Query query = session.getNamedQuery("lookupRelationship.getSourceLookups");
            query.setParameter("type", type);
            query.setParameter("uid", target.getUid());
            return  (List<Lookup>)query.list();
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetSourceLookups,
                    new Object[] { type, target }, exception);
        } finally {
            session.close();  
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getSourceLookups(java.lang.String, java.lang.String)
     */
    public List<Lookup> getSourceLookups(String type, String target) {
        List<Lookup> lookups = null;
        
        try {
            // the type is in the form of sourceConcept.tsrgetConcept
            StringTokenizer tokens = new StringTokenizer(type, ".");
            
            if (tokens.countTokens() != 2) {
                throw new LookupDAOException(
                        LookupDAOException.ErrorCode.InvalidRelationshipType,
                        new Object[] {type});
            }
            
            @SuppressWarnings("unused") String sourceConcept = tokens.nextToken();
            String targetConcept = tokens.nextToken();
            List results = null;
            Session session = getHibernateTemplate().getSessionFactory().openSession();
            try {
                Query query = session.getNamedQuery("lookup.getLookupForConceptAndValue");
                query.setParameter("concept", targetConcept);
                query.setParameter("value", target);
                results = query.list();
            } finally {
                session.close();  
            }
            
            // if we couldn't find the target lookup then throw an exception
            if ((results == null) || (results.size() == 0)) {
                lookups = new ArrayList<Lookup>();
            } else {
                
                // if there are more then just take the first one and issue a 
                // warning
                if (results.size() > 1) {
                    logger.warn("There are multiple lookups with concept:" + 
                            targetConcept + " and value:" + target);
                }
                
                lookups = getSourceLookups(type, (Lookup)results.get(0));
            }
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetSourceLookups,
                    new Object[] { type, target }, exception);
        }
        
        return lookups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getTargetLookups(java.lang.String,
     *      org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    @SuppressWarnings("unchecked")
    public List<Lookup> getTargetLookups(String type, Lookup source) {
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        try {
            Query query = session.getNamedQuery("lookupRelationship.getTargetLookups");
            query.setParameter("type", type);
            query.setParameter("uid", source.getUid());
            return (List<Lookup>)query.list();
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetTargetLookups,
                    new Object[] { type, source }, exception);
        } finally {
            session.close();  
        }
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#getTargetLookups(java.lang.String, java.lang.String)
     */
    public List<Lookup> getTargetLookups(String type, String source) {
        List<Lookup> lookups = null;
        
        try {
            // the type is in the form of sourceConcept.targetConcept
            StringTokenizer tokens = new StringTokenizer(type, ".");
            
            if (tokens.countTokens() != 2) {
                throw new LookupDAOException(
                        LookupDAOException.ErrorCode.InvalidRelationshipType,
                        new Object[] {type});
            }
            
            String sourceConcept = tokens.nextToken();
            List results = null;
            Session session = getHibernateTemplate().getSessionFactory().openSession();
            try {
                Query query = session.getNamedQuery("lookup.getLookupForConceptAndValue");
                query.setParameter("concept", sourceConcept);
                query.setParameter("value", source);
                results = query.list();
            } finally {
                session.close();  
            }
            
            // if we couldn't find the target lookup then return an empty
            if ((results == null) || (results.size() == 0)) {
                lookups = new ArrayList<Lookup>();
            } else {
                // if there are more then just take the first one and issue a 
                // warning
                if (results.size() > 1) {
                    logger.warn("There are multiple lookups with concept:" + 
                            sourceConcept + " and value:" + source);
                }
                
                logger.debug(((Lookup)results.get(0)).toString());
                lookups = getTargetLookups(type, (Lookup)results.get(0));
            }
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToGetTargetLookups,
                    new Object[] { type, source }, exception);
        }
        
        return lookups;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#insert(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void insert(Lookup lookup) {
        try {
            getHibernateTemplate().save(lookup);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToInsertLookup,
                    new Object[] { lookup }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#insert(org.openvpms.component.business.domain.im.lookup.LookupRelationship)
     */
    public void insert(LookupRelationship relationship) {
        try {
            getHibernateTemplate().save(relationship);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToInsertLookupRelationship,
                    new Object[] { relationship });
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#save(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void save(Lookup lookup) {
        try {
            getHibernateTemplate().saveOrUpdate(lookup);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToSaveLookup,
                    new Object[] { lookup });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.dao.im.lookup.ILookupDAO#update(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void update(Lookup lookup) {
        try {
            getHibernateTemplate().saveOrUpdate(lookup);
        } catch (Exception exception) {
            throw new LookupDAOException(
                    LookupDAOException.ErrorCode.FailedToUpdateLookup,
                    new Object[] { lookup });
        }
    }
}
