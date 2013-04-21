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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.manager;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.plugin.test.service.impl.TestServiceImpl;
import org.openvpms.plugins.test.TestPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link PluginManager} when the plugin services and {@link PluginServiceProvider} are bootstrapped from
 * a Spring context.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/plugin-context.xml")
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
     * Verifies that the {@link TestServiceImpl} defined in the Spring context is exposed to the {@link TestPlugin}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testPluginManager() throws Exception {
        assertNull(testService.getValue());
        String name = TestHelper.getPractice().getName();
        assertNotNull(name);
        String path = FelixHelper.getFelixDir();

        PluginManager pluginManager = new PluginManager(path, provider);
        pluginManager.start();
        Thread.sleep(10000);
        pluginManager.destroy();
        assertEquals(name, testService.getValue());
    }
}
