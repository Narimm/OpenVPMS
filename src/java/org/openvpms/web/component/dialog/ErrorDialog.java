package org.openvpms.web.component.dialog;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.openvpms.web.util.Messages;


/**
 * Modal error dialog box.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class ErrorDialog extends MessageDialog {

    /**
     * Construct a new <code>ErrorDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    protected ErrorDialog(String title, String message) {
        super(title, message, Buttons.OK);
        setStyleName("ErrorDialog");

        show();
    }

    /**
     * Helper to show a new error dialog.
     *
     * @param exception the exception to display
     */
    public static void show(Throwable exception) {
        String message = ExceptionUtils.getStackTrace(exception);
        show(Messages.get("errordialog.title"), message);
    }

    /**
     * Helper to show a new error dialog.
     *
     * @param message dialog message
     */
    public static void show(String message) {
        show(Messages.get("errordialog.title"), message);
    }

    /**
     * Helper to show a new error dialog.
     *
     * @param title   the dialog title
     * @param message dialog message
     */
    public static void show(String title, String message) {
        new ErrorDialog(title, message);
    }

}
