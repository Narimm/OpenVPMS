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
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;

import java.util.Collection;


/**
 * Assertions for relationships between objects.
 *
 * @author Tim Anderson
 */
public class RelationshipAssertions extends AbstractRelationshipAssertions {

    /**
     * Assertion to indicate that there can be only one active relationship between two objects.
     */
    public static final String UNIQUE_RELATIONSHIP = "uniqueRelationship";

    /**
     * Assertion to indicate that there can be only one active relationship of a particular type.
     */
    public static final String UNIQUE_RELATIONSHIP_TYPE = "uniqueRelationshipType";

    /**
     * Singleton instance.
     */
    private static final RelationshipAssertions INSTANCE = new RelationshipAssertions();

    /**
     * Validates a relationship assertion.
     *
     * @param context the assertion context
     */
    public static boolean validate(ActionContext context) {
        return INSTANCE.isValid(context);
    }

    /**
     * Validates a relationship assertion.
     *
     * @param context the assertion context
     */
    protected boolean isValid(ActionContext context) {
        boolean result = false;
        AssertionDescriptor assertion = context.getAssertion();
        if (UNIQUE_RELATIONSHIP.equals(assertion.getName())) {
            Collection<IMObjectRelationship> entries = getRelationships(context.getValue(), IMObjectRelationship.class);
            if (entries != null) {
                result = validateUnique(entries);
            }
        } else if (UNIQUE_RELATIONSHIP_TYPE.equals(assertion.getName())) {
            Collection<IMObjectRelationship> entries = getRelationships(context.getValue(), IMObjectRelationship.class);
            if (entries != null) {
                result = validateUniqueType(entries);
            }
        }
        return result;
    }

}
