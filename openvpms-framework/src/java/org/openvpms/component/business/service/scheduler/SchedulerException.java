package org.openvpms.component.business.service.scheduler;

import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * Scheduler exception.
 *
 * @author Tim Anderson
 */
public class SchedulerException extends OpenVPMSException {

    /**
     * Constructs a {@link SchedulerException}.
     *
     * @param cause the cause
     */
    public SchedulerException(Throwable cause) {
        super(cause);
    }
}
