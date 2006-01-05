package org.openvpms.web.app;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.TextComponentFactory;
import org.openvpms.web.component.SplitPaneFactory;
import org.openvpms.web.util.Messages;


/**
 * Login window.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class LoginPane extends ContentPane {

    /**
     * The username field.
     */
    private TextField _username;

    /**
     * The password field.
     */
    private TextField _password;

    /**
     * Layout extent. @todo stylesheet
     */
    private static final Extent PX_300 = new Extent(300, Extent.PX);

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(LoginPane.class);


    /**
     * Construct a new <code>LoginPane</code>.
     */
    public LoginPane() {
        setStyleName("LoginPane.ContentPane");

        _username = TextComponentFactory.create();
        _username.setWidth(PX_300);

        _password = TextComponentFactory.createPassword();
        _password.setWidth(PX_300);

        _username.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setFocus(_password);
            }
        });
        _password.addActionListener(getLoginListener());

        Label username = LabelFactory.create("username");
        username.setStyleName("LoginPane.Prompt");
        Label password = LabelFactory.create("password");
        password.setStyleName("LoginPane.Prompt");

        Grid grid = GridFactory.create(2, username, _username, password, _password);
        grid.setStyleName("LoginPane.LayoutGrid");

        Button button = ButtonFactory.create("ok");
        button.setStyleName("ControlPane.Button");
        button.addActionListener(getLoginListener());

        WindowPane window = new WindowPane();
        window.setStyleName("LoginPane.LoginWindow");
        window.setTitle(Messages.getString("title.login"));
        window.setClosable(false);

        Row controlRow = RowFactory.create(button);
        controlRow.setStyleName("ControlPane");

        SplitPane pane = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, controlRow, grid);
        pane.setSeparatorPosition(new Extent(32));

        window.add(pane);
        add(window);

        setFocus(_username);
    }

    protected void doLogin() {
        String username = _username.getText();
        String password = _password.getText();

        if (authenticate(username, password)) {
            OpenVPMSApp.getInstance().setContent(new ApplicationContentPane());
            _log.debug(username + " successfully logged in to OpenVPMS");
        } else {
            _log.debug(username + " attempted to log in to OpenVPMS but failed.");
        }
    }

    /**
     * Authenticate a user.
     *
     * @param username the user's nane
     * @param password the user's password
     * @return <code>true</code> if username and password are valid; otherwise <code>false</code>
     */
    protected boolean authenticate(String username, String password) {
        return true;
    }

    /**
     * Helper to set the focus.
     *
     * @param component the component to focus on
     */
    protected void setFocus(Component component) {
        ApplicationInstance.getActive().setFocusedComponent(component);
    }

    /**
     * Helper to returns a listener method to invoke {@link #doLogin}.
     *
     * @return s listener to invoke {@link #doLogin}
     */
    protected ActionListener getLoginListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doLogin();
            }
        };
    }

}
