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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.internal.policy;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.insurance.policy.PolicyHolder;

/**
 * Default implementation of the {@link PolicyHolder} interface.
 *
 * @author Tim Anderson
 */
public class PolicyHolderImpl implements PolicyHolder {

    /**
     * The customer that holds the policy.
     */
    private final Party customer;

    /**
     * The customer rules.
     */
    private final CustomerRules rules;

    /**
     * Constructs a {@link PolicyHolderImpl}.
     *
     * @param customer the customer
     * @param rules    the customer rules
     */
    public PolicyHolderImpl(Party customer, CustomerRules rules) {
        this.customer = customer;
        this.rules = rules;
    }

    /**
     * Returns the OpenVPMS identifier for the policy holder.
     *
     * @return the OpenVPMS identifier for the policy holder
     */
    @Override
    public long getId() {
        return customer.getId();
    }

    /**
     * Returns the policy holder name.
     *
     * @return the policy holder name
     */
    @Override
    public String getName() {
        return rules.getFullName(customer);
    }

    /**
     * Returns the policy holder's address.
     *
     * @return the policy holder's address. May be {@code null}
     */
    @Override
    public Contact getAddress() {
        return rules.getAddressContact(customer, ContactArchetypes.BILLING_PURPOSE);
    }

    /**
     * Returns the policy holder's daytime telephone.
     *
     * @return the daytime telephone. May be {@code null}
     */
    @Override
    public Contact getDaytimePhone() {
        return rules.getTelephoneContact(customer, ContactArchetypes.WORK_PURPOSE);
    }

    /**
     * Returns the policy holder's evening telephone.
     *
     * @return the evening telephone. May be {@code null}
     */
    @Override
    public Contact getEveningPhone() {
        return rules.getTelephoneContact(customer, ContactArchetypes.HOME_PURPOSE);
    }

    /**
     * Returns the policy holder's mobile telephone.
     *
     * @return the mobile telephone. May be {@code null}
     */
    @Override
    public Contact getMobilePhone() {
        return rules.getTelephoneContact(customer, ContactArchetypes.MOBILE_PURPOSE);
    }

    /**
     * Returns the policy holder's email address.
     *
     * @return the email address. May be {@code null}
     */
    @Override
    public Contact getEmail() {
        return rules.getEmailContact(customer);
    }
}
