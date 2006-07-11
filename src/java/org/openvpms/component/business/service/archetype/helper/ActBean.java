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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.ActBeanException.ErrorCode.ArchetypeNotFound;


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
     * @param act the act
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
            throw new ActBeanException(ArchetypeNotFound, shortName);
        }
        r.setSource(act.getObjectReference());
        r.setTarget(target.getObjectReference());
        act.addSourceActRelationship(r);
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
            throw new ActBeanException(ArchetypeNotFound, shortName);
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
     *
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

}
