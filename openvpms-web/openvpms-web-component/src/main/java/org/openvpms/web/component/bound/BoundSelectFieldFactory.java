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

package org.openvpms.web.component.bound;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.SelectFieldFactory;


/**
 * Factory for {@link BoundSelectField}s.
 *
 * @author Tim Anderson
 */
public final class BoundSelectFieldFactory extends SelectFieldFactory {

    /**
     * Creates a new bound select field.
     *
     * @param property the property to bind
     * @param model    the model
     */
    public static SelectField create(Property property, ListModel model) {
        SelectField select = new BoundSelectField(property, model);
        setDefaultStyle(select);
        return select;
    }

}
