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

package org.openvpms.web.app.workflow.worklist;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.app.workflow.WorkflowCRUDWindow;
import org.openvpms.web.app.workflow.checkout.CheckOutWorkflow;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;


/**
 * Task CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskCRUDWindow extends WorkflowCRUDWindow {

    /**
     * The check-out button.
     */
    private Button checkOut;

    /**
     * Check-out button identifier.
     */
    private static final String CHECKOUT_ID = "checkout";


    /**
     * Constructs a new <code>TaskCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public TaskCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        super.layoutButtons(buttons);
        if (checkOut == null) {
            checkOut = ButtonFactory.create(CHECKOUT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCheckOut();
                }
            });
        }
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        super.enableButtons(enable);
        Row buttons = getButtons();
        if (enable) {
            Act act = (Act) getObject();
            String status = act.getStatus();
            if ("Pending".equals(status) || "In Progress".equals(status)) {
                if (buttons.indexOf(checkOut) == -1) {
                    buttons.add(checkOut);
                }
            } else {
                buttons.remove(checkOut);
            }
        } else {
            buttons.remove(checkOut);
        }
    }

    /**
     * Invoked when the 'check-out' button is pressed.
     */
    private void onCheckOut() {
        CheckOutWorkflow workflow = new CheckOutWorkflow((Act) getObject());
        workflow.setTaskListener(new TaskListener() {
            public void taskEvent(TaskEvent event) {
                onRefresh(getObject());
            }
        });
        workflow.start();
    }

}
