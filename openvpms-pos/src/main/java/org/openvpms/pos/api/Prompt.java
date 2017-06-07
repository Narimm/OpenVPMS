package org.openvpms.pos.api;

import java.util.List;

/**
 * POS terminal prompt.
 *
 * @author Tim Anderson
 */
public interface Prompt {

    /**
     * Returns the prompt message.
     *
     * @return the prompt message
     */
    String getMessage();

    /**
     * Returns the available prompt options.
     *
     * @return the available prompt options
     */
    List<String> getOptions();

    /**
     * Sends the selected option.
     *
     * @param option the option
     * @throws POSException for any POS error
     */
    void send(String option);
}
