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
import org.openvpms.component.business.domain.im.common.IMObject;
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
    public Object create(ArchetypeId id) {
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
    public Object create(String name) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#remove(org.openvpms.component.business.domain.im.common.IMObject)
     */
    public void remove(IMObject entity) {
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
                            "Failed to derice value for " + node.getName(),
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

            // if the node is a collection and there are cardinality
            // constraints then check them
            if (node.isCollection()) {
                if ((minCardinality > 0)
                        && (getCollectionSize(node, value) < minCardinality)) {
                    errors.add(new ValidationError(node.getName(),
                            " must supply at least " + minCardinality + " "
                                    + node.getBaseName()));

                }

                if ((maxCardinality > 0)
                        && (maxCardinality != NodeDescriptor.UNBOUNDED)
                        && (getCollectionSize(node, value) > maxCardinality)) {
                    errors.add(new ValidationError(node.getName(),
                            " cannot supply more than " + maxCardinality + " "
                                    + node.getBaseName()));
                }
            }

            if ((value != null)
                    && (node.getAssertionDescriptorsAsArray().length > 0)) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node
                        .getAssertionDescriptorsAsArray()) {
                    AssertionTypeDescriptor assertionType = 
                        dCache.getAssertionTypeDescriptor(assertion.getName());

                    // TODO
                    // no validation required where the type is not specified.
                    // This is currently a work around since we need to deal
                    // with assertions and some other type of declaration...
                    // which I don't have a name for.
                    if (assertionType.getActionType("assert") == null) {
                        continue;
                    }

                    try {
                        if (!assertionType.assertTrue(value, node, assertion)) {
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
     * Determine the number of entries in the collection. If the collection is
     * null then return null. The node descriptor defines the type of the
     * collection
     * 
     * @param node
     *            the node descriptor for the collection item
     * @param collection
     *            the collection item
     * @return int
     */
    private int getCollectionSize(NodeDescriptor node, Object collection) {
        if (collection == null) {
            return 0;
        }

        // all collections must implement this interface
        if (collection instanceof Collection) {
            return ((Collection) collection).size();
        } else {
            // TODO should we actually throw an exception here.
            return 0;
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
     * @return Object
     * @throws ArchetypeServiceException
     *             if it failed to create the object
     */
    private Object create(ArchetypeDescriptor descriptor) {
        Object obj = null;
        try {
            Class domainClass = Thread.currentThread().getContextClassLoader()
                    .loadClass(descriptor.getClassName());
            obj = domainClass.newInstance();

            // if the object is an instance of {@link IMObject}
            if (obj instanceof IMObject) {
                // cast to imobject and set the archetype and the uuid.
                IMObject imobj = (IMObject) obj;
                imobj.setArchetypeId(descriptor.getType());
            } 


            // first create a JXPath context and use it to process the nodes
            // in the archetype
            JXPathContext context = JXPathContext.newContext(obj);
            context.setFactory(new JXPathGenericObjectCreationFactory());
            create(context, descriptor.getNodeDescriptors());
        } catch (Exception exception) {
            // rethrow as a runtime exception
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToCreateObject,
                    new Object[] { descriptor.getType().getShortName() },
                    exception);
        }

        return obj;
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

            String modRmName = rmName.replace("*", ".*");
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
