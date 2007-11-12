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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.domain.im.common;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValueMap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A class representing an {@link Entity}'s participantion in an {@link Act}.
 * 
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Participation extends IMObject {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * An integer representing the relative order of the participantion in
     * relation to other participations of the same act.
     */
    private int sequence;

    /**
     * Indicates the mode that the {@link Entity} is participating in the
     * {@link Act}
     *
     * TODO Look at incorporating with the terminology service
     */
    private String mode;

    /**
     * The time that this participation was activitated
     */
    private Date activeStartTime;

    /**
     * The time that this participation was inactivated
     */
    private Date activeEndTime;

    /**
     * The percentage of participation in the specified {@link Act}.
     */
    private int percentage;

    /**
     * Reference to the associated entity
     */
    private IMObjectReference entity;

    /**
     * Reference to the associated act
     */
    private IMObjectReference act;

    /**
     * Holds details about the participation.
     */
    private Map<String, TypedValue> details = new HashMap<String, TypedValue>();


    /**
     * Default constructor.
     */
    public Participation() {
        // do nothing
    }

    /**
     * Constructs a participantion between an {@link Entity} and an {@link Act}.
     *
     * @param archetypeId
     *            the archetype id constraining this object
     * @param entity
     *            the entity in the participation
     * @param act
     *            the act that this participation is associated with
     * @param details
     *            holds dynamic details about the participation.
     */
    public Participation(ArchetypeId archetypeId, IMObjectReference entity,
                         IMObjectReference act, Map<String, Object> details) {
        super(archetypeId);
        this.act = act;
        this.entity = entity;
        this.details = TypedValueMap.create(details);
    }

    /**
     * @return Returns the details.
     */
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(Map<String, Object> details) {
        this.details = TypedValueMap.create(details);
    }

    /**
     * @return Returns the mode.
     * @deprecated no replacement
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode The mode to set.
     * @deprecated no replacement
     */
    @Deprecated
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return Returns the sequence.
     * @deprecated no replacement
     */
    @Deprecated
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence The sequence to set.
     * @deprecated no replacement
     */
    @Deprecated
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * @return Returns the activeEndTime.
     * @deprecated no replacement
     */
    @Deprecated
    public Date getActiveEndTime() {
        return activeEndTime;
    }

    /**
     * @param activeEndTime The activeEndTime to set.
     * @deprecated no replacement
     */
    @Deprecated
    public void setActiveEndTime(Date activeEndTime) {
        this.activeEndTime = activeEndTime;
    }

    /**
     * @return <tt>null</tt>
     * @deprecated no replacement
     */
    @Deprecated
    public Date getActiveStartTime() {
        return activeStartTime;
    }

    /**
     * @param activeStartTime The activeStartTime to set.
     */
    public void setActiveStartTime(Date activeStartTime) {
        this.activeStartTime = activeStartTime;
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
     * @return Returns the percentage.
     * @deprecated no replacement
     */
    @Deprecated
    public int getPercentage() {
        return percentage;
    }

    /**
     * @param percentage The percentage to set.
     * @deprecated no replacement
     */
    @Deprecated
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    /**
     * @param act The act to set.
     */
    public void setAct(IMObjectReference act) {
        this.act = act;
    }

    /**
     * @param entity The entity to set.
     */
    public void setEntity(IMObjectReference entity) {
        this.entity = entity;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Participation copy = (Participation)super.clone();
        copy.act = this.act;
        copy.activeEndTime = (Date)(this.activeEndTime == null ?
                null : this.activeEndTime.clone());
        copy.activeStartTime = (Date)(this.activeStartTime == null ?
                null : this.activeStartTime.clone());
        copy.details = (this.details == null) ? null
                : new HashMap<String, TypedValue>(details);
        copy.entity = this.entity;
        copy.mode = this.mode;
        copy.percentage = this.percentage;
        copy.sequence = this.sequence;

        return copy;
    }
}
