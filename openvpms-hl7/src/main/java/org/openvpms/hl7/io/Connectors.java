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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.io;

import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Manages {@link Connectors}.
 *
 * @author Tim Anderson
 */
public interface Connectors {

    /**
     * Returns a connector given its reference.
     *
     * @param reference the connector reference
     * @return the connector, or {@code null} if none is found
     */
    Connector getConnector(IMObjectReference reference);

}
