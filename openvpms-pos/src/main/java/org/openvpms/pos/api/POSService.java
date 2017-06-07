package org.openvpms.pos.api;

import org.openvpms.component.business.domain.im.common.Entity;

/**
 * POS service.
 *
 * @author Tim Anderson
 */
public interface POSService {

    /**
     * Returns a POS terminal given its configuration.
     *
     * @param terminal the terminal configuration.
     * @return the POS terminal
     * @throws POSException for any error
     */
    Terminal getTerminal(Entity terminal);

}
