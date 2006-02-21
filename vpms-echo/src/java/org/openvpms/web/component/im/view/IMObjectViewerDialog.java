package org.openvpms.web.component.im.view;

import org.openvpms.web.component.dialog.PopupDialog;


/**
 * Displays an {@link IMObjectViewer} in popup window.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectViewerDialog extends PopupDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "IMObjectViewerDialog";


    /**
     * Construct a new <code>IMObjectViewerDialog</code>.
     *
     * @param browser the browser to display
     */
    public IMObjectViewerDialog(IMObjectViewer browser) {
        super(browser.getTitle(), STYLE, Buttons.OK);
        setModal(true);
        getLayout().add(browser.getComponent());
        show();
    }
}
