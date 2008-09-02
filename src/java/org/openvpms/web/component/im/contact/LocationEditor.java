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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.contact;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;


/**
 * An editor for <em>contact.location</em> contacts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocationEditor extends AbstractIMObjectEditor {

    /**
     * Construct a new <tt>LocationEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public LocationEditor(IMObject object, IMObject parent,
                          LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        final Property suburb = getProperty("suburb");
        suburb.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onSuburbChanged((String) suburb.getValue());
            }
        });
    }

    /**
     * Updates the postcode when the suburb changes.
     *
     * @param suburb the suburb
     */
    protected void onSuburbChanged(String suburb) {
        Lookup lookup = LookupServiceHelper.getLookupService().getLookup(
                "lookup.suburb", suburb);
        Property postCode = getProperty("postcode");
        if (lookup != null) {
            IMObjectBean bean = new IMObjectBean(lookup);
            postCode.setValue(bean.getValue("postCode"));
        } else {
            postCode.setValue(null);
        }
    }
}
