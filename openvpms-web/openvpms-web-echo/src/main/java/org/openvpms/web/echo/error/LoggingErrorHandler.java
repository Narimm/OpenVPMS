package org.openvpms.web.echo.error;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of {@link ErrorHandler} that simply logs errors.
 *
 * @author Tim Anderson
 */
public class LoggingErrorHandler extends ErrorHandler {

    /**
     * Singleton instance.
     */
    static final ErrorHandler INSTANCE = new LoggingErrorHandler();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LoggingErrorHandler.class);

    /**
     * Handles an error.
     *
     * @param cause the cause of the error
     */
    @Override
    public void error(Throwable cause) {
        log.error(cause.getMessage(), cause);
    }

    /**
     * Handles an error.
     *
     * @param title    the error title. May be {@code null}
     * @param message  the error message
     * @param cause    the cause. May be {@code null}
     * @param listener the listener. May be {@code null}
     */
    @Override
    public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
        log.error(message, cause);
        if (listener != null) {
            listener.windowPaneClosing(new WindowPaneEvent(this));
        }
    }
}
