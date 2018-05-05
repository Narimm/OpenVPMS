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

package org.openvpms.web.component.mail;

import org.apache.commons.lang.StringUtils;

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

    /**
     * Returns the address, conforming to RFC822.
     *
     * @return the formatted email address
     */
    public String toString() {
        return toString(true);
    }

    /**
     * Returns the formatted email address.
     *
     * @param strict if {@code true}, quote the name to ensure the address conforms to RFC822
     * @return the formatted email address
     */
    public String toString(boolean strict) {
        String result;
        if (!StringUtils.isEmpty(name)) {
            StringBuilder builder = new StringBuilder();
            if (strict) {
                builder.append('"');
                builder.append(name.replaceAll("\"", "")); // remove all quotes, before quoting the name
                builder.append("\" ");
            } else {
                builder.append(name);
                builder.append(' ');
            }
            builder.append('<');
            builder.append(address);
            builder.append('>');
            result = builder.toString();
        } else {
            result = address;
        }
        return result;
    }

    /**
     * Parses an email address.
     *
     * @param address the address. May be {@code null}
     * @return the email address, or {@code null} if the address is invalid
     */
    public static EmailAddress parse(String address) {
        String name = null;
        int less = address.indexOf('<');
        if (less != -1) {
            name = address.substring(0, less).trim();
            name = StringUtils.removeStart(name, "\"");
            name = StringUtils.removeEnd(name, "\"");
            int greater = address.indexOf('>', less);
            if (greater != -1) {
                address = address.substring(less + 1, greater);
            }
        }
        return (address.indexOf('@') != -1) ? new EmailAddress(address, name) : null;
    }

}

