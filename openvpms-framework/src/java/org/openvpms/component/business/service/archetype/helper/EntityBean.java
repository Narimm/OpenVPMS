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
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.ACTIVE_NOW;
import org.openvpms.component.business.service.archetype.functor.RelationshipRef;
import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.SOURCE;
import static org.openvpms.component.business.service.archetype.functor.RelationshipRef.TARGET;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;

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
    public List<EntityRelationship> getRelationships(String shortName,
                                                     boolean active) {
        Set<EntityRelationship> relationships
                = getEntity().getEntityRelationships();
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
     * Returns the source entity from the first active entity relationship
     * with active source entity, for the specified node.
     *
     * @param node the entity relationship node name
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node) {
        return getNodeSourceEntity(node, true);
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
        return getNodeSourceEntity(node, getDefaultPredicate(active), active);
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
        return getNodeSourceEntity(node, predicate, true);
    }

    /**
     * Returns the source entity from the first entity relationship matching
     * the specified predicate.
     *
     * @param node      the entity relationship node name
     * @param predicate the predicate
     * @param active    determines if the entity must be active or not
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Predicate predicate,
                                      boolean active) {
        return getEntity(node, predicate, SOURCE, active);
    }

    /**
     * Returns the target entity from the first active entity relationship
     * with active target entity, for the specified node.
     *
     * @param node the entity relationship node name
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node) {
        return getNodeTargetEntity(node, true);
    }

    /**
     * Returns the target entity from the first entity relationship for the
     * specified node.
     *
     * @param node   the entity relationship node
     * @param active determines if the relationship and target entity must be
     *               active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, boolean active) {
        return getNodeTargetEntity(node, getDefaultPredicate(active), active);
    }

    /**
     * Returns the target entity from the first active entity relationship
     * with active target entity, for the specified node.
     *
     * @param node      the entity relationship node name
     * @param predicate the predicate
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Predicate predicate) {
        return getNodeTargetEntity(node, predicate, true);
    }

    /**
     * Returns the target entity from the first entity relationship for the
     * specified node.
     *
     * @param node      the entity relationship node name
     * @param predicate the predicate
     * @param active    determines if the entity must be active or not
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Predicate predicate,
                                      boolean active) {
        return getEntity(node, predicate, TARGET, active);
    }

    /**
     * Returns the target entity from the first entity relationship
     * matching the specified short name. The relationship must be active at
     * the specified time, and have an active target entity.
     *
     * @param node the entity relationship node name
     * @param time the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Date time) {
        return getNodeSourceEntity(node, time, true);
    }

    /**
     * Returns the source entity from the first entity relationship
     * that is active at the specified time, for the specified node.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeSourceEntity(String node, Date time, boolean active) {
        return getNodeSourceEntity(node, new IsActiveRelationship(time),
                                   active);
    }

    /**
     * Returns the target entity from the first entity relationship
     * with active target entity that is active at the specified time,
     * for the specified node.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Date time) {
        return getNodeTargetEntity(node, time, true);
    }

    /**
     * Returns the target entity from the first entity relationship
     * that is active at the specified time, for the specified node.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entity must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeTargetEntity(String node, Date time, boolean active) {
        return getNodeTargetEntity(node, new IsActiveRelationship(time),
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
        return getEntities(node, ACTIVE_NOW, SOURCE, true);
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
        return getNodeSourceEntities(node, time, true);
    }

    /**
     * Returns the source entities from each relationship for the specified node
     * that is active at the specified time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entities must be active
     * @return a list of source entities. May contain inactive entities if
     *         <tt>active</tt> is <tt>false</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeSourceEntities(String node, Date time,
                                              boolean active) {
        return getNodeSourceEntities(node, new IsActiveRelationship(time),
                                     active);
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
    public List<Entity> getNodeSourceEntities(String node,
                                              Predicate predicate) {
        return getNodeSourceEntities(node, predicate, true);
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
    public List<Entity> getNodeSourceEntities(String node, Predicate predicate,
                                              boolean active) {
        return getEntities(node, predicate, SOURCE, active);
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
        return getNodeTargetEntities(node, ACTIVE_NOW,
                                     true);
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
        return getNodeTargetEntities(node, time, true);
    }

    /**
     * Returns the target entities from each relationship for the specified node
     * that is active at the specified time.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the entities must be active
     * @return a list of target entities. May contain inactive entities if
     *         <tt>active</tt> is <tt>false</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Entity> getNodeTargetEntities(String node, Date time,
                                              boolean active) {
        return getNodeTargetEntities(node, new IsActiveRelationship(time),
                                     active);
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
    public List<Entity> getNodeTargetEntities(String node,
                                              Predicate predicate) {
        return getNodeTargetEntities(node, predicate, true);
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
    public List<Entity> getNodeTargetEntities(String node,
                                              Predicate predicate,
                                              boolean active) {
        return getEntities(node, predicate, TARGET, active);
    }

    /**
     * Returns the source entity references from each active relationship for
     * the specified node.
     *
     * @param node the entity relationship node
     * @return a list of source entity references. May contain references to
     *         both active and inactive entities
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node) {
        return getNodeSourceEntityRefs(node, ACTIVE_NOW);
    }

    /**
     * Returns the source entity references from each relationship that is
     * active at the specified time, for the specified node.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of source entity references. May contain references to
     *         both active and inactive entities
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(String node,
                                                           Date time) {
        return getNodeSourceEntityRefs(node, new IsActiveRelationship(time));
    }

    /**
     * Returns the source entity references from each relationship that is
     * active at the specified time, for the specified node.
     *
     * @param node   the entity relationship node
     * @param time   the time
     * @param active determines if the relationships must be active
     * @return a list of source entity references. May contain references to
     *         both active and inactive entities
     * @deprecated use {@link #getNodeSourceEntities(String, Date)}
     */
    @Deprecated
    public List<IMObjectReference> getNodeSourceEntityRefs(String node,
                                                           Date time,
                                                           boolean active) {
        return getEntityRefs(node, new IsActiveRelationship(time), SOURCE);
    }

    /**
     * Returns the source entity references from each relationship for the
     * specified node that matches the supplied predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of source entity references. May contain references to
     *         both active and inactive entities
     */
    public List<IMObjectReference> getNodeSourceEntityRefs(
            String node, Predicate predicate) {
        return getEntityRefs(node, predicate, SOURCE);
    }

    /**
     * Returns the target entity references from each active relationship for
     * the specified node.
     *
     * @param node the entity relationship node
     * @return a list of target entity references. May contain references to
     *         both active and inactive entities
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node) {
        return getNodeTargetEntityRefs(node, ACTIVE_NOW);
    }

    /**
     * Returns the target entity references from each relationship that is
     * active at the specified time, for the specified node.
     *
     * @param node the entity relationship node
     * @param time the time
     * @return a list of target entity references. May contain references to
     *         both active and inactive entities
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(String node,
                                                           Date time) {
        return getNodeTargetEntityRefs(node, new IsActiveRelationship(time));
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
    public List<IMObjectReference> getNodeTargetEntityRefs(String node,
                                                           Date time,
                                                           boolean active) {
        return getEntityRefs(node, new IsActiveRelationship(time), TARGET);
    }

    /**
     * Returns the target entity references from each relationship for the
     * specified node that matches the supplied predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of target entity references. May contain references to
     *         both active and inactive entities
     */
    public List<IMObjectReference> getNodeTargetEntityRefs(
            String node, Predicate predicate) {
        return getEntityRefs(node, predicate, TARGET);
    }

    /**
     * Returns all relationships for the specified node.
     *
     * @param node the entity relationship node
     * @return a list of relationships
     */
    public List<EntityRelationship> getNodeRelationships(String node) {
        return getValues(node, EntityRelationship.class);
    }

    /**
     * Returns all relationships for the specified node matching the supplied
     * predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return a list of relationships matching the predicate
     */
    public List<EntityRelationship> getNodeRelationships(String node,
                                                         Predicate predicate) {
        return select(getNodeRelationships(node), predicate);
    }

    /**
     * Returns the first relationship for the specified node matching the
     * supplied predicate.
     *
     * @param node      the entity relationship node
     * @param predicate the predicate
     * @return the first relationship matching the predicate
     */
    public EntityRelationship getNodeRelationship(String node,
                                                  Predicate predicate) {
        for (EntityRelationship relationship : getNodeRelationships(node)) {
            if (predicate.evaluate(relationship)) {
                return relationship;
            }
        }
        return null;
    }

    /**
     * Returns the source entity from the first active entity relationship
     * with active source entity, for the specified relationship short name.
     *
     * @param shortName the entity relationship short name
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName) {
        return getSourceEntity(new String[]{shortName});
    }

    /**
     * Returns the source entity from the first entity relationship
     * for the specified relationship short name.
     *
     * @param shortName the entity relationship short name
     * @param active    determines if the relationship and entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, boolean active) {
        return getSourceEntity(new String[]{shortName}, active);
    }

    /**
     * Returns the source entity from the first active entity relationship
     * matching the specified relationship short names and having an active
     * source entity.
     *
     * @param shortNames the entity relationship short names
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames) {
        return getSourceEntity(shortNames, true);
    }

    /**
     * Returns the source entity from the first entity relationship
     * matching the specified relationship short names.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship and source entity must
     *                   be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, boolean active) {
        return getEntity(getEntity().getEntityRelationships(),
                         getActiveIsA(active, shortNames), SOURCE, active);
    }

    /**
     * Returns the target entity from the first active entity relationship
     * with active target entity, for the specified relationship short name.
     *
     * @param shortName the entity relationship short names
     * @return the active entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName) {
        return getTargetEntity(new String[]{shortName});
    }

    /**
     * Returns the target entity from the first entity relationship
     * for the specified relationship short name.
     *
     * @param shortName the entity relationship short names
     * @param active    determines if the relationship and entity must be active
     * @return the entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, boolean active) {
        return getTargetEntity(new String[]{shortName}, active);
    }

    /**
     * Returns the target entity from the first active entity relationship
     * matching the specified relationship short names and having an active
     * target entity.
     *
     * @param shortNames the entity relationship short names
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames) {
        return getTargetEntity(shortNames, true);
    }

    /**
     * Returns the target entity from the first entity relationship
     * matching the specified relationship short names.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship and target entity must
     *                   be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, boolean active) {
        return getEntity(getEntity().getEntityRelationships(),
                         getActiveIsA(active, shortNames), TARGET, active);
    }

    /**
     * Returns the source entity from the first entity relationship
     * matching the specified short name. The relationship must be active at
     * the specified time, and have an active source entity.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, Date time) {
        return getSourceEntity(shortName, time, true);
    }

    /**
     * Returns the source entity from the first entity relationship
     * matching the specified short name. The relationship must be active at
     * the specified time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @param active    determines if the entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String shortName, Date time, boolean active) {
        return getSourceEntity(new String[]{shortName}, time, active);
    }

    /**
     * Returns the source entity from the first entity relationship
     * matching the specified short names. The relationship must be active at
     * the specified time, and have an active source entity.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, Date time) {
        return getSourceEntity(shortNames, time, true);
    }

    /**
     * Returns the source entity from the first entity relationship
     * matching the specified short names. The relationship must be active at
     * the specified time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @param active     determines if the entity must be active
     * @return the source entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getSourceEntity(String[] shortNames, Date time,
                                  boolean active) {
        return getEntity(getEntity().getEntityRelationships(),
                         getActiveIsA(time, shortNames), SOURCE, active);
    }

    /**
     * Returns the source entity from the first entity relationship
     * matching the specified short name. The relationship must be active at
     * the specified time, and have an active target entity.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, Date time) {
        return getTargetEntity(shortName, time, true);
    }

    /**
     * Returns the target entity from the first entity relationship
     * matching the specified short name. The relationship must be active at
     * the specified time.
     *
     * @param shortName the entity relationship short name
     * @param time      the time
     * @param active    determines if the entity must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String shortName, Date time, boolean active) {
        return getTargetEntity(new String[]{shortName}, time, active);
    }

    /**
     * Returns the target entity from the first entity relationship
     * matching the specified short names. The relationship must be active at
     * the specified time, and have an active target entity.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, Date time) {
        return getTargetEntity(shortNames, time, true);
    }

    /**
     * Returns the target entity from the first entity relationship
     * matching the specified short names. The relationship must be active at
     * the specified time.
     *
     * @param shortNames the entity relationship short names
     * @param time       the time
     * @param active     determines if the relationship must be active
     * @return the target entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTargetEntity(String[] shortNames, Date time,
                                  boolean active) {
        return getEntity(getEntity().getEntityRelationships(),
                         getActiveIsA(time, shortNames), TARGET, active);
    }

    /**
     * Returns the source entity reference from the first active entity
     * relationship matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @return the source reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getSourceEntityRef(String shortName) {
        return getSourceEntityRef(shortName, true);
    }

    /**
     * Returns the source entity reference from the first entity relationship
     * matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @param active    determines if the relationship must be active
     * @return the source reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getSourceEntityRef(String shortName,
                                                boolean active) {
        return getSourceEntityRef(new String[]{shortName}, active);
    }

    /**
     * Returns the source entity reference from the first entity relationship
     * matching the specified short names.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship must be active
     * @return the source reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getSourceEntityRef(String[] shortNames,
                                                boolean active) {
        return getEntityRef(shortNames, active, SOURCE);
    }

    /**
     * Returns the target entity reference from the first active entity
     * relationship matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @return the target reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getTargetEntityRef(String shortName) {
        return getTargetEntityRef(shortName, true);
    }

    /**
     * Returns the target entity reference from the first entity relationship
     * matching the specified short name.
     *
     * @param shortName the entity relationship short name
     * @param active    determines if the relationship must be active
     * @return the target reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getTargetEntityRef(String shortName,
                                                boolean active) {
        return getTargetEntityRef(new String[]{shortName}, active);
    }

    /**
     * Returns the target entity reference from the first entity relationship
     * matching the specified short nsame.
     *
     * @param shortNames the entity relationship short names
     * @param active     determines if the relationship must be active
     * @return the target reference, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReference getTargetEntityRef(String[] shortNames,
                                                boolean active) {
        return getEntityRef(shortNames, active, TARGET);
    }

    /**
     * Returns the entity from the first entity relationship for the
     * specified node that matches the specified criteria.
     *
     * @param node      the entity relationship node
     * @param predicate the criteria
     * @param accessor  the entity accessor
     * @param active    determines if the entity must be active
     * @return the first entity, or <tt>null</tt> if none is found
     * @throws IMObjectBeanException     if the node is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getEntity(String node, Predicate predicate,
                             RelationshipRef accessor, boolean active) {
        List<EntityRelationship> relationships = getNodeRelationships(node);
        return getEntity(relationships, predicate, accessor, active);
    }

    /**
     * Returns the entity from the first relationship matching the specified
     * criteria. If active is <tt>true</tt> the entity must be active in order
     * to be returned.
     * <p/>
     * If active is <tt>false</tt>, then an active entity will be returned
     * in preference to an inactive one.
     *
     * @param relationships the relationships
     * @param predicate     the predicate
     * @param accessor      the relationship reference accessor
     * @param active        determines if the entity must be active or not
     * @return the first entity matching the critieria or <tt>null</tt> if none
     *         is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getEntity(Collection<EntityRelationship> relationships,
                             Predicate predicate, RelationshipRef accessor,
                             boolean active) {
        Entity inactive = null;
        for (EntityRelationship relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                Entity entity = getEntity(accessor.transform(relationship));
                if (entity != null) {
                    if (entity.isActive()) {
                        // found a match, so return it
                        return entity;
                    } else if (!active) {
                        // can return inactive, but keep looking for an active
                        // match
                        inactive = entity;
                    }
                }
            }
        }
        // no active match found
        return (!active && inactive != null) ? inactive : null;
    }

    /**
     * Returns the first entity reference matching the specified criteria.
     *
     * @param shortNames the short names
     * @param active     determines if the relationship must be active
     * @param accessor   the entity accessor
     * @return the first matching reference,or <tt>null</tt>
     */
    private IMObjectReference getEntityRef(String[] shortNames, boolean active,
                                           RelationshipRef accessor) {
        Set<EntityRelationship> relationships
                = getEntity().getEntityRelationships();
        IMObjectReference ref = getEntityRef(
                relationships, getActiveIsA(shortNames), accessor);
        if (ref == null && !active) {
            ref = getEntityRef(relationships, new IsA(shortNames), accessor);
        }
        return ref;
    }

    /**
     * Returns all entity references for the specified node that match the
     * specified criteria.
     *
     * @param node      the entity relationship node
     * @param predicate the criteria
     * @param accessor  the entity accessor
     * @return the matching references
     */
    private List<IMObjectReference> getEntityRefs(String node,
                                                  Predicate predicate,
                                                  RelationshipRef accessor) {
        List<EntityRelationship> relationships = getNodeRelationships(node);
        return getEntityRefs(relationships, predicate, accessor);
    }

    /**
     * Returns all entities for the specified node that match the specified
     * criteria.
     *
     * @param node      the entity relationship node
     * @param predicate the criteria to filter relationships
     * @param accessor  the entity accessor
     * @param active    determines if the entities must be active
     * @return a list of entities
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Entity> getEntities(String node, Predicate predicate,
                                     RelationshipRef accessor,
                                     boolean active) {
        List<IMObjectReference> refs = getEntityRefs(node, predicate, accessor);
        if (refs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Entity> result = new ArrayList<Entity>();
        for (IMObjectReference ref : refs) {
            Entity entity = getEntity(ref);
            if (entity != null) {
                if (active && entity.isActive() || !active) {
                    result.add(entity);
                }
            }
        }
        return result;
    }

    /**
     * Returns the first entity reference from the supplied relationship that
     * matches the specified criteria.
     *
     * @param relationships the relationships
     * @param predicate     the criteria
     * @param accessor      the entity accessor
     * @return the matching reference, or <tt>null</tt>
     */
    private IMObjectReference getEntityRef(
            Collection<EntityRelationship> relationships,
            Predicate predicate, RelationshipRef accessor) {
        for (EntityRelationship relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                return accessor.transform(relationship);
            }
        }
        return null;
    }

    /**
     * Returns all entity references from the supplied relationship that match
     * the specified criteria.
     *
     * @param relationships the relationships
     * @param predicate     the criteria
     * @param accessor      the entity accessor
     * @return the matching references
     */
    private List<IMObjectReference> getEntityRefs(
            Collection<EntityRelationship> relationships,
            Predicate predicate, RelationshipRef accessor) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        relationships = select(relationships, predicate);
        for (EntityRelationship relationship : relationships) {
            result.add(accessor.transform(relationship));
        }
        return result;
    }

    /**
     * Selects all relationships matching a predicate.
     *
     * @param relationships the source relationships
     * @param predicate
     * @return the relationships matching the predicate
     */
    private List<EntityRelationship> select(
            Collection<EntityRelationship> relationships, Predicate predicate) {
        List<EntityRelationship> result = new ArrayList<EntityRelationship>();
        for (EntityRelationship relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                result.add(relationship);
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
        return (Entity) resolve(ref);
    }

    /**
     * Helper to return a predicate that checks that a relationship is
     * active at the specified time, and is one of a set of archetypes.
     *
     * @param time       the time
     * @param shortNames the relationship short names to match
     * @return a new predicate
     */
    private Predicate getActiveIsA(Date time, String... shortNames) {
        return new AndPredicate(new IsActiveRelationship(time),
                                new IsA(shortNames));
    }

    /**
     * Helper to rerturn a predicate that checks that a relationship is
     * active now, if <tt>active</tt> is <tt>true</tt>, and is one of a set
     * of archetypes.
     *
     * @param active     determines if the relationship must be active
     * @param shortNames the relationship short names to match
     * @return a new predicate
     */
    private Predicate getActiveIsA(boolean active, String... shortNames) {
        IsA isA = new IsA(shortNames);
        return (active) ? new AndPredicate(ACTIVE_NOW, isA) : isA;
    }

    /**
     * Helper to return a predicate that checks that a relationship is
     * active now, and is one of a set of archetypes.
     *
     * @param shortNames the relationship short names to match
     * @return a new predicate
     */
    private Predicate getActiveIsA(String... shortNames) {
        return new AndPredicate(ACTIVE_NOW, new IsA(shortNames));
    }

    /**
     * Helper to return the default predicate for evaluating relationships.
     *
     * @param active determines if the relationship must be active
     * @return the default predicate
     */
    private Predicate getDefaultPredicate(boolean active) {
        return (active) ? ACTIVE_NOW : PredicateUtils.truePredicate();
    }

}
