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
import org.openvpms.component.business.dao.hibernate.im.common.AbstractIMObjectSessionHandler;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DefaultIMObjectSessionHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectSessionHandler;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of {@link IMObjectSessionHandler} for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActSessionHandler extends AbstractIMObjectSessionHandler {

    /**
     * Default session handler for updating child objects.
     */
    private final IMObjectSessionHandler defaultHandler;


    /**
     * Creates a new <tt>ActSessionHandler<tt>.
     *
     * @param dao the DAO
     */
    public ActSessionHandler(IMObjectDAO dao, CompoundAssembler assembler) {
        super(dao, assembler);
        defaultHandler = new DefaultIMObjectSessionHandler(dao, assembler);
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    @Override
    public void updateIds(IMObject target, IMObject source) {
        Act targetAct = (Act) target;
        Act sourceAct = (Act) source;
        update(targetAct.getActRelationships(), sourceAct.getActRelationships(),
               defaultHandler);
        update(targetAct.getParticipations(), sourceAct.getParticipations(),
               defaultHandler);
        super.updateIds(target, source);
    }

    /**
     * Deletes an object.
     * <p/>
     * This implementation deletes any target act where there is a parent-child
     * relationship, and deletes the relationships from related acts.
     *
     * @param object
     * @param session the session
     */
    @Override
    protected void delete(IMObjectDO object, Session session) {
        ActDO parent = (ActDO) object;
        Set<ActDO> visited = new HashSet<ActDO>();
        delete(parent, visited, session);
    }

    private void delete(ActDO source, Set<ActDO> visited, Session session) {
        visited.add(source);
        ActRelationshipDO[] relationships
                = source.getSourceActRelationships().toArray(
                new ActRelationshipDO[0]);
        for (ActRelationshipDO relationhip : relationships) {
            source.removeSourceActRelationship(relationhip);
            ActDO target = (ActDO) relationhip.getTarget();
            if (target != null) {
                target.removeTargetActRelationship(relationhip);
                if (relationhip.isParentChildRelationship()) {
                    if (!visited.contains(target)) {
                        delete(target, visited, session);
                    }
                }
            }
        }
        session.delete(source);
    }

}
