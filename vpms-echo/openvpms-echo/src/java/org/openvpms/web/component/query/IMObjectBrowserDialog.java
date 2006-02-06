package org.openvpms.web.component.query;

import org.openvpms.web.component.dialog.PopupDialog;


/**
 * Displays an {@link IMObjectBrowser} in popup window.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectBrowserDialog extends PopupDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "IMObjectBrowserDialog";


    /**
     * Construct a new <code>IMObjectBrowserDialog</code>.
     *
     * @param browser the browser to display
     */
    public IMObjectBrowserDialog(IMObjectBrowser browser) {
        super(browser.getTitle(), STYLE, Buttons.OK);
        setModal(true);
        getLayout().add(browser.getComponent());
        show();
    }
}
