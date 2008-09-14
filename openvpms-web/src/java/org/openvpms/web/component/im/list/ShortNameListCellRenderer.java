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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.AbstractListComponent;


/**
 * <code>ListCellRenderer</code> for a {@link ShortNameListModel}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-10-24 21:52:03Z $
 */
public class ShortNameListCellRenderer
        extends AllNoneListCellRenderer<String> {

    /**
     * Constructs a new <tt>ShortNameListCellRenderer</tt>.
     */
    public ShortNameListCellRenderer() {
        super(String.class);
    }

    /**
     * Renders an object.
     *
     * @param list      the list component
     * @param shortName the object to render
     * @param index     the object index
     * @return the rendered object
     */
    protected Object getComponent(Component list, String shortName, int index) {
        AbstractListComponent l = (AbstractListComponent) list;
        ShortNameListModel model = (ShortNameListModel) l.getModel();
        return model.getDisplayName(index);
    }

}
