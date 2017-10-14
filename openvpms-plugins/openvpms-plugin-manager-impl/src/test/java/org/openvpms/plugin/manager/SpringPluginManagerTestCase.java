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

package org.openvpms.plugin.manager;

import org.junit.Test;
import org.openvpms.plugin.internal.manager.PluginManagerImpl;
import org.openvpms.plugin.internal.manager.PluginServiceProvider;
import org.openvpms.plugin.test.service.impl.TestServiceImpl;
import org.openvpms.plugins.test.impl.TestPluginImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PluginManagerImpl} when the plugin services and {@link PluginServiceProvider} are bootstrapped from
 * a Spring context.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/applicationContext.xml")
public class SpringPluginManagerTestCase extends AbstractJUnit4SpringContextTests {

    /**
     * The plugin service provider.
     */
    @Autowired
    PluginServiceProvider provider;

    /**
     * The test service.
     */
    @Autowired
    TestServiceImpl testService;


    /**
     * Verifies that the {@link TestServiceImpl} defined in the Spring context is exposed to the {@link TestPluginImpl}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testPluginManager() throws Exception {
        assertEquals(0, testService.getValue());

        String path = FelixHelper.getFelixDir();
        PluginManagerImpl pluginManager = new PluginManagerImpl(path, provider, new MockServletContext());
        pluginManager.start();
        Thread.sleep(7000);
        pluginManager.stop();
        long value = testService.getValue();

        // NOTE: the fileinstall bundle can redeploy the plugin bundle, if it has been previously deployed.
        // This results in the service being called twice.
        assertTrue(value == 1 || value == 2);
    }
}
