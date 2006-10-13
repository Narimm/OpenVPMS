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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;


/**
 * Helper to access an {@link Entity}'s properties via their names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityBean extends IMObjectBean {

    /**
     * Constructs a new <code>EntityBean</code>.
     *
     * @param entity the entity
     */
    public EntityBean(Entity entity) {
        this(entity, null);
    }

    /**
     * Constructs a new <code>EntityBean</code>.
     *
     * @param entity  the entity
     * @param service the archetype service
     */
    public EntityBean(Entity entity, IArchetypeService service) {
        super(entity, service);
    }

    /**
     * Returns the underlying entity.
     *
     * @return the underlying entity
     */
    public Entity getEntity() {
        return (Entity) getObject();
    }

    /**
     * Adds an entity relationship.
     *
     * @param shortName the relationship short name
     * @param target    the target entity
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EntityRelationship addRelationship(String shortName, Entity target) {
        Entity entity = getEntity();
        EntityRelationship r
                = (EntityRelationship) getArchetypeService().create(shortName);
        if (r == null) {
            throw new IMObjectBeanException(ArchetypeNotFound, shortName);
        }
        r.setSource(entity.getObjectReference());
        r.setTarget(target.getObjectReference());
        entity.addEntityRelationship(r);
        return r;
    }

    /**
     * Returns the first entity relationship with the specified entity as a
     * target.
     *
     * @param target the target entity
     * @return the first entity relationship with <code>target</code> as its
     *         target or <code>null</code> if none is found
     */
    public EntityRelationship getRelationship(Entity target) {
        return getRelationship(target.getObjectReference());
    }

    /**
     * Returns the first entity relationship with the specified entity as a
     * target.
     *
     * @param target the target entity
     * @return the first entity relationship with <code>target</code> as its
     *         target or <code>null</code> if none is found
     */
    public EntityRelationship getRelationship(IMObjectReference target) {
        Entity entity = getEntity();
        for (EntityRelationship r : entity.getEntityRelationships()) {
            if (target.equals(r.getTarget())) {
                return r;
            }
        }
        return null;
    }

    /**
     * Removes an entity relationship.
     *
     * @param relationship the relationship to remove
     */
    public void removeRelationship(EntityRelationship relationship) {
        Entity entity = getEntity();
        entity.removeEntityRelationship(relationship);
    }

}
