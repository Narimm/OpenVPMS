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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import org.openvpms.component.business.domain.archetype.Node;


/**
 * This basic implementation of an archetype service, which reads in the
 * archetypes from the specified XML document and creates an in memory 
 * registry.
 * <p>
 * This implementation has the following constraints 
 * 1. All archetype definitions must be deployed in a single directory. 
 *    The name of hte directory is specified on construction 
 * 2. The archetype records must be stored in a single XML document and the 
 *    structure of the document must comply with XML Schema defined in 
 *    <b>archetype.xsd</b>.
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
    private Map<String, ArchetypeRecord> archetypesByShortName;

    /**
     * In memory cache of the archetype definitions keyed on archetype id.
     */
    private Map<ArchetypeId, ArchetypeRecord> archetypesById;

    /**
     * Construct an instance of this class by loading and parsing all the
     * archetype definitions in the specified file. The file must b
     * <p>
     * The resource specified by afile must be loadable from the classpath. A
     * similar constraint applies to the resourcr specified by adir, it must be
     * a valid path in the classpath.
     * 
     * @param afile
     *            the filename that holds all the archetype records.
     * @throws ArchetypeServiceException
     *             if it cannot successfully bootstrap the service. This is a
     *             runtime exception
     */
    public ArchetypeService(String afile) {
        archetypesByShortName = new HashMap<String, ArchetypeRecord>();
        archetypesById = new HashMap<ArchetypeId, ArchetypeRecord>();
        
        loadArchetypeRecordsFromFile(afile, archetypesByShortName, 
                archetypesById);
    }
    
    /**
     * Construct the archetype service by loading and parsing all archetype
     * definitions in the specified directory. Only process files with the 
     * specified extensions
     * 
     * @param adir
     *            the directory
     * @parsm extensions
     *            only process files with these extensions
     * @throws ArchetypeServiceException
     *            if there is a problem processing one or more files in the 
     *            directory                       
     */
    public ArchetypeService(String adir, String[] extensions) {
        archetypesByShortName = new HashMap<String, ArchetypeRecord>();
        archetypesById = new HashMap<ArchetypeId, ArchetypeRecord>();

        loadArchetypeRecordsInDir(adir, extensions, archetypesByShortName, 
                archetypesById);
}

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeForName(java.lang.String)
     */
    public ArchetypeRecord getArchetypeRecord(String name) {
        return archetypesByShortName.get(name);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeRecord(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public ArchetypeRecord getArchetypeRecord(ArchetypeId id) {
        return archetypesById.get(id);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#createDefaultObject(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public Object createDefaultObject(ArchetypeId id) {
        if (archetypesById.containsKey(id)) {
            return createDefaultObject(archetypesById.get(id));
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
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

    /**
     * Process the archetypes defined in the specified file. Te file must be
     * located in the classpath.
     * 
     * @param afile
     *            the path to the file containing the archetype records
     * @param shortNameCahce
     *            a cache of {@link Archetype} keyed on short name
     * @param idCache
     *            a cache of {@link Archetype} keyed on archetype id                        
     * @throws ArchetypeServiceException
     */
    private void loadArchetypeRecordsFromFile(String afile, 
            Map<String, ArchetypeRecord> shortNameCache, 
            Map<ArchetypeId, ArchetypeRecord> idCache) {
        
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

            records = (Archetypes)Archetypes.unmarshal(
                    new InputStreamReader(this.getClass().getClassLoader()
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

        loadArchetypeRecords(records, shortNameCache, idCache);
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
     * @param shortNameCahce
     *            a cache of {@link Archetype} keyed on short name
     * @param idCache
     *            a cache of {@link Archetype} keyed on archetype id                        
     * @throws ArchetypeServiceException
     */
    private void loadArchetypeRecordsInDir(String adir, String[] extensions, 
            Map<String, ArchetypeRecord> shortNameCache, 
            Map<ArchetypeId, ArchetypeRecord> idCache) {
        
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
                    new Object[]{ adir });
        }
        
        // process all the files in the directory, that match the filter
        Collection collection = FileUtils.listFiles(dir, extensions, true);
        Iterator files = collection.iterator();
        while (files.hasNext()) {
            File file = (File)files.next();
            Archetypes records = null;
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to process records in " 
                            + file.getName());
                }

                records = (Archetypes)Archetypes.unmarshal(new FileReader(file));
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

            loadArchetypeRecords(records, shortNameCache, idCache);
            
        }
            
        

    }
    
    /**
     * Parse the specified file and load all the {@link ArchetypeRecord}
     * instances in memory. The file must be accessible asa resource through the
     * class path.
     * <p>
     * If there is a error parsing or loading the entries then the runtime
     * {@link ArchetypeServiceException} is raised.
     * 
     * @param afile
     *            the path to the file containing the archetype records
     * @param shortNameCahce
     *            a cache of {@link Archetype} keyed on short name
     * @param idCache
     *            a cache of {@link Archetype} keyed on archetype id                        
     * @throws ArchetypeServiceException
     */
    private void loadArchetypeRecords(Archetypes records, 
            Map<String, ArchetypeRecord> shortNameCache, 
            Map<ArchetypeId, ArchetypeRecord> idCache) {
        for (Archetype archetype : records.getArchetype()) {
            ArchetypeRecord record = new ArchetypeRecord(archetype.getShortName(), 
                    new ArchetypeId(archetype.getArchetypeNamespace(), 
                            archetype.getArchetypeName(), archetype.getVersion()), 
                    archetype.getImClass(), archetype);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Processing archetype record " 
                        + archetype.getShortName());
            }
            
            try {
                // if the short name already exists then raise an
                // exception
                if (shortNameCache.containsKey(record.getShortName())) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.ArchetypeAlreadyDefined,
                            new Object[] { record.getShortName()});
                }

                Thread.currentThread().getContextClassLoader().loadClass(
                        archetype.getImClass());
                shortNameCache.put(record.getShortName(), record);
                idCache.put(record.getArchetypeId(), record);
                if (logger.isDebugEnabled()) {
                    logger.debug("Loading [" + record.getShortName() + "] "
                            + record.getArchetypeId().toString());
                }
            } catch (ClassNotFoundException excpetion) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToLoadClass,
                        new Object[] { record.getInfoModelClass() }, excpetion);
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
     *            if it failed to create the object                       
     */
    private Object createDefaultObject(ArchetypeRecord record) {
        Object obj = null;
        try {
            Class domainClass = Class.forName(record.getArchetype().getImClass());
            obj = domainClass.newInstance();
            
            // first create a JXPath context and use it to process the nodes
            // in the archetype
            JXPathContext context = JXPathContext.newContext(obj);
            context.setFactory(new JXPathGenericObjectCreationFactory());
            createDefaultObject(context, record.getArchetype().getNode());
        } catch (Exception exception) {
            // rethrow as a runtime exception
        }
        
        return obj;
    }

    /**
     * Iterate through all the nodes in the archetype definition and create
     * the default object. 
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
            
            // if this node has children then process them 
            // recursively
            if (node.getNodeCount() > 0) {
                createDefaultObject(context, node.getNode());
            }
        }
    }
}
