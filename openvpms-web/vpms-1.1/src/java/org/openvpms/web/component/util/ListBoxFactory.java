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

import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;

import java.util.List;

/**
 * Factory for {@link ListBox}es.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-04 06:28:25Z $
 */
public final class ListBoxFactory extends ComponentFactory {

    /**
     * Creates a new list box with the provided model.
     *
     * @param model the model
     */
    public static ListBox create(ListModel model) {
        ListBox list = new ListBox(model);
        setDefaultStyle(list);
        if (model.size() > 0) {
            // default to the first element
            list.setSelectedIndex(0);
        }
        return list;
    }

    /**
     * Create a new select field that will initially contain the provided list
     * of items.
     *
     * @param items the items to add
     * @return a new select field
     */
    public static ListBox create(List items) {
        return create(items.toArray());
    }

    /**
     * Create a new list box that will initially contain the provided array
     * of items.
     *
     * @param items the items to add
     * @return a new select field
     */
    public static ListBox create(Object[] items) {
        return create(new DefaultListModel(items));
    }

}
