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

package org.openvpms.domain.internal.patient;

import org.openvpms.component.model.object.Identity;
import org.openvpms.domain.internal.object.IdentityDecorator;
import org.openvpms.domain.patient.Microchip;

/**
 * Default implementation fo {@link Microchip}.
 *
 * @author Tim Anderson
 */
public class MicrochipImpl extends IdentityDecorator implements Microchip {

    /**
     * Constructs an {@link MicrochipImpl}.
     *
     * @param peer the peer to delegate to
     */
    public MicrochipImpl(Identity peer) {
        super(peer);
    }
}
