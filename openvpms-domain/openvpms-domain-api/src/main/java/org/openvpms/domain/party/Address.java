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

import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Contact;

/**
 * Address contact.
 *
 * @author Tim Anderson
 */
public interface Address extends Contact {

    /**
     * Returns the street address.
     *
     * @return the street address. May be {@code null}
     */
    String getAddress();

    /**
     * Returns the suburb name.
     *
     * @return the suburb name. May be {@code null
     */
    String getSuburbName();

    /**
     * Returns the suburb code.
     *
     * @return the suburb code. May be {@code null
     */
    String getSuburbCode();

    /**
     * Returns the suburb lookup.
     *
     * @return the suburb lookup. May be {@code null}
     */
    Lookup getSuburbLookup();

    /**
     * Returns the post code.
     *
     * @return the post code. May be {@code null
     */
    String getPostcode();

    /**
     * Returns the state name.
     *
     * @return the state name. May be {@code null
     */
    String getStateName();

    /**
     * Returns the state code.
     *
     * @return the state code. May be {@code null
     */
    String getStateCode();

    /**
     * Returns the state lookup.
     *
     * @return the state lookup. May be {@code null}
     */
    Lookup getStateLookup();

    /**
     * Formats the address.
     *
     * @param singleLine if {@code true}, return the address as a single line
     * @return the formatted address
     */
    String format(boolean singleLine);

}
