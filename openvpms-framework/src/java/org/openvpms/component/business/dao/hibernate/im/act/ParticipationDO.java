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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ParticipationDO extends IMObjectDO {
    /**
     * @return Returns the entity.
     */
    EntityDO getEntity();

    /**
     * @param entity The entity to set.
     */
    void setEntity(EntityDO entity);

    /**
     * @return Returns the act.
     */
    ActDO getAct();

    /**
     * @param act The act to set.
     */
    void setAct(ActDO act);
}
