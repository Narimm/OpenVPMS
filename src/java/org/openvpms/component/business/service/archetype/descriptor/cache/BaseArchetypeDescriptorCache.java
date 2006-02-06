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


package org.openvpms.component.business.service.archetype.descriptor.cache;

// java core
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// log4j
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

/**
 * This is an abstract class which is used by some cache implementations.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class BaseArchetypeDescriptorCache implements IArchetypeDescriptorCache {
    /** 
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(BaseArchetypeDescriptorCache.class);

    /**
     * In memory cache of the archetype definitions keyed on the short name.
     */
    protected Map<String, ArchetypeDescriptor> archetypesByShortName = Collections
            .synchronizedMap(new HashMap<String, ArchetypeDescriptor>());

    /**
     * In memory cache of the archetype definitions keyed on archetype id.
     */
    protected Map<String, ArchetypeDescriptor> archetypesById = Collections
            .synchronizedMap(new HashMap<String, ArchetypeDescriptor>());

    /**
     * Caches the varies assertion types
     */
    protected Map<String, AssertionTypeDescriptor> assertionTypes = Collections
            .synchronizedMap(new HashMap<String, AssertionTypeDescriptor>());
    
    /**
     * Default constructor.
     */
    public BaseArchetypeDescriptorCache() {
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeDescriptor(java.lang.String)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(String name) {
        return archetypesByShortName.get(name);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeDescriptor(org.openvpms.component.business.domain.archetype.ArchetypeId)
     */
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return archetypesById.get(id.getQualifiedName());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeDescriptors()
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors() {
        return new ArrayList<ArchetypeDescriptor>(archetypesById.values());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeDescriptors(java.lang.String)
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        List<ArchetypeDescriptor> descriptors = new ArrayList<ArchetypeDescriptor>();

        for (String key : archetypesByShortName.keySet()) {
            if (key.matches(shortName)) {
                descriptors.add(archetypesByShortName.get(key));
            }
        }

        return descriptors;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeDescriptorsByRmName(java.lang.String)
     */
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(String rmName) {
        List<ArchetypeDescriptor> descriptors = new ArrayList<ArchetypeDescriptor>();

        for (String qName : archetypesById.keySet()) {
            ArchetypeDescriptor adesc = archetypesById.get(qName);
            if (rmName.matches(adesc.getType().getRmName())) {
                descriptors.add(adesc);
            }
        }

        return descriptors;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getAssertionTypeDescriptor(java.lang.String)
     */
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return assertionTypes.get(name);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getAssertionTypeDescriptors()
     */
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors() {
        return new ArrayList<AssertionTypeDescriptor>(assertionTypes.values());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeShortNames(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public List<String> getArchetypeShortNames(String rmName,
            String entityName, String conceptName, boolean primaryOnly) {
        List<String> shortNames = new ArrayList<String>();
        
        // check out if there are any '*' specified
        String trmName = (rmName == null) ? null : rmName.replace("*", ".*");
        String tentityName = (entityName == null) ? null : entityName.replace("*", ".*");
        String tconceptName = (conceptName == null) ? null : conceptName.replace("*", ".*");
        
        for (ArchetypeDescriptor desc : archetypesByShortName.values()) {
            ArchetypeId archId = desc.getType();
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

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache#addArchetypeDescriptor(org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor, boolean)
     */
    public void addArchetypeDescriptor(ArchetypeDescriptor adesc, boolean replace) {
        ArchetypeDescriptor temp = archetypesById.get(adesc.getType().getQualifiedName());
        
        if ((temp == null) ||
            (replace)) {
            addArchetypeById(adesc);
            addArchetypeByShortName(adesc);
        }
    }

    /**
     * Process all the assertions defined for a specified node. This is a
     * re-entrant method.
     * 
     * @param node
     *            the node to process
     * @throws ArchetypeDescriptorCacheException
     *             runtime exception that is raised when the
     */
    protected void checkAssertionsInNode(Map nodes) {
        Iterator niter = nodes.values().iterator();
        while (niter.hasNext()) {
            NodeDescriptor node = (NodeDescriptor) niter.next();
            for (AssertionDescriptor assertion : node
                    .getAssertionDescriptorsAsArray()) {
                AssertionTypeDescriptor atDesc = assertionTypes.get(
                        assertion.getName());
                if (atDesc == null) {
                    logger.warn("Attempting to find [" + assertion.getName()
                            + " in [" + assertionTypes + "]");
                    throw new ArchetypeDescriptorCacheException(
                            ArchetypeDescriptorCacheException.ErrorCode.InvalidAssertionSpecified,
                            new Object[] { assertion.getName() });
                }
            }
    
            if (node.getNodeDescriptors().size() > 0) {
                checkAssertionsInNode(node.getNodeDescriptors());
            }
        }
    }
    
    /**
     * Add the descriptor to the short name cache
     * 
     * @param adesc
     */
    protected void addArchetypeByShortName(ArchetypeDescriptor adesc) {
        ArchetypeId archId = adesc.getType();
        
        // only store one copy of the archetype by short name
        if ((archetypesByShortName.containsKey(archId.getShortName()) == false) || 
            (adesc.isLatest())) {
            archetypesByShortName.put(archId.getShortName(), adesc);
            if (logger.isDebugEnabled()) {
                logger.debug("Loading  [" + archId.getShortName()
                        + "] in shortNameCache");
            }
        }
    }

    /**
     * Add the descriptor to the id cache
     * 
     * @param adesc
     */
    protected void addArchetypeById(ArchetypeDescriptor adesc) {
        archetypesById.put(adesc.getType().getQualifiedName(), adesc);
    }
}
