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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;

import java.util.List;


/**
 * This interface is used for accessing {@link ArchetypeDescriptor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IArchetypeDescriptorCache {
    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified  short name.
     * If there are multiple archetype descriptors with the same name then it
     * will retrieve the first descriptor marked with latest=true.
     *
     * @param name the short name
     * @return ArchetypeDescriptor
     *         the matching archetype descriptor or null.
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     */
    ArchetypeDescriptor getArchetypeDescriptor(String name);

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified
     * {@link ArchetypeId}. If the archetype version isn't specified, it
     * will retrieve the first descriptor marked with latest=true.
     *
     * @param id the archetype id
     * @return ArchetypeDescriptor
     *         the matching archetype descriptor or null.
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     */
    ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id);

    /**
     * Return all the {@link ArchetypeDescriptor} instances managed by this
     * cache.
     *
     * @return List<ArchetypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     */
    List<ArchetypeDescriptor> getArchetypeDescriptors();

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the
     * specified shortName. The short name can be a regular expression.
     *
     * @param shortName the short name, which can be a regular expression
     * @return List<ArchetypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     */
    List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName);

    /**
     * Return all the {@link ArchetypeDescriptor} instance with the specified
     * reference model name.
     *
     * @param rmName the reference model name
     * @return List<ArchetypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     * @deprecated no replacement
     */
    @Deprecated
    List<ArchetypeDescriptor> getArchetypeDescriptorsByRmName(String rmName);

    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     *
     * @param name the name of the assertion type
     * @return AssertionTypeDescriptor
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     */
    AssertionTypeDescriptor getAssertionTypeDescriptor(String name);

    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by
     * this cache
     *
     * @return List<AssertionTypeDescriptor>
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     */
    List<AssertionTypeDescriptor> getAssertionTypeDescriptors();

    /**
     * Return a list of archtype short names (i.e strings) given the
     * nominated criteria
     *
     * @param rmName      the reference model name
     * @param entityName  the entity name
     * @param conceptName the concept name
     * @param primaryOnly indicates whether to return primary objects only.
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     * @deprecated see {@link #getArchetypeShortNames(String entityName,
     *             String conceptName,
     *             boolean primaryOnly)}
     */
    @Deprecated
    List<String> getArchetypeShortNames(String rmName, String entityName,
                                        String conceptName,
                                        boolean primaryOnly);

    /**
     * Return a list of archtype short names (i.e strings) given the
     * nominated criteria
     *
     * @param entityName  the entity name
     * @param conceptName the concept name
     * @param primaryOnly indicates whether to return primary objects only.
     * @throws ArchetypeDescriptorCacheException
     *          if there is a problem completing the request.
     */
    List<String> getArchetypeShortNames(String entityName,
                                        String conceptName,
                                        boolean primaryOnly);

    /**
     * Return all the archetypes which match the specified short name
     *
     * @param shortName   the short name, which may contain a wildcard character
     * @param primaryOnly return only the primary archetypes
     * @return List<String>
     * @throws ArchetypeDescriptorCacheException
     *          a runtime exception if it cannot complete the call
     */
    List<String> getArchetypeShortNames(String shortName, boolean primaryOnly);

    /**
     * Add the specified archetype descriptor to the cache. If the replace
     * flag is specified and the archetype descriptor exists, then replace the
     * existing archetype descriptor with the new one.
     *
     * @param adesc   the archetype descriptor to add
     * @param replace indicates whether it should replace and existing version.
     */
    void addArchetypeDescriptor(ArchetypeDescriptor adesc, boolean replace);

    /**
     * Adds the specified assertion type descriptor to the cache. If the
     * replace flag is specified, and the descriptor exists, then replace the
     * existing descriptor with the new one.
     *
     * @param descriptor the assertion type descriptor to add
     * @param replace    indicates whether it should replace and existing version
     */
    void addAssertionTypeDescriptor(AssertionTypeDescriptor descriptor,
                                    boolean replace);

    /**
     * Return all the archetype short names
     *
     * @return List<String>
     */
    List<String> getArchetypeShortNames();
}
