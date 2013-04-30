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

package org.openvpms.web.app.supplier;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * CRUD Window for supplier acts.
 *
 * @author Tim Anderson
 */
public abstract class SupplierActCRUDWindow<T extends Act> extends ActCRUDWindow<T> {

    /**
     * Constructs a {@code SupplierActCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param actions    determines the operations that may be performed on the selected object
     * @param context    the context
     * @param help       the help context
     */
    public SupplierActCRUDWindow(Archetypes<T> archetypes, ActActions<T> actions, Context context, HelpContext help) {
        super(archetypes, actions, context, help);
    }

    /**
     * Invoked when a new object has been created.
     * <p/>
     * This implementation adds a supplier participation, if there is a
     * supplier in the global context.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(final T act) {
        Party supplier = getContext().getSupplier();
        addParticipations(act, supplier, null);
        super.onCreated(act);
    }

    /**
     * Helper to add supplier and stock location participations to an act.
     *
     * @param act      the act
     * @param supplier the supplier. May be {@code null}
     * @param location the stock location. May be {@code null}
     */
    protected void addParticipations(T act, Party supplier, Party location) {
        try {
            ActBean bean = new ActBean(act);
            if (supplier != null) {
                bean.addParticipation("participation.supplier",
                                      supplier);
            }
            if (location != null) {
                bean.addParticipation("participation.stockLocation",
                                      location);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
