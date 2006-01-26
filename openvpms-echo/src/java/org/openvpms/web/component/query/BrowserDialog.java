package org.openvpms.web.component.query;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.PopupDialog;


/**
 * Displays an {@link Browser} in a popup dialog.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BrowserDialog extends PopupDialog {

    /**
     * New button identifier.
     */
    public static final String NEW_ID = "new";

    /**
     * The browser.
     */
    private Browser _browser;

    /**
     * The selected object.
     */
    private IMObject _selected;

    /**
     * Determines if the user wants to create a new object. Set when the 'New'
     * button is pressed.
     */
    private boolean _createNew = false;

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

        addButton(NEW_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });
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
     * Determines if the 'New' button was selected, indicating that a new object
     * should be created.
     *
     * @return <code>true</code> if 'New' was selected
     */
    public boolean createNew() {
        return _createNew;
    }

    /**
     * Select the current object, and close the browser.
     */
    @Override
    protected void onOK() {
        _selected = _browser.getSelected();
        close();
    }

    /**
     * Flags that the user wants to create a new instance, and closes the
     * browser.
     */
    protected void onNew() {
        _createNew = true;
        close();
    }

}
