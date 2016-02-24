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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.communication;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Customer communication CRUD window.
 *
 * @author Tim Anderson
 */
public class CommunicationCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * The archetypes created by the New button.
     */
    public static final Archetypes<Act> ARCHETYPES = Archetypes.create(CommunicationArchetypes.NOTE, Act.class);

    /**
     * The archetypes created by the Log button.
     */
    public static final Archetypes<Act> LOG_ARCHETYPES = Archetypes.create(
            new String[]{CommunicationArchetypes.EMAIL, CommunicationArchetypes.MAIL, CommunicationArchetypes.PHONE,
                         CommunicationArchetypes.SMS}, Act.class, Messages.get("customer.communication.createtype"));
    /**
     * The log button.
     */
    private static final String LOG_ID = "button.log";


    /**
     * Constructs an {@link CommunicationCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public CommunicationCRUDWindow(Context context, HelpContext help) {
        super(ARCHETYPES, ActActions.edit(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createNewButton());
        buttons.add(ButtonFactory.create(LOG_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onCreate(LOG_ARCHETYPES);
            }
        }));
        buttons.add(createEditButton());
        buttons.add(createDeleteButton());
    }


}
