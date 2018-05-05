package org.openvpms.archetype.rules.contact;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;

/**
 * An {@link AddressFormatter} that formats location contacts using a fixed format.
 *
 * @author Tim Anderson
 */
public class BasicAddressFormatter extends AbstractAddressFormatter {

    /**
     * Constructs a {@link BasicAddressFormatter}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public BasicAddressFormatter(IArchetypeService service, ILookupService lookups) {
        super(service, lookups);
    }
}
