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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.info;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.app.workflow.merge.MergeWorkflow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;


/**
 * Information CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InformationCRUDWindow extends AbstractViewCRUDWindow<Party> {

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
        if (merge == null) {
            merge = ButtonFactory.create("merge", new ActionListener() {
                public void onAction(ActionEvent event) {
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
        buttons.remove(merge);
        if (enable) {
            // only add the merge for admin users
            User user = GlobalContext.getInstance().getUser();
            if (TypeHelper.isA(getObject(), "party.customerperson")
                    && UserHelper.isAdmin(user)) {
                buttons.add(merge);
            }
        }
    }

    /**
     * Merges the current customer with another.
     */
    private void onMerge() {
        final MergeWorkflow workflow = new CustomerMergeWorkflow(getObject());
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
