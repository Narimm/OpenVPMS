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

package org.openvpms.component.model.act;

import org.openvpms.component.model.object.IMObject;

import java.util.Date;
import java.util.Set;

/**
 * An act represents an activity that is being done, has been done, can be done, or is intended or requested to be done.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Act extends IMObject {

    /**
     * Returns the activity start time.
     *
     * @return the start time. May be {@code null}.
     */
    Date getActivityStartTime();

    /**
     * Sets the activity start time.
     *
     * @param time the start time. May be {@code null}
     */
    void setActivityStartTime(Date time);

    /**
     * Returns the activity end time.
     *
     * @return the end time. May be {@code null}.
     */
    Date getActivityEndTime();

    /**
     * Sets the activity end time.
     *
     * @param time the end time. May be {@code null}
     */
    void setActivityEndTime(Date time);

    /**
     * Returns the reason for the activity.
     *
     * @return the reason. May be {@code null}
     */
    String getReason();

    /**
     * Sets the reason for the activity.
     *
     * @param reason the reason. May be {@code null}
     */
    void setReason(String reason);

    /**
     * Returns the status of the activity.
     *
     * @return the status. May be {@code null}
     */
    String getStatus();

    /**
     * Sets the status of the activity.
     *
     * @param status the status. May be {@code null}
     */
    void setStatus(String status);

    /**
     * Returns the secondary status of the activity.
     *
     * @return the secondary status. May be {@code null}
     */
    String getStatus2();

    /**
     * Sets the secondary status of the activity.
     *
     * @param status2 the secondary status. May be {@code null}
     */
    void setStatus2(String status2);

    /**
     * Returns the activity title.
     *
     * @return the title. May be {@code null}
     */
    String getTitle();

    /**
     * Sets the activity title.
     *
     * @param title the title. May be {@code null}
     */
    void setTitle(String title);

    /**
     * Returns the identities for this activity.
     *
     * @return the identities
     */
    Set<ActIdentity> getIdentities();

    /**
     * Adds an identity.
     *
     * @param identity the entity identity to add
     */
    void addIdentity(ActIdentity identity);

    /**
     * Removes an identity.
     *
     * @param identity the identity to remove
     */
    void removeIdentity(ActIdentity identity);

    /**
     * Returns the relationships where this activity is the source.
     *
     * @return the relationships
     */
    Set<ActRelationship> getSourceActRelationships();

    /**
     * Add a relationship where this activity is the source.
     *
     * @param relationship the relationship to add
     */
    void addSourceActRelationship(ActRelationship relationship);

    /**
     * Removes a relationship where this activity is the source.
     *
     * @param relationship the relationship to remove
     */
    void removeSourceActRelationship(ActRelationship relationship);

    /**
     * Returns the relationships where this activity is the target.
     *
     * @return the relationships
     */
    Set<ActRelationship> getTargetActRelationships();

    /**
     * Add a relationship where this activity is the target.
     *
     * @param relationship the relationship to add
     */
    void addTargetActRelationship(ActRelationship relationship);

    /**
     * Removes a relationship where this activity is the target.
     *
     * @param relationship the relationship to remove
     */
    void removeTargetActRelationship(ActRelationship relationship);

    /**
     * Adds a relationship between this activity and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #addSourceActRelationship} or {@link #addTargetActRelationship} accordingly.
     *
     * @param relationship the relationship to add
     */
    void addActRelationship(ActRelationship relationship);

    /**
     * Remove a relationship between this activity and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #removeSourceActRelationship} or {@link #removeTargetActRelationship} accordingly.
     *
     * @param relationship the act relationship to remove
     */
    void removeActRelationship(ActRelationship relationship);

    /**
     * Return all the relationships that the activity has.
     * <p>
     * NOTE: the returned set cannot be used to add or remove relationships.
     *
     * @return the relationships
     */
    Set<ActRelationship> getActRelationships();

    /**
     * Returns the participation relationships for the activity.
     * <p>
     * These determine the entities participating in the activity.
     *
     * @return the participation relationhsips
     */
    Set<Participation> getParticipations();

    /**
     * Adds a participation relationship.
     *
     * @param participation the participation to add
     */
    void addParticipation(Participation participation);

    /**
     * Removes a participation relationship.
     *
     * @param participation the participation to remove
     */
    void removeParticipation(Participation participation);

}
