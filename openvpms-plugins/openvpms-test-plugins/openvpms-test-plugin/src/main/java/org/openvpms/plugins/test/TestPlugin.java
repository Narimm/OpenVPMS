package org.openvpms.plugins.test;

import org.openvpms.plugin.test.service.TestService;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class TestPlugin {

    public TestPlugin(TestService service) {
        service.setValue("hello");
    }
}
