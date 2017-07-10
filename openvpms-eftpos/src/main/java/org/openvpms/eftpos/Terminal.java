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

package org.openvpms.eftpos;

import org.openvpms.component.business.domain.im.party.Party;

import java.math.BigDecimal;

/**
 * EFTPOS terminal.
 *
 * @author Tim Anderson
 */
public interface Terminal {

    /**
     * Determines if the terminal has been registered.
     *
     * @return {@code true} if the terminal has been registered
     * @throws EFTPOSException for any EFTPOS error
     */
    boolean isRegistered();

    /**
     * Registers the terminal prior to use.
     * <p>
     * This is required if {@link #isRegistered} returns {@code false}.
     *
     * @throws EFTPOSException for any EFTPOS error
     */
    void register();

    /**
     * Determines if the terminal is available.
     *
     * @return {@code true} if the terminal is available, {@code false} if it is in use, or has not been registered
     * @throws EFTPOSException for any EFTPOS error
     */
    boolean isAvailable();

    /**
     * Start a payment.
     *
     * @param customer the customer
     * @param amount   the amount
     * @param cashout  the cash-out amount
     * @return the transaction corresponding to the payment
     * @throws EFTPOSException for any EFTPOS error
     */
    Transaction pay(Party customer, BigDecimal amount, BigDecimal cashout);

    /**
     * Returns a transaction given its identifier.
     *
     * @param id the transaction identifier
     * @return the transaction
     * @throws EFTPOSException for any EFTPOS error
     */
    Transaction get(String id);

}
