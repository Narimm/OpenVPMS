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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;


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

    /**
     * Returns the source entity in the first active entity relationship
     * matching the specified node name.
     *
     * @param node the entity relationship node
     * @return the first entity, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node) {
        return getEntity(node, new ActiveCriteria(), new EntityAccessor(true));
    }

    /**
     * Returns the target entity in the first active entity relationship
     * matching the specified node name.
     *
     * @param node the entity relationship node
     * @return the first entity, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node) {
        return getEntity(node, new ActiveCriteria(), new EntityAccessor(false));
    }

    /**
     * Returns the source entity in the first entity relationship for the
     * specified node that has a start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Date time) {
        return getEntity(node, new TimeCriteria(time),
                         new EntityAccessor(true));
    }

    /**
     * Returns the target entity in the first entity relationship for the
     * specified node that has a start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Date time) {
        return getEntity(node, new TimeCriteria(time),
                         new EntityAccessor(false));
    }

    /**
     * Returns the source entity in the first active entity relationship
     * matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @return the first entity, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName) {
        return getSourceEntity(new String[]{shortName});
    }

    /**
     * Returns the source entity in the first active entity relationship
     * matching the specified short names.
     *
     * @param shortNames the entity relationship short names
     * @return the first entity, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames) {
        return getEntity(getEntity().getEntityRelationships(),
                         new ActiveCriteria(shortNames),
                         new EntityAccessor(true));
    }

    /**
     * Returns the target entity in the first active entity relationship
     * matching the specified short name.
     *
     * @param shortName the entity relationship short names
     * @return the first entity, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName) {
        return getTargetEntity(new String[]{shortName});
    }

    /**
     * Returns the target entity in the first active entity relationship
     * matching the specified short names.
     *
     * @param shortNames the entity relationship short names
     * @return the first entity, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames) {
        return getEntity(getEntity().getEntityRelationships(),
                         new ActiveCriteria(shortNames),
                         new EntityAccessor(false));
    }

    /**
     * Returns the source entity in the first entity relationship matching
     * the specified short name that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortName the entity relationship short name
     * @param time       the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, Date time) {
        return getSourceEntity(new String[]{shortName}, time);
    }

    /**
     * Returns the source entity in the first entity relationship matching
     * the specified short names that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, Date time) {
        return getEntity(getEntity().getEntityRelationships(),
                         new TimeCriteria(time, shortNames),
                         new EntityAccessor(true));
    }

    /**
     * Returns the target entity in the first entity relationship matching the
     * specified short name that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortName the entity relationship short name
     * @param time       the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, Date time) {
        return getTargetEntity(new String[]{shortName}, time);
    }

    /**
     * Returns the target entity in the first entity relationship matching the
     * specified short names that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, Date time) {
        return getEntity(getEntity().getEntityRelationships(),
                         new TimeCriteria(time, shortNames),
                         new EntityAccessor(false));
    }

    /**
     * Returns the entity in the first entity relationship for the
     * specified node that matches the specified criteria.
     *
     * @param node     the entity relationship node
     * @param criteria the criteria
     * @param accessor the entity accessor
     * @return the first entity, or <code>null</code> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    private Entity getEntity(String node, Criteria criteria,
                             EntityAccessor accessor) {
        List values = getValues(node);
        List<EntityRelationship> relationship
                = (List<EntityRelationship>) values;
        return getEntity(relationship, criteria, accessor);
    }

    /**
     * Returns the entity from the first relationship matching the specified
     * criteria.
     *
     * @param relationships the relationships
     * @param criteria      the criteria
     * @param accessor      the entity accessor
     * @return the entity or <code>null</code> if none is found
     */
    private Entity getEntity(Collection<EntityRelationship> relationships,
                             Criteria criteria, EntityAccessor accessor) {
        for (EntityRelationship relationship : relationships) {
            if (criteria.matches(relationship, accessor)) {
                return accessor.get(relationship);
            }
        }
        return null;
    }

    /**
     * Helper to return the source or target of an entity relationship.
     */
    private class EntityAccessor {

        private final boolean source;

        public EntityAccessor(boolean source) {
            this.source = source;
        }

        public IMObjectReference getRef(EntityRelationship relationship) {
            return source ? relationship.getSource() : relationship.getTarget();
        }

        public Entity get(EntityRelationship relationship) {
            return (Entity) ArchetypeQueryHelper.getByObjectReference(
                    getArchetypeService(), getRef(relationship));
        }
    }

    /**
     * Helper to determine if an entity relationship matches some criteria.
     */
    private class Criteria {

        private final String[] shortNames;

        public Criteria() {
            this(null);
        }

        public Criteria(String[] shortNames) {
            this.shortNames = shortNames;
        }

        public boolean matches(EntityRelationship relationship,
                               EntityAccessor accessor) {
            if (shortNames != null) {
                return TypeHelper.isA(relationship, shortNames);
            }
            return true;
        }
    }

    private class ActiveCriteria extends Criteria {

        public ActiveCriteria() {
        }

        public ActiveCriteria(String[] shortNames) {
            super(shortNames);
        }

        public boolean matches(EntityRelationship relationship,
                               EntityAccessor accessor) {
            return super.matches(relationship, accessor)
                    && relationship.isActive()
                    && accessor.getRef(relationship) != null;
        }
    }

    private class TimeCriteria extends Criteria {

        private final Date time;

        private TimeCriteria(Date time) {
            this.time = time;
        }

        private TimeCriteria(Date time, String[] shortNames) {
            super(shortNames);
            this.time = time;
        }

        public boolean matches(EntityRelationship relationship,
                               EntityAccessor accessor) {
            if (super.matches(relationship, accessor)) {
                Date start = relationship.getActiveStartTime();
                if (start instanceof Timestamp) {
                    start = new Date(start.getTime());
                }
                if (start != null && start.compareTo(time) <= 0
                        && accessor.getRef(relationship) != null) {
                    Date end = relationship.getActiveEndTime();
                    if (end == null || end.compareTo(time) >= 0) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}
