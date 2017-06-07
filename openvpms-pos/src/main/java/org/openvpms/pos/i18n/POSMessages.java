package org.openvpms.pos.i18n;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.i18n.Messages;

/**
 * Messages reported by the POS API.
 *
 * @author Tim Anderson
 */
public class POSMessages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("POS", POSMessages.class.getName());

    /**
     * Creates a message indicating that a terminal was not found.
     *
     * @param terminal the terminal
     * @return the message
     */
    public static Message terminalNotFound(Entity terminal) {
        return messages.getMessage(100, terminal.getId(), terminal.getName());
    }

    /**
     * Creates a message indicating that a terminal is not available for use.
     *
     * @param terminal the terminal
     * @return the message
     */
    public static Message terminalNotAvailable(Entity terminal) {
        return messages.getMessage(101, terminal.getId(), terminal.getName());
    }
}
