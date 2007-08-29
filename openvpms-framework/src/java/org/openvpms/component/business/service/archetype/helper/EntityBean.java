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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Helper to access an {@link Entity}'s properties via their names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityBean extends IMObjectBean {

    /**
     * Reference accessor that returns the source of a relationship.
     */
    private static RefAccessor SOURCE = new RefAccessor(true);

    /**
     * Reference accessor that returns the target of a relationship.
     */
    private static RefAccessor TARGET = new RefAccessor(false);

    /**
     * Criteria that matches for active relationships.
     */
    private static final Criteria ACTIVE = new Active();

    /**
     * Criteria that matches for not-null references.
     */
    private static final Criteria NOT_NULL = new NotNull();

    /**
     * Criteria that matches for active relationships and not-null references.
     */
    private static final Criteria ACTIVE_NOT_NULL = new And(ACTIVE, NOT_NULL);


    /**
     * Constructs a new <tt>EntityBean</tt>.
     *
     * @param entity the entity
     */
    public EntityBean(Entity entity) {
        this(entity, null);
    }

    /**
     * Constructs a new <tt>EntityBean</tt>.
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
     * @return the first entity relationship with <tt>target</tt> as its
     *         target or <tt>null</tt> if none is found
     */
    public EntityRelationship getRelationship(Entity target) {
        return getRelationship(target.getObjectReference());
    }

    /**
     * Returns the first entity relationship with the specified entity as a
     * target.
     *
     * @param target the target entity
     * @return the first entity relationship with <tt>target</tt> as its
     *         target or <tt>null</tt> if none is found
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
     * Returns all relationships with the specified short name.
     *
     * @param shortName the relationship short name
     * @return all relationships with the specified short name
     */
    public List<EntityRelationship> getRelationships(String shortName) {
        List<EntityRelationship> result = new ArrayList<EntityRelationship>();
        Set<EntityRelationship> relationships
                = getEntity().getEntityRelationships();
        for (EntityRelationship relationship : relationships) {
            if (TypeHelper.isA(relationship, shortName)) {
                result.add(relationship);
            }
        }
        return result;
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
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node) {
        return getEntity(node, ACTIVE_NOT_NULL, SOURCE, true);
    }

    /**
     * Returns the target entity in the first active entity relationship
     * matching the specified node name.
     *
     * @param node the entity relationship node
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node) {
        return getEntity(node, ACTIVE_NOT_NULL, TARGET, true);
    }

    /**
     * Returns the source entity in the first entity relationship for the
     * specified node that has a start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Date time) {
        return getNodeSourceEntity(node, time, true);
    }

    /**
     * Returns the source entity in the first entity relationship for the
     * specified node that has a start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return the active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Date time, boolean active) {
        return getEntity(node, getActiveNotNullTime(active, time), SOURCE,
                         active);
    }

    /**
     * Returns the target entity in the first active entity relationship for the
     * specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Date time) {
        return getNodeTargetEntity(node, time, true);
    }

    /**
     * Returns the target entity in the first entity relationship for the
     * specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationship and entity must be active
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Date time, boolean active) {
        return getEntity(node, getActiveNotNullTime(active, time), TARGET,
                         active);
    }

    /**
     * Returns the active source entities from each active relationship for the
     * specified node. If a source reference cannot be resolved, it will be
     * ignored.
     *
     * @param node the entity relationship node
     * @return a list of active source entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node) {
        return getEntities(node, ACTIVE_NOT_NULL, SOURCE, true);
    }

    /**
     * Returns the active source entities from each relationship for the
     * specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of active source entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node, Date time) {
        return getNodeSourceEntities(node, time, true);
    }

    /**
     * Returns the active source entities from each relationship for the
     * specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationships and entities must be active
     * @return a list of active source entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node, Date time,
                                              boolean active) {
        return getEntities(node, getActiveNotNullTime(active, time), SOURCE,
                           active);
    }

    /**
     * Returns the active target entities from each active relationship for the
     * speciifed node. If a target reference cannot be resolved, it will be
     * ignored.
     *
     * @param node the entity relationship node
     * @return a list of active target entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node) {
        return getEntities(node, ACTIVE_NOT_NULL, TARGET, true);
    }

    /**
     * Returns the active target entities from each relationship for the
     * specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of active target entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node, Date time) {
        return getNodeTargetEntities(node, time, true);
    }

    /**
     * Returns the target entities from each relationship for the specified node
     * that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationships and entities must be active
     * @return a list of target entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node, Date time,
                                              boolean active) {
        return getEntities(node, getActiveNotNullTime(active, time), TARGET,
                           active);
    }

    /**
     * Returns the source entity references from each active relationship for
     * the specified node.
     *
     * @param node the entity relationship node
     * @return a list of source entity references. May contain references to
     *         both active and inactive references
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node) {
        return getEntityRefs(node, ACTIVE_NOT_NULL, SOURCE);
    }

    /**
     * Returns the source entity references from each active relationship for
     * the specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of source entity references. May contain references to
     *         both active and inactive references
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node,
                                                           Date time) {
        return getNodeSourceEntityRefs(node, time, true);
    }

    /**
     * Returns the source entity references from each relationship for the
     * specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationships must be active
     * @return a list of source entity references. May contain references to
     *         both active and inactive references
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node,
                                                           Date time,
                                                           boolean active) {
        return getEntityRefs(node, getActiveNotNullTime(active, time), SOURCE);
    }

    /**
     * Returns the target entity references from each active relationship for
     * the specified node.
     *
     * @param node the entity relationship node
     * @return a list of target entity references. May contain references to
     *         both active and inactive references
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node) {
        return getEntityRefs(node, ACTIVE_NOT_NULL, TARGET);
    }

    /**
     * Returns the target entity references from each active relationship for
     * the specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of target entity references. May contain references to
     *         both active and inactive references
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node,
                                                           Date time) {
        return getNodeTargetEntityRefs(node, time, true);
    }

    /**
     * Returns the target entity references from each relationship for the
     * specified node that has start and end times overlapping the specified
     * time. The end time may be null, indicating an unbounded time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationships must be active
     * @return a list of target entity references. May contain references to
     *         both active and inactive references
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node,
                                                           Date time,
                                                           boolean active) {
        return getEntityRefs(node, getActiveNotNullTime(active, time), TARGET);
    }

    /**
     * Returns the source entity in the first active entity relationship
     * matching the specified short name that has an active entity.
     *
     * @param shortName the entity relationship short name
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName) {
        return getSourceEntity(new String[]{shortName});
    }

    /**
     * Returns the source entity in the first entity relationship
     * matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @param active    determines if the relationship and entity must be active
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, boolean active) {
        return getSourceEntity(new String[]{shortName}, active);
    }

    /**
     * Returns the source entity in the first active entity relationship
     * matching the specified short names that has an active entity.
     *
     * @param shortNames the entity relationship short names
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames) {
        return getSourceEntity(shortNames, true);
    }

    /**
     * Returns the source entity in the first entity relationship
     * matching the specified short names.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship and entity must be
     *                   active
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, boolean active) {
        return getEntity(getEntity().getEntityRelationships(),
                         getActiveNotNullIsA(active, shortNames), SOURCE,
                         active);
    }

    /**
     * Returns the target entity in the first active entity relationship
     * matching the specified short name that has an active entity.
     *
     * @param shortName the entity relationship short names
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName) {
        return getTargetEntity(new String[]{shortName});
    }

    /**
     * Returns the target entity in the first entity relationship
     * matching the specified short name.
     *
     * @param shortName the entity relationship short names
     * @param active    determines if the relationship and entity must be active
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, boolean active) {
        return getTargetEntity(new String[]{shortName}, active);
    }

    /**
     * Returns the target entity in the first active entity relationship
     * matching the specified short names that has an active entity.
     *
     * @param shortNames the entity relationship short names
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames) {
        return getTargetEntity(shortNames, true);
    }

    /**
     * Returns the target entity in the first active entity relationship
     * matching the specified short names that has an active entity.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship and entity must be
     *                   active
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, boolean active) {
        return getEntity(getEntity().getEntityRelationships(),
                         getActiveNotNullIsA(active, shortNames), TARGET,
                         active);
    }

    /**
     * Returns the source entity in the first entity relationship matching
     * the specified short name that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, Date time) {
        return getSourceEntity(shortName, time, true);
    }

    /**
     * Returns the source entity in the first entity relationship matching
     * the specified short name that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @param active    determines if the relationship and entity must be active
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, Date time, boolean active) {
        return getSourceEntity(new String[]{shortName}, time, active);
    }

    /**
     * Returns the source entity in the first entity relationship matching
     * the specified short names that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, Date time) {
        return getSourceEntity(shortNames, time, true);
    }

    /**
     * Returns the source entity in the first entity relationship matching
     * the specified short names that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @param active     determines if the relationship and entity must be
     *                   active
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, Date time,
                                  boolean active) {
        Entity entity = getEntity(getEntity().getEntityRelationships(),
                                  getActiveNotNullIsATime(true, shortNames,
                                                          time), SOURCE, true);
        if (entity == null && !active) {
            entity = getEntity(getEntity().getEntityRelationships(),
                               getActiveNotNullIsATime(false, shortNames, time),
                               SOURCE, false);
        }
        return entity;
    }

    /**
     * Returns the target entity in the first entity relationship matching the
     * specified short name that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, Date time) {
        return getTargetEntity(shortName, time, true);
    }

    /**
     * Returns the target entity in the first entity relationship matching the
     * specified short name that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, Date time, boolean active) {
        return getTargetEntity(new String[]{shortName}, time, active);
    }

    /**
     * Returns the target entity in the first entity relationship matching the
     * specified short names that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, Date time) {
        return getTargetEntity(shortNames, time, true);
    }

    /**
     * Returns the target entity in the first entity relationship matching the
     * specified short names that has start and end times overlapping the
     * specified time. The end time may be null, indicating an unbounded time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, Date time,
                                  boolean active) {
        Entity entity = getEntity(getEntity().getEntityRelationships(),
                                  getActiveNotNullIsATime(true, shortNames,
                                                          time), TARGET, true);
        if (entity == null && !active) {
            entity = getEntity(getEntity().getEntityRelationships(),
                               getActiveNotNullIsATime(false, shortNames, time),
                               TARGET, false);
        }
        return entity;
    }

    /**
     * Returns the entity in the first entity relationship for the
     * specified node that matches the specified criteria.
     *
     * @param node     the entity relationship node
     * @param criteria the criteria
     * @param accessor the entity accessor
     * @param active   determines if the entity must be active
     * @return the first active entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getEntity(String node, Criteria criteria,
                             RefAccessor accessor, boolean active) {
        List<EntityRelationship> relationships
                = getValues(node, EntityRelationship.class);
        return getEntity(relationships, criteria, accessor, active);
    }

    /**
     * Returns the entity from the first relationship matching the specified
     * criteria.
     *
     * @param relationships the relationships
     * @param criteria      the criteria
     * @param accessor      the entity accessor
     * @param active        determines if the entity must be active
     * @return the first active entity or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getEntity(Collection<EntityRelationship> relationships,
                             Criteria criteria, RefAccessor accessor,
                             boolean active) {
        for (EntityRelationship relationship : relationships) {
            if (criteria.matches(relationship, accessor)) {
                Entity entity = getEntity(accessor.getRef(relationship));
                if (entity != null && (!active || entity.isActive())) {
                    return entity;
                }
            }
        }
        return null;
    }

    /**
     * Returns all entity references for the specified node that matches the
     * specified criteria.
     *
     * @param node     the entity relationship node
     * @param criteria the criteria
     * @param accessor the entity accessor
     * @return the matching references
     */
    private List<IMObjectReference> getEntityRefs(String node,
                                                  Criteria criteria,
                                                  RefAccessor accessor) {
        List<EntityRelationship> relationships
                = getValues(node, EntityRelationship.class);
        return getEntityRefs(relationships, criteria, accessor);
    }

    /**
     * Returns all active entities for the specified node that match the
     * specified criteria.
     *
     * @param node     the entity relationship node
     * @param criteria the criteria
     * @param accessor the entity accessor
     * @param active   determines if the entity must be active
     * @return a list of entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Entity> getEntities(String node, Criteria criteria,
                                     RefAccessor accessor, boolean active) {
        List<IMObjectReference> refs = getEntityRefs(node, criteria, accessor);
        if (refs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Entity> result = new ArrayList<Entity>();
        for (IMObjectReference ref : refs) {
            Entity entity = getEntity(ref);
            if (entity != null && (!active || entity.isActive())) {
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * Returns all entity references from the supplied relationship that match
     * the specified criteria.
     *
     * @param relationships the relationships
     * @param criteria      the criteria
     * @param accessor      the entity accessor
     * @return the matching references
     */
    private List<IMObjectReference> getEntityRefs(
            Collection<EntityRelationship> relationships,
            Criteria criteria, RefAccessor accessor) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        for (EntityRelationship relationship : relationships) {
            if (criteria.matches(relationship, accessor)) {
                result.add(accessor.getRef(relationship));
            }
        }
        return result;
    }

    /**
     * Helper to return an entity given its reference.
     *
     * @param ref the reference. May be <tt>null</tt>
     * @return the corresponding entity, or <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getEntity(IMObjectReference ref) {
        if (ref != null) {
            return (Entity) ArchetypeQueryHelper.getByObjectReference(
                    getArchetypeService(), ref);
        }
        return null;
    }

    /**
     * Helper to return a criteria that checks that a relationship is
     * active, the reference is non-null, and that specified time is within
     * the relationship active start and end times.
     *
     * @param active determines if the relationship must be active
     * @param time   the time
     * @return a new criteria
     */
    private Criteria getActiveNotNullTime(boolean active, Date time) {
        Criteria criteria = (active) ? ACTIVE_NOT_NULL : NOT_NULL;
        return new And(criteria, new TimeCriteria(time));
    }

    /**
     * Helper to return a criteria that checks that a relationship is
     * active, the reference is non-null, and that the relationship is a
     * specified type.
     *
     * @param active     determines if the relationship must be active
     * @param shortNames the relationship short names to match
     * @return a new criteria
     */
    private Criteria getActiveNotNullIsA(boolean active, String[] shortNames) {
        Criteria criteria = (active) ? ACTIVE_NOT_NULL : NOT_NULL;
        return new And(criteria, new IsA(shortNames));
    }

    /**
     * Helper to return a criteria that checks that a relationship is
     * active, the reference is non-null, the relationship is a specified type,
     * and that specified time is within the relationship active start and end
     * times.
     *
     * @param active     determines if the relationship must be active
     * @param shortNames the relationship short names to match
     * @param time       the time
     * @return a new criteria
     */
    private Criteria getActiveNotNullIsATime(boolean active,
                                             String[] shortNames, Date time) {
        Criteria criteria = (active) ? ACTIVE_NOT_NULL : NOT_NULL;
        return new And(criteria, new IsA(shortNames), new TimeCriteria(time));
    }

    /**
     * Helper to return the source or target of an entity relationship.
     */
    private static class RefAccessor {

        private final boolean source;

        public RefAccessor(boolean source) {
            this.source = source;
        }

        public IMObjectReference getRef(EntityRelationship relationship) {
            return source ? relationship.getSource() : relationship.getTarget();
        }

    }

    /**
     * Helper to determine if an entity relationship matches some criteria.
     */
    private interface Criteria {
        boolean matches(EntityRelationship relationship, RefAccessor accessor);
    }

    private static class And implements Criteria {
        private Criteria[] criterias;

        public And(Criteria ... criterias) {
            this.criterias = criterias;
        }

        public boolean matches(EntityRelationship relationship,
                               RefAccessor accessor) {
            for (Criteria criteria : criterias) {
                if (!criteria.matches(relationship, accessor)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * <tt>Criteria</tt> that evaluates true if a relationship is of a
     * particular type.
     */
    private static class IsA implements Criteria {

        private final String[] shortNames;

        public IsA(String[] shortNames) {
            this.shortNames = shortNames;
        }

        public boolean matches(EntityRelationship relationship,
                               RefAccessor accessor) {
            return TypeHelper.isA(relationship, shortNames);
        }
    }

    /**
     * <tt>Criteria</tt> that evaluates true if a relationship is active.
     */
    private static class Active implements Criteria {

        public boolean matches(EntityRelationship relationship,
                               RefAccessor accessor) {
            return relationship.isActive();
        }
    }

    /**
     * <tt>Criteria</tt> that evaluates true if a reference is non-null.
     */
    private static class NotNull implements Criteria {
        public boolean matches(EntityRelationship relationship,
                               RefAccessor accessor) {
            return accessor.getRef(relationship) != null;
        }
    }

    /**
     * <tt>Criteria</tt> that evaluates true the specified time falls between
     * the start and end time of a relationship.
     */
    private static class TimeCriteria implements Criteria {

        private final Date time;

        private TimeCriteria(Date time) {
            this.time = time;
        }

        public boolean matches(EntityRelationship relationship,
                               RefAccessor accessor) {
            Date start = relationship.getActiveStartTime();
            if (start instanceof Timestamp) {
                start = new Date(start.getTime());
            }
            if (start != null && start.compareTo(time) <= 0) {
                Date end = relationship.getActiveEndTime();
                if (end instanceof Timestamp) {
                    end = new Date(end.getTime());
                }
                if (end == null || end.compareTo(time) >= 0) {
                    return true;
                }
            }
            return false;
        }
    }

}
