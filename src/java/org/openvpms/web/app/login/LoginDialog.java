/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.login;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Command;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.webcontainer.command.BrowserRedirectCommand;

import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Login dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LoginDialog extends PopupDialog {

    /**
     * The username field.
     */
    private TextField _username;

    /**
     * The password field.
     */
    private TextField _password;

    /**
     * Dialog style name.
     */
    private static final String STYLE = "LoginDialog";

    /**
     * Label style name.
     */
    private static final String LABEL_STYLE = "LoginDialog.Label";

    /**
     * Layout grid style.
     */
    private static final String GRID_STYLE = "LoginDialog.Grid";

    /**
     * Login title localisation key.
     */
    private static final String LOGIN_KEY = "title.login";

    /**
     * User label localisation key.
     */
    private static final String USER_KEY = "username";

    /**
     * Password label localisation key.
     */
    private static final String PASSWORD_KEY = "password";


    /**
     * Construct a new <code>LoginDialog</code>.
     */
    public LoginDialog() {
        super(Messages.get(LOGIN_KEY), STYLE, Buttons.OK);
        setClosable(false);

        _username = TextComponentFactory.create();
        _password = TextComponentFactory.createPassword();

        _username.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setFocus(_password);
            }
        });
        _password.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onOK();
            }
        });

        Label username = LabelFactory.create(USER_KEY);
        username.setStyleName(LABEL_STYLE);
        Label password = LabelFactory.create(PASSWORD_KEY);
        password.setStyleName(LABEL_STYLE);

        Grid grid = GridFactory.create(
                2, username, _username, password, _password);
        grid.setStyleName(GRID_STYLE);
        getLayout().add(grid);

        setFocus(_username);
    }

    /**
     * Invoked when the OK button is pressed.
     */
    @Override
    protected void onOK() {
        String username = _username.getText();
        String password = _password.getText();

        // @todo need to encode
        Command redirect = new BrowserRedirectCommand("j_acegi_security_check?j_username=" + username + "&j_password=" + password);
        ApplicationInstance.getActive().enqueueCommand(redirect);
    }

    /**
     * Helper to set the focus.
     *
     * @param component the component to focus on
     */
    protected void setFocus(Component component) {
        ApplicationInstance.getActive().setFocusedComponent(component);
    }

}
