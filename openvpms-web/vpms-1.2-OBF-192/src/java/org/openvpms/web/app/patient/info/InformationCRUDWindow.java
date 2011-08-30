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
import org.openvpms.web.app.workflow.checkin.CheckInWorkflow;
import org.openvpms.web.app.workflow.merge.MergeWorkflow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;
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
     * The merge button.
     */
    private Button merge;


    /**
     * Creates a new <tt>InformationCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public InformationCRUDWindow(Archetypes<Party> archetypes) {
        super(archetypes);
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
        if (merge == null) {
            merge = ButtonFactory.create("merge", new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onMerge();
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
        buttons.remove(checkIn);
        buttons.remove(merge);
        if (enable) {
            buttons.add(checkIn);
            if (UserHelper.isAdmin(GlobalContext.getInstance().getUser())) {
                buttons.add(merge);
            }
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

    /**
     * Merges the current patient with another.
     */
    private void onMerge() {
        final MergeWorkflow workflow = new PatientMergeWorkflow(getObject());
        workflow.addTaskListener(new TaskListener() {
            /**
             * Invoked when a task event occurs.
             *
             * @param event the event
             */
            public void taskEvent(TaskEvent event) {
                if (event.getType() == TaskEvent.Type.COMPLETED) {
                    onRefresh(getObject());
                }
            }
        });
        workflow.start();
    }


}
