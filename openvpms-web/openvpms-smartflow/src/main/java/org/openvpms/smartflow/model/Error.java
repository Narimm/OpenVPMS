package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Smart Flow Sheet error, returned in 40x responses.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Error {

    /**
     * The error message.
     */
    @JsonProperty("Message")
    private String message;

    /**
     * Sets the error message.
     *
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the error message
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
}
