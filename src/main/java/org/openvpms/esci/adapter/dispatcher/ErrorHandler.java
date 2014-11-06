package org.openvpms.esci.adapter.dispatcher;

/**
 * Handler for {@link ESCIDispatcher} errors.
 *
 * @author Tim Anderson
 */
public interface ErrorHandler {

    /**
     * Determines if the dispatcher should terminate on error.
     *
     * @return {@code true} if the dispatcher should terminate on error, {@code false} if it should continue
     */
    boolean terminateOnError();

    /**
     * Invoked when an error occurs.
     *
     * @param exception the error
     */
    void error(Throwable exception);
}
