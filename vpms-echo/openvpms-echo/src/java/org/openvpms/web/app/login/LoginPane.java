package org.openvpms.web.app.login;

import nextapp.echo2.app.ContentPane;


/**
 * Login pane. This simply pops up an {@link LoginDialog}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
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
