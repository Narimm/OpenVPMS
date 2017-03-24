package org.openvpms.archetype.rules.contact;

import org.openvpms.component.business.domain.im.party.Contact;

/**
 * Address formatter.
 *
 * @author Tim Anderson
 */
public interface AddressFormatter {

    /**
     * Formats an address.
     *
     * @param location   the location
     * @param singleLine if {@code true}, return the address as a single line
     * @return the formatted address. May be {@code null}
     */
    String format(Contact location, boolean singleLine);
}
