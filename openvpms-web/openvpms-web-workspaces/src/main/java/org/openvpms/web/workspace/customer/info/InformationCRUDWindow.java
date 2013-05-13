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

package org.openvpms.web.workspace.customer.info;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.workflow.merge.MergeWorkflow;


/**
 * Information CRUD window.
 *
 * @author Tim Anderson
 */
public class InformationCRUDWindow extends AbstractViewCRUDWindow<Party> {

    /**
     * Merge button identifier.
     */
    private static final String MERGE_ID = "merge";


    /**
     * Constructs an {@code InformationCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public InformationCRUDWindow(Archetypes<Party> archetypes, Context context, HelpContext help) {
        super(archetypes, DefaultIMObjectActions.<Party>getInstance(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        if (UserHelper.isAdmin(getContext().getUser())) {
            // only provide merging for admin users
            Button merge = ButtonFactory.create(MERGE_ID, new ActionListener() {
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
        boolean enableMerge = enable && TypeHelper.isA(getObject(), CustomerArchetypes.PERSON);
        buttons.setEnabled(MERGE_ID, enableMerge);
    }

    /**
     * Merges the current customer with another.
     */
    private void onMerge() {
        HelpContext help = getHelpContext().subtopic("merge");
        final MergeWorkflow workflow = new CustomerMergeWorkflow(getObject(), help);
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
