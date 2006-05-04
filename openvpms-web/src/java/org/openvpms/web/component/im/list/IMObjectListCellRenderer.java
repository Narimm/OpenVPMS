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

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.ListCellRenderer;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * List cell renderer that display's an {@link IMObject}'s description.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectListCellRenderer implements ListCellRenderer {

    /**
     * Renders an item in a list.
     *
     * @param list  the list component
     * @param value the item value
     * @param index the item index
     * @return the rendered form of the list cell
     */
    public Object getListCellRendererComponent(Component list, Object value,
                                               int index) {
        String result = null;
        if (value instanceof IMObject) {
            result = ((IMObject) value).getName();
        }
        if (result == null) {
            result = "";
        }
        return result;
    }
}

