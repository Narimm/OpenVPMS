package org.openvpms.hl7.impl;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.hl7.Connector;

import java.util.List;

/**
 * Manages {@link Connectors}.
 *
 * @author Tim Anderson
 */
public interface Connectors {

    /**
     * Returns sending connectors active at the practice location.
     *
     * @param location the location
     * @return the connectors
     */
    List<Connector> getSenders(Party location);
}
