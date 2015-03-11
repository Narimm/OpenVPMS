/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
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
 * @author Jim Alateras
 * @author Tim Anderson
 */
public abstract class BaseArchetypeDescriptorCache implements IArchetypeDescriptorCache {

    /**
     * Archetype definitions keyed on the short name.
     */
    private Map<String, ArchetypeDescriptor> archetypesByShortName
            = Collections.synchronizedMap(new HashMap<String, ArchetypeDescriptor>());

    /**
     * Archetype definitions keyed on archetype id.
     */
    private Map<String, ArchetypeDescriptor> archetypesById
            = Collections.synchronizedMap(new HashMap<String, ArchetypeDescriptor>());

    /**
     * Caches the assertion types.
     */
    private Map<String, AssertionTypeDescriptor> assertionTypes
            = Collections.synchronizedMap(new HashMap<String, AssertionTypeDescriptor>());

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(BaseArchetypeDescriptorCache.class);


    /**
     * Default constructor.
     */
    public BaseArchetypeDescriptorCache() {
    }

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified short name.
     * <p/>
     * If there are multiple archetype descriptors with the same name then it will retrieve the first descriptor marked
     * with latest=true.
     *
     * @param name the short name
     * @return ArchetypeDescriptor the matching archetype descriptor or {@code null} if none is found
     */
    @Override
    public ArchetypeDescriptor getArchetypeDescriptor(String name) {
        return archetypesByShortName.get(name);
    }

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified {@link ArchetypeId}.
     * <p/>
     * If the archetype version isn't specified, it will retrieve the first descriptor marked with latest=true.
     *
     * @param id the archetype id
     * @return ArchetypeDescriptor the matching archetype descriptor or {@code null} if none is found
     */
    @Override
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        ArchetypeDescriptor result = archetypesById.get(id.getQualifiedName());
        if (result == null && id.getVersion() == null) {
            result = getArchetypeDescriptor(id.getShortName());
        }
        return result;
    }

    /**
     * Return all the {@link ArchetypeDescriptor} instances managed by this cache.
     *
     * @return the descriptors
     */
    @Override
    public List<ArchetypeDescriptor> getArchetypeDescriptors() {
        return new ArrayList<ArchetypeDescriptor>(archetypesById.values());
    }

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the specified shortName.
     *
     * @param shortName the short name, which may contain wildcards
     * @return the matching descriptors
     */
    @Override
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        List<ArchetypeDescriptor> descriptors = new ArrayList<ArchetypeDescriptor>();

        for (String key : archetypesByShortName.keySet()) {
            if (StringUtilities.matches(key, shortName)) {
                descriptors.add(archetypesByShortName.get(key));
            }
        }

        return descriptors;
    }

    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     *
     * @param name the name of the assertion type
     * @return the matching assertion type descriptor, or {@code null} if none is found
     */
    @Override
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return assertionTypes.get(name);
    }

    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by this cache.
     *
     * @return the cached assertion type descriptors
     */
    @Override
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors() {
        return new ArrayList<AssertionTypeDescriptor>(assertionTypes.values());
    }


    /**
     * Return a list of archetype short names given the nominated criteria.
     *
     * @param entityName  the entity name. May contain wildcards
     * @param conceptName the concept name. May contain wildcards
     * @param primaryOnly indicates whether to return primary objects only
     */
    @Override
    public List<String> getArchetypeShortNames(String entityName, String conceptName, boolean primaryOnly) {
        List<String> shortNames = new ArrayList<String>();

        for (ArchetypeDescriptor desc : archetypesByShortName.values()) {
            ArchetypeId archId = desc.getType();

            // do the check on entity name
            if (!StringUtils.isEmpty(entityName) && !StringUtilities.matches(archId.getEntityName(), entityName)) {
                continue;
            }

            // do the check on concept name
            if (!StringUtils.isEmpty(conceptName) && !StringUtilities.matches(archId.getConcept(), conceptName)) {
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

    /**
     * Return all the archetypes which match the specified short name
     *
     * @param shortName   the short name, which may contain wildcards
     * @param primaryOnly return only the primary archetypes
     * @return the matching archetype short names
     */
    @Override
    public List<String> getArchetypeShortNames(String shortName, boolean primaryOnly) {
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

    /**
     * Add an archetype descriptor to the cache.
     *
     * @param descriptor the archetype descriptor to add
     */
    public void addArchetypeDescriptor(ArchetypeDescriptor descriptor) {
        ArchetypeId archId = descriptor.getType();
        if (descriptor.isLatest() || !archetypesByShortName.containsKey(archId.getShortName())) {
            archetypesByShortName.put(archId.getShortName(), descriptor);
        }
        archetypesById.put(archId.getQualifiedName(), descriptor);
    }

    /**
     * Adds an assertion type descriptor to the cache.
     *
     * @param descriptor the assertion type descriptor to add
     */
    public void addAssertionTypeDescriptor(AssertionTypeDescriptor descriptor) {
        assertionTypes.put(descriptor.getName(), descriptor);
    }

    /**
     * Return all the archetype short names.
     *
     * @return the archetype short names
     */
    @Override
    public List<String> getArchetypeShortNames() {
        return new ArrayList<String>(archetypesByShortName.keySet());
    }

    /**
     * Process all the assertions defined for a specified node. This is a re-entrant method.
     *
     * @param nodes the nodes to process
     * @throws ArchetypeDescriptorCacheException
     *          if an invalid assertion is specified
     */
    protected void checkAssertionsInNode(Map<String, NodeDescriptor> nodes) {
        for (NodeDescriptor node : nodes.values()) {
            for (AssertionDescriptor assertion : node.getAssertionDescriptorsAsArray()) {
                AssertionTypeDescriptor descriptor = assertionTypes.get(assertion.getName());
                if (descriptor == null) {
                    log.warn("Attempting to find [" + assertion.getName() + " in [" + assertionTypes + "]");
                    throw new ArchetypeDescriptorCacheException(
                            ArchetypeDescriptorCacheException.ErrorCode.InvalidAssertionSpecified,
                            new Object[]{assertion.getName()});
                } else {
                    assertion.setDescriptor(descriptor);
                }
            }

            if (!node.getNodeDescriptors().isEmpty()) {
                checkAssertionsInNode(node.getNodeDescriptors());
            }
        }
    }

}
