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

package org.openvpms.web.component.util;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.web.component.bound.BoundSelectField;
import org.openvpms.web.component.property.Property;

import java.util.List;


/**
 * Factory for {@link SelectField}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class SelectFieldFactory extends ComponentFactory {

    /**
     * Creates a new select field with the provided model.
     *
     * @param model the model
     */
    public static SelectField create(ListModel model) {
        return create(model, null);
    }

    /**
     * Creates a new select field with the provided model.
     *
     * @param model    the model
     * @param selected the initial selection, or {@code null} to select the first item
     */
    public static SelectField create(ListModel model, Object selected) {
        SelectField select = new SelectField(model);
        setDefaultStyle(select);
        if (selected != null) {
            select.setSelectedItem(selected);
        } else if (model.size() != 0) {
            select.setSelectedIndex(0);
        }
        return select;
    }

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

    /**
     * Create a new select field that will initially contain the provided list of items.
     *
     * @param items the items to add
     * @return a new select field
     */
    public static SelectField create(List items) {
        return create(items.toArray());
    }

    /**
     * Create a new select field that will initially contain the provided array
     * of items.
     *
     * @param items the items to add
     * @return a new select field
     */
    public static SelectField create(Object[] items) {
        return create(new DefaultListModel(items));
    }

}
