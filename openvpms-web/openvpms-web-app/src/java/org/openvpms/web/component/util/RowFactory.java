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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;


/**
 * Factory for {@link Row}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class RowFactory extends ComponentFactory {

    /**
     * Create a new row.
     *
     * @return a new row
     */
    public static Row create() {
        return new Row();
    }

    /**
     * Create a new row, and containing a set of components.
     *
     * @param components the components to add
     * @return a new row
     */
    public static Row create(Component... components) {
        Row row = create();
        add(row, components);
        return row;
    }

    /**
     * Create a new row with a specific style, and containing a set of
     * components.
     *
     * @return a new row
     */
    public static Row create(String style, Component... components) {
        Row row = create(components);
        setStyle(row, style);
        return row;
    }

}
