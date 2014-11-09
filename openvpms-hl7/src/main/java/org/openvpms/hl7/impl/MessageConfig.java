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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

/**
 * Configures message population.
 *
 * @author Tim Anderson
 */
public class MessageConfig {

    /**
     * Determines if date/times should include milliseconds.
     */
    private boolean includeMillis = true;

    /**
     * Determines if date/times should include timezone offsets.
     */
    private boolean includeTimeZone = true;

    /**
     * Determines if date/times should include milliseconds.
     *
     * @return {@code true} if date/times should include milliseconds
     */
    public boolean isIncludeMillis() {
        return includeMillis;
    }

    /**
     * Determines if date/times should include milliseconds.
     *
     * @param includeMillis if {@code true}, date/times should include milliseconds
     */
    public void setIncludeMillis(boolean includeMillis) {
        this.includeMillis = includeMillis;
    }

    /**
     * Determines if date/times should include time zones.
     *
     * @return {@code true} if date/times should include time zones
     */
    public boolean isIncludeTimeZone() {
        return includeTimeZone;
    }

    /**
     * Determines if date/times should include time zones.
     *
     * @param includeTimeZone if {@code true}, date/times should include time zones
     */
    public void setIncludeTimeZone(boolean includeTimeZone) {
        this.includeTimeZone = includeTimeZone;
    }
}
