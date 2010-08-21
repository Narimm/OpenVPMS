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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * CRUD Window for Reminders and Alerts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ReminderCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * Reminder and alert shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {ReminderArchetypes.REMINDER, "act.patientAlert"};

    /**
     * Resend button identifier.
     */
    private static final String RESEND_ID = "resend";


    /**
     * Constructs a <tt>ReminderCRUDWindow</tt>.
     */
    public ReminderCRUDWindow() {
        super(Archetypes.create(SHORT_NAMES, Act.class,
                                Messages.get("patient.reminder.createtype")));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button resend = ButtonFactory.create(RESEND_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onResend();
            }
        });
        buttons.add(resend);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        boolean enableResend = false;
        if (enable) {
            enableResend = TypeHelper.isA(getObject(), ReminderArchetypes.REMINDER);
        }
        buttons.setEnabled(RESEND_ID, enableResend);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(Act act) {
        Party patient = GlobalContext.getInstance().getPatient();
        if (patient != null) {
            try {
                IArchetypeService service
                        = ServiceHelper.getArchetypeService();
                Participation participation
                        = (Participation) service.create(
                        "participation.patient");
                participation.setEntity(new IMObjectReference(patient));
                participation.setAct(new IMObjectReference(act));
                act.addParticipation(participation);
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
        super.onCreated(act);
    }

    /**
     * Invoked to resend a reminder.
     */
    private void onResend() {
        try {
            ResendReminderDialog dialog = ResendReminderDialog.create(getObject());
            if (dialog != null) {
                dialog.addWindowPaneListener(new WindowPaneListener() {
                    public void onClose(WindowPaneEvent event) {
                        onRefresh(getObject());
                    }
                });
                dialog.show();
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

}
