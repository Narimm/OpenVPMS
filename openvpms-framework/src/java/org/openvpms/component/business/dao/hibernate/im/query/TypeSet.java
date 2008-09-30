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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.query;

import org.apache.commons.lang.ArrayUtils;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.*;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.query.ArchetypeConstraint;
import org.openvpms.component.system.common.query.ArchetypeIdConstraint;
import org.openvpms.component.system.common.query.LongNameConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Type set.
 */
class TypeSet {

    /**
     * The set of distinct archetypes.
     */
    private final Set<ArchetypeDescriptor> descriptors;

    /**
     * The type that matches all the specfied descriptors.
     */
    private final String className;

    /**
     * Type alias.
     */
    private String alias;


    /**
     * Constructs a new <code>TypeSet</code>.
     *
     * @param alias       the type alias. May be <code>null</code>.
     * @param descriptors the archetype descriptors in the set
     * @param assembler   the assembler
     * @throws QueryBuilderException if the descriptors refer to more than one
     *                               type
     */
    public TypeSet(String alias, Set<ArchetypeDescriptor> descriptors,
                   CompoundAssembler assembler) {
        this.alias = alias;
        Class baseType = getClass(descriptors);
        className = assembler.getDOClassName(baseType.getName());
        this.descriptors = descriptors;
    }

    /**
     * Returns the type alias.
     *
     * @return the type alias. May be <code>null</code>
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the type alias
     *
     * @param alias the type alias. May be <code>null</code>
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Returns the archetype class name.
     *
     * @return the archetype class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the archetype descriptors.
     *
     * @return the archetype descriptors
     */
    public Set<ArchetypeDescriptor> getDescriptors() {
        return descriptors;
    }

    /**
     * Returns the short names.
     *
     * @return the short names
     */
    public Set<String> getShortNames() {
        Set<String> result = new HashSet<String>();
        for (ArchetypeDescriptor descriptor : descriptors) {
            result.add(descriptor.getShortName());
        }
        return result;
    }

    /**
     * Creates a new type set from an {@link ArchetypeIdConstraint}.
     *
     * @param constraint the constraint
     * @param cache      the archetype descriptor cache
     * @return a new type set
     * @throws ArchetypeServiceException for any archetype service error
     * @throws QueryBuilderException     if there are no archetypes for the id
     */
    public static TypeSet create(ArchetypeIdConstraint constraint,
                                 IArchetypeDescriptorCache cache,
                                 CompoundAssembler assembler) {
        ArchetypeId id = constraint.getArchetypeId();
        ArchetypeDescriptor descriptor = cache.getArchetypeDescriptor(id);
        if (descriptor == null) {
            throw new QueryBuilderException(NoArchetypesForId, id);
        }
        Set<ArchetypeDescriptor> descriptors
                = new HashSet<ArchetypeDescriptor>();
        descriptors.add(descriptor);
        return new TypeSet(constraint.getAlias(), descriptors, assembler);
    }

    /**
     * Craetes a new type set from an {@link ArchetypeConstraint} and
     * node descriptor.
     *
     * @param constraint the constraint
     * @param descriptor the node descriptor
     * @param cache      the archetype descriptor cache
     * @return a new type set
     * @throws ArchetypeServiceException for any archetype service error
     * @throws QueryBuilderException     if there are no matching archetypes for
     *                                   the constraint
     */
    public static TypeSet create(ArchetypeConstraint constraint,
                                 NodeDescriptor descriptor,
                                 IArchetypeDescriptorCache cache,
                                 CompoundAssembler assembler) {
        String[] shortNames = descriptor.getArchetypeRange();
        if (shortNames == null || shortNames.length == 0) {
            if (descriptor.getFilter() == null) {
                throw new QueryBuilderException(NoArchetypeRangeAssertion,
                                                descriptor.getName());
            }
            shortNames = new String[]{descriptor.getFilter()};
        }
        Set<ArchetypeDescriptor> descriptors
                = getDescriptors(shortNames, constraint.isPrimaryOnly(),
                                 cache);
        if (descriptors.isEmpty()) {
            throw new QueryBuilderException(
                    NoMatchingArchetypesForShortName,
                    ArrayUtils.toString(shortNames));
        }
        return new TypeSet(constraint.getAlias(), descriptors,
                           assembler);
    }

    /**
     * Creates a new type set from a {@link ShortNameConstraint}.
     *
     * @param constraint the constraint
     * @param cache      the archetype descriptor cache
     * @return a new type set
     * @throws ArchetypeServiceException for any archetype service error
     * @throws QueryBuilderException     if there are no matching archetypes for
     *                                   the constraint
     */
    public static TypeSet create(ShortNameConstraint constraint,
                                 IArchetypeDescriptorCache cache,
                                 CompoundAssembler assembler) {
        Set<ArchetypeDescriptor> descriptors
                = getDescriptors(constraint.getShortNames(),
                                 constraint.isPrimaryOnly(), cache);
        // check that we have at least one match
        if (descriptors.isEmpty()) {
            throw new QueryBuilderException(
                    NoMatchingArchetypesForShortName,
                    ArrayUtils.toString(constraint.getShortNames()));
        }
        return new TypeSet(constraint.getAlias(), descriptors, assembler);
    }

    /**
     * Creates a new type set from a {@link LongNameConstraint}.
     *
     * @param constraint the constraint
     * @param cache      the archetype descriptor cache
     * @return a new type set
     * @throws ArchetypeServiceException for any archetype service error
     * @throws QueryBuilderException     if there are no matching archetypes for
     *                                   the constraint
     */
    @Deprecated
    public static TypeSet create(LongNameConstraint constraint,
                                 IArchetypeDescriptorCache cache,
                                 CompoundAssembler assembler) {
        Set<ArchetypeDescriptor> descriptors
                = getDescriptors(constraint.getEntityName(),
                                 constraint.getConceptName(),
                                 constraint.isPrimaryOnly(),
                                 cache);

        // check that we have at least one match
        if (descriptors.isEmpty()) {
            throw new QueryBuilderException(
                    NoMatchingArchetypesForLongName, constraint.getRmName(),
                    constraint.getEntityName(), constraint.getConceptName());
        }
        return new TypeSet(constraint.getAlias(), descriptors,
                           assembler);
    }

    /**
     * Returns all archetype descriptors matching the criteria.
     *
     * @param entityName  the archetype entity name. May be <code>null</code>
     * @param conceptName the archetype concept name. May be <code>null</code>
     * @param cache       the archetype descriptor cache
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static Set<ArchetypeDescriptor> getDescriptors(
            String entityName, String conceptName, boolean primaryOnly,
            IArchetypeDescriptorCache cache) {
        List<String> shortNames = cache.getArchetypeShortNames(
                entityName, conceptName, primaryOnly);
        return getDescriptors(shortNames.toArray(new String[0]), cache);
    }

    /**
     * Returns all archetype descriptors matching the short names.
     *
     * @param shortNames  a list of short names to search against
     * @param primaryOnly determines whether to restrict processing to primary only
     * @param cache       the archetype descriptor cache
     * @return matching archetypes
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static Set<ArchetypeDescriptor> getDescriptors(
            String[] shortNames, boolean primaryOnly,
            IArchetypeDescriptorCache cache) {
        // expand any wilcards
        Set<String> expanded = new HashSet<String>();
        for (String shortName : shortNames) {
            List<String> matches = cache.getArchetypeShortNames(shortName,
                                                                primaryOnly);
            expanded.addAll(matches);
        }
        return getDescriptors(expanded.toArray(new String[0]), cache);
    }

    /**
     * Returns the distinct type that matches all short names.
     *
     * @param shortNames the short names. Must be expanded.
     * @param cache      the archetype descriptor cache
     * @return a new <code>TypeSet</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static Set<ArchetypeDescriptor> getDescriptors(
            String[] shortNames, IArchetypeDescriptorCache cache) {
        Set<ArchetypeDescriptor> result = new HashSet<ArchetypeDescriptor>();
        for (String shortName : shortNames) {
            ArchetypeDescriptor descriptor
                    = cache.getArchetypeDescriptor(shortName);
            result.add(descriptor);
        }
        return result;
    }

    /**
     * Returns the common base class for a set of archetype descriptors.
     *
     * @param descriptors the archetype descirptors
     * @return the common base class
     * @throws QueryBuilderException if the classes don't have a common base
     *                               class
     */
    private Class getClass(Set<ArchetypeDescriptor> descriptors) {
        Class superType = null;
        for (ArchetypeDescriptor descriptor : descriptors) {
            Class type = descriptor.getClazz();
            if (superType == null) {
                superType = type;
            } else if (type.isAssignableFrom(superType)) {
                superType = type;
            } else if (!superType.isAssignableFrom(type)) {
                // doesn't allow > 1 level of subclasses but good enough for
                // now
                throw new QueryBuilderException(
                        CannotQueryAcrossTypes, superType,
                        descriptor.getClassName());
            }
        }
        return superType;
    }

}
