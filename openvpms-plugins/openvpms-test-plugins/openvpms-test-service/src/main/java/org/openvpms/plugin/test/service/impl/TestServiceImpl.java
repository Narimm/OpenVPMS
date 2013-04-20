package org.openvpms.plugin.test.service.impl;

import org.openvpms.plugin.test.service.TestService;

/**
 * Implementation of {@link TestService}.
 *
 * @author Tim Anderson
 */
public class TestServiceImpl implements TestService {

    private String value;

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
