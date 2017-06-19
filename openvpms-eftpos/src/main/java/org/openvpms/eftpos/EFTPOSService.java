package org.openvpms.eftpos;

import org.openvpms.component.business.domain.im.common.Entity;

/**
 * EFTPOS service.
 *
 * @author Tim Anderson
 */
public interface EFTPOSService {

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
     * @throws EFTPOSException for any error
     */
    Terminal getTerminal(Entity configuration);

}
