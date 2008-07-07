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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;

/**
 * A class representing an {@link Entity}'s participantion in an {@link Act}.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-11-12 14:15:48 +1100 (Mon, 12 Nov 2007) $
 */
public class ParticipationDO extends IMObjectDO {

    /**
     * The entity.
     */
    private EntityDO entity;

    /**
     * The act.
     */
    private ActDO act;


    /**
     * Default constructor.
     */
    public ParticipationDO() {
        // do nothing
    }

    /**
     * @return Returns the entity.
     */
    public EntityDO getEntity() {
        return entity;
    }

    /**
     * @param entity The entity to set.
     */
    public void setEntity(EntityDO entity) {
        this.entity = entity;
    }

    /**
     * @return Returns the act.
     */
    public ActDO getAct() {
        return act;
    }

    /**
     * @param act The act to set.
     */
    public void setAct(ActDO act) {
        this.act = act;
    }

}
