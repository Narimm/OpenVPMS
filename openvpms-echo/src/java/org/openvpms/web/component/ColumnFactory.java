package org.openvpms.web.component;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;


/**
 * Factory for {@link Column}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public final class ColumnFactory extends ComponentFactory {

    /**
     * Create a new column.
     *
     * @param style the style name
     */
    public static Column create(String style) {
        Column column = new Column();
        column.setStyleName(style);
        return column;
    }

    /**
     * Create a new column.
     *
     * @return a new column
     */
    public static Column create() {
        Column column = new Column();
        setDefaults(column);
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
