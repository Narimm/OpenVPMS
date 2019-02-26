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

package org.openvpms.domain.internal.party;

import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.party.ContactDecorator;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Contact;
import org.openvpms.domain.party.Address;

/**
 * Default implementation of {@link Address}.
 *
 * @author Tim Anderson
 */
public class AddressImpl extends ContactDecorator implements Address {

    /**
     * The bean.
     */
    private final IMObjectBean bean;

    /**
     * The rules.
     */
    private final PartyRules rules;

    /**
     * The address node.
     */
    private static final String ADDRESS = "address";

    /**
     * The suburb node.
     */
    private static final String SUBURB = "suburb";

    /**
     * The postcode node.
     */
    private static final String POSTCODE = "postcode";

    /**
     * The state code.
     */
    private static final String STATE = "state";

    /**
     * Constructs a {@link AddressImpl}.
     *
     * @param peer    the peer to delegate to
     * @param service the archetype service
     * @param rules   the rules
     */
    public AddressImpl(Contact peer, IArchetypeService service, PartyRules rules) {
        super(peer);
        bean = service.getBean(peer);
        this.rules = rules;
    }

    /**
     * Returns the street address.
     *
     * @return the street address. May be {@code null
     */
    @Override
    public String getAddress() {
        return bean.getString(ADDRESS);
    }

    /**
     * Returns the suburb name.
     *
     * @return the suburb name. May be {@code null
     */
    @Override
    public String getSuburbName() {
        Lookup lookup = getSuburbLookup();
        return lookup != null ? lookup.getName() : null;
    }

    /**
     * Returns the suburb code.
     *
     * @return the suburb code. May be {@code null
     */
    @Override
    public String getSuburbCode() {
        return bean.getString(SUBURB);
    }

    /**
     * Returns the suburb.
     *
     * @return the suburb. May be {@code null
     */
    @Override
    public Lookup getSuburbLookup() {
        return bean.getLookup(SUBURB);
    }

    /**
     * Returns the post code.
     *
     * @return the post code. May be {@code null
     */
    @Override
    public String getPostcode() {
        return bean.getString(POSTCODE);
    }

    /**
     * Returns the state name.
     *
     * @return the state name. May be {@code null
     */
    @Override
    public String getStateName() {
        Lookup lookup = getStateLookup();
        return lookup != null ? lookup.getName() : null;
    }

    /**
     * Returns the state code.
     *
     * @return the state code. May be {@code null
     */
    @Override
    public String getStateCode() {
        return bean.getString(STATE);
    }

    /**
     * Returns the state.
     *
     * @return the state. May be {@code null}
     */
    @Override
    public Lookup getStateLookup() {
        return bean.getLookup(STATE);
    }

    /**
     * Formats the address.
     *
     * @param singleLine if {@code true}, return the address as a single line
     * @return the formatted address
     */
    @Override
    public String format(boolean singleLine) {
        return rules.formatAddress(getPeer(), singleLine);
    }
}
