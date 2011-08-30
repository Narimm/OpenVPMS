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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * {@link IMObjectRelationship} helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class RelationshipHelper {

    /**
     * Helper to return the short names for the target of a set of relationships.
     *
     * @param relationshipTypes the relationship types
     * @return the target node archetype short names
     */
    public static String[] getTargetShortNames(String... relationshipTypes) {
        return DescriptorHelper.getNodeShortNames(relationshipTypes, "target");
    }

    /**
     * Returns the targets of a list of relationships.
     * <p/>
     * If a target cannot be resolved, it is silently ignored.
     *
     * @param relationships the relationships
     * @return the targets of the relationships
     */
    @SuppressWarnings("unchecked")
    public static <R extends IMObjectRelationship, T extends IMObject> List<T> getTargets(List<R> relationships) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        List<T> targets = new ArrayList<T>();
        for (IMObjectRelationship relationship : relationships) {
            if (relationship.getTarget() != null) {
                T target = (T) service.get(relationship.getTarget());
                if (target != null) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }
}
