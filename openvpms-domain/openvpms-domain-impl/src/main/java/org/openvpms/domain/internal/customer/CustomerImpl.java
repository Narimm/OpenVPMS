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

package org.openvpms.domain.internal.customer;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Party;
import org.openvpms.domain.customer.Customer;
import org.openvpms.domain.internal.party.ContactablePartyImpl;
import org.openvpms.domain.party.Address;
import org.openvpms.domain.party.Phone;

/**
 * Default implementation of {@link Customer}.
 *
 * @author Tim Anderson
 */
public class CustomerImpl extends ContactablePartyImpl implements Customer {

    /**
     * The bean.
     */
    private final IMObjectBean bean;

    /**
     * The title node.
     */
    private static final String TITLE = "title";

    /**
     * The first name node.
     */
    private static final String FIRST_NAME = "firstName";

    /**
     * The last name node.
     */
    private static final String LAST_NAME = "lastName";

    /**
     * The company name node.
     */
    private static final String COMPANY_NAME = "companyName";

    /**
     * Constructs a {@link CustomerImpl}.
     *
     * @param peer    the peer to delegate to
     * @param service the archetype service
     * @param rules   the party rules
     */
    public CustomerImpl(Party peer, IArchetypeService service, PartyRules rules) {
        super(peer, service, rules);
        this.bean = service.getBean(peer);
    }

    /**
     * Returns the person's title.
     *
     * @return the title. May be {@code null}
     */
    @Override
    public Lookup getTitleLookup() {
        return bean.getLookup(TITLE);
    }

    /**
     * Returns the person's title name.
     *
     * @return the title name. May be {@code null}
     */
    @Override
    public String getTitleName() {
        Lookup lookup = getTitleLookup();
        return (lookup != null) ? lookup.getName() : null;
    }

    /**
     * Returns the person's title code.
     *
     * @return the title code. May be {@code null}
     */
    @Override
    public String getTitleCode() {
        return bean.getString(TITLE);
    }

    /**
     * Returns the person's first name.
     *
     * @return the first name
     */
    @Override
    public String getFirstName() {
        return bean.getString(FIRST_NAME);
    }

    /**
     * Returns the person's last name.
     *
     * @return the last name
     */
    @Override
    public String getLastName() {
        return bean.getString(LAST_NAME);
    }

    /**
     * Returns the person's full name.
     *
     * @return the full name. May be {@code null}
     */
    @Override
    public String getFullName() {
        StringBuilder result = new StringBuilder();
        String title = getTitleName();
        String firstName = getFirstName();
        String lastName = getLastName();
        if (title != null) {
            result.append(title);
        }
        if (firstName != null) {
            if (result.length() != 0) {
                result.append(" ");
            }
            result.append(firstName);
        }
        if (lastName != null) {
            if (result.length() != 0) {
                result.append(" ");
            }
            result.append(lastName);
        }
        return result.length() != 0 ? result.toString() : null;
    }


    /**
     * Returns the company name, if this customer is a representative of a company.
     *
     * @return the company name. May be {@code null}
     */
    @Override
    public String getCompanyName() {
        return bean.hasNode(COMPANY_NAME) ? bean.getString(COMPANY_NAME) : null;
    }

    /**
     * Returns the work telephone.
     *
     * @return the work telephone. May be {@code null}
     */
    @Override
    public Phone getWorkPhone() {
        return getPhone(ContactArchetypes.WORK_PURPOSE);
    }

    /**
     * Returns the home telephone.
     *
     * @return the home telephone. May be {@code null}
     */
    @Override
    public Phone getHomePhone() {
        return getPhone(ContactArchetypes.HOME_PURPOSE);
    }

    /**
     * Returns the mobile telephone.
     *
     * @return the mobile telephone. May be {@code null}
     */
    @Override
    public Phone getMobilePhone() {
        return getPhone(ContactArchetypes.MOBILE_PURPOSE);
    }

    /**
     * Returns the person's address.
     *
     * @return the person's address. May be {@code null}
     */
    @Override
    public Address getAddress() {
        return getAddress(ContactArchetypes.BILLING_PURPOSE);
    }

}
