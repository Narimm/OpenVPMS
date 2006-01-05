package org.openvpms.web.component;

import nextapp.echo2.app.Row;
import nextapp.echo2.app.Component;
import org.openvpms.web.component.ComponentFactory;


/**
 * Factory for {@link Row}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public final class RowFactory extends ComponentFactory {

    /**
     * Create a new row.
     *
     * @return a new row
     */
    public static Row create() {
        Row row = new Row();
        setDefaults(row);
        return row;
    }

    /**
     * Create a row containing a set of components.
     *
     * @param components the components to add
     * @return a new row
     */
    public static Row create(Component ... components) {
        Row row = create();
        add(row, components);
        return row;
    }

}
