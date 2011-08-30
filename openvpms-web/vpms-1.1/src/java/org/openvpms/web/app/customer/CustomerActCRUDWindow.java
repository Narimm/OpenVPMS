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

package org.openvpms.web.app.customer;

import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * CRUD Window for customer acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class CustomerActCRUDWindow<T extends Act>
        extends ActCRUDWindow<T> {

    /**
     * Determines if the current act is posted or not.
     */
    private boolean posted;


    /**
     * Create a new <tt>CustomerActCRUDWindow</tt>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create
     */
    public CustomerActCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(T object) {
        posted = (object != null) && POSTED.equals(object.getStatus());
        super.setObject(object);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(T act) {
        Party customer = GlobalContext.getInstance().getCustomer();
        if (customer != null) {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                Participation participation
                        = (Participation) service.create(
                        "participation.customer");
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
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(T object, boolean isNew) {
        boolean prevPosted = posted;
        super.onSaved(object, isNew);
        String status = object.getStatus();
        if (!prevPosted && POSTED.equals(status)) {
            onPosted(object);
        }
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
