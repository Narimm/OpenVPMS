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
import org.openvpms.component.business.domain.im.lookup.Lookup;


/**
 * <code>ListCellRenderer</code> for a {@link LookupListModel}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupListCellRenderer extends AllNoneListCellRenderer<String> {

    /**
     * The singleton instance.
     */
    public static LookupListCellRenderer INSTANCE
        = new LookupListCellRenderer();


    /**
     * Creates a new <tt>LookupListCellRenderer</tt>.
     */
    protected LookupListCellRenderer() {
        super(String.class);
    }

    /**
     * Renders an object.
     *
     * @param list   the list component
     * @param object the object to render
     * @param index  the object index
     * @return the rendered object
     */
    protected Object getComponent(Component list, String object, int index) {
        AbstractListComponent l = (AbstractListComponent) list;
        LookupListModel model = (LookupListModel) l.getModel();
        Lookup lookup = model.getLookup(index);
        return lookup.getName();
    }

}
