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

package org.openvpms.web.app.patient.info;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.app.workflow.checkin.CheckInWorkflow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Information CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InformationCRUDWindow extends AbstractViewCRUDWindow<Party> {

    /**
     * The check-in button.
     */
    private Button checkIn;


    /**
     * Creates a new <code>InformationCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public InformationCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        if (checkIn == null) {
            checkIn = ButtonFactory.create("checkin", new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCheckIn();
                }
            });
        }
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
        if (enable) {
            buttons.add(checkIn);
        } else {
            buttons.remove(checkIn);
        }
    }

    /**
     * Checks in the current patient.
     */
    private void onCheckIn() {
        Party customer = GlobalContext.getInstance().getCustomer();
        Party patient = GlobalContext.getInstance().getPatient();
        User clinician = GlobalContext.getInstance().getClinician();
        if (customer != null && patient != null) {
            CheckInWorkflow workflow
                    = new CheckInWorkflow(customer, patient, clinician);
            workflow.start();
        } else {
            String title = Messages.get("patient.checkin.title");
            String msg = Messages.get("patient.checkin.needcustomerpatient");
            ErrorHelper.show(title, msg);
        }
    }

}
