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

package org.openvpms.web.app.supplier;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;


/**
 * Supplier information workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends CRUDWorkspace<Party> {

    /**
     * Construct a new <tt>InformationWorkspace</tt>.
     */
    public InformationWorkspace() {
        super("supplier", "info", new ShortNameList("party.supplier*"));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        GlobalContext.getInstance().setSupplier(object);
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <tt>null</tt>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Party) {
            setObject((Party) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Party.class.getName());
        }
    }


}
