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
     * Determines if the terminal is available.
     *
     * @return {@code true} if the terminal is available, {@code false} if it is in use
     */
    boolean isAvailable();

    /**
     * Start a payment.
     *
     * @param customer the customer
     * @param amount   the amount
     * @param cashout  the cash-out amount
     * @return the transaction corresponding to the payment
     * @throws EFTPOSException for any POS error
     */
    Transaction pay(Party customer, BigDecimal amount, BigDecimal cashout);

    /**
     * Returns a transaction given its identifier.
     *
     * @param id the transaction identifier
     * @return the transaction
     * @throws EFTPOSException for any POS error
     */
    Transaction get(String id);

}
