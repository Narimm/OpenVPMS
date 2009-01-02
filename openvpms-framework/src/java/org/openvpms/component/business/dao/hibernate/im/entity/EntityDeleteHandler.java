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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.hibernate.Session;
import org.openvpms.component.business.dao.hibernate.im.common.AbstractDeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.im.common.Entity;


/**
 * Implementation of {@link DeleteHandler} for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityDeleteHandler extends AbstractDeleteHandler {

    /**
     * Creates a new <tt>EntityDeleteHandler<tt>.
     *
     * @param assembler the assembler
     */
    public EntityDeleteHandler(CompoundAssembler assembler) {
        super(assembler);
    }

    /**
     * Deletes an object.
     * <p/>
     * This implementation removes relationships associated with the entity
     * prior to its deletion.
     *
     * @param object  the object to delete
     * @param session the session
     * @param context
     */
    @Override
    protected void delete(IMObjectDO object, Session session, Context context) {
        EntityDO entity = (EntityDO) object;
        // remove relationships where the entity is the source.
        EntityRelationshipDO[] relationships
                = entity.getSourceEntityRelationships().toArray(
                new EntityRelationshipDOImpl[0]);
        for (EntityRelationshipDO relationhip : relationships) {
            entity.removeSourceEntityRelationship(relationhip);
            EntityDO target = (EntityDO) relationhip.getTarget();
            if (target != null) {
                target.removeTargetEntityRelationship(relationhip);
            }
        }

        // now remove relationships where the act is the target
        relationships = entity.getTargetEntityRelationships().toArray(
                new EntityRelationshipDOImpl[0]);
        for (EntityRelationshipDO relationship : relationships) {
            entity.removeTargetEntityRelationship(relationship);
            EntityDO source = (EntityDO) relationship.getSource();
            if (source != null) {
                source.removeSourceEntityRelationship(relationship);
            }
        }
        context.remove(entity);
    }

}
