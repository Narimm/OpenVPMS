package org.openvpms.pos.api;

/**
 * POS receipt.
 *
 * @author Tim Anderson
 */
public interface Receipt {

    /**
     * Determines if a signature is required.
     *
     * @return {@code true} if a signature is required
     */
    boolean isSignatureRequired();

    /**
     * Returns the formatted receipt.
     *
     * @return the formatted receipt
     */
    String getReceipt();

}
