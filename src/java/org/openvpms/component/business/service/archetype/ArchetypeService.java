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

package org.openvpms.component.business.service.archetype;

// java core
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// log4j
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.business.service.archetype.query.QueryBuilder;
import org.openvpms.component.business.service.archetype.query.QueryContext;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.search.PagingCriteria;
import org.openvpms.component.system.common.search.SortCriteria;
import org.openvpms.component.system.service.hibernate.EntityInterceptor;

/**
 * This basic implementation of an archetype service, which reads in the
 * archetypes from the specified XML document and creates an in memory registry.
 * <p>
 * This implementation has the following constraints 1. All archetype
 * definitions must be deployed in a single directory. The name of hte directory
 * is specified on construction 2. The archetype records must be stored in a
 * single XML document and the structure of the document must comply with XML
 * Schema defined in <b>archetype.xsd</b>.
 * <p>
 * NOTE: The archetype service is currently supporting both styles of queries
 * but this will change to support a single query api for all queries once it
 * has been validated.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeService implements IArchetypeService {
    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(ArchetypeService.class);

    /**
     * A reference to the archetype descriptor cache
     */
    private IArchetypeDescriptorCache dCache;
    
    /**
     * The DAO instance it will use...optional
     */
    private IMObjectDAO dao;
    
    /**
     * A reference to the query builder. The query build is stuff is not
     * well abstracted at this stage since there are no requirements to 
     * support anything outside (hibernate).
     */
    private QueryBuilder builder = new QueryBuilder(this);

    /**
     * The entity interceptor that is used to intercept calls hibernate
     * calls
     */
    private EntityInterceptor entityInterceptor;

    /**
     * Construct an instance of this service using the specified archetpe class by loading and parsing all the
     * descripor cache
     * <p>
     * The resource specified by afile must be loadable from the classpath. A
     * similar constraint applies to the resourcr specified by adir, it must be
     * a valid path in the classpath.
     * 
     * @param archeFile
     *            the file that holds all the archetype records.
     * @param assertFile
     *            the file that holds the assertions
     * @throws ArchetypeServiceException
     * @throws ArchetypeDescriptorCacheException
     */
    public ArchetypeService(IArchetypeDescriptorCache cache) {
        dCache = cache;
    }

    /**
     * @return Returns the dao.
     */
    public IMObjectDAO getDao() {
        return dao;
    }

    /**
     * @param dao
     *            The dao to set.
     */
    public void setDao(IMObjectDAO dao) {
        this.dao = dao;
    }

    /**
     * @return Returns the entityInterceptor.
     */
    public EntityInterceptor getEntityInterceptor() {
        return entityInterceptor;
    }

    /**
     * @param entityInterceptor The entityInterceptor to set.
     */
    public void setEntityInterceptor(EntityInterceptor entityInterceptor) {
        this.entityInterceptor = entityInterceptor;
        entityInterceptor.setDescriptorCache(dCache);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptor(java.lang.String)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String name) {
        return dCache.getArchetypeDescriptor(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptor(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return dCache.getArchetypeDescriptor(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecord(java.lang.String)
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return dCache.getAssertionTypeDescriptor(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecords()
     */
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors() {
        return dCache.getAssertionTypeDescriptors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#createDefaultObject(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public IMObject create(ArchetypeId id) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.create: Creating object of type " 
                    + id.getShortName());
        }
        
        ArchetypeDescriptor desc = dCache.getArchetypeDescriptor(id);
        if (desc != null) {
            return create(desc);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#createDefaultObject(java.lang.String)
     */
    public IMObject create(String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.create: Creating object of type " 
                    + name);
        }

        ArchetypeDescriptor desc = dCache.getArchetypeDescriptor(name);
        if (desc != null) {
            return create(desc);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#validateObject(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void validateObject(IMObject object) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.validateObject: Validating object of type " 
                    + object.getArchetypeId().getShortName() 
                    + " with uid " + object.getUid()
                    + " and version " + object.getVersion());
        }

        List<ValidationError> errors = new ArrayList<ValidationError>();

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object
                .getArchetypeId());
        if (descriptor == null) {
            errors.add(new ValidationError(null, new StringBuffer(
                    "No archetype definition for ").append(
                    object.getArchetypeId()).toString()));
            logger.error(new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                    new Object[] { object.getArchetypeId().toString() }));
        }

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        if (descriptor.getNodeDescriptors().size() > 0) {
            JXPathContext context = JXPathHelper.newContext(object);
            validateObject(context, descriptor.getNodeDescriptors(), errors);
        }

        /**
         * if we have accumulated any errors then throw an exception
         */
        if (errors.size() > 0) {
            throw new ValidationException(
                    errors,
                    ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype,
                    new Object[] { object.getArchetypeId() });

        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#deriveValues(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void deriveValues(IMObject object) {
        // check for a non-null object
        if (object == null) {
            return;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.deriveValues: Deriving values for type" 
                    + object.getArchetypeId().getShortName() 
                    + " with uid " + object.getUid()
                    + " and version " + object.getVersion());
        }
        
        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object.getArchetypeId());
        if (descriptor == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                    new Object[] { object.getArchetypeId().toString() });
        }

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        if (descriptor.getNodeDescriptors().size() > 0) {
            JXPathContext context = JXPathHelper.newContext(object);
            deriveValues(context, descriptor.getNodeDescriptors());
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#deriveValue(org.openvpms.component.business.domain.im.common.IMObject, java.lang.String)
     */
    public void deriveValue(IMObject object, String node) {
        if (object == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NonNullObjectRequired);
        }
        
        if (StringUtils.isEmpty(node)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NonNullNodeNameRequired);
        }
        
        // check that the node name is valid for the specified object
        ArchetypeDescriptor adesc = getArchetypeDescriptor(object.getArchetypeId());
        if (adesc == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidArchetypeDescriptor,
                    new Object[] {object.getArchetypeId()});
        }
        
        NodeDescriptor ndesc = adesc.getNodeDescriptor(node);
        if (ndesc == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidNodeDescriptor,
                    new Object[] {node, object.getArchetypeId()});
        }

        // derive the value
        ndesc.deriveValue(object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeRecords()
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors() {
        return dCache.getArchetypeDescriptors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptors(java.lang.String)
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        return dCache.getArchetypeDescriptors(shortName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptorsByRmName(java.lang.String)
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(String rmName) {
        return dCache.getArchetypeDescriptorsByRmName(rmName);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(org.openvpms.component.system.common.query.ArchetypeQuery)
     */
    public IPage<IMObject> get(ArchetypeQuery query) {
       if (query == null) {
           return null;
       }
       
       if (logger.isDebugEnabled()) {
           logger.debug("ArchetypeService.get: query " + query);
       }
       
       try {
           QueryContext context = builder.build(query);
           if (logger.isDebugEnabled()) {
               logger.debug("ArchetypeService.get: query " + context.getQueryString());
           }
           
           return dao.get(context.getQueryString(), context.getValueMap(),
                   new PagingCriteria(query.getFirstRow(), query.getNumOfRows()));
       } catch (Exception exception) {
           throw new ArchetypeServiceException(
                   ArchetypeServiceException.ErrorCode.FailedToExecuteQuery,
                   new Object[] { query.toString()}, 
                   exception);
       }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Deprecated
    public IPage<IMObject> get(String rmName, String entityName,
            String conceptName, String instanceName, boolean activeOnly, 
            PagingCriteria pagingCriteria, SortCriteria sortCriteria) {
        return get(rmName, entityName, conceptName, instanceName, false, 
                activeOnly, pagingCriteria, sortCriteria);
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, int, int)
     */
    @Deprecated
    public IPage<IMObject> get(String rmName, String entityName, String conceptName, String instanceName, boolean primaryOnly, boolean activeOnly, PagingCriteria pagingCriteria, SortCriteria sortCriteria) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.get: rmName " + rmName
                    + " entityName " + entityName
                    + " conceptName " + conceptName
                    + " instanceName " + instanceName);
        }
        
        IPage<IMObject> page = null;
        DistinctTypesResultSet distinct = getDistinctTypes(rmName, entityName, 
                conceptName, primaryOnly);
        
        if (distinct.types.size() == 1) {
            // We can only let the database do the sorting and paging if
            // a single type is represented by the class request.
            try {
                page = dao.get(rmName, entityName, conceptName, instanceName, 
                        distinct.types.iterator().next(), activeOnly, pagingCriteria, 
                        getSortProperty(distinct.descriptors, sortCriteria), 
                        isAscending(sortCriteria));
            } catch (IMObjectDAOException exception) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToFindObjects,
                        new Object[] { rmName, entityName, conceptName, instanceName}, 
                        exception);
            }
        } else {
            // We do not support a search across different classes
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.CanOnlySearchAgainstSingleType,
                    new Object[] {distinct.types.toString()});
        }

        return page;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String[])
     */
    @Deprecated
    public IPage<IMObject> get(String[] shortNames, boolean activeOnly, 
            PagingCriteria pagingCriteria, SortCriteria sortCriteria) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.get: shortNames " + shortNames);
        }
        
        IPage<IMObject> page = null;
        DistinctTypesResultSet distinct = getDistinctTypes(shortNames, false); 
        
        if (distinct.types.size() == 1) {
            // We can only let the database do the sorting and paging if
            // a single type is represented by the client request.
            try {
                page = dao.get(shortNames, null, distinct.types.iterator().next(), 
                        activeOnly, pagingCriteria, 
                        getSortProperty(distinct.descriptors, sortCriteria), 
                        isAscending(sortCriteria));
            } catch (IMObjectDAOException exception) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToFindObjectsMatchingShortNames,
                        new Object[] { distinct.types.toString()}, 
                        exception);
            }
        } else {
            // We do not support a search across different classes
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.CanOnlySearchAgainstSingleType,
                    new Object[] {distinct.types.toString()});
        }

        return page;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getById(org.openvpms.component.business.domain.archetype.ArchetypeId,
     *      long)
     */
    @Deprecated
    public IMObject getById(ArchetypeId archId, long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.getById: Retrieving object of type " 
                    + archId.getShortName() 
                    + " and with uid " + id);
        }

        // check that we have an archetype id defined
        if (archId == null) {
            return null;
        }

        // check that we have a dao defined
        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[] {});
        }

        // retrieve the descriptor and call the dao
        ArchetypeDescriptor desc = getArchetypeDescriptor(archId);

        try {
            return dao.getById(desc.getClassName(), id);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToFindObject,
                    new Object[] { desc.getClassName(), id }, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String[], java.lang.String, boolean, boolean)
     */
    @Deprecated
    public IPage<IMObject> get(String[] shortNames, String instanceName, 
            boolean primaryOnly, boolean activeOnly, PagingCriteria pagingCriteria, 
            SortCriteria sortCriteria) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.get: shortNames " + shortNames
                    + " instanceName " + instanceName);
        }
        
        IPage<IMObject> page = null;
        DistinctTypesResultSet distinct = getDistinctTypes(shortNames, primaryOnly); 
        
        if (distinct.types.size() == 1) {
            // We can only let the database do the sorting and paging if
            // a single type is represented by the client request.
            page = dao.get(shortNames, instanceName, distinct.types.iterator().next(), 
                    activeOnly, pagingCriteria, 
                    getSortProperty(distinct.descriptors, sortCriteria), 
                    isAscending(sortCriteria));
        } else {
            // We do not support a search across different classes
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.CanOnlySearchAgainstSingleType,
                    new Object[] {distinct.types.toString()});
        }

        return page;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getByNamedQuery(java.lang.String, java.util.Map)
     */
    @Deprecated
    public IPage<IMObject> getByNamedQuery(String name, Map<String, Object> params, 
            PagingCriteria pagingCriteria) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.getByNamedQuery: query " + name);
        }
        
        // check that we have a non0null query name
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        // check that we have a dao defined
        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[] {});
        }

        try {
            return dao.getByNamedQuery(name, params, pagingCriteria, null, true);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedInGetByNamedQuery,
                    new Object[] {name}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(org.openvpms.component.business.domain.im.common.IMObjectReference)
     */
    @Deprecated
    public IMObject get(IMObjectReference reference) {
        if (reference == null) {
            return null;
        }

        // check that we have a dao defined
        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[] {});
        }

        try {
            // retrieve the descriptor and call the dao
            ArchetypeDescriptor desc = getArchetypeDescriptor(reference.getArchetypeId());
            IMObject imobj  = dao.getByLinkId(desc.getClassName(), reference.getLinkId());
            if (logger.isDebugEnabled()) {
                logger.debug("ArchetypeService.get: reference " 
                        + reference.getArchetypeId().getShortName()
                        + " uid " + imobj.getUid()
                        + " version " + imobj.getVersion());
            }
            
            return imobj;
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedInGetByObjectReference,
                    new Object[] {reference}, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#remove(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void remove(IMObject entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.remove: Removing object of type "  
                    + entity.getArchetypeId().getShortName()
                    + " with uid " + entity.getUid()
                    + " and version " + entity.getVersion());
        }

        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[] {});
        }

        validateObject(entity);
        try {
            dao.delete(entity);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToDeleteObject,
                    new Object[] { entity.getUid() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#save(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void save(IMObject entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.save: Saving object of type "  
                    + entity.getArchetypeId().getShortName()
                    + " with uid " + entity.getUid()
                    + " and version " + entity.getVersion());
        }

        if (dao == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDaoConfigured,
                    new Object[] {});
        }

        validateObject(entity);
        try {
            dao.save(entity);
             
            // a quick fix to the archetype descriptor update problem
            // couldn't get the hibernate interceptors working
            // TODO Review the interceptors
            if (entity instanceof ArchetypeDescriptor) {
                ArchetypeDescriptor adesc = (ArchetypeDescriptor)entity;
                if (dCache != null) {
                    dCache.addArchetypeDescriptor(adesc, true);
                }
            }
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToSaveObject,
                    new Object[] { entity }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeShortNames(java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    public List<String> getArchetypeShortNames(String rmName,
            String entityName, String conceptName, boolean primaryOnly) {
        return dCache.getArchetypeShortNames(rmName, entityName, conceptName, 
                primaryOnly);
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeShortNames(java.lang.String, boolean)
     */
    public List<String> getArchetypeShortNames(String shortName, boolean primaryOnly) {
        return dCache.getArchetypeShortNames(shortName, primaryOnly);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeShortNames()
     */
    public List<String> getArchetypeShortNames() {
        return dCache.getArchetypeShortNames();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#getActs(long, java.lang.String, java.lang.String, java.util.Date, java.util.Date, java.lang.String, boolean)
     */
    @Deprecated
    public IPage<Act> getActs(IMObjectReference ref, String pConceptName, String entityName, String aConceptName, Date startTimeFrom, Date startTimeThru, Date endTimeFrom, Date endTimeThru, String status, boolean activeOnly, PagingCriteria pagingCriteria, SortCriteria sortCriteria) {
        if (ref == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.EntityUidNotSpecified);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.getActs: ref " + ref  
                    + " pConceptName " + pConceptName
                    + " entityName " + entityName
                    + " aConceptName " + aConceptName);
        }

        DistinctTypesResultSet distinct = getDistinctTypes(Act.class.getName(),
                entityName, aConceptName);
        try {
            return dao.getActs(ref, pConceptName, entityName, aConceptName, startTimeFrom, 
                    startTimeThru, endTimeFrom, endTimeThru, status, activeOnly, 
                    pagingCriteria, getSortProperty(distinct.descriptors, sortCriteria), 
                    isAscending(sortCriteria));
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToGetActForEntity,
                    new Object[] { ref }, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#getActs(java.lang.String, java.lang.String, java.util.Date, java.util.Date, java.lang.String, boolean)
     */
    @Deprecated
    public IPage<Act> getActs(String entityName, String conceptName, Date startTimeFrom, Date startTimeThru, Date endTimeFrom, Date endTimeThru, String status, boolean activeOnly, PagingCriteria pagingCriteria, SortCriteria sortCriteria) {
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.getActs: "  
                    + " entityName " + entityName
                    + " conceptName " + conceptName);
        }

        if ((StringUtils.isEmpty(entityName)) &&
            (StringUtils.isEmpty(conceptName))) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.EntityConceptNotSpecified);
        }
        
        DistinctTypesResultSet distinct = getDistinctTypes(Act.class.getName(),
                entityName, conceptName);
        
        try {
            return dao.getActs(entityName, conceptName, startTimeFrom, 
                    startTimeThru, endTimeFrom, endTimeThru, status, activeOnly, 
                    pagingCriteria, getSortProperty(distinct.descriptors, sortCriteria), 
                    isAscending(sortCriteria));
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToGetActs,
                    new Object[] { entityName, conceptName }, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#getParticipations(long, java.lang.String, java.util.Date, java.util.Date, boolean)
     */
    @Deprecated
    public IPage<Participation> getParticipations(IMObjectReference ref, String conceptName, Date startTimeFrom, Date startTimeThru, Date endTimeFrom, Date endTimeThru, boolean activeOnly, PagingCriteria pagingCriteria, SortCriteria sortCriteria) {
        if (ref == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.EntityUidNotSpecified);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("ArchetypeService.getParticipations: ref " + ref  
                    + " conceptName " + conceptName);
        }

        DistinctTypesResultSet distinct = getDistinctTypes(Participation.class.getName(),
                null, conceptName);

        try {
            return dao.getParticipations(ref, conceptName, startTimeFrom, 
                    startTimeThru, endTimeFrom, endTimeThru, activeOnly,
                    pagingCriteria, getSortProperty(distinct.descriptors, sortCriteria), 
                    isAscending(sortCriteria));
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToGetParticipations,
                    new Object[] { ref }, exception);
        }
    }

    /**
     * Iterate through all the nodes and ensure that the object meets all the
     * specified assertions. The assertions are defined in the node and can be
     * hierarchical, which means that this method is re-entrant.
     * 
     * @param context
     *            holds the object to be validated
     * @param nodes
     *            assertions are managed by the nodes object
     * @param errors
     *            the errors are collected in this object
     */
    private void validateObject(JXPathContext context, Map nodes,
            List<ValidationError> errors) {
        Iterator iter = nodes.values().iterator();
        while (iter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor) iter.next();
            Object value = null;
            try {
                value = context.getValue(node.getPath());
            } catch (Exception ignore) {
                // ignore since context.setLenient doesn't
                // seem to be working.
                // TODO Need to sort out a better way since this
                // can also cause problems
            }

            // first check whether the value for this node is derived and if it
            // is then derive the value
            if (node.isDerived()) {
                try {
                    value = context.getValue(node.getDerivedValue());
                    context.getPointer(node.getPath()).setValue(value);
                } catch (Exception exception) {
                    value = null;
                    errors.add(new ValidationError(node.getName(),
                            "Cannot derive value"));
                    logger.error("Failed to derive value for " + 
                            node.getName(),exception);
                }
            }

            // check the cardinality
            int minCardinality = node.getMinCardinality();
            int maxCardinality = node.getMaxCardinality();
            if ((minCardinality == 1) && (value == null)) {
                errors.add(new ValidationError(node.getName(),
                        "value is required"));

                if (logger.isDebugEnabled()) {
                    logger.debug("Validation failed for Node: "
                            + node.getName() + " min cardinality violated");
                }
            }

            // do collection related processing
            if (node.isCollection()) {
                Collection collection = node.toCollection(value);
                // check the min cardinality if specified
                if ((minCardinality > 0) && 
                    (collection == null || collection.size() < minCardinality)) {
                    errors.add(new ValidationError(node.getName(),
                            " must supply at least " + minCardinality + " "
                                    + node.getBaseName()));

                }

                // check the max cardinality if specified
                if ((maxCardinality > 0) && 
                    (maxCardinality != NodeDescriptor.UNBOUNDED) && 
                    (collection != null && collection.size() > maxCardinality)) {
                    errors.add(new ValidationError(node.getName(),
                            " cannot supply more than " + maxCardinality + " "
                                    + node.getBaseName()));
                }
                
                // if it's a parent-child relationship then validate the
                // children. This is the recursive validation
                if (node.isParentChild()) {
                    for (Object obj : collection) {
                        if ((obj == null) ||
                            (!(obj instanceof IMObject))) {
                            continue;
                        }
                        
                        // cast to an imobject and ensure that we can retrieve
                        // the archetypeId. If we can them attempt to retrieve
                        // the associated descriptor. The the IMObject does not
                        // contain and archetypeId then it was incorrectly 
                        // created.
                        IMObject imobj = (IMObject)obj;
                        if (imobj.getArchetypeId() == null) {
                            errors.add(new ValidationError(null, new StringBuffer(
                                "No archetype Id was set for object of type ")
                                .append(imobj.getClass().getName()).toString()));
                            continue;
                        }
                        
                        ArchetypeDescriptor adesc = getArchetypeDescriptor(
                                imobj.getArchetypeId());
                        if (adesc == null) {
                            errors.add(new ValidationError(null, new StringBuffer(
                                    "No archetype definition for ").append(
                                            imobj.getArchetypeId()).toString()));
                            logger.error(new ArchetypeServiceException(
                                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                                    new Object[] { imobj.getArchetypeId().toString() }));
                            continue;
                        }
    
                        // if there are nodes attached to the archetype then validate the
                        // associated assertions
                        if ((adesc.getNodeDescriptors() != null) &&
                            (adesc.getNodeDescriptors().size() > 0)) {
                            JXPathContext childContext = JXPathHelper.newContext(imobj);
                            childContext.setLenient(true);
                            validateObject(childContext, adesc.getNodeDescriptors(), errors);
                        }
                    }
                }
            }

            if ((value != null)&& 
                (node.getAssertionDescriptorsAsArray().length > 0)) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node
                        .getAssertionDescriptorsAsArray()) {
                    AssertionTypeDescriptor assertionType = 
                        dCache.getAssertionTypeDescriptor(assertion.getName());
                    if (assertionType == null) {
                        throw  new ArchetypeServiceException(
                                ArchetypeServiceException.ErrorCode.AssertionTypeNotSpecified,
                                new Object[]{assertion.getName()});
                    }

                    try {
                        if (!assertionType.validate(value, node, assertion)) {
                            errors.add(new ValidationError(node.getName(),
                                    assertion.getErrorMessage()));
                            if (logger.isDebugEnabled()) {
                                logger.debug("Assertion failed for Node: "
                                        + node.getName() + " and Assertion "
                                        + assertion.getName());
                            }
                        }
                    } catch (Exception exception) {
                        // log the error
                        logger.error("Error in validateObject for node "
                                + node.getName(), exception);
                        errors.add(new ValidationError(node.getName(),
                                assertion.getErrorMessage()));
                    }
                }
            }

            // if this node has other nodes then re-enter this method
            if (node.getNodeDescriptors().size() > 0) {
                validateObject(context, node.getNodeDescriptors(), errors);
            }
        }
    }


    /**
     * Iterate through the {@link NodeDescriptors} and set all derived values.
     * Do it recursively.
     * 
     * @param context
     *          the context object
     * @param nodes
     *          a list of node descriptors
     * @throws ArchetypeServiceException          
     */
    private void deriveValues(JXPathContext context, Map<String, NodeDescriptor> nodes) {
        Iterator iter = nodes.values().iterator();
        while (iter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor) iter.next();
            if (node.isDerived()) {
                try {
                    Object value = context.getValue(node.getDerivedValue());
                    context.getPointer(node.getPath()).setValue(value);
                } catch (Exception exception) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToDeriveValue,
                            new Object[] {node.getName(), node.getPath()},
                            exception);
                }
            }
            
            // if this node contains other nodes then make a recursive call
            if (node.getNodeDescriptors().size() > 0) {
                deriveValues(context, node.getNodeDescriptors());
            }
        }
    }

    /**
     * This method will create a default object using the specified archetype
     * descriptor. Fundamentally, it will set the default value when specified
     * and it will also create an object through a default constructur if a
     * cardinality constraint is specified.
     * 
     * @param descriptor
     *            the archetype descriptor
     * @return IMObject
     * @throws ArchetypeServiceException
     *             if it failed to create the object
     */
    private IMObject create(ArchetypeDescriptor descriptor) {
        IMObject imobj = null;
        try {
            Class domainClass = Thread.currentThread().getContextClassLoader()
                    .loadClass(descriptor.getClassName());
            if (IMObject.class.isAssignableFrom(domainClass) == false) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidDomainClass,
                        new Object[] {descriptor.getClassName()});
            }
            
            imobj = (IMObject)domainClass.newInstance();
            imobj.setArchetypeId(descriptor.getType());

            // first create a JXPath context and use it to process the nodes
            // in the archetype
            JXPathContext context = JXPathHelper.newContext(imobj);
            context.setFactory(new JXPathGenericObjectCreationFactory());
            create(context, descriptor.getNodeDescriptors());
        } catch (Exception exception) {
            // rethrow as a runtime exception
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToCreateObject,
                    new Object[] { descriptor.getType().getShortName() },
                    exception);
        }

        return imobj;
    }

    /**
     * Check whether the sort direction is ascending
     * 
     * @param sortCriteria
     *            the sort criteria
     * @return boolean
     *            true if ascending            
     */
    private boolean isAscending(SortCriteria sortCriteria) {
        if (sortCriteria == null) {
            return true;
        }
        
        return sortCriteria.isAscending();
    }
    
    /**
     * Search for a the specified node in the archetype descriptor and then 
     * derive the sort property. A sort property can only be a top level 
     * property in the adesc (i.e. have a simple path). If the node does not
     * exist or the node exisits but it is not a top level node then raise an
     * exception. In addition if more than one archetype is specified then 
     * the sort property must be defined in all the archetypes.
     * 
     * @param adesc
     *            the archetype descriptor
     * @param sortCriteria
     *            the sort criteris
     * @return String
     *            the sort property
     * @throws ArchetypeServiceException            
     */
    private String getSortProperty(Set<ArchetypeDescriptor> adescs, SortCriteria sortCriteria) {
        if (sortCriteria == null) {
            return null;
        }
        
        // ensure the property is defined in all archetypes
        String sortNode = sortCriteria.getSortNode();
        NodeDescriptor ndesc = null;
        String property = null;
        
        for (ArchetypeDescriptor adesc : adescs) {
            ndesc = adesc.getNodeDescriptor(sortNode);
            if (ndesc == null) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidSortProperty,
                        new Object[] { sortNode });
            }
            
            // stip the leading /, if it exists
            String aprop = ndesc.getPath();
            if (aprop.startsWith("/")) {
                aprop = ndesc.getPath().substring(1);
            }
            
            // now check for any more / characters
            if (aprop.contains("/")) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.CannotSortOnProperty,
                        new Object[] { sortNode });
            }
            
            // now check that all archetypes refer to the same property
            if ((property != null) &&
                !(property.equals(aprop))) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.SortPropertyNotSupported,
                        new Object[] { sortNode });
            }
            property = aprop;
        }
        
        return property;
    }

    /**
     * Iterate through all the nodes in the archetype definition and create the
     * default object.
     * 
     * @param context
     *            the JXPath
     * @param nodes
     *            the node descriptors for the archetype
     */
    @SuppressWarnings("unchecked")
    private void create(JXPathContext context, Map nodes) {
        Iterator iter = nodes.values().iterator();
        while (iter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor) iter.next();

            // only ceate a node if it is a collection of if it contains
            // children nodes or if it has a default vaules specified
            if ((node.isCollection()) ||
                (node.getNodeDescriptorCount() > 0) ||
                (StringUtils.isEmpty(node.getDefaultValue()) == false)) {
               if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to create path " + node.getPath()
                            + " for node " + node.getName());
                }

                // if we have a value to set then do a create and set
                // otherwise do only a create
                String value = node.getDefaultValue();
                context.getVariables().declareVariable("node", node);
                if (StringUtils.isEmpty(value)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("calling createPath for node "
                                + node.getName() + " and path "
                                + node.getPath());
                    }
                    context.createPath(node.getPath());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("calling createPathAndSetValue for node "
                                + node.getName() + " path " + node.getPath()
                                + " and default value "
                                + node.getDefaultValue());
                    }
                    context.createPath(node.getPath());
                    
                    // evaluate the default value
                    Object defValue = context.getValue(value);
                    if (!defValue.getClass().isAssignableFrom(node.getClazz())) {
                        try {
                            defValue = convertValue(defValue, node.getClazz());
                        } catch (Exception exception) {
logger.error("def clazz:" + defValue.getClass() + " node clazz:" + node.getClazz());                            
                            throw new ArchetypeServiceException(
                                    ArchetypeServiceException.ErrorCode.InvalidDefaultValue,
                                    new Object[]{node.getDefaultValue(), node.getName()},
                                    exception);
                        }
                    }
                    context.setValue(node.getPath(), defValue);
                }
            }

            // determine whether any of the node's associated assertions
            // want to hook in to the creation phase
            if (node.getAssertionDescriptorsAsArray().length > 0) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node
                        .getAssertionDescriptorsAsArray()) {
                    AssertionTypeDescriptor assertionType = 
                        dCache.getAssertionTypeDescriptor(assertion.getName());
                    if (assertionType == null) {
                        throw  new ArchetypeServiceException(
                                ArchetypeServiceException.ErrorCode.AssertionTypeNotSpecified,
                                new Object[]{assertion.getName()});
                    }

                    try {
                        assertionType.create(context.getContextBean(), node, assertion);
                    } catch (Exception exception) {
                        throw new ArchetypeServiceException(
                                ArchetypeServiceException.ErrorCode.FailedToExecuteCreateFunction,
                                new Object[]  {assertion.getName()},
                                exception);
                    }
                }
            }
            
            // if this node has children then process them
            // recursively
            if (node.getNodeDescriptors().size() > 0) {
                create(context, node.getNodeDescriptors());
            }
        }
    }

    /**
     * Convert value to the specified type
     * 
     * @param value 
     *            the value to convert
     * @param clazz
     *            the class that the value should be converted too
     * @return Object
     *            the converted value      
     * @thorws Exception
     *            let the client handle the exception                              
     */
    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class clazz) 
    throws Exception {
        return ConvertUtils.convert(ConvertUtils.convert(value), clazz);
    }
    /**
     * Iterate through all the archetype id's and return the different types (as
     * string) given the nominated rmName and entityName
     * 
     * @param rmName
     *            the reference model name (complete or partial)
     * @param entityName
     *            the entity name (complete or partial)
     * @param concept
     *            the concept name (complete )            
     * @param primaryOnly
     *            determines whether to restrict processing to primary only            
     * @return DistinctTypesResultSet
     */
    @SuppressWarnings("unchecked")
    private DistinctTypesResultSet getDistinctTypes(String rmName, String entityName,
            String concept, boolean primaryOnly) {
        DistinctTypesResultSet results = new DistinctTypesResultSet();
        
        String modRmName = (rmName == null) ? null : 
            rmName.replace(".", "\\.").replace("*", ".*");
        String modEntityName = (entityName == null) ? null : 
            entityName.replace(".", "\\.").replace("*", ".*");
        String modConcept = (concept == null) ? null :
            concept.replace(".", "\\.").replace("*", ".*");
        
        // search through the cache for matching archetype descriptors
        for (ArchetypeDescriptor desc : dCache.getArchetypeDescriptors()) {
            ArchetypeId archId = desc.getType();
            if ((primaryOnly) &&
                (desc.isPrimary() == false)) {
                continue;
            }

            if (((StringUtils.isEmpty(modRmName)) ||
                 (archId.getRmName().matches(modRmName))) &&
                ((StringUtils.isEmpty(modEntityName)) || 
                 (archId.getEntityName().matches(modEntityName))) &&
                ((StringUtils.isEmpty(modConcept)) || 
                 (archId.getConcept().matches(modConcept)))) {
                results.descriptors.add(desc);
                if (!results.types.contains(desc.getClassName())) {
                    results.types.add(desc.getClassName());
                }
            }
        }

        return results;
    }


    /**
     * Iterate through all the archetype descriptors and return those matching
     * the specified class name, entity and concept name. 
     * 
     * @param clazz
     *            the class name (mandatory)
     * @param entityName
     *            the entity name (complete or partial)
     * @param concept
     *            the concept name (complete or partial)            
     * @return DistinctTypesResultSet
     */
    @SuppressWarnings("unchecked")
    private DistinctTypesResultSet getDistinctTypes(String clazz, String entityName,
            String concept) {
        DistinctTypesResultSet results = new DistinctTypesResultSet();
        
        results.types.add(clazz);

        // modify to regular expression syntax
        String modEntityName = (entityName == null) ? null : 
            entityName.replace(".", "\\.").replace("*", ".*");
        String modConcept = (concept == null) ? null :
            concept.replace(".", "\\.").replace("*", ".*");
        
        // search through the cache for matching archetype descriptors
        for (ArchetypeDescriptor desc : dCache.getArchetypeDescriptors()) {
            ArchetypeId archId = desc.getType();
            if ((desc.getClassName().equals(clazz)) &&
                (StringUtils.isEmpty(modEntityName) || 
                 archId.getEntityName().matches(modEntityName)) &&
                (StringUtils.isEmpty(modConcept) ||
                 archId.getConcept().equals(modConcept))) {
                results.descriptors.add(desc);
            }
        }

        return results;
    }

    /**
     * Iterate through all the archetype short names and return the distinct 
     * types.
     * 
     * @param shortNames
     *            a list of short names to search against
     * @param primaryOnly
     *            determines whether to restrict processing to primary only            
     * @return DistinctTypesResultSet
     */
    @SuppressWarnings("unchecked")
    private DistinctTypesResultSet getDistinctTypes(String[] shortNames, boolean primaryOnly) {
        DistinctTypesResultSet results = new DistinctTypesResultSet();

        if ((shortNames == null) ||
            (shortNames.length == 0)) {
            return results;
        }
        
        // first go through the list of short names and translate '*' to
        // '.*' to perform the regular expression matches
        String[] modShortNames = new String[shortNames.length];
        for (int index = 0; index < shortNames.length; index++) {
            modShortNames[index] = shortNames[index]
                          .replace(".", "\\.")
                          .replace("*", ".*");
        }
        // adjust the reference model name
        for (String name : dCache.getArchetypeShortNames()) {
            ArchetypeDescriptor desc = dCache.getArchetypeDescriptor(name); 
            if ((primaryOnly) &&
                (!desc.isPrimary())) {
                    continue;
            }
            
            for (String modShortName : modShortNames) {
                if (name.matches(modShortName)) {
                    results.descriptors.add(desc);
                    if (!results.types.contains(desc.getClassName())) {
                        results.types.add(desc.getClassName());
                    }
                    break;
                }
            }
        }

        return results;
    }
    
    /**
     * Private anonymous class to hold the results of {@link getDistinctTypes}
     */
    private class DistinctTypesResultSet {
        /**
         * The set of distinct archetypes
         */
        Set<ArchetypeDescriptor> descriptors =
            new HashSet<ArchetypeDescriptor>();
        
        /**
         * The set of distinct class names matching the {@link #descriptors}
         */
        Set<String> types = new HashSet<String>();
    }
}
