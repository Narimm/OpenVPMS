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

package org.openvpms.web.app.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;


/**
 * Lookup workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupWorkspace extends CRUDWorkspace<Lookup> {

    /**
     * Construct a new <tt>LookupWorkspace</tt>.
     */
    public LookupWorkspace() {
        super("admin", "lookup", new ShortNameList("lookup.*"));
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <tt>null</tt>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Lookup) {
            setObject((Lookup) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Lookup.class.getName());
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Lookup object, boolean isNew) {
        super.onSaved(object, isNew);
        if (TypeHelper.isA(object, "lookup.macro")) {
            refreshMacros();
        } else if (TypeHelper.isA(object, "lookup.currency")) {
            refreshCurrencies();
        }
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(Lookup object) {
        super.onDeleted(object);
        if (TypeHelper.isA(object, "lookup.macro")) {
            refreshMacros();
        } else if (TypeHelper.isA(object, "lookup.currency")) {
            refreshCurrencies();
        }
    }

    /**
     * Helper to refresh the macro cache if a macro is saved or deleted.
     */
    private void refreshMacros() {
        try {
            ServiceHelper.getMacroCache().refresh();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Helper to refresh the currencies cache if a currency is saved or deleted.
     */
    private void refreshCurrencies() {
        try {
            ServiceHelper.getCurrencies().refresh();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }
}
