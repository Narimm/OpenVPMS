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
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListCellRenderer;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.resource.util.Messages;


/**
 * <code>ListCellRenderer</code> for a {@link LookupListModel}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupListCellRenderer implements ListCellRenderer {

    /**
     * Localised display name for "all".
     */
    private final String ALL = Messages.get("list.all");

    /**
     * Localised display name for "none".
     */
    private final String NONE = Messages.get("list.none");


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
        Object result = value;
        if (value == null) {
            // dummy lookup being rendered.
            AbstractListComponent box = (AbstractListComponent) list;
            LookupListModel model = (LookupListModel) box.getModel();
            Lookup lookup = model.getLookup(index);
            if (lookup.getArchetypeId() == null && lookup.getValue() == null) {
                // dummy lookup
                String code = lookup.getCode();
                if (LookupListModel.ALL.equals(code)) {
                    result = ALL;
                } else if (LookupListModel.NONE.equals(code)) {
                    result = NONE;
                }
            }
        }
        return result;
    }
}
