package org.openvpms.insurance.service;

/**
 * Policy validation status.
 *
 * @author Tim Anderson
 */
public class PolicyValidationStatus {

    public enum Status {
        VALID,                // policy is valid, and may be used for claims
        INVALID,              // policy number is invalid (e.g incorrectly formatted)
        NOT_FOUND,            // policy number is correctly formatted, but not found
        EXPIRED,              // policy has expired, and there is no newer policy
        CHANGE_POLICY_NUMBER, // the supplied number should be used as the new policy number
        UNSUPPORTED           // used when validation is not supported
    }

    /**
     * The status.
     */
    private final Status status;

    /**
     * The policy number.
     */
    private final String policyNumber;

    /**
     * The error message.
     */
    private final String message;

    /**
     * Constructs a {@link PolicyValidationStatus}.
     *
     * @param status the status
     */
    private PolicyValidationStatus(Status status) {
        this(status, null);
    }

    /**
     * Constructs a {@link PolicyValidationStatus}.
     *
     * @param status       the status
     * @param policyNumber the policy number. May be {@code null}
     */
    private PolicyValidationStatus(Status status, String policyNumber) {
        this(status, policyNumber, null);
    }

    /**
     * Constructs a {@link PolicyValidationStatus}.
     *
     * @param status       the status
     * @param policyNumber the policy number. May be {@code null}
     * @param message      the error message. May be {@code null}
     */
    private PolicyValidationStatus(Status status, String policyNumber, String message) {
        this.status = status;
        this.policyNumber = policyNumber;
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
     * Returns the policy number.
     * <p>
     * Only applicable if {@code status == CHANGE_POLICY_NUMBER}
     *
     * @return the policy number. May be {@code null}
     */
    public String getPolicyNumber() {
        return policyNumber;
    }

    /**
     * Returns the error message.
     * <p>
     * Only applicable if {@code status == INVALID}.
     *
     * @return the error message. May be {@code null}
     */
    public String getMessage() {
        return message;
    }

    /**
     * Creates a status indicating that the policy is valid.
     *
     * @return a new status
     */
    public static PolicyValidationStatus valid() {
        return new PolicyValidationStatus(Status.VALID);
    }

    /**
     * Creates a status indicating that the policy is invalid.
     *
     * @param message the error message
     * @return a new status
     */
    public static PolicyValidationStatus invalid(String message) {
        return new PolicyValidationStatus(Status.INVALID, null, message);
    }

    /**
     * Creates a status indicating that the policy was not found.
     *
     * @return a new status
     */
    public static PolicyValidationStatus notFound() {
        return new PolicyValidationStatus(Status.NOT_FOUND);
    }

    /**
     * Creates a status indicating that the policy has expired.
     *
     * @return a new status
     */
    public static PolicyValidationStatus expired() {
        return new PolicyValidationStatus(Status.EXPIRED);
    }

    /**
     * Creates a status indicating to change the policy number to that supplied.
     * <p>
     * This can be used to indicate that:
     * <ul>
     * <li>the original policy number was invalid; or</li>
     * <li>the specified policy has expired, but a later one exists</li>
     * </ul>
     *
     * @param policyNumber the new policy number
     * @return a new status
     */
    public static PolicyValidationStatus changePolicyNumber(String policyNumber) {
        return new PolicyValidationStatus(Status.CHANGE_POLICY_NUMBER, policyNumber);
    }

    /**
     * Creates a status indicating that policy validation is not supported.
     *
     * @return a new status
     */
    public static PolicyValidationStatus unsupported() {
        return new PolicyValidationStatus(Status.UNSUPPORTED);
    }

}
