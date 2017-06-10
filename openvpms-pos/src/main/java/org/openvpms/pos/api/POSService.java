package org.openvpms.pos.api;

import org.openvpms.component.business.domain.im.common.Entity;

/**
 * POS service.
 *
 * @author Tim Anderson
 */
public interface POSService {

    /**
     * Returns the terminal configuration archetype that this service supports.
     *
     * @return the terminal configuration archetype
     */
    String getConfigurationType();

    /**
     * Returns a POS terminal given its configuration.
     *
     * @param configuration the terminal configuration
     * @return the POS terminal
     * @throws POSException for any error
     */
    Terminal getTerminal(Entity configuration);

}
