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

package org.openvpms.booking.domain;

import java.util.List;

/**
 * Schedule free and busy time ranges.
 *
 * @author Tim Anderson
 */
public class FreeBusy {

    /**
     * The free ranges.
     */
    private final List<Range> free;

    /**
     * The busy ranges.
     */
    private final List<Range> busy;

    /**
     * Constructs a {@link FreeBusy}.
     *
     * @param free the free ranges
     * @param busy the busy ranges
     */
    public FreeBusy(List<Range> free, List<Range> busy) {
        this.free = free;
        this.busy = busy;
    }

    /**
     * Returns the free ranges.
     *
     * @return the free ranges
     */
    public List<Range> getFree() {
        return free;
    }

    /**
     * Returns the busy ranges.
     *
     * @return the busy ranges
     */
    public List<Range> getBusy() {
        return busy;
    }
}
