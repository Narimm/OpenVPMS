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

package org.openvpms.web.component.im.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.PropertyComponentEditor;
import org.openvpms.web.component.property.Property;

/**
 * An editor for lookup properties that uses a {@link LookupField} to display the lookups.
 *
 * @author Tim Anderson
 */
public class DefaultLookupPropertyEditor extends PropertyComponentEditor implements LookupPropertyEditor {

    /**
     * Constructs an {@link DefaultLookupPropertyEditor}.
     *
     * @param property the property being edited
     */
    public DefaultLookupPropertyEditor(Property property, IMObject parent) {
        super(property, LookupFieldFactory.create(property, parent));
    }

}
