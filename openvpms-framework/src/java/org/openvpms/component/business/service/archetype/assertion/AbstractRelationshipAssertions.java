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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.assertion;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.model.object.Reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for relationship assertions.
 *
 * @author Tim Anderson
 */
public class AbstractRelationshipAssertions {

    /**
     * Helper to determine if a relationship is present in a list. If not, adds it.
     *
     * @param relationship the relationship
     * @param active       the relationship list
     * @return {@code true} if the relationship was present, {@code false} otherwise
     */
    protected <T extends IMObjectRelationship> boolean contains(T relationship, List<T> active) {
        Reference source = relationship.getSource();
        Reference target = relationship.getTarget();
        for (T other : active) {
            if (ObjectUtils.equals(source, other.getSource()) && ObjectUtils.equals(target, other.getTarget())) {
                return true;
            }
        }
        active.add(relationship);
        return false;
    }

    /**
     * Validates that each active relationship is unique.
     *
     * @param entries the relationships to validate
     * @return {@code true} if the relationships are unique, otherwise {@code false}
     */
    protected <T extends IMObjectRelationship> boolean validateUnique(Collection<T> entries) {
        if (entries.size() > 1) {
            List<T> active = new ArrayList<>();
            for (T entry : entries) {
                if (entry.isActive()) {
                    if (contains(entry, active)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Validates that each active relationship is of a different type.
     *
     * @param entries the relationships to validate
     * @return {@code true} if the relationships are a unique type, otherwise {@code false}
     */
    protected <T extends IMObjectRelationship> boolean validateUniqueType(Collection<T> entries) {
        if (entries.size() > 1) {
            Set<String> types = new HashSet<>();
            for (T entry : entries) {
                if (entry.isActive()) {
                    String shortName = entry.getArchetypeId().getShortName();
                    if (!types.add(shortName)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Helper to verify that an object is a collection of {@link IMObjectRelationship} relationships.
     *
     * @param object the object to check
     * @return the collection, or {@code null}, if {@code object} is invalid
     */
    @SuppressWarnings("unchecked")
    protected <T extends IMObjectRelationship> Collection<T> getRelationships(Object object, Class<T> type) {
        if (object instanceof Collection) {
            Collection list = (Collection) object;
            for (Object element : list) {
                if (!type.isAssignableFrom(element.getClass())) {
                    return null;
                }
            }
            return (Collection<T>) object;
        }
        return null;
    }

}
