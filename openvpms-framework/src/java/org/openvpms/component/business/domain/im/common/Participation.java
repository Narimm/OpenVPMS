/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.domain.im.common;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.model.object.Reference;

import java.util.Map;


/**
 * A class representing an {@link Entity}'s participantion in an {@link Act}.
 *
 * @author Jim Alateras
 */
public class Participation extends IMObject implements org.openvpms.component.model.act.Participation {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 3L;

    /**
     * Reference to the associated entity
     */
    private IMObjectReference entity;

    /**
     * Reference to the associated act
     */
    private IMObjectReference act;


    /**
     * Default constructor.
     */
    public Participation() {
        // do nothing
    }

    /**
     * Constructs a participantion between an {@link Entity} and an {@link Act}.
     *
     * @param archetypeId the archetype id constraining this object
     * @param entity      the entity in the participation
     * @param act         the act that this participation is associated with
     * @param details     holds dynamic details about the participation.
     */
    public Participation(ArchetypeId archetypeId, IMObjectReference entity,
                         IMObjectReference act, Map<String, Object> details) {
        super(archetypeId);
        this.act = act;
        this.entity = entity;
        if (details != null) {
            setDetails(details);
        }
    }

    /**
     * @return Returns the act.
     */
    public IMObjectReference getAct() {
        return act;
    }

    /**
     * @return Returns the entity.
     */
    public IMObjectReference getEntity() {
        return entity;
    }

    /**
     * @param act The act to set.
     */
    public void setAct(IMObjectReference act) {
        this.act = act;
    }

    /**
     * Sets the act reference.
     * <p>
     * This is synonymous with {@link #setSource(Reference)}.
     *
     * @param act the act reference
     */
    @Override
    public void setAct(Reference act) {
        setAct((IMObjectReference) act);
    }

    /**
     * @param entity The entity to set.
     */
    public void setEntity(IMObjectReference entity) {
        this.entity = entity;
    }

    /**
     * Sets the entity reference.
     * <p>
     * This is synonymous with {@link #setTarget(Reference)}.
     *
     * @param entity the entity reference
     */
    @Override
    public void setEntity(Reference entity) {
        setEntity((IMObjectReference) entity);
    }

    /**
     * Returns a reference to the source object.
     *
     * @return the source object reference
     */
    @Override
    public Reference getSource() {
        return getAct();
    }

    /**
     * Sets the source object reference.
     *
     * @param source the source object reference
     */
    @Override
    public void setSource(Reference source) {
        setAct(source);
    }

    /**
     * Returns a reference to the target object.
     *
     * @return the target object reference
     */
    @Override
    public Reference getTarget() {
        return getEntity();
    }

    /**
     * Sets the target object reference.
     *
     * @param target the target object reference
     */
    @Override
    public void setTarget(Reference target) {
        setEntity(target);
    }
}
