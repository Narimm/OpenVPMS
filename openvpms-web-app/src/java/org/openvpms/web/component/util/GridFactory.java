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
import nextapp.echo2.app.Grid;


/**
 * Factory for {@link Grid}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class GridFactory extends ComponentFactory {

    /**
     * Create a new grid.
     *
     * @param columns the number of columns
     * @return a new grid
     */
    public static Grid create(int columns) {
        Grid grid = new Grid(columns);
        setDefaultStyle(grid);
        return grid;
    }

    /**
     * Create a grid containing a set of components.
     *
     * @param components the components to add
     * @param columns    the number of columns
     */
    public static Grid create(int columns, Component... components) {
        Grid grid = create(columns);
        add(grid, components);
        return grid;
    }

}
