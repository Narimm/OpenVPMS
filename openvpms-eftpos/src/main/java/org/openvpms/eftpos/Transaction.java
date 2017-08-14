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

import java.util.List;

/**
 * EFTPOS transaction.
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
     * Cancels the transaction.
     *
     * @throws EFTPOSException if the transaction cannot be cancelled
     */
    void cancel();

}
