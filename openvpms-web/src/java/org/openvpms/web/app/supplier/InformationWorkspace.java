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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.BasicCRUDWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.resource.util.Messages;


/**
 * Supplier information workspace.
 *
 * @author Tim Anderson
 */
public class InformationWorkspace extends BasicCRUDWorkspace<Party> {

    /**
     * Constructs an {@code InformationWorkspace}.
     *
     * @param context the context
     */
    public InformationWorkspace(Context context) {
        super("supplier", "info", Archetypes.create("party.supplier*", Party.class, Messages.get("supplier.info.type")),
              context);
        setMailContext(new SupplierMailContext(context, getHelpContext()));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        getContext().setSupplier(object);
    }

}
