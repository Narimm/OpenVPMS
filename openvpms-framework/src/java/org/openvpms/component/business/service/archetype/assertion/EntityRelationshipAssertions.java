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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.assertion;

import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipAssertions {


    /**
     * This will iterate through the collection for the specified node
     * descriptor and check that each element is in the archetype range.
     *
     * @param target    the target object
     * @param node      the node descriptor for this assertion
     * @param assertion the particular assertion
     */
    public static boolean validate(Object target, NodeDescriptor node,
                                   AssertionDescriptor assertion) {
        // first check to see if the object is of a collection type
        Collection<EntityRelationship> entries = getRelationships(target);
        if (entries == null) {
            return false;
        }
        if (entries.size() > 1) {
            Date now = new Date();
            List<EntityRelationship> active
                    = new ArrayList<EntityRelationship>();
            for (EntityRelationship entry : entries) {
                if (entry.isActive(now)) {
                    for (EntityRelationship other : active) {
                        if (entry.getSource().equals(other.getSource())
                                && entry.getTarget().equals(other.getTarget()))
                        {
                            return false;
                        }
                    }
                    active.add(entry);
                }
            }
        }
        return true;
    }

    private static Collection<EntityRelationship>
            getRelationships(Object target) {
        if (target instanceof Collection) {
            Collection list = (Collection) target;
            for (Object object : list) {
                if (!(object instanceof EntityRelationship)) {
                    return null;
                }
            }
            return (Collection<EntityRelationship>) target;
        }
        return null;
    }

}
