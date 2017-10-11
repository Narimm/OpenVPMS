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

package org.openvpms.plugin.service.config;

import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * A service that is configured from an {@link IMObject}.
 *
 * @author Tim Anderson
 */
public interface ConfigurableService {

    /**
     * Returns the archetype that this service is configured with.
     *
     * @return the archetype short name
     */
    String getArchetype();

    /**
     * Invoked when the service is registered, and each time the configuration is updated.
     *
     * @param config may be {@code null}, if no configuration exists
     */
    void updated(IMObject config);

    /**
     * Invoked when the configuration is removed.
     *
     * @param config the removed configuration
     */
    void removed(IMObject config);
}
