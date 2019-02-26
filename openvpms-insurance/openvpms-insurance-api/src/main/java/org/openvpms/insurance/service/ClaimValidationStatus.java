package org.openvpms.insurance.service;

/**
 * Claim validation status.
 *
 * @author Tim Anderson
 */
public class ClaimValidationStatus {

    public enum Status {
        VALID,                // claim is valid and may be submitted
        WARNING,              // the claim may be submitted, but may be rejected. Details are provided in the message.
        ERROR                 // claim is invalid and may not be submitted. Details are provided in the message.
    }

    /**
     * The status.
     */
    private final Status status;

    /**
     * The status message, for warnings and errors.
     */
    private final String message;

    /**
     * Construct a {@link ClaimValidationStatus}.
     *
     * @param status the status
     */
    private ClaimValidationStatus(Status status) {
        this(status, null);
    }

    /**
     * Construct a {@link ClaimValidationStatus}.
     *
     * @param status  the status
     * @param message the message
     */
    private ClaimValidationStatus(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the message.
     *
     * @return the message. May be {@code null} if the status is {@code VALID}
     */
    public String getMessage() {
        return message;
    }

    /**
     * Creates a status indicating that the claim is valid.
     *
     * @return a new status
     */
    public static ClaimValidationStatus valid() {
        return new ClaimValidationStatus(Status.VALID);
    }

    /**
     * Creates a status indicating that the claim is valid but has a warning.
     *
     * @param message the warning message
     * @return a new status
     */
    public static ClaimValidationStatus warning(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Argument 'message' must be provided");
        }
        return new ClaimValidationStatus(Status.WARNING, message);
    }

    /**
     * Creates a status indicating that the claim is invalid..
     *
     * @param message the error message
     * @return a new status
     */
    public static ClaimValidationStatus error(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Argument 'message' must be provided");
        }
        return new ClaimValidationStatus(Status.ERROR, message);
    }

}
