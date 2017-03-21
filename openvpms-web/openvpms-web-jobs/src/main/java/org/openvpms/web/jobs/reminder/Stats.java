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

package org.openvpms.web.jobs.reminder;

/**
 * Patient reminder processing statistics.
 *
 * @author Tim Anderson
 */
public class Stats {

    public static final Stats ZERO = new Stats();

    private final int sent;
    private final int cancelled;
    private final int errors;

    /**
     * Constructs a {@link Stats}.
     */
    public Stats() {
        this(0, 0, 0);
    }

    /**
     * Constructs a {@link Stats}.
     *
     * @param sent      the no. of sent reminders
     * @param cancelled the no. of cancelled reminders
     * @param errors    the no. of errors
     */
    public Stats(int sent, int cancelled, int errors) {
        this.sent = sent;
        this.cancelled = cancelled;
        this.errors = errors;
    }

    /**
     * Returns the number of sent reminders.
     *
     * @return the number of sent reminders
     */
    public int getSent() {
        return sent;
    }

    /**
     * Returns the number of cancelled reminders.
     *
     * @return the number of cancelled reminders
     */
    public int getCancelled() {
        return cancelled;
    }

    /**
     * Returns the number of reminders that couldn't be sent due to error.
     *
     * @return the number of errors
     */
    public int getErrors() {
        return errors;
    }

    /**
     * Adds statistics.
     *
     * @param stats the statistics to add.
     * @return the new statistics
     */
    public Stats add(Stats stats) {
        return new Stats(sent + stats.getSent(), cancelled + stats.getCancelled(), errors + stats.getErrors());
    }
}
