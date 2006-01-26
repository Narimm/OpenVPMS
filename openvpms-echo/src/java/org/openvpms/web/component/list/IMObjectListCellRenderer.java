package org.openvpms.web.component.list;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.ListCellRenderer;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * List cell renderer that display's an {@link IMObject}'s description.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectListCellRenderer implements ListCellRenderer {

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
        String result;
        if (value instanceof IMObject) {
            result = ((IMObject) value).getDescription();
        } else {
            result = null;
        }
        return result;
    }
}

