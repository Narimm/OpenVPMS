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

package org.openvpms.component.business.domain.im.act;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * A class representing the various internal and external identifiers for an act.
 *
 * @author Tim Anderson
 */
public class ActIdentity extends IMObject implements org.openvpms.component.model.act.ActIdentity {

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The identity.
     */
    private String identity;

    /**
     * The parent act.
     */
    private Act act;

    /**
     * Default constructor.
     */
    public ActIdentity() {
        // do nothing
    }

    /**
     * Constructs an {@link ActIdentity}.
     *
     * @param archetypeId the archetype id
     * @param identity    the identity
     */
    public ActIdentity(ArchetypeId archetypeId, String identity) {
        super(archetypeId);
        setIdentity(identity);
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
     * Returns the parent act.
     *
     * @return the act
     */
    public Act getAct() {
        return act;
    }

    /**
     * Sets the parent act.
     *
     * @param act the act
     */
    public void setAct(Act act) {
        this.act = act;
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
