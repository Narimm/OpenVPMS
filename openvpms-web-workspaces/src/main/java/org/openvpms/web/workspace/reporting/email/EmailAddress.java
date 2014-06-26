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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.email;

/**
 * Email address.
 *
 * @author Tim Anderson
 */
public class EmailAddress {

    /**
     * The email address.
     */
    private final String address;

    /**
     * The email name.
     */
    private final String name;


    /**
     * Constructs an {@link EmailAddress}.
     *
     * @param address the address
     * @param name    the name
     */
    public EmailAddress(String address, String name) {
        this.address = address;
        this.name = name;
    }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the email name.
     *
     * @return the email name
     */
    public String getName() {
        return name;
    }
}

