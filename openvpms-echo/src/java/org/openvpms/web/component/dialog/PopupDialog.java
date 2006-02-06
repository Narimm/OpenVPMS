package org.openvpms.web.component.dialog;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;


/**
 * Generic popup dialog, providing OK and Cancel buttons.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class PopupDialog extends PopupWindow {

    /**
     * OK button identifier.
     */
    public static final String OK_ID = "ok";

    /**
     * Cancel button identifier.
     */
    public static final String CANCEL_ID = "cancel";

    /**
     * Used to indicate which buttons to display.
     */
    public static enum Buttons {
        OK,
        CANCEL,
        OK_CANCEL
    }

    /**
     * Construct a new <code>PopupDialog</code>.
     *
     * @param title   the window title
     * @param buttons the buttons to display
     */
    public PopupDialog(String title, Buttons buttons) {
        this(title, null, buttons);
    }

    /**
     * Construct a new <code>PopupDialog</code>.
     *
     * @param title   the window title
     * @param style   the window style
     * @param buttons the buttons to display
     */
    public PopupDialog(String title, String style, Buttons buttons) {
        super(title, style);

        if (buttons == Buttons.OK || buttons == Buttons.OK_CANCEL) {
            addButton(OK_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onOK();
                }
            });
        }
        if (buttons == Buttons.CANCEL || buttons == Buttons.OK_CANCEL) {
            addButton(CANCEL_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCancel();
                }
            });
        }
    }

    /**
     * Convenience method, invoked when the OK button is pressed. This closes
     * the window.
     */
    protected void onOK() {
        close();
    }

    /**
     * Convenience method, invoked when the cancel button is pressed. This
     * closes the window.
     */
    protected void onCancel() {
        close();
    }

}
