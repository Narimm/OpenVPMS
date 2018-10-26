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

package org.openvpms.web.component.alert;

import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Party;

/**
 * An alert from a customer's account type.
 *
 * @author Tim Anderson
 */
public class AccountTypeAlert extends Alert {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The customer's account type.
     */
    private final AccountType accountType;

    /**
     * Constructs a {@link AccountTypeAlert}.
     *
     * @param customer   the customer
     * @param accountType the customer's account type
     * @param alertType  the alert type. A <em>lookup.customerAlertType</em>
     */
    public AccountTypeAlert(Party customer, AccountType accountType, Lookup alertType) {
        super(alertType);
        this.customer = customer;
        this.accountType = accountType;
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    public Party getCustomer() {
        return customer;
    }

    /**
     * Returns the account type.
     *
     * @return the account type
     */
    public AccountType getAccountType() {
        return accountType;
    }

    /**
     * Returns the alert type.
     *
     * @return the alert type
     */
    @Override
    public Lookup getAlertType() {
        return (Lookup) super.getAlertType();
    }
}
