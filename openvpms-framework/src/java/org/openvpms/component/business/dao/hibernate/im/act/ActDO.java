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
import org.openvpms.component.business.domain.im.act.Act;

import java.util.Date;
import java.util.Set;


/**
 * Data object interface corresponding to the {@link Act} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ActDO extends IMObjectDO {

    /**
     * Returns the title.
     *
     * @return the title. May be <tt>null</tt>
     */
    String getTitle();

    /**
     * Sets the title.
     *
     * @param title the title. May be <tt>null</tt>
     */
    void setTitle(String title);

    /**
     * Returns the act start time.
     *
     * @return the act start time. May be <tt>null</tt>
     */
    Date getActivityStartTime();

    /**
     * Sets the act start time.
     *
     * @param activityStartTime the start time. May be <tt>null</tt>
     */
    void setActivityStartTime(Date activityStartTime);

    /**
     * Returns the act end time.
     *
     * @return the end time. May be <tt>null</tt>
     */
    Date getActivityEndTime();

    /**
     * Sets the act end time.
     *
     * @param activityEndTime the end time. May be <tt>null</tt>
     */
    void setActivityEndTime(Date activityEndTime);

    /**
     * Returns the reason for the act.
     *
     * @return the reason. May be <tt>null</tt>
     */
    String getReason();

    /**
     * Sets the reason for the act.
     *
     * @param reason the reason. May be <tt>null</tt>
     */
    void setReason(String reason);

    /**
     * Returns the act status.
     *
     * @return the act status. May be <tt>null</tt>
     */
    String getStatus();

    /**
     * Sets the act status.
     *
     * @param status the act status. May be <tt>null</tt>
     */
    void setStatus(String status);

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
