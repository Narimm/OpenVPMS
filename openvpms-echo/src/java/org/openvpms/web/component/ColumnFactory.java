package org.openvpms.web.component;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;


/**
 * Factory for {@link Column}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public final class ColumnFactory extends ComponentFactory {

    /**
     * Create a new column.
     *
     * @return a new column
     */
    public static Column create() {
        return new Column();
    }

    /**
     * Create a new column.
     *
     * @param style the style name
     */
    public static Column create(String style) {
        Column column = create();
        column.setStyleName(style);
        return column;
    }

    /**
     * Create a column containing a set of components.
     *
     * @param components the components to add
     * @return a new column
     */
    public static Column create(Component ... components) {
        Column column = create();
        add(column, components);
        return column;
    }

}
