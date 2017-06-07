package org.openvpms.pos.api;

import org.openvpms.component.business.domain.im.act.Act;

import java.util.List;

/**
 * POS transaction.
 *
 * @author Tim Anderson
 */
public interface Transaction {

    enum Status {
        PENDING, PROMPT, APPROVED, REVERSED, DECLINED, ERROR
    }

    /**
     * Returns the unique identifier for the transaction.
     *
     * @return the unique identifier
     */
    String getId();

    /**
     * Returns the transaction status.
     *
     * @return the transaction status
     */
    Status getStatus();

    /**
     * Returns the prompt.
     *
     * @return the prompt, or {@code null} if no prompt is required
     */
    Prompt getPrompt();

    /**
     * Returns the messages.
     *
     * @return the messages
     */
    List<String> getMessages();

    /**
     * Returns the transaction receipts.
     *
     * @return the transaction receipts
     */
    List<Receipt> getReceipts();

    /**
     * Returns the persistent state of the transaction.
     * <p>
     * This is used for reporting and recovery.
     *
     * @return the persistent state of the transaction
     */
    Act getAct();

    /**
     * Cancels the transaction.
     *
     * @throws POSException if the transaction cannot be cancelled
     */
    void cancel();

}
