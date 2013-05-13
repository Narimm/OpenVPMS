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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;


/**
 * Editor for <em>lookup.suburb</em> lookups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SuburbLookupEditor extends AbstractLookupEditor {

    /**
     * Creates a new <tt>SuburbLookupEditor</tt>.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>.
     */
    public SuburbLookupEditor(Lookup object, IMObject parent, LayoutContext context) {
        super(object, parent, context);

        if (object.isNew()) {
            Property postCode = getProperty("postCode");
            if (postCode != null) {
                // update the code when the postcode changes
                postCode.addModifiableListener(new ModifiableListener() {
                    public void modified(Modifiable modifiable) {
                        updateCode();
                    }
                });
            }
        }
    }

    /**
     * Creates a code for the lookup based on the name and postcode.
     *
     * @return a new code
     */
    @Override
    protected String createCode() {
        String result = super.createCode();
        Property postCode = getProperty("postCode");
        if (postCode != null) {
            result += "_" + postCode.getValue();
        }
        return result;
    }
}
