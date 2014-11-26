/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.echo.dialog.MessageDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.PasswordField;
import org.openvpms.web.resource.i18n.Messages;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Dialog that prompts the user to re-enter their password to unlock their screen.
 *
 * @author Tim Anderson
 */
class LockScreenDialog extends MessageDialog {

    /**
     * The password.
     */
    private PasswordField password;

    /**
     * The message/password/error container.
     */
    private Column container;


    /**
     * Constructs a {@link LockScreenDialog}.
     *
     * @param app the app
     */
    public LockScreenDialog(final OpenVPMSApp app) {
        super(Messages.get("lockscreen.title"), Messages.get("lockscreen.message"), OK);
        setClosable(false);
        addButton("button.logout", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                app.logout();
            }
        });
        password = TextComponentFactory.createPassword(20);
        password.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onOK();
            }
        });
    }

    /**
     * Processes a user request to close the window (via the close button).
     * <p/>
     * This restores the previous focus
     */
    @Override
    public void userClose() {
        if (getAction() == null) {
            // need an action, otherwise onOK() will be invoked as it is the default action
            setAction(CANCEL_ID);
        }
        super.userClose();
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            if (StringUtils.equals(password.getText(), user.getPassword())) {
                super.onOK();
            } else {
                password.setText(null);
                if (container.getComponentCount() == 2) {
                    container.add(LabelFactory.create("lockscreen.error", "login.error"));
                }
            }
        }
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create();
        message.setText(getMessage());
        container = ColumnFactory.create(Styles.WIDE_CELL_SPACING, message, password);
        Row row = RowFactory.create(Styles.LARGE_INSET, container);
        getLayout().add(row);
    }
}
