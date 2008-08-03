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
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;
import org.openvpms.component.business.domain.im.common.Participation;

import java.util.Date;


/**
 * Data object interface corresponding to the {@link Participation} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ParticipationDO extends IMObjectDO {

    /**
     * Returns the entity.
     *
     * @return the entity
     */
    EntityDO getEntity();

    /**
     * Sets the entity.
     *
     * @param entity the entity to set
     */
    void setEntity(EntityDO entity);

    /**
     * Returns the act.
     *
     * @return the act
     */
    ActDO getAct();

    /**
     * Sets the act.
     *
     * @param act the act to set
     */
    void setAct(ActDO act);

    /**
     * Returns the act start time.
     *
     * @return the act start time. May be <tt>null</tt>
     */
    Date getActivityStartTime();

    /**
     * Sets the act start time.
     *
     * @param startTime the start time to set. May be <tt>null</tt>
     */
    void setActivityStartTime(Date startTime);

    /**
     * Returns the act end time.
     *
     * @return the end time. May be <tt>null</tt>
     */
    Date getActivityEndTime();

    /**
     * Sets the act end time.
     *
     * @param endTime the end time to set. May be <tt>null</tt>
     */
    void setActivityEndTime(Date endTime);
}
