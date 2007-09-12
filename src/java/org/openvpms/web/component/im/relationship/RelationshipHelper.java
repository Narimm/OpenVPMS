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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanException;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.List;


/**
 * {@link EntityRelationship} helper routines.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipHelper {

    /**
     * Returns the target from the default entity relationship from the
     * specified relationship node.
     *
     * @param entity the parent entity
     * @param node   the relationship node
     * @return the default target, or the the first non-null target if the
     *         default target is <tt>null</tt>, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node does't exist or an element
     *                                   is of the wrong type
     */
    public static IMObject getDefaultTarget(Entity entity, String node) {
        IMObjectBean bean = new IMObjectBean(entity);
        return getDefaultTarget(bean.getValues(node, EntityRelationship.class));
    }

    /**
     * Returns the target from the default entity relationship from the supplied
     * relationship list.
     *
     * @param relationships a list of relationship objects
     * @return the default target, or the the first non-null target if the
     *         default target is <tt>null</tt>, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMObject getDefaultTarget(
            List<EntityRelationship> relationships) {
        IMObject result = null;
        for (EntityRelationship relationship : relationships) {
            if (result == null) {
                result = IMObjectHelper.getObject(relationship.getTarget());
            } else {
                IMObjectBean bean = new IMObjectBean(relationship);
                if (bean.hasNode("default") && bean.getBoolean("default")) {
                    result = IMObjectHelper.getObject(relationship.getTarget());
                    if (result != null) {
                        break;
                    }
                }

            }
        }
        return result;
    }

}
