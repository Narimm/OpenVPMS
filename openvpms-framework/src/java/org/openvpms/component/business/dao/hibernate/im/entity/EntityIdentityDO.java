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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface EntityIdentityDO extends IMObjectDO {
    /**
     * Returns the identity.
     *
     * @return the identity
     */
    String getIdentity();

    /**
     * Sets the identity.
     *
     * @param identity the identity
     */
    void setIdentity(String identity);

    /**
     * Returns the entity that has the identity.
     *
     * @return the entity
     */
    EntityDO getEntity();

    /**
     * Sets the entity that has the identity.
     *
     * @param entity the entity
     */
    void setEntity(EntityDO entity);
}
