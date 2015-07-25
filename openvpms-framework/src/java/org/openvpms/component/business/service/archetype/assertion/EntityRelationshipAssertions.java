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

package org.openvpms.component.business.service.archetype.assertion;

import org.openvpms.component.business.domain.im.archetype.descriptor.ActionContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
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
public class EntityRelationshipAssertions extends AbstractRelationshipAssertions {

    /**
     * Assertion to indicate that there can be only one active relationship between two objects.
     */
    public static final String UNIQUE_ENTITY_RELATIONSHIP = "uniqueEntityRelationship";

    /**
     * The singleton instance.
     */
    private static EntityRelationshipAssertions INSTANCE = new EntityRelationshipAssertions();

    /**
     * Validates an entity relationship assertion.
     *
     * @param context the assertion context
     * @return {@code true} if the assertion is valid
     */
    public static boolean validate(ActionContext context) {
        return INSTANCE.isValid(context);
    }

    /**
     * Determines if an assertion is valkid.
     *
     * @param context the assertion context
     * @return {@code true} if the assertion is valid
     */
    protected boolean isValid(ActionContext context) {
        boolean result = false;
        AssertionDescriptor assertion = context.getAssertion();
        if (UNIQUE_ENTITY_RELATIONSHIP.equals(assertion.getName())) {
            Collection<PeriodRelationship> entries = getRelationships(context.getValue(), PeriodRelationship.class);
            if (entries != null) {
                result = checkUnique(entries);
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
    protected boolean checkUnique(Collection<PeriodRelationship> entries) {
        if (entries.size() > 1) {
            Date now = new Date();
            List<PeriodRelationship> active = new ArrayList<PeriodRelationship>();
            for (PeriodRelationship entry : entries) {
                if (entry.isActive(now)) {
                    if (contains(entry, active)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
