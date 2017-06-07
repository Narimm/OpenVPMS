package org.openvpms.pos.api;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.i18n.Message;

/**
 * POS exception.
 *
 * @author Tim Anderson
 */
public class POSException extends OpenVPMSException {

    /**
     * Constructs a {@link POSException}.
     *
     * @param message the message
     */
    public POSException(Message message) {
        super(message.toString());
    }

    /**
     * Constructs a {@link POSException}.
     *
     * @param message the message
     * @param cause   the root cause
     */
    public POSException(Message message, Throwable cause) {
        super(message.toString(), cause);
    }

}
