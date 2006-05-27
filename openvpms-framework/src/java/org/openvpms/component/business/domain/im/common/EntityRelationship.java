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

// java core
import java.util.Date;

// openvpms-framework
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Describes the relationship between two entities.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityRelationship extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates the active end time for this relationship
     */
    private Date activeEndTime;

    /**
     * Indicates the active start time for this relationship
     */
    private Date activeStartTime;

    /**
     * Records details of the relationship between the entities.
     */
    private DynamicAttributeMap details;

    /**
     * A relationship may also have an associated entity identity
     */
    private EntityIdentity identity;

    /**
     * TODO Definition for reason
     */
    private String reason;

    /**
     * TODO Definition for sequence
     */
    private int sequence;

    /**
     * Record the source entity reference in the relationship
     */
    private IMObjectReference source;

    /**
     * Record the target entity reference in the relationship
     */
    private IMObjectReference target;

    /**
     * Default constructor
     */
    public EntityRelationship() {
        // do nothing
    }

    /**
     * Constructs a valid intance of an entity relationship
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param source
     *            the relationship source
     * @param target
     *            the relationship target
     * @param details
     *            The details of the address object
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    public EntityRelationship(ArchetypeId archetypeId,
            IMObjectReference source, IMObjectReference target,
            DynamicAttributeMap details) {
        super(archetypeId);

        this.source = source;
        this.target = target;
        this.details = details;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        EntityRelationship copy = (EntityRelationship) super.clone();
        copy.activeEndTime = (Date) (this.activeEndTime == null ? null
                : this.activeEndTime.clone());
        copy.activeStartTime = (Date) (this.activeStartTime == null ? null
                : this.activeStartTime.clone());
        copy.details = (DynamicAttributeMap) (this.details == null ? null
                : this.details.clone());
        copy.identity = this.identity;
        copy.reason = this.reason;
        copy.sequence = this.sequence;
        copy.source = (IMObjectReference) this.source.clone();
        copy.target = (IMObjectReference) this.target.clone();

        return copy;
    }

    /**
     * @return Returns the activeEndTime.
     */
    public Date getActiveEndTime() {
        return activeEndTime;
    }

    /**
     * @return Returns the activeStartTime.
     */
    public Date getActiveStartTime() {
        return activeStartTime;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @return Returns the entityIdentity.
     */
    public EntityIdentity getIdentity() {
        return identity;
    }

    /**
     * @return Returns the reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * @return Returns the sequence.
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @return Returns the sourceEntity.
     */
    public IMObjectReference getSource() {
        return source;
    }

    /**
     * @return Returns the target.
     */
    public IMObjectReference getTarget() {
        return target;
    }

    /**
     * @param activeEndTime
     *            The activeEndTime to set.
     */
    public void setActiveEndTime(Date activeEndTime) {
        this.activeEndTime = activeEndTime;
    }

    /**
     * @param activeStartTime
     *            The activeStartTime to set.
     */
    public void setActiveStartTime(Date activeStartTime) {
        this.activeStartTime = activeStartTime;
    }

    /**
     * @param details
     *            The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /**
     * @param identity
     *            The identity to set.
     */
    public void setIdentity(EntityIdentity identity) {
        this.identity = identity;
    }

    /**
     * @param reason
     *            The reason to set.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @param sequence
     *            The sequence to set.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * @param source
     *            The source to set.
     */
    public void setSource(IMObjectReference source) {
        this.source = source;
    }

    /**
     * @param target
     *            The target to set.
     */
    public void setTarget(IMObjectReference target) {
        this.target = target;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(null)
            .append("source", source)
            .append("target", target)
            .append("sequence", sequence)
            .append("identity", identity)
            .toString();
    }
}
