package org.openvpms.web.component.util;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;

import org.openvpms.web.component.util.ComponentFactory;


/**
 * Factory for {@link Grid}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
        setDefaults(grid);
        return grid;
    }

    /**
     * Create a grid containing a set of components.
     *
     * @param components the components to add
     * @param columns    the number of columns
     */
    public static Grid create(int columns, Component ... components) {
        Grid grid = create(columns);
        add(grid, components);
        return grid;
    }

}
