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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.manager;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.plugin.internal.manager.PluginManagerImpl;
import org.openvpms.plugin.internal.manager.PluginServiceProvider;
import org.openvpms.plugin.test.service.TestService;
import org.openvpms.plugin.test.service.impl.TestServiceImpl;
import org.openvpms.plugins.test.api.TestPlugin;
import org.openvpms.plugins.test.impl.TestPluginImpl;
import org.osgi.framework.ServiceRegistration;
import org.springframework.mock.web.MockServletContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link PluginManagerImpl}.
 *
 * @author Tim Anderson
 */
public class PluginManagerTestCase extends ArchetypeServiceTest {

    /**
     * Verifies that services can be provided to a plugin, and that the plugin can call them.
     * <p>
     * This provides a {@link TestService} to the {@link TestPluginImpl} plugin, which calls the {@link TestService}
     * with its next value.
     *
     * @throws Exception for any error
     */
    @Test
    public void testPluginManager() throws Exception {
        String felixDir = FelixHelper.getFelixDir();

        // make sure directories have been packaged correctly
        Path etc = Paths.get(felixDir, "etc");
        Path data = Paths.get(felixDir, "data");
        Path deploy = Paths.get(felixDir, "deploy");
        assertTrue(Files.isDirectory(etc));
        assertTrue(Files.isDirectory(data));
        assertTrue(Files.isDirectory(deploy));

        TestServiceImpl service = new TestServiceImpl();
        service.setValue(10);

        // provide the TestService to the plugin
        PluginServiceProvider provider = context -> {
            ServiceRegistration<?> testService = context.registerService(TestService.class.getName(), service,
                                                                         new Hashtable<>());
            return Collections.<ServiceRegistration<?>>singletonList(testService);
        };

        // start the plugin manager
        MockServletContext servletContext = new MockServletContext();
        PluginManagerImpl manager = new PluginManagerImpl(felixDir, provider, servletContext);
        manager.start();
        for (int i = 0; i < 20; ++i) {
            if (service.getValue() > 10) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }

        // verify the service was called by the plugin
        assertEquals(11, service.getValue());

        for (int i = 0; i < 20; ++i) {
            if (manager.getService(TestPlugin.class) != null) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }

        // now verify the plugin was exported
        assertNotNull(manager.getService(TestPlugin.class));

        manager.stop();
    }

}
