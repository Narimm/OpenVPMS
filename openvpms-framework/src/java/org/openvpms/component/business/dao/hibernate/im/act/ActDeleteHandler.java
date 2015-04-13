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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.act;

import org.hibernate.Session;
import org.openvpms.component.business.dao.hibernate.im.common.AbstractDeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.im.act.Act;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of {@link DeleteHandler} for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActDeleteHandler extends AbstractDeleteHandler {

    /**
     * Creates a new <tt>ActDeleteHandler<tt>.
     *
     * @param assembler the assembler
     */
    public ActDeleteHandler(CompoundAssembler assembler) {
        super(assembler);
    }

    /**
     * Deletes an object.
     * <p/>
     * This implementation deletes any target act where there is a parent-child
     * relationship, and deletes the relationships from related acts.
     *
     * @param object  the object to delete
     * @param session the session
     * @param context the assembly context
     */
    @Override
    protected void delete(IMObjectDO object, Session session, Context context) {
        ActDO parent = (ActDO) object;
        Set<ActDO> visited = new HashSet<ActDO>();
        delete(parent, visited, context);
    }

    /**
     * Recursively removes acts where there is a parent-child relationship.
     *
     * @param act     the act to remove
     * @param visited the acts that have been visited
     * @param context the assembly context
     */
    private void delete(ActDO act, Set<ActDO> visited, Context context) {
        visited.add(act);

        // remove relationships where the act is the source. If a relationship
        // is a parent-child relationship, also remove the target act
        ActRelationshipDO[] relationships
                = act.getSourceActRelationships().toArray(
                new ActRelationshipDO[0]);
        for (ActRelationshipDO relationhip : relationships) {
            act.removeSourceActRelationship(relationhip);
            ActDO target = (ActDO) relationhip.getTarget();
            if (target != null) {
                target.removeTargetActRelationship(relationhip);
                if (relationhip.isParentChildRelationship()) {
                    if (!visited.contains(target)) {
                        delete(target, visited, context);
                    }
                }
            }
        }

        // now remove relationships where the act is the target
        relationships = act.getTargetActRelationships().toArray(
                new ActRelationshipDO[0]);
        for (ActRelationshipDO relationship : relationships) {
            act.removeTargetActRelationship(relationship);
            ActDO source = (ActDO) relationship.getSource();
            if (source != null) {
                source.removeSourceActRelationship(relationship);
            }
        }
        context.remove(act);
    }

}
