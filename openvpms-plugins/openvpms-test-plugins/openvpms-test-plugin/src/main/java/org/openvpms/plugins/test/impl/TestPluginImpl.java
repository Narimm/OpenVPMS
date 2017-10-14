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

package org.openvpms.plugins.test.impl;

import org.openvpms.plugin.test.service.TestService;
import org.openvpms.plugins.test.api.TestPlugin;


/**
 * Test plugin.
 *
 * @author Tim Anderson
 */
public class TestPluginImpl implements TestPlugin {

    /**
     * Constructs a {@link TestPluginImpl}.
     *
     * @param service the test service
     */
    public TestPluginImpl(TestService service) {
        service.setValue(service.getValue() + 1);
    }

}
