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


package org.openvpms.component.business.domain.im.act;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.im.common.EntityException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.model.object.Reference;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * A class representing an activity that is being done, has been done,
 * can be done, or is intended or requested to be done.  An Act instance
 * is a record of an intentional business action.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class Act extends IMObject implements org.openvpms.component.model.act.Act {

    /**
     * The serialization version identifier.
     */
    private static final long serialVersionUID = 4L;

    /**
     * Represents the title of the act.
     */
    private String title;

    /**
     * The start time of this act
     */
    private Date activityStartTime;

    /**
     * The end time of this activity
     */
    private Date activityEndTime;

    /**
     * Text representing the reason for the Act. Often this is beter
     * represented by a realtionship to another Act of type "has reason".
     */
    private String reason;

    /**
     * A String representing the status or state of the Act. (i.e  Normal,
     * Aborted, Completed, Suspended, Cancelled etc
     */
    private String status;

    /**
     * A secondary status of the act.
     */
    private String status2;

    /**
     * The identities of this.
     */
    private Set<org.openvpms.component.model.act.ActIdentity> identities = new HashSet<>();

    /**
     * The {@link Participation}s for this act.
     */
    private Set<org.openvpms.component.model.act.Participation> participations = new HashSet<>();

    /**
     * Holds all the {@link ActRelationship}s that this act is a source off.
     */
    private Set<org.openvpms.component.model.act.ActRelationship> sourceActRelationships = new HashSet<>();

    /**
     * Holds all the {@link ActRelationship}s that this act is a target off.
     */
    private Set<org.openvpms.component.model.act.ActRelationship> targetActRelationships = new HashSet<>();

    /**
     * Default constructor.
     */
    public Act() {
        // do nothing
    }

    /**
     * @return Returns the activityEndTime.
     */
    public Date getActivityEndTime() {
        return activityEndTime;
    }

    /**
     * @param time The activityEndTime to set.
     */
    public void setActivityEndTime(Date time) {
        this.activityEndTime = time;
    }

    /**
     * @return Returns the activityStartTime.
     */
    public Date getActivityStartTime() {
        return activityStartTime;
    }

    /**
     * @param time The activityStartTime to set.
     */
    public void setActivityStartTime(Date time) {
        this.activityStartTime = time;
    }

    /**
     * @return Returns the reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason The reason to set.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the secondary status.
     *
     * @return the secondary status. May be {@code null}
     */
    public String getStatus2() {
        return status2;
    }

    /**
     * Sets the secondary status.
     *
     * @param status2 the secondary status. May be {@code null}
     */
    public void setStatus2(String status2) {
        this.status2 = status2;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Adds an identity.
     *
     * @param identity the entity identity to add
     */
    public void addIdentity(org.openvpms.component.model.act.ActIdentity identity) {
        ((ActIdentity) identity).setAct(this);
        identities.add(identity);
    }

    /**
     * Removes an identity.
     *
     * @param identity the identity to remove
     * @return {@code true} if the identity was removed
     */
    public void removeIdentity(org.openvpms.component.model.act.ActIdentity identity) {
        ((ActIdentity) identity).setAct(null);
        identities.remove(identity);
    }

    /**
     * Returns the identities.
     *
     * @return the identities
     */
    public Set<org.openvpms.component.model.act.ActIdentity> getIdentities() {
        return identities;
    }

    /**
     * @return Returns the sourceActRelationships.
     */
    public Set<org.openvpms.component.model.act.ActRelationship> getSourceActRelationships() {
        return sourceActRelationships;
    }


    /**
     * @param sourceActRelationships The sourceActRelationships to set.
     */
    public void setSourceActRelationships(
            Set<org.openvpms.component.model.act.ActRelationship> sourceActRelationships) {
        this.sourceActRelationships = sourceActRelationships;
    }

    /**
     * Add a source {@link ActRelationship}.
     *
     * @param relationship
     */
    public void addSourceActRelationship(org.openvpms.component.model.act.ActRelationship relationship) {
        this.sourceActRelationships.add(relationship);
    }

    /**
     * Remove a source {@link ActRelationship}.
     *
     * @param relationship
     */
    public void removeSourceActRelationship(org.openvpms.component.model.act.ActRelationship relationship) {
        this.sourceActRelationships.remove(relationship);
    }

    /**
     * @return Returns the targetActRelationships.
     */
    public Set<org.openvpms.component.model.act.ActRelationship> getTargetActRelationships() {
        return targetActRelationships;
    }

    /**
     * Set this act to be a targt of an {@link ActRelationship}.
     *
     * @param targetActRelationships The targetActRelationships to set.
     */
    public void setTargetActRelationships(
            Set<org.openvpms.component.model.act.ActRelationship> targetActRelationships) {
        this.targetActRelationships = targetActRelationships;
    }

    /**
     * Add a target {@link ActRelationship}.
     *
     * @param relationship add a new target.
     */
    public void addTargetActRelationship(org.openvpms.component.model.act.ActRelationship relationship) {
        this.targetActRelationships.add(relationship);
    }

    /**
     * Remove a target {@link ActRelationship}.
     *
     * @param relationship
     */
    public void removeTargetActRelationship(org.openvpms.component.model.act.ActRelationship relationship) {
        this.targetActRelationships.remove(relationship);
    }

    /**
     * Add a relationship to this act. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param relationship the act relationship to add
     * @throws EntityException if this relationship cannot be added to this act
     */
    public void addActRelationship(org.openvpms.component.model.act.ActRelationship relationship) {
        if ((relationship.getSource().getLinkId().equals(getLinkId())) &&
            (relationship.getSource().getArchetype().equals(getArchetype()))) {
            addSourceActRelationship(relationship);
        } else if ((relationship.getTarget().getLinkId().equals(this.getLinkId())) &&
                   (relationship.getTarget().getArchetype().equals(this.getArchetype()))) {
            addTargetActRelationship(relationship);
        } else {
            throw new EntityException(
                    EntityException.ErrorCode.FailedToAddActRelationship,
                    new Object[]{relationship.getSource(), relationship.getTarget()});
        }
    }

    /**
     * Remove a relationship to this act. It will determine whether it is a
     * source or target relationship before removing it.
     *
     * @param relationship the act relationship to remove
     * @throws EntityException if this relationship cannot be removed from this act
     */
    public void removeActRelationship(org.openvpms.component.model.act.ActRelationship relationship) {
        Reference source = relationship.getSource();
        Reference target = relationship.getTarget();
        if (source.getLinkId().equals(getLinkId()) && source.getArchetype().equals(getArchetype())) {
            removeSourceActRelationship(relationship);
        } else if (target.getLinkId().equals(getLinkId()) && target.getArchetype().equals(getArchetype())) {
            removeTargetActRelationship(relationship);
        } else {
            throw new EntityException(
                    EntityException.ErrorCode.FailedToRemoveActRelationship,
                    new Object[]{source, target});
        }
    }

    /**
     * Return all the act relationships.
     *
     * @return Set<ActRelationship>
     */
    public Set<org.openvpms.component.model.act.ActRelationship> getActRelationships() {
        Set<org.openvpms.component.model.act.ActRelationship> relationships = new HashSet<>(sourceActRelationships);
        relationships.addAll(targetActRelationships);

        return relationships;
    }

    /**
     * Return the associated {@link Participation} instances.
     *
     * @return Participation
     */
    public Set<org.openvpms.component.model.act.Participation> getParticipations() {
        return participations;
    }

    /**
     * @param participations The participations to set.
     */
    public void setParticipations(Set<org.openvpms.component.model.act.Participation> participations) {
        this.participations = participations;
    }

    /**
     * Add a {@link Participation}.
     *
     * @param participation
     */
    public void addParticipation(org.openvpms.component.model.act.Participation participation) {
        participations.add(participation);
    }

    /**
     * Remove a {@link Participation}.
     *
     * @param participation
     */
    public void removeParticipation(org.openvpms.component.model.act.Participation participation) {
        participations.remove(participation);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Act copy = (Act) super.clone();
        copy.identities = new HashSet<>(identities);
        copy.participations = new HashSet<>(participations);
        copy.sourceActRelationships = new HashSet<>(sourceActRelationships);
        copy.targetActRelationships = new HashSet<>(targetActRelationships);
        return copy;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this, STYLE)
                .appendSuper(super.toString())
                .append("title", title)
                .append("activityStartTime", activityStartTime)
                .append("activityEndTime", activityEndTime)
                .append("reason", reason)
                .append("status", status)
                .append("identities", identities)
                .append("participations", participations)
                .append("sourceActRelationships", sourceActRelationships)
                .append("targetActRelationships", targetActRelationships)
                .toString();
    }

}
