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
 */

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.help.HelpContext;


/**
 * CRUD Window for customer acts.
 *
 * @author Tim Anderson
 */
public abstract class CustomerActCRUDWindow<T extends Act>
    extends ActCRUDWindow<T> {

    /**
     * Constructs a {@code CustomerActCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param object     the CRUD object
     * @param context    the context
     * @param help       the help context
     */
    public CustomerActCRUDWindow(Archetypes<T> archetypes, ActActions<T> object, Context context, HelpContext help) {
        super(archetypes, object, context, help);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(T act) {
        Party customer = getContext().getCustomer();
        if (customer != null) {
            try {
                IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
                Participation participation = (Participation) service.create("participation.customer");
                participation.setEntity(new IMObjectReference(customer));
                participation.setAct(new IMObjectReference(act));
                act.addParticipation(participation);
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
        super.onCreated(act);
    }

    /**
     * Invoked when posting of an act is complete. This pops up a dialog to
     * print the act
     *
     * @param act the act
     */
    @Override
    protected void onPosted(T act) {
        print(act);
    }
}
