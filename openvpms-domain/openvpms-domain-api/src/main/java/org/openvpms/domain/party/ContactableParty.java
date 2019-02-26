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

package org.openvpms.domain.party;

import org.openvpms.component.model.party.Party;

/**
 * A {@link Party} with contacts.
 *
 * @author Tim Anderson
 */
public interface ContactableParty extends Party {

    /**
     * Returns the address.
     *
     * @return the address. May be {@code null}
     */
    Address getAddress();

    /**
     * Returns the phone.
     *
     * @return the phone. May be {@code null}
     */
    Phone getPhone();

    /**
     * Returns the email address.
     *
     * @return the email address. May be {@code null}
     */
    Email getEmail();

}

