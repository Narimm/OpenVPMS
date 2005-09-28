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

// openvpms-framework
import org.openvpms.component.business.domain.archetype.Archetype;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.archetype.Archetypes;
import org.openvpms.component.business.domain.archetype.AssertionType;
import org.openvpms.component.business.domain.archetype.Assertion;
import org.openvpms.component.business.domain.archetype.AssertionTypes;
import org.openvpms.component.business.domain.archetype.Node;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.service.uuid.IUUIDGenerator;

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
     * A reference to the UUIDGenerator, which it uses during the create process
     */
    private IUUIDGenerator uuidGenerator;

    /**
     * In memory cache of the archetype definitions keyed on the short name.
     */
    private Map<String, ArchetypeRecord> archetypesByShortName;

    /**
     * In memory cache of the archetype definitions keyed on archetype id.
     */
    private Map<ArchetypeId, ArchetypeRecord> archetypesById;

    /**
     * Caches the varies assertion types
     */
    private Map<String, AssertionTypeRecord> assertionTypes;

    /**
     * Construct an instance of this class by loading and parsing all the
     * archetype definitions in the specified file. The file must b
     * <p>
     * The resource specified by afile must be loadable from the classpath. A
     * similar constraint applies to the resourcr specified by adir, it must be
     * a valid path in the classpath.
     * 
     * @param uuidGenerator
     *            the uuid generator is used to assign ids during objct creation
     * @param archeFile
     *            the file that holds all the archetype records.
     * @param assertFile
     *            the file that holds the assertions
     * @throws ArchetypeServiceException
     *             if it cannot successfully bootstrap the service. This is a
     *             runtime exception
     */
    public ArchetypeService(IUUIDGenerator uuidGenerator, String archeFile,
            String assertFile) {
        this.uuidGenerator = uuidGenerator;
        this.archetypesByShortName = new HashMap<String, ArchetypeRecord>();
        this.archetypesById = new HashMap<ArchetypeId, ArchetypeRecord>();
        this.assertionTypes = new HashMap<String, AssertionTypeRecord>();

        loadAssertionTypeRecordsFromFile(assertFile);
        loadArchetypeRecordsFromFile(archeFile);
    }

    /**
     * Construct the archetype service by loading and parsing all archetype
     * definitions in the specified directory. Only process files with the
     * specified extensions
     * 
     * @param uuidGenerator
     *            the uuid generator is used to assign ids during object
     *            creation
     * @param archeDir
     *            the directory
     * @parsm extensions only process files with these extensions
     * @param assertFile
     *            the file that holds the assertions
     * @throws ArchetypeServiceException
     *             if there is a problem processing one or more files in the
     *             directory
     */
    public ArchetypeService(IUUIDGenerator uuidGenerator, String archDir,
            String[] extensions, String assertFile) {
        this.uuidGenerator = uuidGenerator;
        this.archetypesByShortName = new HashMap<String, ArchetypeRecord>();
        this.archetypesById = new HashMap<ArchetypeId, ArchetypeRecord>();
        this.assertionTypes = new HashMap<String, AssertionTypeRecord>();

        loadAssertionTypeRecordsFromFile(assertFile);
        loadArchetypeRecordsInDir(archDir, extensions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeForName(java.lang.String)
     */
    public ArchetypeRecord getArchetypeRecord(String name) {
        return archetypesByShortName.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeRecord(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public ArchetypeRecord getArchetypeRecord(ArchetypeId id) {
        return archetypesById.get(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecord(java.lang.String)
     */
    public AssertionTypeRecord getAssertionTypeRecord(String name) {
        return assertionTypes.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getAssertionTypeRecords()
     */
    public AssertionTypeRecord[] getAssertionTypeRecords() {
        return (AssertionTypeRecord[]) assertionTypes.values().toArray(
                new AssertionTypeRecord[assertionTypes.size()]);
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
        ArchetypeRecord record = getArchetypeRecord(object.getArchetypeId());
        if (record == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                    new Object[] { object.getArchetypeId().toString() });
        }

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        boolean result = true;
        if (record.getArchetype().getNodeCount() > 0) {
            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            result = validateObject(context, record.getArchetype().getNode());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeRecords()
     */
    public ArchetypeRecord[] getArchetypeRecords() {
        return (ArchetypeRecord[]) this.archetypesById.values().toArray(
                new ArchetypeRecord[this.archetypesById.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeRecords(java.lang.String)
     */
    public ArchetypeRecord[] getArchetypeRecords(String shortName) {
        List<ArchetypeRecord> records = new ArrayList<ArchetypeRecord>();

        for (String name : this.archetypesByShortName.keySet()) {
            if ((name.matches(shortName)) &&
                !(records.contains(this.archetypesByShortName.get(name)))) {
                records.add(this.archetypesByShortName.get(name));
            }
        }

        return (ArchetypeRecord[]) records.toArray(new ArchetypeRecord[records
                .size()]);
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeDescriptor(java.lang.String)
     */
    public IArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        return null;
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
    private boolean validateObject(JXPathContext context, Node[] nodes) {
        boolean valid = true;

        for (Node node : nodes) {
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

            if (value != null) {
                // only check the assertions for non-null values
                for (Assertion assertion : node.getAssertion()) {
                    AssertionTypeRecord assertionType = assertionTypes
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
            if (node.getNodeCount() > 0) {
                if (!validateObject(context, node.getNode())) {
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
    private void loadArchetypeRecordsFromFile(String afile) {

        // check that a non-null file was specified
        if (StringUtils.isEmpty(afile)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoFileSpecified);
        }

        Archetypes records = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to process records in " + afile);
            }
            records = (Archetypes) Archetypes.unmarshal(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream(afile)));
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidFile,
                    new Object[] { afile }, exception);
        }

        if (!records.isValid()) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidFile,
                    new Object[] { afile });
        }

        loadArchetypeRecords(records);
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
    private void loadArchetypeRecordsInDir(String adir, String[] extensions) {

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
            Archetypes records = null;
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to process records in "
                            + file.getName());
                }

                records = (Archetypes) Archetypes
                        .unmarshal(new FileReader(file));
            } catch (Exception exception) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidFile,
                        new Object[] { file.getName() }, exception);
            }

            if (!records.isValid()) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidFile,
                        new Object[] { file.getName() });
            }

            loadArchetypeRecords(records);

        }

    }

    /**
     * Parse the specified file and load all the {@link ArchetypeRecord}
     * instances in memory. The file must be accessible as a resource through
     * the class path.
     * <p>
     * If there is a error parsing or loading the entries then the runtime
     * {@link ArchetypeServiceException} is raised.
     * 
     * @param afile
     *            the path to the file containing the archetype records
     * @throws ArchetypeServiceException
     */
    private void loadArchetypeRecords(Archetypes records) {
        for (Archetype archetype : records.getArchetype()) {
            ArchetypeId archId = new ArchetypeId(archetype.getName());
            
            ArchetypeRecord record = new ArchetypeRecord(archId, 
                    archetype.getClassName(), archetype);

            if (logger.isDebugEnabled()) {
                logger.debug("Processing archetype record "
                        + archId.getShortName());
            }

            try {
                // if the short name already exists then raise an
                // exception
                if (archetypesByShortName.containsKey(archId.getShortName())) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.ArchetypeAlreadyDefined,
                            new Object[] { archId.getShortName() });
                }

                Thread.currentThread().getContextClassLoader().loadClass(
                        record.getClassName());
                
                // cache the record
                archetypesByShortName.put(archId.getShortName(), record);
                archetypesById.put(archId, record);
                
                // if this is the latest version of an archetype then we 
                // create another alias for it
                if (archetype.getLatest()) {
                    archetypesByShortName.put(
                            archId.getEntityName() + "." + archId.getConcept(),
                            record);
                }
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Loading [" + archId.getShortName() + "] "
                            + archId.toString());
                }
            } catch (ClassNotFoundException excpetion) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToLoadClass,
                        new Object[] { record.getClassName() }, excpetion);
            }

            // check that the assertions are specified correctly
            if (archetype.getNodeCount() > 0) {
                checkAssertionsInNode(archetype.getNode());
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
    private void checkAssertionsInNode(Node[] nodes) {
        for (Node node : nodes) {
            for (Assertion assertion : node.getAssertion()) {
                if (!assertionTypes.containsKey(assertion.getType())) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.InvalidAssertionSpecified,
                            new Object[] { assertion.getType() });
                }
            }

            if (node.getNodeCount() > 0) {
                checkAssertionsInNode(node.getNode());
            }
        }
    }

    /**
     * This method will create a default object using the specified archetype
     * record. Fundamentally, it will set the default value when specified and
     * it will also create an object through a default constructur if a
     * cardinality constraint is specified.
     * 
     * @param record
     *            the archetype record
     * @return Object
     * @throws ArchetypeServiceException
     *             if it failed to create the object
     */
    private Object createDefaultObject(ArchetypeRecord record) {
        Object obj = null;
        try {
            Class domainClass = Class.forName(record.getClassName());
            obj = domainClass.newInstance();

            // the object must be an instance of {@link IMObject}
            if (!(obj instanceof IMObject)) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidIMClass,
                        new Object[] { record.getClassName() });
            }

            // cast to imobject and set the archetype and the uuid.
            IMObject imobj = (IMObject) obj;
            imobj.setArchetypeId(record.getArchetypeId());
            imobj.setUid(uuidGenerator.nextId());

            // first create a JXPath context and use it to process the nodes
            // in the archetype
            JXPathContext context = JXPathContext.newContext(obj);
            context.setFactory(new JXPathGenericObjectCreationFactory());
            createDefaultObject(context, record.getArchetype().getNode());
        } catch (Exception exception) {
            // rethrow as a runtime exception
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToCreateObject,
                    new Object[] { record.getArchetypeId().getShortName() }, 
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
     *            the archetype nodes in the definition
     */
    private void createDefaultObject(JXPathContext context, Node[] nodes) {
        for (Node node : nodes) {
            // only ceate a node if the minimum cardinality is 1
            if (node.getMinCardinality() == 1) {
                context.getVariables().declareVariable("node", node);
                context.createPath(node.getPath());
            }

            // set the default value, if one is specified
            if (node.getDefaultValue() != null) {
                context.setValue(node.getPath(), node.getDefaultValue());
            }

            // if this node has children then process them
            // recursively
            if (node.getNodeCount() > 0) {
                createDefaultObject(context, node.getNode());
            }
        }
    }

    /**
     * Process the file and load all the {@link AssertionTypeRecord} instances
     * 
     * @param assertFile
     *            the name of the file holding the assertion types
     */
    private void loadAssertionTypeRecordsFromFile(String assertFile) {
        // check that a non-null file was specified
        if (StringUtils.isEmpty(assertFile)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoAssertionTypeFileSpecified);
        }

        AssertionTypes types = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to process file " + assertFile);
            }
            types = (AssertionTypes) AssertionTypes
                    .unmarshal(new InputStreamReader(Thread.currentThread()
                            .getContextClassLoader().getResourceAsStream(
                                    assertFile)));
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidAssertionFile,
                    new Object[] { assertFile }, exception);
        }

        if (!types.isValid()) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidAssertionFile,
                    new Object[] { assertFile });
        }

        // process all the records
        logger.debug("Number of assertion types : "
                + types.getAssertionTypeCount());
        for (AssertionType atype : types.getAssertionType()) {
            AssertionTypeRecord record = new AssertionTypeRecord(atype);
            assertionTypes.put(atype.getName(), record);

            if (logger.isDebugEnabled()) {
                logger.debug("Loaded assertion " + atype.getName());
            }
        }
    }
}
