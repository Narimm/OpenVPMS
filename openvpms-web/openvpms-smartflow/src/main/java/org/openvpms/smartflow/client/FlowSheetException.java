package org.openvpms.smartflow.client;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.i18n.Message;

/**
 * Smart Flow Sheet interface exception.
 *
 * @author Tim Anderson
 */
public class FlowSheetException extends OpenVPMSException {

    /**
     * Constructs a {@link FlowSheetException}.
     *
     * @param message the message
     */
    public FlowSheetException(Message message) {
        super(message.toString());
    }

    /**
     * Constructs a {@link FlowSheetException}.
     *
     * @param message the message
     * @param cause   the root cause
     */
    public FlowSheetException(Message message, Throwable cause) {
        super(message.toString(), cause);
    }
}
