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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.app.workflow.checkin.CheckInWorkflow;
import org.openvpms.web.app.workflow.merge.MergeWorkflow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.resource.util.Messages;


/**
 * Information CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InformationCRUDWindow extends AbstractViewCRUDWindow<Party> {

    /**
     * The check-in button identifier.
     */
    private static final String CHECKIN_ID = "checkin";

    /**
     * The merge button identifier.
     */
    private static final String MERGE_ID = "merge";


    /**
     * Creates a new <tt>InformationCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public InformationCRUDWindow(Archetypes<Party> archetypes) {
        super(archetypes, DefaultIMObjectActions.<Party>getInstance());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button checkIn = ButtonFactory.create("checkin", new ActionListener() {
            public void onAction(ActionEvent event) {
                onCheckIn();
            }
        });
        buttons.add(checkIn);
        if (UserHelper.isAdmin(GlobalContext.getInstance().getUser())) {
            // only provide merge for admin users
            Button merge = ButtonFactory.create("merge", new ActionListener() {
                public void onAction(ActionEvent event) {
                    onMerge();
                }
            });
            buttons.add(merge);
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
        buttons.setEnabled(CHECKIN_ID, enable);
        buttons.setEnabled(MERGE_ID, enable);
    }

    /**
     * Checks in the current patient.
     */
    private void onCheckIn() {
        GlobalContext context = GlobalContext.getInstance();
        Party customer = context.getCustomer();
        Party patient = context.getPatient();
        User clinician = context.getClinician();
        if (customer != null && patient != null) {
            CheckInWorkflow workflow = new CheckInWorkflow(customer, patient, clinician, context);
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
        workflow.addTaskListener(new DefaultTaskListener() {
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
