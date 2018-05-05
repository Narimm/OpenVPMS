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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;

import java.util.Date;
import java.util.Set;


/**
 * Data object interface corresponding to the {@link Act} class.
 *
 * @author Tim Anderson
 */
public interface ActDO extends IMObjectDO {

    /**
     * Returns the title.
     *
     * @return the title. May be {@code null}
     */
    String getTitle();

    /**
     * Sets the title.
     *
     * @param title the title. May be {@code null}
     */
    void setTitle(String title);

    /**
     * Returns the act start time.
     *
     * @return the act start time. May be {@code null}
     */
    Date getActivityStartTime();

    /**
     * Sets the act start time.
     *
     * @param activityStartTime the start time. May be {@code null}
     */
    void setActivityStartTime(Date activityStartTime);

    /**
     * Returns the act end time.
     *
     * @return the end time. May be {@code null}
     */
    Date getActivityEndTime();

    /**
     * Sets the act end time.
     *
     * @param activityEndTime the end time. May be {@code null}
     */
    void setActivityEndTime(Date activityEndTime);

    /**
     * Returns the reason for the act.
     *
     * @return the reason. May be {@code null}
     */
    String getReason();

    /**
     * Sets the reason for the act.
     *
     * @param reason the reason. May be {@code null}
     */
    void setReason(String reason);

    /**
     * Returns the act status.
     *
     * @return the act status. May be {@code null}
     */
    String getStatus();

    /**
     * Sets the act status.
     *
     * @param status the act status. May be {@code null}
     */
    void setStatus(String status);

    /**
     * Returns the secondary act status.
     *
     * @return the act status. May be {@code null}
     */
    String getStatus2();

    /**
     * Sets the secondary act status.
     *
     * @param status the secondary act status. May be {@code null}
     */
    void setStatus2(String status);

    /**
     * Adds an identity.
     *
     * @param identity the entity identity to add
     */
    void addIdentity(ActIdentityDO identity);

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
    Set<ActIdentityDO> getIdentities();

    /**
     * Returns the relationships to other acts where this is the source.
     *
     * @return the source relationships
     */
    Set<ActRelationshipDO> getSourceActRelationships();

    /**
     * Adds a relationship where this is the source.
     *
     * @param source the relationship
     */
    void addSourceActRelationship(ActRelationshipDO source);

    /**
     * Removes a source relationship.
     *
     * @param source the relationship to remove
     */
    void removeSourceActRelationship(ActRelationshipDO source);

    /**
     * Returns the relationships to other acts where this is the target.
     *
     * @return the target relationships
     */
    Set<ActRelationshipDO> getTargetActRelationships();

    /**
     * Adds a relationship where this is the target.
     *
     * @param target the relationship
     */
    void addTargetActRelationship(ActRelationshipDO target);

    /**
     * Removes a target relationship.
     *
     * @param target the relationship to remove
     */
    void removeTargetActRelationship(ActRelationshipDO target);

    /**
     * Returns the act participations.
     *
     * @return the participations
     */
    Set<ParticipationDO> getParticipations();

    /**
     * Adds a participation.
     *
     * @param participation the participation to add
     */
    void addParticipation(ParticipationDO participation);

    /**
     * Removes a participation.
     *
     * @param participation the participation to remove
     */
    void removeParticipation(ParticipationDO participation);
}
