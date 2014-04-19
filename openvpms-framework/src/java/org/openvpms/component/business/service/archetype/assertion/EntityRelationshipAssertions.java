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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.assertion;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Assertions for relationships between entities.
 *
 * @author Tim Anderson
 */
public class EntityRelationshipAssertions {

    /**
     * Validates an entity relationship assertion.
     *
     * @param context the assertion context
     */
    public static boolean validate(ActionContext context) {
        boolean result = false;
        AssertionDescriptor assertion = context.getAssertion();
        if (assertion.getName().equals("uniqueEntityRelationship")) {
            Collection<PeriodRelationship> entries = getRelationships(context.getValue());
            if (entries != null) {
                result = validateUnique(entries);
            }
        }
        return result;
    }

    /**
     * Validates that each active entity relationship is unique.
     *
     * @param entries the relationships to validate
     * @return {@code true} if the relationships are unique, otherwise {@code false}
     */
    private static boolean validateUnique(Collection<PeriodRelationship> entries) {
        if (entries.size() > 1) {
            Date now = new Date();
            List<PeriodRelationship> active = new ArrayList<PeriodRelationship>();
            for (PeriodRelationship entry : entries) {
                if (entry.isActive(now)) {
                    IMObjectReference source = entry.getSource();
                    IMObjectReference target = entry.getTarget();
                    for (PeriodRelationship other : active) {
                        if (ObjectUtils.equals(source, other.getSource())
                            && ObjectUtils.equals(target, other.getTarget())) {
                            return false;
                        }
                    }
                    active.add(entry);
                }
            }
        }
        return true;
    }

    /**
     * Helper to verify that an object is a collection of entity relationships.
     *
     * @param object the object to check
     * @return the collection, or {@code null}, if {@code object} is invalid
     */
    private static Collection<PeriodRelationship> getRelationships(Object object) {
        if (object instanceof Collection) {
            Collection list = (Collection) object;
            for (Object element : list) {
                if (!(element instanceof PeriodRelationship)) {
                    return null;
                }
            }
            return (Collection<PeriodRelationship>) object;
        }
        return null;
    }

}
