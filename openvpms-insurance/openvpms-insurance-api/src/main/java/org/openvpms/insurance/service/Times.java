package org.openvpms.insurance.service;

import java.time.OffsetDateTime;

/**
 * Date/time range.
 *
 * @author Tim Anderson
 */
public class Times {

    /**
     * Indicates that gap claims can be submitted at any time.
     */
    public static final Times UNBOUNDED = new Times(null, null);

    /**
     * The lower bound of the range.
     */
    private final OffsetDateTime from;

    /**
     * The upper bound of the range
     */
    private final OffsetDateTime to;

    /**
     * Constructs a {@link Times}.
     *
     * @param from the from time, or {@code null} if there is no lower limit
     * @param to   the to time, or {@code null} if there is no upper limit
     */
    public Times(OffsetDateTime from, OffsetDateTime to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the from submission time.
     *
     * @return the from submission time, or {@code null} if there is no lower limit
     */
    public OffsetDateTime getFrom() {
        return from;
    }

    /**
     * Returns the to submission time.
     *
     * @return the to submission time, or {@code null} if there is no upper limit
     */
    public OffsetDateTime getTo() {
        return to;
    }

    /**
     * Determines if a time is in the range.
     *
     * @param time the time
     * @return {@code true} if the time is in the range, otherwise {@code false}
     */
    public boolean inRange(OffsetDateTime time) {
        return (from == null || from.compareTo(time) <= 0) && (to == null || to.compareTo(time) > 0);
    }
}
