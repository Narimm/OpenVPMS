package org.openvpms.eftpos;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.i18n.Message;

/**
 * EFTPOS exception.
 *
 * @author Tim Anderson
 */
public class EFTPOSException extends OpenVPMSException {

    /**
     * Constructs a {@link EFTPOSException}.
     *
     * @param message the message
     */
    public EFTPOSException(Message message) {
        super(message.toString());
    }

    /**
     * Constructs a {@link EFTPOSException}.
     *
     * @param message the message
     * @param cause   the root cause
     */
    public EFTPOSException(Message message, Throwable cause) {
        super(message.toString(), cause);
    }

}
