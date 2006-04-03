package org.openvpms.web.app.login;

import nextapp.echo2.app.ContentPane;


/**
 * Login pane. This simply pops up an {@link LoginDialog}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision$ $Date$
 */
public class LoginPane extends ContentPane {

    /**
     * Style name.
     */
    private static final String STYLE = "LoginPane";


    /**
     * Construct a new <code>LoginPane</code>.
     */
    public LoginPane() {
        setStyleName(STYLE);
    }

    /**
     * Initialise this.
     */
    @Override
    public void init() {
        super.init();
        new LoginDialog();
    }

}
