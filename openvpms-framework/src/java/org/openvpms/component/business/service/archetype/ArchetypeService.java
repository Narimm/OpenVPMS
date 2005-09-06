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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.Archetype;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.archetype.Archetypes;


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
public class ArchetypeService implements IArchetypeRegistry,
        IArchetypeService {
    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(ArchetypeService.class);

    /**
     * In memory cache of the archetype definitions keyed on the archetypeId
     */
    private Map<String, ArchetypeRecord> archetypes;

    /**
     * Construct an instance of this class by loading and parsing all the
     * archetype definitions in the specified <i>adir</i> and also load and
     * parse all the archetype records in <i>afile</i>.
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
        archetypes = loadArchetypeRecords(afile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.archetype.IArchetypeService#getArchetypeForName(java.lang.String)
     */
    public ArchetypeRecord getArchetypeRecord(String name) {
        return archetypes.get(name);
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

        for (Archetype archetype : records.getArchetype()) {
            ArchetypeRecord record = new ArchetypeRecord(archetype.getShortName(), 
                    new ArchetypeId(archetype.getArchetypeNamespace(), 
                            archetype.getArchetypeName(), archetype.getVersion()), 
                    archetype.getImClass(), archetype);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Processing archetype record " 
                        + archetype.getShortName());
            }
            
            // only add the record to the cache if and only if the information
            // model class can be loaded in to memory. 
            try {
                if (!archetypes.containsKey(record.getShortName())) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.NoArchetypeDefinition,
                            new Object[] { record.getShortName(),
                                    record.getArchetypeId().toString()});
                }

                Thread.currentThread().getContextClassLoader().loadClass(
                        archetype.getImClass());
                entries.put(record.getShortName(), record);
            } catch (ClassNotFoundException excpetion) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToLoadClass,
                        new Object[] { record.getInfoModelClass() }, excpetion);
            }
        }

        return entries;
    }
}
