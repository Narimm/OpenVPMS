package org.openvpms.plugin.test.service;

/**
 * Test service API to inject to plugins.
 *
 * @author Tim Anderson
 */
public interface TestService {

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setValue(String value);
}