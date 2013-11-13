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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.functor;

import org.openvpms.component.business.domain.im.common.EntityRelationship;

import java.util.Comparator;

/**
 * Helper to order {@link EntityRelationship} instances using their {@link EntityRelationship#getSequence()}.
 *
 * @author Tim Anderson
 */
public class SequenceComparator implements Comparator<EntityRelationship> {

    /**
     * Singleton instance.
     */
    public static final Comparator<EntityRelationship> INSTANCE = new SequenceComparator();

    /**
     * Default constructor.
     */
    private SequenceComparator() {
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     */
    @Override
    public int compare(EntityRelationship o1, EntityRelationship o2) {
        return o1.getSequence() - o2.getSequence();
    }
}
