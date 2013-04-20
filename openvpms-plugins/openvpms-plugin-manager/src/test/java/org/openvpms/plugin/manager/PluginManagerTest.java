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

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class PluginManagerTest {

    @Test
    public void test() throws Exception {
        final TestServiceImpl service = new TestServiceImpl();
        PluginServiceProvider provider = new PluginServiceProvider() {
            public List<ServiceRegistration<?>> provide(BundleContext context) {
                ServiceRegistration<?> result = context.registerService(TestService.class.getName(), service,
                                                                        new Hashtable<String, Object>());
                return Arrays.<ServiceRegistration<?>>asList(result);
            }
        };
        PluginManager manager = new PluginManager(getFelixDir(), provider);
        manager.start();
        Thread.sleep(10000);
        manager.destroy();
        assertEquals("hello", service.getValue());
    }

    private String getFelixDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File dir = new File(relPath + "../../target/felix");
        return dir.getPath();
    }
}
