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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.AbstractIMObjectActions;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;

/**
 * CRUD window for HL7 connectors.
 *
 * @author Tim Anderson
 */
public class HL7ConnectorCRUDWindow extends AbstractViewCRUDWindow<Entity> {

    /**
     * Messages button identifier.
     */
    private static final String MESSAGES_ID = "button.messages";

    /**
     * Stop button identifier.
     */
    private static final String STOP_ID = "button.stop";

    /**
     * Start button identifier.
     */
    private static final String START_ID = "button.start";


    /**
     * Constructs an {@link HL7ConnectorCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create. If {@code null}
     *                   the subclass must override {@link #getArchetypes}
     *                   actions should be registered via {@link #setActions(IMObjectActions)}
     * @param context    the context
     * @param help       the help context
     */
    public HL7ConnectorCRUDWindow(Archetypes<Entity> archetypes, Context context, HelpContext help) {
        super(archetypes, new Actions(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(MESSAGES_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onMessages();
            }
        });
        buttons.add(STOP_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onStop();
            }
        });
        buttons.add(START_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onStart();
            }
        });
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
        buttons.setEnabled(MESSAGES_ID, enable);
        Entity connector = getObject();
        Actions actions = getActions();
        buttons.setEnabled(STOP_ID, enable && actions.canStop(connector));
        buttons.setEnabled(START_ID, enable && actions.canStart(connector));
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected Actions getActions() {
        return (Actions) super.getActions();
    }

    /**
     * Invoked when the messages button is pressed.
     * <p/>
     * Displays messages for the selected connector.
     */
    private void onMessages() {
        Entity connector = getObject();
        if (connector != null) {
            HelpContext help = getHelpContext().subtopic("message");
            HL7MessageDialog dialog = new HL7MessageDialog(connector, getContext(), help);
            dialog.show();
        }
    }

    /**
     * Invoked to stop messaging for a connector.
     */
    private void onStop() {
        Entity connector = IMObjectHelper.reload(getObject());
        if (connector != null && getActions().canStop(connector)) {
            getActions().stop(connector);
            onRefresh(connector);
        }
    }

    /**
     * Invoked to start messaging for a connector.
     */
    private void onStart() {
        Entity connector = IMObjectHelper.reload(getObject());
        if (connector != null && getActions().canStart(connector)) {
            getActions().start(connector);
            onRefresh(connector);
        }
    }

    private static class Actions extends AbstractIMObjectActions<Entity> {

        /**
         * Determines if a connector can be stopped.
         *
         * @param connector the connector
         * @return {@code true} if the connector can be stopped
         */
        public boolean canStop(Entity connector) {
            IMObjectBean bean = new IMObjectBean(connector);
            return bean.hasNode("suspended") && !bean.getBoolean("suspended");
        }

        /**
         * Determines if a connector can be started.
         *
         * @param connector the connector
         * @return {@code true} if the connector can be started
         */
        public boolean canStart(Entity connector) {
            IMObjectBean bean = new IMObjectBean(connector);
            return bean.hasNode("suspended") && bean.getBoolean("suspended");
        }

        /**
         * Stops a connector.
         *
         * @param connector the connector
         */
        public void stop(Entity connector) {
            IMObjectBean bean = new IMObjectBean(connector);
            bean.setValue("suspended", true);
            bean.save();
        }

        /**
         * Starts a connector.
         *
         * @param connector the connector
         */
        public void start(Entity connector) {
            IMObjectBean bean = new IMObjectBean(connector);
            bean.setValue("suspended", false);
            bean.save();
        }
    }
}
