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

package org.openvpms.component.business.service.archetype.castor;

// java core
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// log4j
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

// openehr-java-kernel
import org.openehr.am.archetype.Archetype;
import org.openehr.rm.common.archetyped.Archetyped;

// acode-parser
import se.acode.openehr.parser.ADLParser;
import se.acode.openehr.parser.ParseException;

// openvpms-service
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeRegistry;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ArchetypeRecord;
import org.openvpms.component.business.service.archetype.castor.ArchetypeEntry;
import org.openvpms.component.business.service.archetype.castor.Archetypes;


/**
 * This basic implementation of an archetype service, which reads in the
 * {@link ArchetypeRecord} and {@link Archetype} objects into memory. It
 * implements the {@link IArchetypeService} interface.
 * <p>
 * This implementation has the following constraints 1. All archetype
 * definitions must be deployed in a single directory. The name of hte directory
 * is specified on construction 2. The archetype records must be stored in a
 * single XML document and the structure of the document must comply with XML
 * Schema defined in <b>archetype-registry.xsd</b>.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CastorArchetypeService implements IArchetypeRegistry,
        IArchetypeService {
    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(CastorArchetypeService.class);

    /**
     * In memory cache of the archetype definitions keyed on the archetypeId
     */
    private Map<String, Archetype> archetypes;

    /**
     * In memory cache of the archetypeRecord keyed on the common name
     */
    private Map<String, ArchetypeRecord> archetypeRecords;

    /**
     * Construct an instance of this class by loading and parsing all the
     * archetype definitions in the specified <i>adir</i> and also load and
     * parse all the archetype records in <i>afile</i>.
     * <p>
     * The resource specified by afile must be loadable from the classpath. A
     * similar constraint applies to the resourcr specified by adir, it must be
     * a valid path in the classpath.
     * 
     * @param adir
     *            the directory where all the archetype definitions
     * @param afile
     *            the filename that holds all te archetype records.
     * @throws ArchetypeServiceException
     *             if it cannot successfully bootstrap the service. This is a
     *             runtime exception
     */
    public CastorArchetypeService(String adir, String afile) {
        archetypes = loadArchetypeDefinitions(adir);
        archetypeRecords = loadArchetypeRecords(afile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.domain.am.registry.IArchetypeRegistry#getArchetypeRecord(java.lang.String)
     */
    public ArchetypeRecord getArchetypeRecord(String name) {
        return archetypeRecords.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeInfoForName(java.lang.String)
     */
    public Archetyped getArchetypeInfoForName(String name) {
        return archetypeRecords.get(name) == null ? null : archetypeRecords
                .get(name).getArchetypeInfo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeForName(java.lang.String)
     */
    public Archetype getArchetypeForName(String name) {
        return archetypes.get(archetypeRecords.get(name).getArchetypeId());
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
     * @return Map<String. ArchetypeRecord> the loaded records
     * @throws ArchetypeServiceException
     */
    private Map<String, ArchetypeRecord> loadArchetypeRecords(String afile) {
        Archetypes records = null;
        Map<String, ArchetypeRecord> entries = new HashMap<String, ArchetypeRecord>();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to process records in " + afile);
            }

            records = Archetypes.unmarshal(new InputStreamReader(this
                    .getClass().getClassLoader().getResourceAsStream(afile)));
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToInitializeRegistry,
                    exception);
        }

        if (!records.isValid()) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidFile,
                    new Object[] { afile });
        }

        for (ArchetypeEntry archetype : records.getArchetypeEntry()) {
            ArchetypeRecord record = new ArchetypeRecord(archetype.getName(),
                    archetype.getArchetypeId(), archetype.getInfoModelClass(),
                    archetype.getInfoModelVersion());

            if (logger.isDebugEnabled()) {
                logger.debug("Processing archetype record " + record.getName());
            }
            
            // only add the record to the cache if and only if the information
            // object can be loaded in to memory and th associated archetype
            // definition exsits.
            try {
                if (!archetypes.containsKey(record.getArchetypeId())) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                            new Object[] { record.getName(),
                                    record.getArchetypeId() });
                }

                Thread.currentThread().getContextClassLoader().loadClass(
                        record.getInfoModelClass());
                entries.put(record.getName(), record);
            } catch (ClassNotFoundException excpetion) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToLoadClass,
                        new Object[] { record.getInfoModelClass() }, excpetion);
            }
        }

        return entries;
    }

    /**
     * Process all the ADL files (i.e. fles with an ADL extension) in the
     * specified directory. If the directory does not exist then throw an
     * {@link ArchetypeServiceException}.
     * 
     * @param adir
     *            the directory where all the archetype files are located
     * @return Map<String, Archetype> the loaded archetype definitions
     * @throws ArchetypeServiceException
     */
    private Map<String, Archetype> loadArchetypeDefinitions(String adir) {
        Map<String, Archetype> entries = new HashMap<String, Archetype>();

        // check that the directory exists.
        File dir = new File(adir);
        if (!dir.exists()) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.InvalidArchetypeDefDir,
                    new Object[] { adir });
        }

        // retrieve all the files in the directory and process them
        Collection files = FileUtils.listFiles(dir, new String[] { "adl" },
                false);
        Iterator iter = files.iterator();
        while (iter.hasNext()) {
            Archetype arch = createArchetype((File) iter.next());
            archetypes.put(arch.getArchetypeId().getValue(), arch);
        }

        return entries;
    }

    /**
     * Parse the specified file and return the generated {@link Archetype}.
     * 
     * @param file
     *            the file that contains the archetype definition
     * @return Archetype
     */
    private Archetype createArchetype(File file) {
        if (logger.isDebugEnabled()) {
            logger.debug("Parsing the archetype in file " + file.getName());
        }
        
        try {
            return new ADLParser(Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("bank-account.draft.adl"))
                    .parse();
        } catch (ParseException exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                    new Object[] {file.getName()}, exception);
        }
    }
}
