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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.system.common.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is an abstract class which is used by some cache implementations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class BaseArchetypeDescriptorCache
        implements IArchetypeDescriptorCache {

    /**
     * In memory cache of the archetype definitions keyed on the short name.
     */
   protected Map<String, ArchetypeDescriptor> archetypesByShortName
            = Collections.synchronizedMap(
            new HashMap<String, ArchetypeDescriptor>());

    /**
     * In memory cache of the archetype definitions keyed on archetype id.
     */
    protected Map<String, ArchetypeDescriptor> archetypesById
            = Collections.synchronizedMap(
            new HashMap<String, ArchetypeDescriptor>());

    /**
     * Caches the assertion types.
     */
    private Map<String, AssertionTypeDescriptor> assertionTypes
            = Collections.synchronizedMap(
            new HashMap<String, AssertionTypeDescriptor>());

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            BaseArchetypeDescriptorCache.class);


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
        ArchetypeDescriptor result = archetypesById.get(id.getQualifiedName());
        if (result == null && id.getVersion() == null) {
            result = getArchetypeDescriptor(id.getShortName());
        }
        return result;
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
            if (StringUtilities.matches(key, shortName)) {
                descriptors.add(archetypesByShortName.get(key));
            }
        }

        return descriptors;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeDescriptorsByRmName(java.lang.String)
     */
    @Deprecated
    public List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(
            String rmName) {
        return Collections.emptyList();
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
    @Deprecated
    public List<String> getArchetypeShortNames(String rmName,
                                               String entityName,
                                               String conceptName,
                                               boolean primaryOnly) {
        return getArchetypeShortNames(entityName, conceptName, primaryOnly);
    }

    /* (non-Javadoc)
    * @see org.openvpms.component.business.domain.im.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeShortNames(java.lang.String, java.lang.String, boolean)
    */
    public List<String> getArchetypeShortNames(String entityName,
                                               String conceptName,
                                               boolean primaryOnly) {
        List<String> shortNames = new ArrayList<String>();

        for (ArchetypeDescriptor desc : archetypesByShortName.values()) {
            ArchetypeId archId = desc.getType();

            // do the check on entity name
            if (!StringUtils.isEmpty(entityName) && !StringUtilities.matches(
                    archId.getEntityName(), entityName)) {
                continue;
            }

            // do the check on concept name
            if (!StringUtils.isEmpty(conceptName) && !StringUtilities.matches(
                    archId.getConcept(), conceptName)) {
                continue;
            }

            // are we requesting only primary
            if (primaryOnly && !desc.isPrimary()) {
                continue;
            }

            shortNames.add(archId.getShortName());
        }

        return shortNames;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeShortNames(java.lang.String, boolean)
     */
    public List<String> getArchetypeShortNames(String shortName,
                                               boolean primaryOnly) {
        List<String> shortNames = new ArrayList<String>();

        for (String archshortName : archetypesByShortName.keySet()) {
            ArchetypeDescriptor desc = archetypesByShortName.get(archshortName);
            if (StringUtilities.matches(archshortName, shortName)) {
                // are we requesting only primary
                if (primaryOnly && !desc.isPrimary()) {
                    continue;
                }

                shortNames.add(archshortName);
            }
        }

        return shortNames;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache#getArchetypeShortNames()
     */
    public List<String> getArchetypeShortNames() {
        return new ArrayList<String>(archetypesByShortName.keySet());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache#addArchetypeDescriptor(org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor, boolean)
     */
    public void addArchetypeDescriptor(ArchetypeDescriptor adesc,
                                       boolean replace) {
        ArchetypeDescriptor temp = archetypesById.get(
                adesc.getType().getQualifiedName());

        if (temp == null || replace) {
            addArchetypeById(adesc);
            addArchetypeByShortName(adesc);
        }
    }

    /**
     * Adds the specified assertion type descriptor to the cache. If the
     * replace flag is specified, and the descriptor exists, then replace the
     * existing descriptor with the new one.
     *
     * @param descriptor the assertion type descriptor to add
     * @param replace    indicates whether it should replace and existing version
     */
    public void addAssertionTypeDescriptor(AssertionTypeDescriptor descriptor,
                                           boolean replace) {
        AssertionTypeDescriptor existing
                = assertionTypes.get(descriptor.getName());

        if (existing == null || replace) {
            assertionTypes.put(descriptor.getName(), descriptor);
        }
    }

    /**
     * Process all the assertions defined for a specified node. This is a
     * re-entrant method.
     *
     * @param nodes the nodes to process
     * @throws ArchetypeDescriptorCacheException
     *          runtime exception that is raised when the
     */
    protected void checkAssertionsInNode(Map<String, NodeDescriptor> nodes) {
        for (NodeDescriptor node : nodes.values()) {
            for (AssertionDescriptor assertion
                    : node.getAssertionDescriptorsAsArray()) {
                AssertionTypeDescriptor atDesc = assertionTypes.get(
                        assertion.getName());
                if (atDesc == null) {
                    log.warn("Attempting to find [" + assertion.getName()
                            + " in [" + assertionTypes + "]");
                    throw new ArchetypeDescriptorCacheException(
                            ArchetypeDescriptorCacheException.ErrorCode.InvalidAssertionSpecified,
                            new Object[]{assertion.getName()});
                }  else {
                    assertion.setDescriptor(atDesc);
                }
            }

            if (node.getNodeDescriptors().size() > 0) {
                checkAssertionsInNode(node.getNodeDescriptors());
            }
        }
    }

    /**
     * Add the descriptor to the short name cache.
     *
     * @param adesc the archetype descriptor
     */
    protected void addArchetypeByShortName(ArchetypeDescriptor adesc) {
        ArchetypeId archId = adesc.getType();

        // only store one copy of the archetype by short name
        if (!archetypesByShortName.containsKey(archId.getShortName())
                || adesc.isLatest()) {
            archetypesByShortName.put(archId.getShortName(), adesc);
            if (log.isDebugEnabled()) {
                log.debug("Loading  [" + archId.getShortName()
                        + "] in shortNameCache");
            }
        }
    }

    /**
     * Add the descriptor to the id cache.
     *
     * @param adesc the archetype descriptor
     */
    protected void addArchetypeById(ArchetypeDescriptor adesc) {
        archetypesById.put(adesc.getType().getQualifiedName(), adesc);
    }
}
