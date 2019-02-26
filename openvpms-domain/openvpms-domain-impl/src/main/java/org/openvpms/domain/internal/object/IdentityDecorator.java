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

package org.openvpms.domain.internal.object;

import org.openvpms.component.business.domain.im.common.IMObjectDecorator;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Identity;

/**
 * Decorator for {@link Identity}.
 *
 * @author Tim Anderson
 */
public class IdentityDecorator extends IMObjectDecorator implements Identity {

    /**
     * Constructs an {@link IdentityDecorator}.
     *
     * @param peer the peer to delegate to
     */
    public IdentityDecorator(IMObject peer) {
        super(peer);
    }

    /**
     * Sets the identity.
     *
     * @param identity the identity
     */
    @Override
    public void setIdentity(String identity) {
        getPeer().setIdentity(identity);
    }

    /**
     * Returns the identity.
     *
     * @return the identity
     */
    @Override
    public String getIdentity() {
        return getPeer().getIdentity();
    }

    /**
     * Returns the peer.
     *
     * @return the peer
     */
    @Override
    protected Identity getPeer() {
        return (Identity) super.getPeer();
    }
}
