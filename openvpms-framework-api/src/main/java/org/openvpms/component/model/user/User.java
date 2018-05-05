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

package org.openvpms.component.model.user;

import org.openvpms.component.model.party.Party;

/**
 * This class represents the user details and the list of associated authorities.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface User extends Party {

    /**
     * Returns the username used to authenticate the user.
     *
     * @return the username
     */
    String getUsername();

    /**
     * Sets the user's login name.
     *
     * @param userName the user's login name
     */
    void setUsername(String userName);

}
