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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

// log4j
import org.apache.commons.io.FileUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

// openvpms-framework
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.service.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.AssertionTypeDescriptors;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;
import org.xml.sax.InputSource;

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
     * In memory cache of the archetype definitions keyed on the short name.
     */
    private Map<String, ArchetypeDescriptor> archetypesByShortName = Collections
            .synchronizedMap(new HashMap<String, ArchetypeDescriptor>());

    /**
     * In memory cache of the archetype definitions keyed on archetype id.
     */
    private Map<String, ArchetypeDescriptor> archetypesById = Collections
            .synchronizedMap(new HashMap<String, ArchetypeDescriptor>());

    /**
     * Caches the varies assertion types
     */
    private Map<String, AssertionTypeDescriptor> assertionTypes = Collections
            .synchronizedMap(new HashMap<String, AssertionTypeDescriptor>());

    /**
     * The DAO instance it will use...optional
     */
    private IMObjectDAO dao;

    /**
     * The timer is used to schedule tasks
     */
    private Timer timer = new Timer();

    /**
     * Construct an instance of this class by loading and parsing all the
     * archetype definitions in the specified file.
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
     *             if it cannot successfully bootstrap the service. This is a
     *             runtime exception
     */
    public ArchetypeService(String archeFile, String assertFile) {
        loadAssertionTypeDescriptorsFromFile(assertFile);
        loadArchetypeDescriptorsInFile(archeFile);
    }

    /**
     * Construct the archetype service by loading and parsing all archetype
     * definitions in the specified directory. Only process files with the
     * specified extensions
     * 
     * @param archeDir
     *            the directory
     * @parsm extensions only process files with these extensions
     * @param assertFile
     *            the file that holds the assertions
     * @throws ArchetypeServiceException
     *             if there is a problem processing one or more files in the
     *             directory
     */
    public ArchetypeService(String archDir, String[] extensions,
            String assertFile) {
        loadAssertionTypeDescriptorsFromFile(assertFile);
        loadArchetypeDescriptorsInDir(archDir, extensions);
    }

    /**
     * Construct the archetype service by loading and parsing all archetype
     * definitions in the specified directory. Only process files with the
     * specified extensions.
     * <p>
     * If a scanInterval greater than 0 is specified then a thread will be
     * created to monitor changes in the archetype definition.
     * 
     * @param archeDir
     *            the directory
     * @parsm extensions only process files with these extensions
     * @param assertFile
     *            the file that holds the assertions
     * @param scanInterval
     *            the interval that the archetype directory is scanned.
     * @throws ArchetypeServiceException
     *             if there is a problem processing one or more files in the
     *             directory
     */
    public ArchetypeService(String archDir, String[] extensions,
            String assertFile, long scanInterval) {
        this(archDir, extensions, assertFile);

        // determine whether we should create a monitor thread
        createArchetypeMonitorThread(archDir, extensions, scanInterval);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptor(java.lang.String)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String name) {
        return archetypesByShortName.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptor(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return archetypesById.get(id.getQName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecord(java.lang.String)
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return assertionTypes.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecords()
     */
    public AssertionTypeDescriptor[] getAssertionTypeDescriptors() {
        return (AssertionTypeDescriptor[]) assertionTypes.values().toArray(
                new AssertionTypeDescriptor[assertionTypes.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#createDefaultObject(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public Object create(ArchetypeId id) {
        if (archetypesById.containsKey(id.getQName())) {
            return create(archetypesById.get(id.getQName()));
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
        if (archetypesByShortName.containsKey(name)) {
            return create(archetypesByShortName.get(name)
                    .getArchetypeId());
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
    public ArchetypeDescriptor[] getArchetypeDescriptors() {
        return (ArchetypeDescriptor[]) archetypesById.values().toArray(
                new ArchetypeDescriptor[archetypesById.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptors(java.lang.String)
     */
    public ArchetypeDescriptor[] getArchetypeDescriptors(String shortName) {
        List<ArchetypeDescriptor> descriptors = new ArrayList<ArchetypeDescriptor>();

        for (String key : archetypesByShortName.keySet()) {
            if (key.matches(shortName)) {
                descriptors.add(archetypesByShortName.get(key));
            }
        }

        return (ArchetypeDescriptor[]) descriptors
                .toArray(new ArchetypeDescriptor[descriptors.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptorsByRmName(java.lang.String)
     */
    public ArchetypeDescriptor[] getArchetypeDescriptorsByRmName(String rmName) {
        List<ArchetypeDescriptor> descriptors = new ArrayList<ArchetypeDescriptor>();

        for (String qName : archetypesById.keySet()) {
            ArchetypeDescriptor adesc = archetypesById.get(qName);
            if (rmName.matches(adesc.getArchetypeId().getRmName())) {
                descriptors.add(adesc);
            }
        }

        return (ArchetypeDescriptor[]) descriptors
                .toArray(new ArchetypeDescriptor[descriptors.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#get(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public List<IMObject> get(String rmName, String entityName,
            String conceptName, String instanceName) {
        List<IMObject> results = new ArrayList<IMObject>();
        Set<String> types = getDistinctTypes(rmName, entityName);
        for (String type : types) {
            try {
                List<IMObject> objects = dao.get(rmName, entityName,
                        conceptName, instanceName, type);
                results.addAll(objects);
            } catch (IMObjectDAOException exception) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToFindObjects,
                        new Object[] { rmName, entityName, conceptName,
                                instanceName }, exception);
            }
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
            return dao.getById(desc.getType(), id);
        } catch (IMObjectDAOException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToFindObject,
                    new Object[] { desc.getType(), id }, exception);
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
        List<String> shortNames = new ArrayList<String>();
        
        // check out if there are any '*' specified
        String trmName = (rmName == null) ? null : rmName.replace("*", ".*");
        String tentityName = (entityName == null) ? null : entityName.replace("*", ".*");
        String tconceptName = (conceptName == null) ? null : conceptName.replace("*", ".*");
        
        for (ArchetypeDescriptor desc : archetypesByShortName.values()) {
            ArchetypeId archId = desc.getArchetypeId();
            // do a check on rm name
            if ((StringUtils.isEmpty(trmName) == false) && 
                (archId.getRmName().matches(trmName) == false)) {
                continue;
            }

            // do the check on entity name
            if ((StringUtils.isEmpty(tentityName) == false) && 
                (archId.getEntityName().matches(tentityName) == false)) {
                continue;
            }

            // do the check on concept name
            if ((StringUtils.isEmpty(tconceptName) == false) && 
                (archId.getConcept().matches(tconceptName) == false)) {
                continue;
            }
            
            // are we requesting only primary
            if ((primaryOnly) &&
                (!desc.isPrimary())) {
                continue;
            }

            shortNames.add(archId.getShortName());
        }
        
        return shortNames;
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
                    AssertionTypeDescriptor assertionType = assertionTypes
                            .get(assertion.getType());

                    // TODO
                    // no validation required where the type is not specified.
                    // This is currently a work around since we need to deal
                    // with assertions and some other type of declaration...
                    // which I don't have a name for.
                    if (assertionType.getType() == null) {
                        continue;
                    }

                    try {
                        if (!assertionType.assertTrue(value, node, assertion)) {
                            errors.add(new ValidationError(node.getName(),
                                    assertion.getErrorMessage()));
                            if (logger.isDebugEnabled()) {
                                logger.debug("Assertion failed for Node: "
                                        + node.getName() + " and Assertion "
                                        + assertion.getType());
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
     * Process the archetypes defined in the specified file. Te file must be
     * located in the classpath.
     * 
     * @param afile
     *            the path to the file containing the archetype records
     * @throws ArchetypeServiceException
     */
    private void loadArchetypeDescriptorsInFile(String afile) {

        // check that a non-null file was specified
        if (StringUtils.isEmpty(afile)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoFileSpecified);
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to process records in " + afile);
            }
            processArchetypeDescriptors(loadArchetypeDescriptors(afile));
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidFile,
                    new Object[] { afile }, exception);
        }
    }

    /**
     * Load and parse all the archetypes that are defined in the files of the
     * speciied directory. Only process files with the nominated extensions. If
     * the directory is invalid or if any of the files in the directory are
     * invalid throw an exception.
     * <p>
     * The archetypes will be cached in memory.
     * 
     * @param adir
     *            the directory to search
     * @param extensions
     *            the file extensions to check
     * @throws ArchetypeServiceException
     */
    private void loadArchetypeDescriptorsInDir(String adir, String[] extensions) {

        // check that a non-null directory was specified
        if (StringUtils.isEmpty(adir)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoDirSpecified);
        }

        // check that a valid directory was specified
        File dir = new File(adir);
        if (!dir.isDirectory()) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidDir,
                    new Object[] { adir });
        }

        // process all the files in the directory, that match the filter
        Collection collection = FileUtils.listFiles(dir, extensions, true);
        Iterator files = collection.iterator();
        while (files.hasNext()) {
            File file = (File) files.next();
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to process records in "
                            + file.getName());
                }

                processArchetypeDescriptors(loadArchetypeDescriptors(new FileReader(
                        file)));
            } catch (Exception exception) {
                // do not throw an exception but log a warning
                logger
                        .warn(
                                "Failed to load archetype",
                                new ArchetypeServiceException(
                                        ArchetypeServiceException.ErrorCode.InvalidFile,
                                        new Object[] { file.getName() },
                                        exception));
            }

        }

    }

    /**
     * Iterate over all the descriptors and add them to the existing set of
     * descriptores
     * 
     * @param descriptors
     *            the descriptors to process
     * @throws ArchetypeServiceException
     */
    private void processArchetypeDescriptors(ArchetypeDescriptors descriptors) {
        for (ArchetypeDescriptor descriptor : descriptors
                .getArchetypeDescriptorsAsArray()) {
            ArchetypeId archId = descriptor.getArchetypeId();

            if (logger.isDebugEnabled()) {
                logger.debug("Processing archetype record "
                        + archId.getShortName());
            }

            try {
                // make sure that the underlying type is loadable
                Thread.currentThread().getContextClassLoader().loadClass(
                        descriptor.getType());

                // only store one copy of the archetype by short name
                if ((archetypesByShortName.containsKey(archId.getShortName()) == false)
                        || (descriptor.isLatest())) {
                    archetypesByShortName
                            .put(archId.getShortName(), descriptor);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading  [" + archId.getShortName()
                                + "] in shortNameCache");
                    }
                }

                archetypesById.put(archId.getQName(), descriptor);

                if (logger.isDebugEnabled()) {
                    logger.debug("Loading [" + archId.getShortName()
                            + "] in archIdCache");
                }
            } catch (ClassNotFoundException excpetion) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToLoadClass,
                        new Object[] { descriptor.getType() }, excpetion);
            }

            // check that the assertions are specified correctly
            if (descriptor.getNodeDescriptors().size() > 0) {
                checkAssertionsInNode(descriptor.getNodeDescriptors());
            }
        }
    }

    /**
     * Process all the assertions defined for a specified node. This is a
     * re-entrant method.
     * 
     * @param node
     *            the node to process
     * @throws ArchetypeServiceException
     *             runtime exception that is raised when the
     */
    private void checkAssertionsInNode(Map nodes) {
        Iterator niter = nodes.values().iterator();
        while (niter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor) niter.next();
            for (AssertionDescriptor assertion : node
                    .getAssertionDescriptorsAsArray()) {
                if (!assertionTypes.containsKey(assertion.getType())) {
                    logger.warn("Attempting to find [" + assertion.getType()
                            + " in [" + assertionTypes + "]");
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.InvalidAssertionSpecified,
                            new Object[] { assertion.getType() });
                }
            }

            if (node.getNodeDescriptors().size() > 0) {
                checkAssertionsInNode(node.getNodeDescriptors());
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
     * @return Object
     * @throws ArchetypeServiceException
     *             if it failed to create the object
     */
    private Object create(ArchetypeDescriptor descriptor) {
        Object obj = null;
        try {
            Class domainClass = Thread.currentThread().getContextClassLoader()
                    .loadClass(descriptor.getType());
            obj = domainClass.newInstance();

            // the object must be an instance of {@link IMObject}
            if (!(obj instanceof IMObject)) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidIMClass,
                        new Object[] { descriptor.getType() });
            }

            // cast to imobject and set the archetype and the uuid.
            IMObject imobj = (IMObject) obj;
            imobj.setArchetypeId(descriptor.getArchetypeId());

            // first create a JXPath context and use it to process the nodes
            // in the archetype
            JXPathContext context = JXPathContext.newContext(obj);
            context.setFactory(new JXPathGenericObjectCreationFactory());
            create(context, descriptor.getNodeDescriptors());
        } catch (Exception exception) {
            // rethrow as a runtime exception
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToCreateObject,
                    new Object[] { descriptor.getArchetypeId().getShortName() },
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

            // only ceate a node if the minimum cardinality is 1
            if ((node.getMinCardinality() == 1)
                    || (StringUtils.isEmpty(node.getDefaultValue()) == false)) {
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
     * Process the file and load all the {@link AssertionTypeRecord} instances
     * 
     * @param assertFile
     *            the name of the file holding the assertion types
     */
    private void loadAssertionTypeDescriptorsFromFile(String assertFile) {
        // check that a non-null file was specified
        if (StringUtils.isEmpty(assertFile)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoAssertionTypeFileSpecified);
        }

        AssertionTypeDescriptors types = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to process file " + assertFile);
            }
            types = loadAssertionTypeDescriptors(assertFile);
            Iterator iter = types.getAssertionTypeDescriptors().values()
                    .iterator();
            while (iter.hasNext()) {
                AssertionTypeDescriptor descriptor = (AssertionTypeDescriptor) iter
                        .next();
                assertionTypes.put(descriptor.getName(), descriptor);

                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded assertion type "
                            + descriptor.getName());
                }
            }
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidAssertionFile,
                    new Object[] { assertFile }, exception);
        }
    }

    /**
     * Return the {@link ArchetypeDescriptors} declared in the specified file.
     * In this case the file must be a resource in the class path.
     * 
     * @param resourceName
     *            the name of the resource that the archetypes are dfined
     * @return ArchetypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private ArchetypeDescriptors loadArchetypeDescriptors(String resourceName)
            throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping
                .loadMapping(new InputSource(
                        new InputStreamReader(
                                Thread
                                        .currentThread()
                                        .getContextClassLoader()
                                        .getResourceAsStream(
                                                "org/openvpms/component/business/service/archetype/descriptor/archetype-mapping-file.xml"))));

        return (ArchetypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(new InputSource(new InputStreamReader(Thread
                        .currentThread().getContextClassLoader()
                        .getResourceAsStream(resourceName))));
    }

    /**
     * Return the {@link ArchetypeDescriptors} declared in the specified file.
     * 
     * @param name
     *            the file name
     * @return ArchetypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private ArchetypeDescriptors loadArchetypeDescriptorsFromFile(String name)
            throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping
                .loadMapping(new InputSource(
                        new InputStreamReader(
                                Thread
                                        .currentThread()
                                        .getContextClassLoader()
                                        .getResourceAsStream(
                                                "org/openvpms/component/business/service/archetype/descriptor/archetype-mapping-file.xml"))));

        return (ArchetypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(new InputSource(new FileInputStream(name)));
    }

    /**
     * Return the {@link ArchetypeDescriptors} declared in the specified file.
     * In this case the file must be a resource in the class path.
     * 
     * @param reader
     *            the reader that declares all the archetypes
     * @return ArchetypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private ArchetypeDescriptors loadArchetypeDescriptors(Reader reader)
            throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping
                .loadMapping(new InputSource(
                        new InputStreamReader(
                                Thread
                                        .currentThread()
                                        .getContextClassLoader()
                                        .getResourceAsStream(
                                                "org/openvpms/component/business/service/archetype/descriptor/archetype-mapping-file.xml"))));

        return (ArchetypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(reader);
    }

    /**
     * Return the {@link AssertionTypeDescriptors} in the specified file
     * 
     * @param assertFile
     *            the file that declares the assertions
     * @return AssertionTypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private AssertionTypeDescriptors loadAssertionTypeDescriptors(
            String assertFile) throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping
                .loadMapping(new InputSource(
                        new InputStreamReader(
                                Thread
                                        .currentThread()
                                        .getContextClassLoader()
                                        .getResourceAsStream(
                                                "org/openvpms/component/business/service/archetype/descriptor/assertion-type-mapping-file.xml"))));

        return (AssertionTypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(new InputSource(new InputStreamReader(Thread
                        .currentThread().getContextClassLoader()
                        .getResourceAsStream(assertFile))));
    }

    /**
     * Create athread to monitor changes in archeype definitions. A thread will
     * only be created if an scanInteval greater than 0 is defined.
     * 
     * @param dir
     *            the base directory name
     * @param ext
     *            the extension to look for
     * @param interval
     *            the scan interval
     * 
     */
    private void createArchetypeMonitorThread(String dir, String[] ext,
            long interval) {
        File fdir = new File(dir);
        if (fdir.exists()) {
            if (interval > 0) {
                timer.schedule(new ArchetypeMonitor(fdir, ext), interval,
                        interval);
            }
        } else {
            logger.warn("The directory " + dir + " does not exist.");
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
     * @return List<String> a list of types
     */
    private Set<String> getDistinctTypes(String rmName, String entityName) {
        Set<String> results = new HashSet<String>();
        
        // adjust the reference model name
        for (ArchetypeDescriptor desc : archetypesById.values()) {
            ArchetypeId archId = desc.getArchetypeId();
            if (StringUtils.isEmpty(rmName)) {
                // a null or empty rmName is a no match
                continue;
            }

            String modRmName = rmName.replace("*", ".*");
            if (archId.getRmName().matches(modRmName)) {
                String modEntityName = (entityName == null) ? null : 
                    entityName.replace("*", ".*");
                if ((StringUtils.isEmpty(modEntityName)) || 
                    (archId.getEntityName().matches(modEntityName))) {
                    results.add(desc.getType());
                }
            }
        }

        return results;
    }

    /**
     * This class is used to monitor changes in archetypes. It doesn't handle
     * the removal of an archetype definition from the system.
     */
    private class ArchetypeMonitor extends TimerTask {
        /**
         * This is the base directory where the archetypes are stored
         */
        private File dir;

        /**
         * This is the extensions to filter on
         */
        private String[] extensions;

        /**
         * The last time this was run
         */
        private Date lastScan = new Date(System.currentTimeMillis());

        /**
         * Instantiate an instance of this class using a base directory and a
         * list of file extensions. It will only deal with files that fulfill
         * this criteria. By default it will do a recursive search.
         * 
         * @param dir
         *            the base directory
         * @param extensions
         *            only search for these files
         * @param interval
         *            the scan interval
         */
        ArchetypeMonitor(File dir, String[] extensions) {
            this.dir = dir;
            this.extensions = extensions;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Date startTime = new Date(System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("Executing the ArchetypeMonitor");
            }
            List<File> changedFiles = getChangedFiles();
            for (File file : changedFiles) {
                try {
                    loadArchetypeDescriptorsFromFile(file.getPath());
                    logger
                            .info("Reloaded archetypes in file "
                                    + file.getPath());
                } catch (Exception exception) {
                    logger.warn(
                            "Failed to load archetype in " + file.getPath(),
                            exception);
                }
            }

            // update the last scan
            lastScan = startTime;
        }

        /**
         * Return a list of files that have changed since the last run.
         */
        private List<File> getChangedFiles() {
            ArrayList<File> changedFiles = new ArrayList<File>();

            try {
                Collection collection = FileUtils.listFiles(dir, extensions,
                        true);
                Iterator files = collection.iterator();
                while (files.hasNext()) {
                    File file = (File) files.next();
                    if (FileUtils.isFileNewer(file, lastScan)) {
                        changedFiles.add(file);
                    }
                }
            } catch (Exception exception) {
                logger.warn("Failure in getChangedFiles", exception);
            }

            return changedFiles;
        }
    }
}
