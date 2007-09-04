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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;

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
     * Constructs a new <code>ActBean</code>.
     *
     * @param act the act
     */
    public ActBean(Act act) {
        this(act, null);
    }

    /**
     * Constructs a new <code>ActBean</code>.
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
     * Adds an act relationship.
     *
     * @param shortName the relationship short name
     * @param target    the target act
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ActRelationship addRelationship(String shortName, Act target) {
        Act act = getAct();
        ActRelationship r = (ActRelationship) getArchetypeService().create(
                shortName);
        if (r == null) {
            throw new IMObjectBeanException(ArchetypeNotFound, shortName);
        }
        r.setSource(act.getObjectReference());
        r.setTarget(target.getObjectReference());
        act.addActRelationship(r);
        return r;
    }

    /**
     * Returns the first act relationship with the specified act as a target.
     *
     * @param target the target act
     * @return the first act relationship with <code>target</code> as its target
     *         or <code>null</code> if none is found
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
     * Resolves and returns a list of the child acts.
     *
     * @return a list of the child acts
     * @throws ArchetypeServiceException for any archetype service
     */
    public List<Act> getActs() {
        List<Act> result = new ArrayList<Act>();
        Act act = getAct();
        for (ActRelationship r : act.getSourceActRelationships()) {
            IMObjectReference target = r.getTarget();
            if (target != null) {
                IArchetypeService service = getArchetypeService();
                Act child = (Act) ArchetypeQueryHelper.getByObjectReference(
                        service, target);
                if (child != null) {
                    result.add(child);
                }
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
                IArchetypeService service = getArchetypeService();
                Act child = (Act) ArchetypeQueryHelper.getByObjectReference(
                        service, target);
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
     */
    public List<Act> getActsForNode(String name) {
        List<IMObject> relationships = getValues(name);
        List<Act> result = new ArrayList<Act>();
        IMObjectReference ref = getObject().getObjectReference();
        for (IMObject object : relationships) {
            ActRelationship relationship = (ActRelationship) object;
            Act child = getSourceOrTarget(relationship, ref);
            if (child != null) {
                result.add(child);
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
     * @return the participation or <code>null</code> if none is found
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
     * Removes a participation.
     *
     * @param shortName the participation shortName
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
     * @return the entity, or <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getParticipant(String shortName) {
        Entity result = null;
        IMObjectReference ref = getParticipantRef(shortName);
        if (ref != null) {
            result = (Entity) ArchetypeQueryHelper.getByObjectReference(
                    getArchetypeService(), ref);
        }
        return result;
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
            IArchetypeService service = getArchetypeService();
            return (Act) ArchetypeQueryHelper.getByObjectReference(
                    service, child);
        }
        return null;
    }

}
