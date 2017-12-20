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

package org.openvpms.plugin.test.service.impl;

import org.openvpms.plugin.test.service.TestService;

/**
 * Implementation of {@link TestService}.
 *
 * @author Tim Anderson
 */
public class TestServiceImpl implements TestService {

    /**
     * The value
     */
    private long value;

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    @Override
    public long getValue() {
        return value;
    }
}
