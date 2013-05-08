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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.sms;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ReloadingContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.component.macro.MacroVariables;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Dialog to edit and send SMS messages.
 *
 * @author Tim Anderson
 */
public class SMSDialog extends PopupDialog {

    /**
     * The SMS editor.
     */
    private SMSEditor editor;


    /**
     * Constructs an {@code SMSDialog}.
     *
     * @param phone   the phone contact to send to
     * @param context the context
     * @param help    the help context
     */
    public SMSDialog(Contact phone, Context context, HelpContext help) {
        this(Arrays.asList(phone), context, help);
    }

    /**
     * Constructs an {@code SMSDialog}.
     *
     * @param phones  the phone numbers to select from. May be {@code null}
     * @param context the context
     * @param help    the help context
     */
    public SMSDialog(List<Contact> phones, final Context context, HelpContext help) {
        super(Messages.get("sms.send.title"), "SMSDialog", OK_CANCEL, help);
        setModal(true);

        MacroVariables variables = new MacroVariables(new ReloadingContext(context),
                                                      ServiceHelper.getArchetypeService(),
                                                      ServiceHelper.getLookupService());
        editor = new SMSEditor(phones, variables);

        Column column = ColumnFactory.create("Inset", editor.getComponent());
        getLayout().add(column);
        getFocusGroup().add(0, editor.getFocusGroup());

        getButtons().addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_M, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMacro(context);
            }
        });
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        if (send()) {
            super.onOK();
        }
    }

    /**
     * Displays the macros.
     *
     * @param context the context
     */
    protected void onMacro(Context context) {
        MacroDialog dialog = new MacroDialog(context, getHelpContext());
        dialog.show();
    }

    /**
     * Sends the message.
     *
     * @return {@code true} if the message was sent
     */
    private boolean send() {
        boolean result = false;
        try {
            editor.send();
            result = true;
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

}
