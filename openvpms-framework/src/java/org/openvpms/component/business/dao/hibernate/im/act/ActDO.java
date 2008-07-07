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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A class representing an activity that is being done, has been done,
 * can be done, or is intended or requested to be done.  An Act instance
 * is a record of an intentional business action.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-07-10 15:32:07 +1000 (Tue, 10 Jul 2007) $
 */
public class ActDO extends IMObjectDO {

    /**
     * The act title.
     */
    private String title;

    /**
     * The start time of this act.
     */
    private Date startTime;

    /**
     * The end time of this act.
     */
    private Date endTime;

    /**
     * The reason for the act.
     */
    private String reason;

    /**
     * The status of the act.
     */
    private String status;

    /**
     * The relationships to other acts where this is the source.
     */
    private Set<ActRelationshipDO> sourceActRelationships =
            new HashSet<ActRelationshipDO>();

    /**
     * The relationships to other acts where this is the target.
     */
    private Set<ActRelationshipDO> targetActRelationships =
            new HashSet<ActRelationshipDO>();

    /**
     * The participations for this act.
     */
    private Set<ParticipationDO> participations =
            new HashSet<ParticipationDO>();


    /**
     * Default constructor.
     */
    public ActDO() {
        // do nothing
    }

    /**
     * Returns the title.
     *
     * @return the title. May be <tt>null</tt>
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title. May be <tt>null</tt>
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the act start time.
     *
     * @return the act start time. May be <tt>null</tt>
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the act start time.
     *
     * @param startTime the start time. May be <tt>null</tt>
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the act end time.
     *
     * @return the end time. May be <tt>null</tt>
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the act end time.
     *
     * @param endTime the end time. May be <tt>null</tt>
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the reason for the act.
     *
     * @return the reason. May be <tt>null</tt>
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason for the act.
     *
     * @param reason the reason. May be <tt>null</tt>
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the act status.
     *
     * @return the act status. May be <tt>null</tt>
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the act status.
     *
     * @param status the act status. May be <tt>null</tt>
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the relationships to other acts where this is the source.
     *
     * @return the source relationships
     */
    public Set<ActRelationshipDO> getSourceActRelationships() {
        return sourceActRelationships;
    }

    /**
     * Adds a relationship where this is the source.
     *
     * @param source the relationship
     */
    public void addSourceActRelationship(ActRelationshipDO source) {
        sourceActRelationships.add(source);
        source.setSource(this);
    }

    /**
     * Removes a source relationship.
     *
     * @param source the relationship to remove
     */
    public void removeSourceActRelationship(ActRelationshipDO source) {
        sourceActRelationships.remove(source);
    }

    /**
     * Returns the relationships to other acts where this is the target.
     *
     * @return the target relationships
     */
    public Set<ActRelationshipDO> getTargetActRelationships() {
        return targetActRelationships;
    }

    /**
     * Adds a relationship where this is the target.
     *
     * @param target the relationship
     */
    public void addTargetActRelationship(ActRelationshipDO target) {
        targetActRelationships.add(target);
        target.setTarget(this);
    }

    /**
     * Removes a target relationship.
     *
     * @param target the relationship to remove
     */
    public void removeTargetActRelationship(ActRelationshipDO target) {
        targetActRelationships.remove(target);
    }

    /**
     * Returns the act participations.
     *
     * @return the participations
     */
    public Set<ParticipationDO> getParticipations() {
        return participations;
    }

    /**
     * Adds a participation.
     *
     * @param participation the participation to add
     */
    public void addParticipation(ParticipationDO participation) {
        participations.add(participation);
        participation.setAct(this);
    }

    /**
     * Removes a participation.
     *
     * @param participation the participation to remove
     */
    public void removeParticipation(ParticipationDO participation) {
        participations.remove(participation);
    }

    /**
     * Sets the relationships where this is the source.
     *
     * @param relationships the relationships
     */
    protected void setSourceActRelationships(
            Set<ActRelationshipDO> relationships) {
        sourceActRelationships = relationships;
    }

    /**
     * Sets the relationships where this is the target.
     *
     * @param relationships the relationships
     */
    protected void setTargetActRelationships(
            Set<ActRelationshipDO> relationships) {
        targetActRelationships = relationships;
    }

    /**
     * Sets the act participations.
     *
     * @param participations the participations
     */
    protected void setParticipations(Set<ParticipationDO> participations) {
        this.participations = participations;
    }

}
