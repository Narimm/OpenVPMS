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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Helper to determine which node descriptors refer to a lookup.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LookupUsageFinder {

    /**
     * The archetype descriptor cache.
     */
    private final IArchetypeDescriptorCache archetypes;

    /**
     * Constructs a <tt>LookupUsageFinder</tt>.
     *
     * @param archetypes the archetypes
     */
    public LookupUsageFinder(IArchetypeDescriptorCache archetypes) {
        this.archetypes = archetypes;
    }

    /**
     * Finds all node descriptors that reference a lookup archetype by its code.
     *
     * @param shortName the lookup short name
     * @return a map of the node descriptors and their corresponding archetypes
     */
    public Map<NodeDescriptor, ArchetypeDescriptor> getCodeReferences(String shortName) {
        Map<NodeDescriptor, ArchetypeDescriptor> result = new HashMap<NodeDescriptor, ArchetypeDescriptor>();
        ArchetypeId id = new ArchetypeId(shortName);
        for (ArchetypeDescriptor archetype : archetypes.getArchetypeDescriptors()) {
            if (!TypeHelper.isA(archetype.getType(), shortName)) {
                for (NodeDescriptor descriptor : archetype.getAllNodeDescriptors()) {
                    if (descriptor.isLookup()) {
                        AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup");
                        String type = getValue(assertion, "type");
                        if ("lookup".equals(type)) {
                            if (referencesLookup(assertion, id)) {
                                result.put(descriptor, archetype);
                            }
                        } else if ("targetLookup".equals(type)) {
                            if (referencesTargetLookup(assertion, id)) {
                                result.put(descriptor, archetype);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines if a lookup assertion references a lookup.
     *
     * @param assertion the remote lookup assertion
     * @param lookup    the lookup id
     * @return <tt>true</tt> if the assertion references the lookup
     */
    private boolean referencesLookup(AssertionDescriptor assertion, ArchetypeId lookup) {
        String shortName = getValue(assertion, "source");
        return  (shortName != null) && TypeHelper.isA(lookup, shortName);
    }

    /**
     * Determines if a target lookup assertion references a lookup.
     *
     * @param assertion the target assertion
     * @param lookup    the lookup id
     * @return <tt>true</tt> if the assertion references the lookup
     */
    private boolean referencesTargetLookup(AssertionDescriptor assertion, ArchetypeId lookup) {
        String relationship = getValue(assertion, "relationship");
        for (ArchetypeDescriptor archetype : archetypes.getArchetypeDescriptors(relationship)) {
            NodeDescriptor node = archetype.getNodeDescriptor("target");
            if (node != null && references(node, lookup)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a node references a lookup by its archetype short name.
     *
     * @param descriptor the node descriptor
     * @param lookup     the lookup archetype id
     * @return <tt>true</tt> if node references the lookup
     */
    private boolean references(NodeDescriptor descriptor, ArchetypeId lookup) {
        String[] shortNames = getShortNames(descriptor.getArchetypeRange());
        if (shortNames.length == 0 && !StringUtils.isEmpty(descriptor.getFilter())) {
            shortNames = getShortNames(new String[]{descriptor.getFilter()});
        }
        return TypeHelper.isA(lookup, shortNames);
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortNames the shortNames. May contain wildcards
     * @return a list of short names matching the criteria
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any error
     */
    private String[] getShortNames(String[] shortNames) {
        Set<String> result = new LinkedHashSet<String>();
        for (String shortName : shortNames) {
            List<String> matches = archetypes.getArchetypeShortNames(shortName, false);
            result.addAll(matches);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the value of the named property from an assertion descriptor.
     *
     * @param assertion the assertion descriptor
     * @param name      the property name
     * @return the property value, or <code>null</code> if it doesn't exist
     */
    private static String getValue(AssertionDescriptor assertion, String name) {
        NamedProperty property = assertion.getProperty(name);
        return (property != null) ? (String) property.getValue() : null;
    }
}
