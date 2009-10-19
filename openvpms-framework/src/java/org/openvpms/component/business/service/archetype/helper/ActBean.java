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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Helper to access an {@link Act}'s properties via their names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActBean extends IMObjectBean {

    /**
     * Constructs a new <tt>ActBean</tt>.
     *
     * @param act the act
     */
    public ActBean(Act act) {
        this(act, null);
    }

    /**
     * Constructs a new <tt>ActBean</tt>.
     *
     * @param act     the act
     * @param service the archetype service
     */
    public ActBean(Act act, IArchetypeService service) {
        super(act, service);
    }

    /**
     * Returns the underlying act.
     *
     * @return the underlying act
     */
    public Act getAct() {
        return (Act) getObject();
    }

    /**
     * Sets the act status.
     *
     * @param status the act status
     */
    public void setStatus(String status) {
        getAct().setStatus(status);
    }

    /**
     * Returns the act status.
     *
     * @return the act status
     */
    public String getStatus() {
        return getAct().getStatus();
    }

    /**
     * Adds an act relationship, to both acts.
     *
     * @param shortName the relationship short name
     * @param target    the target act
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if <tt>shortName</tt> is an invalid archetype
     */
    public ActRelationship addRelationship(String shortName, Act target) {
        Act act = getAct();
        ActRelationship r = (ActRelationship) getArchetypeService().create(shortName);
        if (r == null) {
            throw new IMObjectBeanException(ArchetypeNotFound, shortName);
        }
        r.setSource(act.getObjectReference());
        r.setTarget(target.getObjectReference());
        act.addActRelationship(r);
        target.addActRelationship(r);
        return r;
    }

    /**
     * Returns the first act relationship with the specified act as a target.
     *
     * @param target the target act
     * @return the first act relationship with <tt>target</tt> as its target
     *         or <tt>null</tt> if none is found
     */
    public ActRelationship getRelationship(Act target) {
        Act act = getAct();
        IMObjectReference ref = target.getObjectReference();
        for (ActRelationship r : act.getSourceActRelationships()) {
            if (ref.equals(r.getTarget())) {
                return r;
            }
        }
        return null;
    }

    /**
     * Determines if a relationship exists with the specified short name and
     * target.
     *
     * @param shortName the relationship short name
     * @param target    the target act
     * @return <tt>true</tt> if a relationship exists with the specified short
     *         name and target
     */
    public boolean hasRelationship(String shortName, Act target) {
        return hasRelationship(shortName, target.getObjectReference());
    }

    /**
     * Determines if a relationship exists with the specified short name and
     * target.
     *
     * @param shortName the relationship short name
     * @param target    the target act reference
     * @return <tt>true</tt> if a relationship exists with the specified short
     *         name and target
     */
    public boolean hasRelationship(String shortName, IMObjectReference target) {
        for (ActRelationship relationship : getRelationships(shortName)) {
            if (relationship.getTarget().equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an act relationship.
     *
     * @param relationship the relationship to remove
     */
    public void removeRelationship(ActRelationship relationship) {
        Act act = getAct();
        act.removeActRelationship(relationship);
    }

    /**
     * Returns all relationships matching the specified short name.
     *
     * @param shortName the short name
     * @return a list of all relationships matching the short name
     */
    public List<ActRelationship> getRelationships(String shortName) {
        List<ActRelationship> result = new ArrayList<ActRelationship>();
        Set<ActRelationship> relationships = getAct().getActRelationships();
        for (ActRelationship r : relationships) {
            if (TypeHelper.isA(r, shortName)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Adds a new relationship between the current act (the source), and the supplied target.
     *
     * @param name   the act relationship node name, used to determine which relationship to create
     * @param target the target act
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if <tt>name</tt> is an invalid node, or a relationship cannot be added
     */
    public ActRelationship addNodeRelationship(String name, Act target) {
        String[] range = getArchetypeRange(name);
        IArchetypeService service = getArchetypeService();
        IMObjectReference targetRef = target.getObjectReference();
        ActRelationship result = null;
        for (String shortName : range) {
            ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(shortName);
            if (descriptor != null) {
                NodeDescriptor node = descriptor.getNodeDescriptor("target");
                if (node != null && TypeHelper.isA(targetRef, node.getArchetypeRange())) {
                    result = addRelationship(shortName, target);
                    break;
                }
            }
        }
        if (result == null) {
            throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.CannotAddTargetToNode,
                                            targetRef.getArchetypeId().getShortName(), name);
        }
        return result;
    }

    /**
     * Resolves and returns a list of the child acts.
     *
     * @return a list of the child acts
     * @throws ArchetypeServiceException for any archetype service
     */
    public List<Act> getActs() {
        List<Act> result = new ArrayList<Act>();
        Act act = getAct();
        for (ActRelationship r : act.getSourceActRelationships()) {
            Act child = (Act) resolve(r.getTarget());
            if (child != null) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * Resolves and returns a list of the child acts with the specified short
     * name.
     *
     * @param shortName the act short name. May contain wildcards
     * @return a list of the child acts
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Act> getActs(String shortName) {
        List<Act> result = new ArrayList<Act>();
        Act act = getAct();
        for (ActRelationship r : act.getSourceActRelationships()) {
            IMObjectReference target = r.getTarget();
            if (TypeHelper.isA(target, shortName)) {
                Act child = (Act) resolve(target);
                if (child != null) {
                    result.add(child);
                }
            }
        }
        return result;
    }

    /**
     * Resolves and returns a list of the child acts for the specified node.
     *
     * @param name the node name
     * @return a list of the child acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node doesn't exist
     * @deprecated use {@link #getNodeActs} instead
     */
    @Deprecated
    public List<Act> getActsForNode(String name) {
        return getNodeActs(name);
    }

    /**
     * Resolves and returns a list of the child acts for the specified node.
     *
     * @param name the node name
     * @return a list of the child acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node doesn't exist
     */
    public List<Act> getNodeActs(String name) {
        return getNodeActs(name, Act.class);
    }

    /**
     * Returns the values of a collection node, converted to the supplied type.
     *
     * @param name the node name
     * @param type the class type
     * @return a list of the child acts
     * @throws IMObjectBeanException if the node does't exist or an element
     *                               is of the wrong type
     */
    @SuppressWarnings("unchecked")
    public <T extends Act> List<T> getNodeActs(String name, Class<T> type) {
        List<T> result = new ArrayList<T>();
        IMObjectReference ref = getReference();
        for (ActRelationship relationship
                : getValues(name, ActRelationship.class)) {
            Act child = getSourceOrTarget(relationship, ref);
            if (child != null) {
                if (!type.isInstance(child)) {
                    throw new IMObjectBeanException(
                            InvalidClassCast, type.getName(),
                            child.getClass().getName());
                }
                result.add((T) child);
            }
        }
        return result;
    }

    /**
     * Adds a participation.
     *
     * @param shortName the participation short name
     * @param entity    the entity
     * @return the new participation
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Participation addParticipation(String shortName, Entity entity) {
        return addParticipation(shortName, entity.getObjectReference());
    }

    /**
     * Adds a participation.
     *
     * @param shortName the participation short name
     * @param entity    the entity
     * @return the new participation
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Participation addParticipation(String shortName,
                                          IMObjectReference entity) {
        Act act = getAct();
        Participation p = (Participation) getArchetypeService().create(
                shortName);
        if (p == null) {
            throw new IMObjectBeanException(ArchetypeNotFound, shortName);
        }
        p.setAct(act.getObjectReference());
        p.setEntity(entity);
        act.addParticipation(p);
        return p;
    }

    /**
     * Returns the first participation of the specified type.
     *
     * @param shortName the participation short name
     * @return the participation or <tt>null</tt> if none is found
     */
    public Participation getParticipation(String shortName) {
        for (Participation p : getAct().getParticipations()) {
            if (p.getArchetypeId().getShortName().equals(shortName)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Removes the first participation matching the specified short name.
     *
     * @param shortName the participation short name
     * @return the removed participation, or <tt>null</tt> if none is found
     */
    public Participation removeParticipation(String shortName) {
        Participation p = getParticipation(shortName);
        if (p != null) {
            getAct().removeParticipation(p);
        }
        return p;
    }

    /**
     * Returns a reference to the participant of the first participation
     * matching the specified type.
     *
     * @param shortName the participation short name
     * @return the entity reference, or <tt>null</tt> if none is found
     */
    public IMObjectReference getParticipantRef(String shortName) {
        Participation p = getParticipation(shortName);
        return (p != null) ? p.getEntity() : null;
    }

    /**
     * Returns the participant of the first participation matching the specified
     * type.
     *
     * @param shortName the participation short name
     * @return the entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getParticipant(String shortName) {
        IMObjectReference ref = getParticipantRef(shortName);
        return (Entity) resolve(ref);
    }

    /**
     * Sets the entity of a participation, creating the participation if it
     * doesn't exist.
     *
     * @param shortName the participation short name
     * @param entity    the entity
     * @return the participation
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Participation setParticipant(String shortName, Entity entity) {
        return setParticipant(shortName, entity.getObjectReference());
    }

    /**
     * Sets the entity of a participation, creating the participation if it
     * doesn't exist.
     *
     * @param shortName the participation short name
     * @param entity    the entity
     * @return the participation
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Participation setParticipant(String shortName,
                                        IMObjectReference entity) {
        Participation p = getParticipation(shortName);
        if (p == null) {
            p = addParticipation(shortName, entity);
        } else {
            p.setEntity(entity);
        }
        return p;
    }

    /**
     * Returns a reference to the participant of the first participation
     * for the specified node.
     *
     * @param name the node name
     * @return the entity reference, or <tt>null</tt> if none is found
     */
    public IMObjectReference getNodeParticipantRef(String name) {
        for (Participation p : getValues(name, Participation.class)) {
            if (p.getEntity() != null) {
                return p.getEntity();
            }
        }
        return null;
    }

    /**
     * Returns the participant of the first participation for the specified
     * node.
     *
     * @param name the participation short name
     * @return the entity, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getNodeParticipant(String name) {
        IMObjectReference ref = getNodeParticipantRef(name);
        return (Entity) resolve(ref);
    }

    /**
     * Adds a new participation relationship between the act (the source), and the supplied target entity.
     *
     * @param name   the participation relationship node name, used to determine which relationship to create
     * @param target the target entity
     * @return the new relationship
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if <tt>name</tt> is an invalid node, or a relationship cannot be added
     */
    public Participation addNodeParticipation(String name, Entity target) {
        String[] range = getArchetypeRange(name);
        IArchetypeService service = getArchetypeService();
        IMObjectReference targetRef = target.getObjectReference();
        Participation result = null;
        for (String shortName : range) {
            ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(shortName);
            if (descriptor != null) {
                NodeDescriptor node = descriptor.getNodeDescriptor("entity");
                if (node != null && TypeHelper.isA(targetRef, node.getArchetypeRange())) {
                    result = addParticipation(shortName, target);
                    break;
                }
            }
        }
        if (result == null) {
            throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.CannotAddTargetToNode,
                                            targetRef.getArchetypeId().getShortName(), name);
        }
        return result;
    }

    /**
     * Returns the source or target of a relationship that is not the same
     * as the supplied reference.
     *
     * @param relationship the relationship
     * @param ref          the reference
     * @return the source or target, or <tt>null</tt>
     */
    private Act getSourceOrTarget(ActRelationship relationship,
                                  IMObjectReference ref) {
        IMObjectReference target = relationship.getTarget();
        IMObjectReference child = null;
        if (target != null && !target.equals(ref)) {
            child = target;
        } else {
            IMObjectReference source = relationship.getSource();
            if (source != null && !source.equals(ref)) {
                child = source;
            }
        }
        if (child != null) {
            return (Act) resolve(child);
        }
        return null;
    }

}
