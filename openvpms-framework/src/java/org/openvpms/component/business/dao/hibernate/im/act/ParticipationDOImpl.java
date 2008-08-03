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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;

import java.util.Date;


/**
 * Implementation of the {@link ParticipationDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-11-12 14:15:48 +1100 (Mon, 12 Nov 2007) $
 */
public class ParticipationDOImpl extends IMObjectDOImpl
        implements ParticipationDO {

    /**
     * The entity.
     */
    private EntityDO entity;

    /**
     * The act.
     */
    private ActDO act;

    /**
     * The act start time.
     */
    private Date startTime;

    /**
     * The act end time.
     */
    private Date endTime;


    /**
     * Default constructor.
     */
    public ParticipationDOImpl() {
        // do nothing
    }

    /**
     * Returns the entity.
     *
     * @return the entity
     */
    public EntityDO getEntity() {
        return entity;
    }

    /**
     * Sets the entity.
     *
     * @param entity the entity to set
     */
    public void setEntity(EntityDO entity) {
        this.entity = entity;
    }

    /**
     * Returns the act.
     *
     * @return the act
     */
    public ActDO getAct() {
        return act;
    }

    /**
     * Sets the act.
     *
     * @param act the act to set
     */
    public void setAct(ActDO act) {
        this.act = act;
    }

    /**
     * Returns the act start time.
     *
     * @return the act start time. May be <tt>null</tt>
     */
    public Date getActivityStartTime() {
        return startTime;
    }

    /**
     * Sets the act start time.
     *
     * @param startTime the start time to set. May be <tt>null</tt>
     */
    public void setActivityStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the act end time.
     *
     * @return the end time. May be <tt>null</tt>
     */
    public Date getActivityEndTime() {
        return endTime;
    }

    /**
     * Sets the act end time.
     *
     * @param endTime the end time to set. May be <tt>null</tt>
     */
    public void setActivityEndTime(Date endTime) {
        this.endTime = endTime;
    }

}
