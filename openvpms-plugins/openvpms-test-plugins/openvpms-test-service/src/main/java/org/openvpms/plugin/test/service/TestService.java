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

package org.openvpms.plugin.test.service;

/**
 * Test service API to inject to plugins.
 *
 * @author Tim Anderson
 */
public interface TestService {

    /**
     * Returns the value.
     *
     * @return the value
     */
    long getValue();

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    void setValue(long value);
}