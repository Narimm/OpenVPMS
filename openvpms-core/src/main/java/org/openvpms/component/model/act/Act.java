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
import java.util.HashSet;
import java.util.Set;

/**
 * .
 *
 * @author Tim Anderson
 */
interface Act extends IMObject {

    /**
     * @return Returns the activityEndTime.
     */
    Date getActivityEndTime();

    /**
     * @param activityEndTime The activityEndTime to set.
     */
    void setActivityEndTime(Date activityEndTime);

    /**
     * @return Returns the activityStartTime.
     */
    Date getActivityStartTime();

    /**
     * @param activityStartTime The activityStartTime to set.
     */
    void setActivityStartTime(Date activityStartTime);

    /**
     * @return Returns the reason.
     */
    String getReason();

    /**
     * @param reason The reason to set.
     */
    void setReason(String reason);

    /**
     * @return Returns the status.
     */
    String getStatus();

    /**
     * @param status The status to set.
     */
    void setStatus(String status);

    /**
     * Returns the secondary status.
     *
     * @return the secondary status. May be {@code null}
     */
    String getStatus2();

    /**
     * Sets the secondary status.
     *
     * @param status2 the secondary status. May be {@code null}
     */
    void setStatus2(String status2);

    /**
     * @return Returns the title.
     */
    String getTitle();

    /**
     * @param title The title to set.
     */
    void setTitle(String title);

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
     * @return {@code true} if the identity was removed
     */
    boolean removeIdentity(ActIdentity identity);

    /**
     * Returns the identities.
     *
     * @return the identities
     */
    Set<ActIdentity> getIdentities();

    /**
     * @return Returns the sourceActRelationships.
     */
    Set<ActRelationship> getSourceActRelationships();

    /**
     * Add a source {@link ActRelationship}.
     *
     * @param source
     */
    void addSourceActRelationship(ActRelationship source);

    /**
     * Remove a source {@link ActRelationship}.
     *
     * @param source
     */
    void removeSourceActRelationship(ActRelationship source);

    /**
     * @return Returns the targetActRelationships.
     */
    Set<ActRelationship> getTargetActRelationships();

    /**
     * Add a target {@link ActRelationship}.
     *
     * @param target add a new target.
     */
    void addTargetActRelationship(ActRelationship target);

    /**
     * Remove a target {@link ActRelationship}.
     *
     * @param target
     */
    void removeTargetActRelationship(ActRelationship target);

    /**
     * Add a relationship to this act. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param actRel the act relationship to add
     */
    void addActRelationship(ActRelationship actRel);

    /**
     * Remove a relationship to this act. It will determine whether it is a
     * source or target relationship before removing it.
     *
     * @param actRel the act relationship to remove
     */
    void removeActRelationship(ActRelationship actRel);

    /**
     * Return all the act relationships. Do not use the returned set to
     * add and remove act relationships. Instead use {@link #addActRelationship(ActRelationship)}
     * and {@link #removeActRelationship(ActRelationship)} repsectively.
     *
     * @return Set<ActRelationship>
     */
    Set<ActRelationship> getActRelationships();

    /**
     * Return the associated {@link Participation} instances.
     *
     * @return Participation
     */
    Set<Participation> getParticipations();

    /**
     * Add a {@link Participation}.
     *
     * @param participation
     */
    void addParticipation(Participation participation);

    /**
     * Remove a {@link Participation}.
     *
     * @param participation
     */
    void removeParticipation(Participation participation);
    
}
