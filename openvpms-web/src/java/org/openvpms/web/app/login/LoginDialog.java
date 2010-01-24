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
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.webcontainer.command.BrowserRedirectCommand;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.ServletHelper;


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
    private TextField username;

    /**
     * The password field.
     */
    private TextField password;

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
        super(Messages.get(LOGIN_KEY), STYLE, OK);
        setClosable(false);

        username = TextComponentFactory.create();
        password = TextComponentFactory.createPassword();

        username.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setFocus(password);
            }
        });
        password.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onOK();
            }
        });

        Label userLabel = LabelFactory.create(USER_KEY, LABEL_STYLE);
        Label passLabel = LabelFactory.create(PASSWORD_KEY, LABEL_STYLE);

        Grid grid = GridFactory.create(
                2, userLabel, username, passLabel, password);
        grid.setStyleName(GRID_STYLE);
        getLayout().add(grid);

        getFocusGroup().add(0, username);
        getFocusGroup().add(1, password);
        setFocus(username);
    }

    /**
     * Invoked when the OK button is pressed.
     */
    @Override
    protected void onOK() {
        String name = username.getText();
        String pass = password.getText();

        String check = "j_spring_security_check?j_username=" + name + "&j_password=" + pass;
        Command redirect = new BrowserRedirectCommand(ServletHelper.getRedirectURI(check));
        ApplicationInstance.getActive().enqueueCommand(redirect);
    }

}
