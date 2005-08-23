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

// java-core
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// commons-lang
import org.apache.commons.lang.StringUtils;

// apache log4j
import org.apache.log4j.Logger;

// openvpms-domain-registry
import org.openvpms.component.business.service.archetype.ArchetypeRecord;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeRegistry;

/**
 * This is an {@link IArchetypeRegistry} based on castor and an XML Schema file.
 * The file is loaded and cached in memory once.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CastorArchetypeRegistry implements IArchetypeRegistry {
    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger.getLogger(
            CastorArchetypeRegistry.class);
    
    /**
     * The caches all the available archeype entries.
     */
    private Map<String, ArchetypeRecord> entries;
    
    /**
     * Construct a registry by parsing the file specified in the constructor and
     * loading all the entries. If the file cannot be found, or the file is not
     * in a valid format then raise an {@link ArchetypeServiceException}
     * 
     * @param fileName
     *            the name of the file containing the mappings
     * @throws ArchetypeServiceException            
     */
    public CastorArchetypeRegistry(String fileName)
    throws ArchetypeServiceException {
        if (StringUtils.isEmpty(fileName)) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.NoFileSpecified);
        }
        
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Archetypes are declared in " + fileName);
            }
             
            Archetypes archetypes = Archetypes.unmarshal(new InputStreamReader(
                    this.getClass().getClassLoader().getResourceAsStream(fileName)));
            if (!archetypes.isValid()) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.InvalidFile,
                        new Object[] {fileName});
            }
            
            // process the file and load each entry in the regitry
            entries = loadArchetypes(archetypes);
        } catch (Exception exception) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToInitializeRegistry,
                    exception);
        }
    }
    
    /**
     * Process each archetype entry and return a map of all entries. For each
     * entry make sure that the class exists before accepting it. If the class
     * cannot be loaded then abort the load process and raise an exception.
     * 
     * @param archetypes - holds all the archetype declarations
     * @return Map - the loaded entries
     */
    private Map<String, ArchetypeRecord> loadArchetypes(Archetypes archetypes) {
        HashMap<String, ArchetypeRecord> entries = new HashMap<String, ArchetypeRecord>();
        for (ArchetypeEntry archetype : archetypes.getArchetypeEntry()) {
            ArchetypeRecord record = new ArchetypeRecord(archetype.getName(), 
                    archetype.getArchetypeId(), archetype.getInfoModelClass(), 
                    archetype.getInfoModelVersion());
            // only if can fault in the class should we add the entry to to
            // the registry
            try {
                Thread.currentThread().getContextClassLoader().loadClass(
                        record.getInfoModelClass());
                entries.put(record.getName(), record);
            } catch (ClassNotFoundException excpetion) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToLoadClass,
                        new Object[] {record.getInfoModelClass()}, excpetion);
            }
        }
        
        return entries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.domain.am.registry.IArchetypeRegistry#getArchetype(java.lang.String)
     */
    public ArchetypeRecord getArchetypeRecord(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        
        return entries.get(name);
    }
}
