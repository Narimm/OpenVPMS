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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace;

import echopointng.LabelEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.echo.text.PasswordField;
import org.openvpms.web.resource.i18n.Messages;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Dialog that prompts the user to re-enter their password to unlock their screen.
 *
 * @author Tim Anderson
 */
class LockScreenDialog extends PopupDialog {

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
        super(Messages.get("lockscreen.title"), "LockScreenDialog", OK);
        setModal(true);
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
     * Invoked when the 'OK' button is pressed.
     */
    @Override
    protected void onOK() {
        User user = getUser();
        if (user != null) {
            if (StringUtils.equals(password.getText(), user.getPassword())) {
                super.onOK();
            } else {
                password.setText(null);
                if (container.getComponentCount() == 3) {
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
        Label loggedIn = LabelFactory.create("lockscreen.loggedin");
        Label name = LabelFactory.create(null, Styles.BOLD);
        User user = getUser();
        if (user != null) {
            name.setText(user.getUsername());
        }

        LabelEx space = new LabelEx(new XhtmlFragment(TableHelper.SPACER));
        container = ColumnFactory.create(Styles.WIDE_CELL_SPACING,
                                         RowFactory.create(loggedIn, space, name),
                                         LabelFactory.create("lockscreen.message"), password);
        Row row = RowFactory.create(Styles.LARGE_INSET, container);
        getLayout().add(row);
    }

    /**
     * Returns the current user.
     *
     * @return the current user, or {@code null} if there is no user
     */
    private User getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof User) ? (User) principal : null;
    }
}
