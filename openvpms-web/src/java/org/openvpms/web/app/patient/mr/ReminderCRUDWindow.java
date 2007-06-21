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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
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
    private static final String[] SHORT_NAMES = {"act.patientReminder",
                                                 "act.patientAlert"};

    /**
     * Create a new <code>ReminderCRUDWindow</code>.
     */
    public ReminderCRUDWindow() {
        super(Messages.get("patient.reminder.createtype"),
              new ShortNameList(SHORT_NAMES));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
        } else {
            buttons.add(getCreateButton());
        }
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

}
