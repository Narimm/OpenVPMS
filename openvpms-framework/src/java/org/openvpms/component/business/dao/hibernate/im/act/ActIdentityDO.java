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


/**
 * Data object interface corresponding to the {@link ActIdentityDO} class.
 *
 * @author Tim Anderson
 */
public interface ActIdentityDO extends IMObjectDO {

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
     * Returns the act that has the identity.
     *
     * @return the entity
     */
    ActDO getAct();

    /**
     * Sets the act that has the identity.
     *
     * @param act the act
     */
    void setAct(ActDO act);
}
