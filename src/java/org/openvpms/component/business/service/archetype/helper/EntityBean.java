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

import org.apache.commons.collections.Predicate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.SOURCE;
import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.TARGET;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;


/**
 * Helper to access an {@link Entity}'s properties via their names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityBean extends IMObjectBean {

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
     * Adds an entity relationship to both the source and target entity.
     *
     * @param shortName the relationship short name
     * @param target    the target entity
     * @return the new relationship
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
        target.addEntityRelationship(r);
        return r;
    }

    /**
     * Adds a new relationship between the current entity (the source), and the supplied target.
     *
     * @param name   the entity relationship node name, used to determine which relationship to create
     * @param target the target entity
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if <tt>name</tt> is an invalid node, there is no relationship that supports
     *                                   <tt>target</tt>, or multiple relationships can support <tt>target</tt>
     */
    public EntityRelationship addNodeRelationship(String name, Entity target) {
        String shortName = getRelationshipShortName(name, target, "target");
        return addRelationship(shortName, target);
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
     * Returns all active relationships with the specified short name.
     *
     * @param shortName the relationship short name
     * @return all active relationships with the specified short name
     */
    public List<EntityRelationship> getRelationships(String shortName) {
        return getRelationships(shortName, true);
    }

    /**
     * Returns all relationships with the specified short name.
     *
     * @param shortName the relationship short name
     * @param active    determines if the relationships must be active or not
     * @return all relationships with the specified short name
     */
    public List<EntityRelationship> getRelationships(String shortName, boolean active) {
        Set<EntityRelationship> relationships = getEntity().getEntityRelationships();
        return select(relationships, getActiveIsA(active, shortName));
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
     * Returns the source entity from the first active entity relationship with active source entity, for the specified
     * node.
     *
     * @param node the entity relationship node name
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node) {
        return (Entity) getNodeSourceObject(node);
    }

    /**
     * Returns the source entity from the first entity relationship for the
     * specified node.
     *
     * @param node   the entity relationship node name
     * @param active determines if the relationship and source entity must be
     *               active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, boolean active) {
        return (Entity) getNodeSourceObject(node, active);
    }

    /**
     * Returns the source entity from the first active entity relationship
     * with active source entity, matching the specified predicate.
     *
     * @param node      the entity relationship node name
     * @param predicate the predicate
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Predicate predicate) {
        return (Entity) getNodeSourceObject(node, predicate);
    }

    /**
     * Returns the source entity from the first entity relationship matching the specified predicate.
     *
     * @param node      the entity relationship node name
     * @param predicate the predicate
     * @param active    determines if the entity must be active or not
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Predicate predicate, boolean active) {
        return (Entity) getNodeSourceObject(node, predicate, active);
    }

    /**
     * Returns the target entity from the first active entity relationship with active target entity, for the specified
     * node.
     *
     * @param node the entity relationship node name
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node) {
        return (Entity) getNodeTargetObject(node);
    }

    /**
     * Returns the target entity from the first entity relationship for the specified node.
     *
     * @param node   the entity relationship node
     * @param active determines if the relationship and target entity must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, boolean active) {
        return (Entity) getNodeTargetObject(node, active);
    }

    /**
     * Returns the target entity from the first active entity relationship with active target entity, for the specified
     * node.
     *
     * @param node      the entity relationship node name
     * @param predicate the predicate
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Predicate predicate) {
        return (Entity) getNodeTargetObject(node, predicate);
    }

    /**
     * Returns the target entity from the first entity relationship for the specified node.
     *
     * @param node      the entity relationship node name
     * @param predicate the predicate
     * @param active    determines if the entity must be active or not
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Predicate predicate, boolean active) {
        return (Entity) getNodeTargetObject(node, predicate, active);
    }

    /**
     * Returns the target entity from the first entity relationship matching the specified short name. The relationship
     * must be active at the specified time, and have an active target entity.
     *
     * @param node the entity relationship node name
     * @param time the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Date time) {
        return (Entity) getNodeSourceObject(node, time);
    }

    /**
     * Returns the source entity from the first entity relationship that is active at the specified time, for the
     * specified node.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Date time, boolean active) {
        return (Entity) getNodeSourceObject(node, time, active);
    }

    /**
     * Returns the target entity from the first entity relationship with active target entity that is active at the
     * specified time, for the specified node.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Date time) {
        return (Entity) getNodeTargetObject(node, time);
    }

    /**
     * Returns the target entity from the first entity relationship that is active at the specified time, for the
     * specified node.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entity must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Date time, boolean active) {
        return (Entity) getNodeTargetObject(node, time, active);
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
        return getNodeSourceObjects(node, Entity.class);
    }

    /**
     * Returns the active source entities from each relationship for the
     * specified node that is active at the specified time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of active source entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node, Date time) {
        return getNodeSourceObjects(node, time, Entity.class);
    }

    /**
     * Returns the source entities from each relationship for the specified node
     * that is active at the specified time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entities must be active
     * @return a list of source entities. May contain inactive entities if <tt>active</tt> is <tt>false</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node, Date time, boolean active) {
        return getNodeSourceObjects(node, time, active, Entity.class);
    }

    /**
     * Returns the active source entities from each relationship for the
     * specified node that matches the specified predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of source entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node, Predicate predicate) {
        return getNodeSourceObjects(node, predicate, true, Entity.class);
    }

    /**
     * Returns the source entities from each relationship for the
     * specified node that matches the specified predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @param active    determines if the entities must be active
     * @return a list of source entities. May contain inactive entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node, Predicate predicate, boolean active) {
        return getNodeSourceObjects(node, predicate, active, Entity.class);
    }

    /**
     * Returns the active target entities from each active relationship for the
     * specified node. If a target reference cannot be resolved, it will be
     * ignored.
     *
     * @param node the entity relationship node
     * @return a list of active target entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node) {
        return getNodeTargetObjects(node, Entity.class);
    }

    /**
     * Returns the active target entities from each relationship for the
     * specified node that is active at the specified time.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of active target entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node, Date time) {
        return getNodeTargetObjects(node, time, Entity.class);
    }

    /**
     * Returns the target entities from each relationship for the specified node
     * that is active at the specified time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entities must be active
     * @return a list of target entities. May contain inactive entities if <tt>active</tt> is <tt>false</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node, Date time, boolean active) {
        return getNodeTargetObjects(node, time, active, Entity.class);
    }

    /**
     * Returns the active target entities from each relationship for the
     * specified node that matches the specified predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of target entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node, Predicate predicate) {
        return getNodeTargetObjects(node, predicate, Entity.class);
    }

    /**
     * Returns the target entities from each relationship for the
     * specified node that matches the specified predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @param active    determines if the entities must be active
     * @return a list of target entities. May  contain inactive entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node, Predicate predicate, boolean active) {
        return getNodeTargetObjects(node, predicate, active, Entity.class);
    }

    /**
     * Returns the source entity references from each active relationship for the specified node.
     *
     * @param node the entity relationship node
     * @return a list of source entity references. May contain references to both active and inactive entities
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node) {
        return getNodeSourceObjectRefs(node);
    }

    /**
     * Returns the source entity references from each relationship that is
     * active at the specified time, for the specified node.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of source entity references. May contain references to both active and inactive entities
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node, Date time) {
        return getNodeSourceObjectRefs(node, time);
    }

    /**
     * Returns the source entity references from each relationship that is active at the specified time, for the
     * specified node.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationships must be active
     * @return a list of source entity references. May contain references to both active and inactive entities
     * @deprecated use {@link #getNodeSourceEntities(String, Date)}
     */
    @Deprecated
    public List<IMObjectReference> getNodeSourceEntityRefs(String node, Date time, boolean active) {
        return getRelatedObjectRefs(node, new IsActiveRelationship(time), SOURCE);
    }

    /**
     * Returns the source entity references from each relationship for the specified node that matches the supplied
     * predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of source entity references. May contain references to both active and inactive entities
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node, Predicate predicate) {
        return getNodeSourceObjectRefs(node, predicate);
    }

    /**
     * Returns the target entity references from each active relationship for the specified node.
     *
     * @param node the entity relationship node
     * @return a list of target entity references. May contain references to both active and inactive entities
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node) {
        return getNodeTargetObjectRefs(node);
    }

    /**
     * Returns the target entity references from each relationship that is active at the specified time, for the
     * specified node.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of target entity references. May contain references to both active and inactive entities
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node, Date time) {
        return getNodeTargetObjectRefs(node, time);
    }

    /**
     * Returns the target entity references from each relationship that is
     * active at the specified time, for the specified node.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationships must be active
     * @return a list of target entity references. May contain references to
     *         both active and inactive entities
     * @deprecated use {@link #getNodeTargetEntityRefs(String, Date)}
     */
    @Deprecated
    public List<IMObjectReference> getNodeTargetEntityRefs(String node, Date time, boolean active) {
        return getRelatedObjectRefs(node, new IsActiveRelationship(time), TARGET);
    }

    /**
     * Returns the target entity references from each relationship for the specified node that matches the supplied
     * predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of target entity references. May contain references to both active and inactive entities
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node, Predicate predicate) {
        return getNodeTargetObjectRefs(node, predicate);
    }

    /**
     * Returns all entity relationships for the specified node.
     *
     * @param node the entity relationship node
     * @return a list of relationships
     */
    public List<EntityRelationship> getNodeRelationships(String node) {
        return getValues(node, EntityRelationship.class);
    }

    /**
     * Returns all relationships for the specified node matching the supplied predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of relationships matching the predicate
     */
    public List<EntityRelationship> getNodeRelationships(String node, Predicate predicate) {
        return getValues(node, predicate, EntityRelationship.class);
    }

    /**
     * Returns the first relationship for the specified node matching the supplied predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return the first relationship matching the predicate
     */
    public EntityRelationship getNodeRelationship(String node, Predicate predicate) {
        return (EntityRelationship) getValue(node, predicate);
    }

    /**
     * Returns the source entity from the first active entity relationship with active source entity, for the specified
     * relationship short name.
     *
     * @param shortName the entity relationship short name
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortName);
    }

    /**
     * Returns the source entity from the first entity relationship for the specified relationship short name.
     *
     * @param shortName the entity relationship short name
     * @param active    determines if the relationship and entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, boolean active) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortName, active);
    }

    /**
     * Returns the source entity from the first active entity relationship matching the specified relationship short
     * names and having an active source entity.
     *
     * @param shortNames the entity relationship short names
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortNames);
    }

    /**
     * Returns the source entity from the first entity relationship matching the specified relationship short names.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship and source entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, boolean active) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortNames, active);
    }

    /**
     * Returns the target entity from the first active entity relationship with active target entity, for the specified
     * relationship short name.
     *
     * @param shortName the entity relationship short names
     * @return the active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortName);
    }

    /**
     * Returns the target entity from the first entity relationship for the specified relationship short name.
     *
     * @param shortName the entity relationship short names
     * @param active    determines if the relationship and entity must be active
     * @return the entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, boolean active) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortName, active);
    }

    /**
     * Returns the target entity from the first active entity relationship matching the specified relationship short
     * names and having an active target entity.
     *
     * @param shortNames the entity relationship short names
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortNames);
    }

    /**
     * Returns the target entity from the first entity relationship matching the specified relationship short names.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship and target entity must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, boolean active) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortNames, active);
    }

    /**
     * Returns the source entity from the first entity relationship matching the specified short name. The relationship
     * must be active at the specified time, and have an active source entity.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, Date time) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortName, time);
    }

    /**
     * Returns the source entity from the first entity relationship matching the specified short name. The relationship
     * must be active at the specified time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @param active    determines if the entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, Date time, boolean active) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortName, time, active);
    }

    /**
     * Returns the source entity from the first entity relationship matching the specified short names. The relationship
     * must be active at the specified time, and have an active source entity.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, Date time) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortNames, time);
    }

    /**
     * Returns the source entity from the first entity relationship matching the specified short names. The
     * relationship must be active at the specified time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @param active     determines if the entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, Date time, boolean active) {
        return (Entity) getSourceObject(getEntity().getEntityRelationships(), shortNames, time, active);
    }

    /**
     * Returns the source entity from the first entity relationship matching the specified short name. The relationship
     * must be active at the specified time, and have an active target entity.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, Date time) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortName, time);
    }

    /**
     * Returns the target entity from the first entity relationship matching the specified short name. The relationship
     * must be active at the specified time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @param active    determines if the entity must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, Date time, boolean active) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortName, time, active);
    }

    /**
     * Returns the target entity from the first entity relationship matching the specified short names. The relationship
     * must be active at the specified time, and have an active target entity.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, Date time) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortNames, time);
    }

    /**
     * Returns the target entity from the first entity relationship matching the specified short names. The relationship
     * must be active at the specified time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @param active     determines if the relationship must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, Date time, boolean active) {
        return (Entity) getTargetObject(getEntity().getEntityRelationships(), shortNames, time, active);
    }

    /**
     * Returns the source entity reference from the first active entity relationship matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @return the source reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getSourceEntityRef(String shortName) {
        return getSourceObjectRef(getEntity().getEntityRelationships(), shortName);
    }

    /**
     * Returns the source entity reference from the first entity relationship matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @param active    determines if the relationship must be active
     * @return the source reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getSourceEntityRef(String shortName, boolean active) {
        return getSourceObjectRef(getEntity().getEntityRelationships(), shortName, active);
    }

    /**
     * Returns the source entity reference from the first entity relationship matching the specified short names.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship must be active
     * @return the source reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getSourceEntityRef(String[] shortNames, boolean active) {
        return getSourceObjectRef(getEntity().getEntityRelationships(), shortNames, active);
    }

    /**
     * Returns the target entity reference from the first active entity relationship matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @return the target reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getTargetEntityRef(String shortName) {
        return getTargetObjectRef(getEntity().getEntityRelationships(), shortName);
    }

    /**
     * Returns the target entity reference from the first entity relationship matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @param active    determines if the relationship must be active
     * @return the target reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getTargetEntityRef(String shortName, boolean active) {
        return getTargetObjectRef(getEntity().getEntityRelationships(), shortName, active);
    }

    /**
     * Returns the target entity reference from the first entity relationship matching the specified short name.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship must be active
     * @return the target reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getTargetEntityRef(String[] shortNames, boolean active) {
        return getTargetObjectRef(getEntity().getEntityRelationships(), shortNames, active);
    }

}
