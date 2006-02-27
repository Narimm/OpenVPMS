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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// log4j
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
            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
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
            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            deriveValues(context, descriptor.getNodeDescriptors());
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public List<IMObject> get(String rmName, String entityName,
            String conceptName, String instanceName, boolean activeOnly) {
        return get(rmName, entityName, conceptName, instanceName, false, activeOnly);
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public List<IMObject> get(String rmName, String entityName,
            String conceptName, String instanceName, boolean primaryOnly,
            boolean activeOnly) {
        List<IMObject> results = new ArrayList<IMObject>();
        Set<String> types = getDistinctTypes(rmName, entityName, primaryOnly);
        
        for (String type : types) {
            try {
                List<IMObject> objects = dao.get(rmName, entityName,
                        conceptName, instanceName, type, activeOnly);
                results.addAll(objects);
            } catch (IMObjectDAOException exception) {
                logger.error("ArchetypeService.get", new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToFindObjects,
                        new Object[] { rmName, entityName, conceptName,
                                instanceName }, exception)); 
            }
        }

        return results;
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String[])
     */
    public List<IMObject> get(String[] shortNames, boolean activeOnly) {
        List<IMObject> results = new ArrayList<IMObject>();
        for (String shortName : shortNames) {
            
            ArchetypeDescriptor adesc =  dCache.getArchetypeDescriptor(shortName);
            if (adesc == null) {
                continue;
            }
            ArchetypeId aid = adesc.getType();
            List<IMObject> objects = get(aid.getRmName(), aid.getEntityName(), 
                    aid.getConcept(), null, activeOnly);
            results.addAll(objects);
        }
        
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getById(org.openvpms.component.business.domain.archetype.ArchetypeId,
     *      long)
     */
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
    public List<IMObject> get(String[] shortNames, String instanceName, 
            boolean primaryOnly, boolean activeOnly) {
        List<IMObject> results =  new ArrayList<IMObject>();
        if ((shortNames != null) &&
            (shortNames.length > 0)) {
            
            // first go through the list of short names and translate '*' to
            // '.*' to perform the regular expression matches
            String[] modShortNames = new String[shortNames.length];
            for (int index = 0; index < shortNames.length; index++) {
                modShortNames[index] = shortNames[index]
                              .replace(".", "\\.")
                              .replace("*", ".*");
            }
            
            // TODO if may be best to do this in the DAO since with many 
            // arhetypes this will be very inefficient
            for (String archetypeShortName : dCache.getArchetypeShortNames()) {
                for (String shortName : modShortNames) {
                    // iterate to see whether the short name matches any of the 
                    // specified shortName. A specified short name may contain 
                    // wild card characters. 
                    if (archetypeShortName.matches(shortName)) {
                        ArchetypeDescriptor adesc = getArchetypeDescriptor(archetypeShortName);
                        ArchetypeId id = adesc.getType();

                        // if the primaryFlag has been specified check that 
                        // the archetype meets that constaint.
                        if ((primaryOnly) &&
                            (!adesc.isPrimary())) { 
                            break;
                        }

                        // if we get here then call the dao
                        List<IMObject> objects = dao.get(id.getRmName(), 
                                id.getEntityName(), id.getConcept(), 
                                instanceName, adesc.getClassName(), activeOnly);
                        results.addAll(objects);
                        break;
                    }
                }
            }
        }
        
        return results;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getByNamedQuery(java.lang.String, java.util.Map)
     */
    public List<IMObject> getByNamedQuery(String name, Map<String, Object> params) {
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
            return dao.getByNamedQuery(name, params);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedInGetByNamedQuery,
                    new Object[] {name}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(org.openvpms.component.business.domain.im.common.IMObjectReference)
     */
    public IMObject get(IMObjectReference reference) {
        return getById(reference.getArchetypeId(), reference.getUid());
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
     * @see org.openvpms.component.business.service.entity.IEntityService#getActs(long, java.lang.String, java.lang.String, java.util.Date, java.util.Date, java.lang.String, boolean)
     */
    public List<Act> getActs(long entityUid, String pConceptName, String entityName, String aConceptName, Date startTimeFrom, Date startTimeThru, Date endTimeFrom, Date endTimeThru, String status, boolean activeOnly) {
        if (entityUid <= 0) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.EntityUidNotSpecified);
        }
        
        try {
            return dao.getActs(entityUid, pConceptName, entityName, aConceptName, startTimeFrom, 
                    startTimeThru, endTimeFrom, endTimeThru, status, activeOnly);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToGetActForEntity,
                    new Object[] { entityUid }, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#getActs(java.lang.String, java.lang.String, java.util.Date, java.util.Date, java.lang.String, boolean)
     */
    public List<Act> getActs(String entityName, String conceptName, Date startTimeFrom, Date startTimeThru, Date endTimeFrom, Date endTimeThru, String status, boolean activeOnly) {
        if ((StringUtils.isEmpty(entityName)) &&
            (StringUtils.isEmpty(conceptName))) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.EntityConceptNotSpecified);
        }
        try {
            return dao.getActs(entityName, conceptName, startTimeFrom, 
                    startTimeThru, endTimeFrom, endTimeThru, status, activeOnly);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToGetActs,
                    new Object[] { entityName, conceptName }, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.entity.IEntityService#getParticipations(long, java.lang.String, java.util.Date, java.util.Date, boolean)
     */
    public List<Participation> getParticipations(long entityUid, String conceptName, Date startTimeFrom, Date startTimeThru, Date endTimeFrom, Date endTimeThru, boolean activeOnly) {
        if (entityUid <= 0) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.EntityUidNotSpecified);
        }
        
        try {
            return dao.getParticipations(entityUid, conceptName, startTimeFrom, 
                    startTimeThru, endTimeFrom, endTimeThru, activeOnly);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToGetParticipations,
                    new Object[] { entityUid }, exception);
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
                    logger.error(
                            "Failed to derive value for " + node.getName(),
                            exception);
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
                            JXPathContext childContext = JXPathContext.newContext(imobj);
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
            JXPathContext context = JXPathContext.newContext(imobj);
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
     * Iterate through all the nodes in the archetype definition and create the
     * default object.
     * 
     * @param context
     *            the JXPath
     * @param nodes
     *            the node descriptors for the archetype
     */
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
                    context.setValue(node.getPath(), context.getValue(value));
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
     * Iterate through all the archetype id's and return the different types (as
     * string) given the nominated rmName and entityName
     * 
     * @param rmName
     *            the reference model name (complete or partial)
     * @param entityName
     *            the entity name (complete or partial)
     * @param primaryOnly
     *            determines whether to restrict processing to primary only            
     * @return List<String> a list of types
     */
    @SuppressWarnings("unchecked")
    private Set<String> getDistinctTypes(String rmName, String entityName,
            boolean primaryOnly) {
        Set<String> results = new HashSet<String>();
        
        // adjust the reference model name
        for (ArchetypeDescriptor desc : dCache.getArchetypeDescriptors()) {
            ArchetypeId archId = desc.getType();
            if (StringUtils.isEmpty(rmName)) {
                // a null or empty rmName is a no match
                continue;
            }
            
            if ((primaryOnly) &&
                (desc.isPrimary() == false)) {
                continue;
            }

            String modRmName = rmName.replace(".", "\\.").replace("*", ".*");
            if (archId.getRmName().matches(modRmName)) {
                String modEntityName = (entityName == null) ? null : 
                    entityName.replace("*", ".*");
                if ((StringUtils.isEmpty(modEntityName)) || 
                    (archId.getEntityName().matches(modEntityName))) {
                    results.add(desc.getClassName());
                }
            }
        }

        return results;
    }
    
}
