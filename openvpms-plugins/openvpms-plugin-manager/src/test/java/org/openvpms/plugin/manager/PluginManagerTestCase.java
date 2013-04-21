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
import org.openvpms.plugin.test.service.TestService;
import org.openvpms.plugin.test.service.impl.TestServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link PluginManager}.
 *
 * @author Tim Anderson
 */
public class PluginManagerTestCase {

    @Test
    public void test() throws Exception {
        final TestServiceImpl service = new TestServiceImpl();
        assertNull(service.getValue());

        PluginServiceProvider provider = new PluginServiceProvider() {
            public List<ServiceRegistration<?>> provide(BundleContext context) {
                ServiceRegistration<?> result = context.registerService(TestService.class.getName(), service,
                                                                        new Hashtable<String, Object>());
                return Arrays.<ServiceRegistration<?>>asList(result);
            }
        };
        PluginManager manager = new PluginManager(FelixHelper.getFelixDir(), provider);
        manager.start();
        Thread.sleep(10000);
        manager.destroy();
        assertEquals("hello", service.getValue());
    }

}
