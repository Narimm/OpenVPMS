/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

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
