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

import java.util.Date;

/**
 * Date/time range.
 *
 * @author Tim Anderson
 */
public class Range {

    /**
     * The start of the range.
     */
    private final Date start;

    /**
     * The end of the range.
     */
    private final Date end;

    /**
     * Constructs a {@link Range}.
     *
     * @param start the start of the range
     * @param end   the end of the range
     */
    public Range(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start of the range.
     *
     * @return the start of the range
     */
    public Date getStart() {
        return start;
    }

    /**
     * Returns the end of the range.
     *
     * @return the end of the range
     */
    public Date getEnd() {
        return end;
    }
}
