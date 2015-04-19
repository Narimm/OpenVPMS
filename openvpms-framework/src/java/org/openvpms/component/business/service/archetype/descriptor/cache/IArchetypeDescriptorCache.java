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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;

import java.util.List;


/**
 * This interface is used for accessing {@link ArchetypeDescriptor}.
 *
 * @author Jim Alateras
 */
public interface IArchetypeDescriptorCache {

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified short name.
     * <p/>
     * If there are multiple archetype descriptors with the same name then it will retrieve the first descriptor marked
     * with latest=true.
     *
     * @param name the short name
     * @return ArchetypeDescriptor the matching archetype descriptor or {@code null} if none is found
     */
    ArchetypeDescriptor getArchetypeDescriptor(String name);

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified {@link ArchetypeId}.
     * <p/>
     * If the archetype version isn't specified, it will retrieve the first descriptor marked with latest=true.
     *
     * @param id the archetype id
     * @return ArchetypeDescriptor the matching archetype descriptor or {@code null} if none is found
     */
    ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id);

    /**
     * Return all the {@link ArchetypeDescriptor} instances managed by this cache.
     *
     * @return the descriptors
     */
    List<ArchetypeDescriptor> getArchetypeDescriptors();

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the specified shortName.
     *
     * @param shortName the short name, which may contain wildcards
     * @return the matching descriptors
     */
    List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName);

    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     *
     * @param name the name of the assertion type
     * @return the matching assertion type descriptor, or {@code null} if none is found
     */
    AssertionTypeDescriptor getAssertionTypeDescriptor(String name);

    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by this cache.
     *
     * @return the cached assertion type descriptors
     */
    List<AssertionTypeDescriptor> getAssertionTypeDescriptors();

    /**
     * Return a list of archetype short names given the nominated criteria.
     *
     * @param entityName  the entity name. May contain wildcards
     * @param conceptName the concept name. May contain wildcards
     * @param primaryOnly indicates whether to return primary objects only
     */
    List<String> getArchetypeShortNames(String entityName, String conceptName, boolean primaryOnly);

    /**
     * Return all the archetypes which match the specified short name
     *
     * @param shortName   the short name, which may contain wildcards
     * @param primaryOnly return only the primary archetypes
     * @return the matching archetype short names
     */
    List<String> getArchetypeShortNames(String shortName, boolean primaryOnly);

    /**
     * Add an archetype descriptor to the cache.
     *
     * @param descriptor the archetype descriptor to add
     */
    void addArchetypeDescriptor(ArchetypeDescriptor descriptor);

    /**
     * Adds an assertion type descriptor to the cache.
     *
     * @param descriptor the assertion type descriptor to add
     */
    void addAssertionTypeDescriptor(AssertionTypeDescriptor descriptor);

    /**
     * Return all the archetype short names.
     *
     * @return the archetype short names
     */
    List<String> getArchetypeShortNames();
}
