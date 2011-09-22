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

package org.openvpms.web.component.im.contact;

import nextapp.echo2.app.Column;
import org.openvpms.web.app.sms.sms.SMSEditor;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


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
     * @param phone the phone number to send to. May be <tt>null</tt>
     */
    public SMSDialog(String phone) {
        this(phone != null ? new String[]{phone} : null);
    }

    /**
     * Constructs an <tt>SMSDialog</tt>.
     *
     * @param phones the phone numbers to select from. May be <tt>null</tt>
     */
    public SMSDialog(String[] phones) {
        super(Messages.get("sms.send.title"), OK_CANCEL);
        setModal(true);

        editor = new SMSEditor(phones);
        Column column = ColumnFactory.create("Inset", editor.getComponent());
        getLayout().add(column);
        getFocusGroup().add(0, editor.getFocusGroup());
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
