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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

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
     * <p>
     * This must be an entity archetype.
     *
     * @return the terminal configuration archetype
     */
    String getConfigurationType();

    /**
     * Returns an EFTPOS terminal given its configuration.
     *
     * @param configuration the terminal configuration
     * @return the EFTPOS terminal
     * @throws EFTPOSException for any error
     */
    Terminal getTerminal(Entity configuration);

}
