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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Implementation of the {@link ActIdentityDO} interface.
 *
 * @author Tim Anderson
 */
public class ActIdentityDOImpl extends IMObjectDOImpl implements ActIdentityDO {

    /**
     * The identity.
     */
    private String identity;

    /**
     * The act that has the identity.
     */
    private ActDO act;


    /**
     * Default constructor.
     */
    public ActIdentityDOImpl() {
        super();
    }

    /**
     * Constructs a {@link ActIdentityDOImpl}.
     *
     * @param archetypeId the archetype id
     */
    public ActIdentityDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the identity.
     *
     * @return the identity
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Sets the identity.
     *
     * @param identity the identity
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * Returns the act that has the identity.
     *
     * @return the act
     */
    @Override
    public ActDO getAct() {
        return act;
    }

    /**
     * Sets the act that has the identity.
     *
     * @param act the act
     */
    @Override
    public void setAct(ActDO act) {
        this.act = act;
    }
}
