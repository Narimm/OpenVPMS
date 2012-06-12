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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.sms;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.Arrays;
import java.util.List;

/**
 * Dialog to edit and send SMS messages.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSDialog extends PopupDialog {

    /**
     * The SMS editor.
     */
    private SMSEditor editor;


    /**
     * Constructs an <tt>SMSDialog</tt>.
     *
     * @param phone   the phone contact to send to
     * @param context the context
     */
    public SMSDialog(Contact phone, Context context) {
        this(Arrays.asList(phone), context);
    }

    /**
     * Constructs an <tt>SMSDialog</tt>.
     *
     * @param phones  the phone numbers to select from. May be <tt>null</tt>
     * @param context the context
     */
    public SMSDialog(List<Contact> phones, Context context) {
        super(Messages.get("sms.send.title"), "SMSDialog", OK_CANCEL);
        setModal(true);

        editor = new SMSEditor(phones);
        editor.declareVariable("patient", context.getPatient());
        editor.declareVariable("customer", context.getCustomer());

        Column column = ColumnFactory.create("Inset", editor.getComponent());
        getLayout().add(column);
        getFocusGroup().add(0, editor.getFocusGroup());

        getButtons().addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_M, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMacro();
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
     */
    protected void onMacro() {
        MacroDialog dialog = new MacroDialog();
        dialog.show();
    }

    /**
     * Sends the message.
     *
     * @return <tt>true</tt> if the message was sent
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
