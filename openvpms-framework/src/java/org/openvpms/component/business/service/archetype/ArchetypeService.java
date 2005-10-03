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
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// log4j
import org.apache.commons.io.FileUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

// openvpms-framework
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
    private Map<String, ArchetypeDescriptor> archetypesByShortName;

    /**
     * In memory cache of the archetype definitions keyed on archetype id.
     */
    private Map<ArchetypeId, ArchetypeDescriptor> archetypesById;

    /**
     * Caches the varies assertion types
     */
    private Map<String, AssertionTypeDescriptor> assertionTypes;

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
        this.archetypesByShortName = new HashMap<String, ArchetypeDescriptor>();
        this.archetypesById = new HashMap<ArchetypeId, ArchetypeDescriptor>();
        this.assertionTypes = new HashMap<String, AssertionTypeDescriptor>();

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
    public ArchetypeService(String archDir, String[] extensions, String assertFile) {
        this.archetypesByShortName = new HashMap<String, ArchetypeDescriptor>();
        this.archetypesById = new HashMap<ArchetypeId, ArchetypeDescriptor>();
        this.assertionTypes = new HashMap<String, AssertionTypeDescriptor>();

        loadAssertionTypeDescriptorsFromFile(assertFile);
        loadArchetypeDescriptorsInDir(archDir, extensions);
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
        return archetypesById.get(id);
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
    public Object createDefaultObject(ArchetypeId id) {
        if (archetypesById.containsKey(id)) {
            return createDefaultObject(archetypesById.get(id));
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#createDefaultObject(java.lang.String)
     */
    public Object createDefaultObject(String name) {
        if (archetypesByShortName.containsKey(name)) {
            return createDefaultObject(archetypesByShortName.get(name)
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
    public boolean validateObject(IMObject object) {

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object.getArchetypeId());
        if (descriptor == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                    new Object[] { object.getArchetypeId().toString() });
        }

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        boolean result = true;
        if (descriptor.getNodeDescriptors().size() > 0) {
            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            result = validateObject(context, descriptor.getNodeDescriptors());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeRecords()
     */
    public ArchetypeDescriptor[] getArchetypeDescriptors() {
        return (ArchetypeDescriptor[])archetypesById.values().toArray(
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

        return (ArchetypeDescriptor[]) descriptors.toArray(
                new ArchetypeDescriptor[descriptors.size()]);
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptorsByRmName(java.lang.String)
     */
    public ArchetypeDescriptor[] getArchetypeDescriptorsByRmName(String rmName) {
        List<ArchetypeDescriptor> descriptors = new ArrayList<ArchetypeDescriptor>();

        for (ArchetypeId archId : archetypesById.keySet()) {
            if (rmName.matches(archId.getRmName())) {
                descriptors.add(archetypesById.get(archId));
            }
        }

        return (ArchetypeDescriptor[]) descriptors.toArray(
                new ArchetypeDescriptor[descriptors.size()]);
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
     * @return boolean true if the object is valid; false otherwise
     */
    private boolean validateObject(JXPathContext context, HashMap nodes) {
        boolean valid = true;

        Iterator iter = nodes.values().iterator();
        while (iter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor)iter.next();
            Object value = null;
            try {
                value = context.getValue(node.getPath());
            } catch (Exception ignore) {
                // ignore since context.setLenient doesn't
                // seem to be working.
                // TODO Need to sort out a better way since this
                // can also cause problems
            }

            // first check the cardinality
            if ((node.getMinCardinality() == 1) && (value == null)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Validation failed for Node: " +
                            node.getName() + " min cardinality violated");
                }
                
                valid = false;
                break;
            }

            if ((value != null) &&
                (node.getAssertionDescriptors().size() > 0)){
                // only check the assertions for non-null values
                Iterator aiter = node.getAssertionDescriptors().values().iterator();
                while (aiter.hasNext()) {
                    AssertionDescriptor assertion = (AssertionDescriptor)aiter.next();
                    AssertionTypeDescriptor assertionType = assertionTypes
                            .get(assertion.getType());
    
                    if (!assertionType.assertTrue(value, assertion)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Assertion failed for Node: " +
                                    node.getName() + " and Assertion " +
                                    assertion.getType());
                        }
                        
                        valid = false;
                        break;
                    }
                }
            }

            // if this node has other nodes then re-enter this method
            if (node.getNodeDescriptors().size() > 0) {
                if (!validateObject(context, node.getNodeDescriptors())) {
                    // if the object is invalid then ignore the
                    // rest of the validation and return false.
                    valid = false;
                }
            }

            if (!valid) {
                break;
            }
        }

        return valid;
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

                processArchetypeDescriptors(loadArchetypeDescriptors(
                        new FileReader(file)));
            } catch (Exception exception) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidFile,
                        new Object[] { file.getName() }, exception);
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
        Iterator iter = descriptors.getArchetypeDescriptors().values().iterator();
        while (iter.hasNext()) {
            ArchetypeDescriptor descriptor = (ArchetypeDescriptor)iter.next();
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
                if ((archetypesByShortName.containsKey(archId.getShortName()) == false) || 
                    (descriptor.isLatest())) {
                    archetypesByShortName.put(archId.getShortName(), descriptor);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading  [" + archId.getShortName() 
                                + "] in shortNameCache");
                    }
                }

                archetypesById.put(archId, descriptor);
                
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
    private void checkAssertionsInNode(HashMap nodes) {
        Iterator niter = nodes.values().iterator();
        while (niter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor)niter.next();
            Iterator iter = node.getAssertionDescriptors().values().iterator();
            while (iter.hasNext()) {
                AssertionDescriptor assertion = (AssertionDescriptor)iter.next();
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
     * descriptor. Fundamentally, it will set the default value when specified and
     * it will also create an object through a default constructur if a
     * cardinality constraint is specified.
     * 
     * @param descriptor
     *            the archetype descriptor
     * @return Object
     * @throws ArchetypeServiceException
     *             if it failed to create the object
     */
    private Object createDefaultObject(ArchetypeDescriptor descriptor) {
        Object obj = null;
        try {
            Class domainClass = Thread.currentThread()
                .getContextClassLoader().loadClass(descriptor.getType());
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
            createDefaultObject(context, descriptor.getNodeDescriptors());
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
    private void createDefaultObject(JXPathContext context, HashMap nodes) {
        Iterator iter = nodes.values().iterator();
        while (iter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor)iter.next();

            // only ceate a node if the minimum cardinality is 1
            if (node.getMinCardinality() == 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to create path " + node.getPath() 
                            + " for node " + node.getName());
                }
                context.getVariables().declareVariable("node", node);
                context.createPath(node.getPath());
            }

            // set the default value, if one is specified
            if (node.getDefaultValue() != null) {
                context.setValue(node.getPath(), node.getDefaultValue());
            }

            // if this node has children then process them
            // recursively
            if (node.getNodeDescriptors().size() > 0) {
                createDefaultObject(context, node.getNodeDescriptors());
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
            Iterator iter = types.getAssertionTypeDescriptors().values().iterator();
            while (iter.hasNext()) {
                AssertionTypeDescriptor descriptor = 
                    (AssertionTypeDescriptor)iter.next();
                assertionTypes.put(descriptor.getName(), descriptor);
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded assertion type " + descriptor.getName());
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
     *            propagate exception to caller            
     */
    private ArchetypeDescriptors loadArchetypeDescriptors(String resourceName) 
    throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();
        
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("org/openvpms/component/business/service/archetype/descriptor/archetype-mapping-file.xml"))));
        
        return (ArchetypeDescriptors)new Unmarshaller(mapping)
                .unmarshal(new InputSource(new InputStreamReader(
                        Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream(resourceName))));
    }
    
    /**
     * Return the {@link ArchetypeDescriptors} declared in the specified file.
     * In this case the file must be a resource in the class path.
     * 
     * @param reader
     *            the reader that declares all the archetypes
     * @return ArchetypeDescriptors
     * @throws Exception
     *            propagate exception to caller
     */
    private ArchetypeDescriptors loadArchetypeDescriptors(Reader reader) 
    throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();
        
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("org/openvpms/component/business/service/archetype/descriptor/archetype-mapping-file.xml"))));
        
        return (ArchetypeDescriptors)new Unmarshaller(mapping).unmarshal(reader);
    }
    
    /**
     * Return the {@link AssertionTypeDescriptors} in the specified file
     * 
     * @param assertFile
     *            the file that declares the assertions
     * @return AssertionTypeDescriptors
     * @throws Exception
     *            propagate exception to caller
     */
    private AssertionTypeDescriptors loadAssertionTypeDescriptors(String assertFile)
    throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();
        
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("org/openvpms/component/business/service/archetype/descriptor/assertion-type-mapping-file.xml"))));
        
        return (AssertionTypeDescriptors)new Unmarshaller(mapping)
                .unmarshal(new InputSource(new InputStreamReader(
                        Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream(assertFile))));
    }
}
