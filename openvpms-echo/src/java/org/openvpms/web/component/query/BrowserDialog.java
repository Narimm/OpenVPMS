package org.openvpms.web.component.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.PopupDialog;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class BrowserDialog extends PopupDialog {

    /**
     * The browser.
     */
    private Browser _browser;

    /**
     * The selected object.
     */
    private IMObject _selected;

    /**
     * Window style name.
     */
    private static final String STYLE = "BrowserDialog";


    /**
     * Construct a new <code>BrowserDialog</code>.
     *
     * @param browser the editor
     */
    public BrowserDialog(String title, Browser browser) {
        super(title, STYLE, Buttons.OK_CANCEL);
        _browser = browser;
        getLayout().add(browser);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none was selected
     */
    public IMObject getSelected() {
        return _selected;
    }

    /**
     * Select the current object, and close the browser.
     */
    @Override
    protected void onOK() {
        _selected = _browser.getSelected();
        close();
    }


}
