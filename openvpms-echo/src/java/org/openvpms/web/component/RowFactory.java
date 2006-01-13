package org.openvpms.web.component;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;


/**
 * Factory for {@link Row}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public final class RowFactory extends ComponentFactory {

    /**
     * Create a new row with the default style.
     *
     * @return a new row
     */
    public static Row create() {
        Row row = new Row();
        setDefaults(row);
        return row;
    }

    /**
     * Create a new row with the default style, and containing a set of
     * components.
     *
     * @param components the components to add
     * @return a new row
     */
    public static Row create(Component ... components) {
        Row row = create();
        add(row, components);
        return row;
    }

    /**
     * Create a new row with a specific style, , and containing a set of
     * components.
     *
     * @return a new row
     */
    public static Row create(String style, Component ... components) {
        Row row = new Row();
        row.setStyleName(style);
        add(row, components);
        return row;
    }


}
